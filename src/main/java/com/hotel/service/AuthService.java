package com.hotel.service;

import com.hotel.domain.dto.auth.AuthResponse;
import com.hotel.domain.dto.auth.LoginRequest;
import com.hotel.domain.dto.auth.SignupRequest;
import com.hotel.domain.dto.user.UserDto;
import com.hotel.domain.entity.Role;
import com.hotel.domain.entity.User;
import com.hotel.exception.ConflictException;
import com.hotel.exception.RateLimitExceededException;
import com.hotel.mapper.UserMapper;
import com.hotel.repository.UserRepository;
import com.hotel.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service handling authentication operations.
 * Includes first-time login detection, email notifications, and audit logging.
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final LoginAuditService loginAuditService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager,
                       UserMapper userMapper,
                       EmailService emailService,
                       LoginAuditService loginAuditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
        this.emailService = emailService;
        this.loginAuditService = loginAuditService;
    }

    /**
     * Register a new user.
     */
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        logger.info("Processing signup request for email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email address is already registered");
        }

        // Create new user
        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .phone(request.getPhone())
                .role(Role.USER)
                .isActive(true)
                .build();

        user = userRepository.save(user);
        logger.info("User registered successfully: {}", user.getEmail());

        // Generate JWT token with full claims (role, hotelId if applicable)
        String token = jwtTokenProvider.generateToken(user);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .user(userMapper.toDto(user))
                .build();
    }

    /**
     * Authenticate user and return JWT token.
     * Includes first-time login detection, email notifications, and audit logging.
     */
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String email = request.getEmail().toLowerCase().trim();
        logger.info("Processing login request for email: {}", email);

        // Check rate limiting - block if too many failed attempts
        if (loginAuditService.shouldBlockLogin(email, ipAddress)) {
            int remainingMinutes = loginAuditService.getRemainingLockoutMinutes(email);
            logger.warn("Login blocked due to rate limiting - email: {}, IP: {}", email, ipAddress);
            throw new RateLimitExceededException(
                    "Too many failed login attempts. Please try again in " + remainingMinutes + " minutes.",
                    remainingMinutes * 60);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = (User) authentication.getPrincipal();
            
            // Check if account is temporarily locked
            if (user.isTemporarilyLocked()) {
                logger.warn("Login attempt on locked account: {}", email);
                throw new LockedException("Account is temporarily locked. Please try again later.");
            }

            // Detect first-time login
            boolean isFirstLogin = user.isFirstTimeLogin();
            
            // Update login tracking fields
            user.setLastLoginAt(LocalDateTime.now());
            user.setLoginCount(user.getLoginCount() == null ? 1 : user.getLoginCount() + 1);
            user.setFailedLoginAttempts(0); // Reset failed attempts on success
            user.setLockedUntil(null); // Clear any lock
            
            if (isFirstLogin) {
                user.setFirstLoginCompleted(true);
                user.setMustChangePassword(true); // Force password setup for first login
                logger.info("First-time login detected for user: {}", email);
            }
            
            // Save updated user
            user = userRepository.save(user);

            // Send async email notification (non-blocking)
            if (isFirstLogin) {
                emailService.sendFirstLoginEmail(user, ipAddress, userAgent);
            } else {
                // Optional: send login notification for returning users
                // emailService.sendLoginNotificationEmail(user, ipAddress, userAgent);
            }

            // Record audit log (async)
            loginAuditService.recordLoginSuccess(user, ipAddress, userAgent, isFirstLogin);

            // Generate JWT token with full claims (role, hotelId for HOTEL_OWNER)
            String token = jwtTokenProvider.generateToken(user);

            logger.info("User logged in successfully: {} (first login: {})", email, isFirstLogin);

            return AuthResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getExpirationTime())
                    .user(userMapper.toDto(user))
                    .build();

        } catch (BadCredentialsException ex) {
            logger.warn("Login failed for email: {}", email);
            
            // Record failed attempt (async)
            User user = userRepository.findByEmail(email).orElse(null);
            loginAuditService.recordLoginFailure(email, ipAddress, userAgent, "Invalid credentials", 
                    user != null ? user.getId() : null);
            
            // Increment failed attempts if user exists
            if (user != null) {
                user.setFailedLoginAttempts(
                        user.getFailedLoginAttempts() == null ? 1 : user.getFailedLoginAttempts() + 1);
                
                // Lock account if max attempts reached
                if (user.getFailedLoginAttempts() >= 5) {
                    user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                    loginAuditService.recordAccountLocked(user, ipAddress, userAgent);
                    emailService.sendSecurityAlertEmail(user, "Account Locked", 
                            "Your account has been locked due to multiple failed login attempts from IP: " + ipAddress);
                }
                
                userRepository.save(user);
            }
            
            throw new BadCredentialsException("Invalid email or password");
        } catch (LockedException ex) {
            logger.warn("Login attempt on locked account: {}", email);
            throw ex;
        }
    }

    /**
     * Authenticate user and return JWT token (backward compatible - no IP/UA tracking).
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        return login(request, "unknown", "unknown");
    }

    /**
     * Get currently authenticated user.
     */
    @Transactional(readOnly = true)
    public UserDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new com.hotel.exception.AuthenticationException("User not authenticated");
        }

        User user = (User) authentication.getPrincipal();
        return userMapper.toDto(user);
    }

    /**
     * Get current user entity.
     */
    @Transactional(readOnly = true)
    public User getCurrentUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new com.hotel.exception.AuthenticationException("User not authenticated");
        }

        return (User) authentication.getPrincipal();
    }
}

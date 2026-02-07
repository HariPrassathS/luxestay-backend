package com.hotel.service;

import com.hotel.domain.dto.booking.BookingDto;
import com.hotel.domain.dto.booking.CreateBookingRequest;
import com.hotel.domain.dto.groupbooking.*;
import com.hotel.domain.entity.*;
import com.hotel.domain.entity.GroupBooking.GroupBookingStatus;
import com.hotel.domain.entity.GroupBookingParticipant.ParticipantStatus;
import com.hotel.exception.BadRequestException;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service for Group Booking operations.
 * 
 * REAL-TIME FEATURES:
 * - WebSocket broadcasts for participant updates
 * - Live availability checks
 * - Synchronized room selection
 * 
 * OWNERSHIP MODEL:
 * - Organizer has full control (lock, confirm, cancel)
 * - Participants can select rooms and cancel own participation
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GroupBookingService {
    
    private final GroupBookingRepository groupBookingRepository;
    private final GroupBookingParticipantRepository participantRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final SimpMessagingTemplate messagingTemplate;
    
    // ==================== CREATE & JOIN ====================
    
    /**
     * Create a new group booking.
     */
    public GroupBookingDto createGroup(CreateGroupBookingRequest request, Long organizerId) {
        log.info("Creating group booking for user {}", organizerId);
        
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", organizerId));
        
        Hotel hotel = hotelRepository.findByIdAndIsActiveTrue(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", request.getHotelId()));
        
        // Validate dates
        if (request.getCheckInDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Check-in date cannot be in the past");
        }
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new BadRequestException("Check-out must be after check-in");
        }
        
        GroupBooking group = GroupBooking.builder()
                .name(request.getName())
                .organizer(organizer)
                .hotel(hotel)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .maxParticipants(request.getMaxParticipants() != null ? request.getMaxParticipants() : 10)
                .notes(request.getNotes())
                .joinDeadline(request.getJoinDeadline())
                .status(GroupBookingStatus.OPEN)
                .build();
        
        // Add organizer as first participant
        GroupBookingParticipant organizerParticipant = GroupBookingParticipant.builder()
                .user(organizer)
                .isOrganizer(true)
                .status(ParticipantStatus.PENDING)
                .build();
        group.addParticipant(organizerParticipant);
        
        group = groupBookingRepository.save(group);
        log.info("Created group booking {} with code {}", group.getId(), group.getGroupCode());
        
        return toDto(group);
    }
    
    /**
     * Join an existing group by code.
     */
    public GroupBookingDto joinGroup(String groupCode, Long userId) {
        log.info("User {} joining group {}", userId, groupCode);
        
        GroupBooking group = groupBookingRepository.findByGroupCode(groupCode)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with code: " + groupCode));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Validate can join
        if (!group.canJoin()) {
            throw new BadRequestException("This group is no longer accepting participants");
        }
        
        // Check not already a participant
        if (groupBookingRepository.isUserParticipant(group.getId(), userId)) {
            throw new BadRequestException("You are already a participant in this group");
        }
        
        // Add participant
        GroupBookingParticipant participant = GroupBookingParticipant.builder()
                .user(user)
                .isOrganizer(false)
                .status(ParticipantStatus.PENDING)
                .build();
        group.addParticipant(participant);
        
        group = groupBookingRepository.save(group);
        
        // Broadcast update to group
        broadcastGroupUpdate(group, "PARTICIPANT_JOINED", user.getFirstName() + " joined the group");
        
        return toDto(group);
    }
    
    // ==================== ROOM SELECTION ====================
    
    /**
     * Select a room for a participant.
     */
    public GroupBookingDto selectRoom(Long groupId, Long userId, Long roomId, Integer numGuests) {
        log.info("User {} selecting room {} for group {}", userId, roomId, groupId);
        
        GroupBooking group = groupBookingRepository.findByIdWithParticipants(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupBooking", "id", groupId));
        
        if (group.getStatus() != GroupBookingStatus.OPEN) {
            throw new BadRequestException("Room selection is closed for this group");
        }
        
        // Find participant
        GroupBookingParticipant participant = group.getParticipants().stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("You are not a participant in this group"));
        
        // Validate room
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));
        
        if (!room.getHotel().getId().equals(group.getHotel().getId())) {
            throw new BadRequestException("Room does not belong to the group's hotel");
        }
        
        // Check room not already selected by another participant
        boolean roomTaken = group.getParticipants().stream()
                .filter(p -> !p.getUser().getId().equals(userId))
                .anyMatch(p -> p.getRoom() != null && p.getRoom().getId().equals(roomId));
        
        if (roomTaken) {
            throw new BadRequestException("This room has already been selected by another participant");
        }
        
        // Select room
        participant.selectRoom(room);
        participant.setNumGuests(numGuests != null ? numGuests : 1);
        participantRepository.save(participant);
        
        // Broadcast update
        broadcastGroupUpdate(group, "ROOM_SELECTED", 
                participant.getUser().getFirstName() + " selected " + room.getName());
        
        return toDto(groupBookingRepository.findByIdWithParticipants(groupId).get());
    }
    
    // ==================== ORGANIZER ACTIONS ====================
    
    /**
     * Lock the group (no more participants).
     */
    public GroupBookingDto lockGroup(Long groupId, Long organizerId) {
        GroupBooking group = getGroupAsOrganizer(groupId, organizerId);
        
        if (group.getStatus() != GroupBookingStatus.OPEN) {
            throw new BadRequestException("Group is not open");
        }
        
        group.setStatus(GroupBookingStatus.LOCKED);
        group = groupBookingRepository.save(group);
        
        broadcastGroupUpdate(group, "GROUP_LOCKED", "Group has been locked by the organizer");
        
        return toDto(group);
    }
    
    /**
     * Confirm all bookings in the group.
     */
    public GroupBookingDto confirmGroup(Long groupId, Long organizerId) {
        log.info("Confirming group {} by organizer {}", groupId, organizerId);
        
        GroupBooking group = getGroupAsOrganizer(groupId, organizerId);
        
        if (group.getStatus() == GroupBookingStatus.CONFIRMED) {
            throw new BadRequestException("Group is already confirmed");
        }
        
        // Ensure all participants have selected rooms
        List<GroupBookingParticipant> participants = group.getParticipants().stream()
                .filter(p -> p.getStatus() != ParticipantStatus.CANCELLED)
                .toList();
        
        boolean allRoomsSelected = participants.stream()
                .allMatch(p -> p.getRoom() != null);
        
        if (!allRoomsSelected) {
            throw new BadRequestException("All participants must select a room before confirming");
        }
        
        // Create individual bookings for each participant
        for (GroupBookingParticipant participant : participants) {
            if (participant.getStatus() == ParticipantStatus.CANCELLED) continue;
            
            CreateBookingRequest bookingReq = CreateBookingRequest.builder()
                    .roomId(participant.getRoom().getId())
                    .checkInDate(group.getCheckInDate())
                    .checkOutDate(group.getCheckOutDate())
                    .numGuests(participant.getNumGuests())
                    .specialRequests(participant.getSpecialRequests())
                    .build();
            
            try {
                BookingDto booking = bookingService.createBookingForUser(bookingReq, participant.getUser().getId());
                
                // Link booking to participant
                Booking bookingEntity = new Booking();
                bookingEntity.setId(booking.getId());
                participant.confirm(bookingEntity);
                participantRepository.save(participant);
                
            } catch (Exception e) {
                log.error("Failed to create booking for participant {}", participant.getId(), e);
                throw new BadRequestException("Failed to create booking for " + 
                        participant.getUser().getFirstName() + ": " + e.getMessage());
            }
        }
        
        // Update group status
        group.setStatus(GroupBookingStatus.CONFIRMED);
        group.setConfirmedAt(LocalDateTime.now());
        group.recalculateTotalPrice();
        group = groupBookingRepository.save(group);
        
        broadcastGroupUpdate(group, "GROUP_CONFIRMED", "All bookings have been confirmed!");
        
        return toDto(group);
    }
    
    /**
     * Cancel the entire group.
     */
    public GroupBookingDto cancelGroup(Long groupId, Long organizerId, String reason) {
        GroupBooking group = getGroupAsOrganizer(groupId, organizerId);
        
        // Cancel all associated bookings
        for (GroupBookingParticipant participant : group.getParticipants()) {
            if (participant.getBooking() != null) {
                try {
                    bookingService.cancelBooking(participant.getBooking().getId(), reason);
                } catch (Exception e) {
                    log.warn("Failed to cancel booking for participant {}", participant.getId(), e);
                }
            }
            participant.cancel();
        }
        
        group.setStatus(GroupBookingStatus.CANCELLED);
        group = groupBookingRepository.save(group);
        
        broadcastGroupUpdate(group, "GROUP_CANCELLED", "Group booking has been cancelled");
        
        return toDto(group);
    }
    
    // ==================== PARTICIPANT ACTIONS ====================
    
    /**
     * Leave a group.
     */
    public void leaveGroup(Long groupId, Long userId) {
        log.info("User {} leaving group {}", userId, groupId);
        
        GroupBooking group = groupBookingRepository.findByIdWithParticipants(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupBooking", "id", groupId));
        
        GroupBookingParticipant participant = group.getParticipants().stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("You are not a participant in this group"));
        
        if (participant.getIsOrganizer()) {
            throw new BadRequestException("Organizer cannot leave the group. Cancel the group instead.");
        }
        
        if (participant.getBooking() != null) {
            throw new BadRequestException("Cannot leave after booking is confirmed. Cancel your booking instead.");
        }
        
        String userName = participant.getUser().getFirstName();
        group.removeParticipant(participant);
        groupBookingRepository.save(group);
        
        broadcastGroupUpdate(group, "PARTICIPANT_LEFT", userName + " left the group");
    }
    
    // ==================== QUERIES ====================
    
    /**
     * Get group by ID.
     */
    @Transactional(readOnly = true)
    public Optional<GroupBookingDto> getGroup(Long groupId) {
        return groupBookingRepository.findByIdWithParticipants(groupId)
                .map(this::toDto);
    }
    
    /**
     * Get group by ID with user access validation.
     * User must be organizer or participant.
     */
    @Transactional(readOnly = true)
    public Optional<GroupBookingDto> getGroupForUser(Long groupId, Long userId) {
        return groupBookingRepository.findByIdWithParticipants(groupId)
                .filter(group -> isUserMemberOfGroup(group, userId))
                .map(this::toDto);
    }
    
    /**
     * Get group by code.
     */
    @Transactional(readOnly = true)
    public Optional<GroupBookingDto> getGroupByCode(String groupCode) {
        return groupBookingRepository.findByGroupCodeWithParticipants(groupCode)
                .map(this::toDto);
    }
    
    /**
     * Get group by code with user access validation.
     * User must be organizer or participant.
     */
    @Transactional(readOnly = true)
    public Optional<GroupBookingDto> getGroupByCodeForUser(String groupCode, Long userId) {
        return groupBookingRepository.findByGroupCodeWithParticipants(groupCode)
                .filter(group -> isUserMemberOfGroup(group, userId))
                .map(this::toDto);
    }
    
    /**
     * Check if user is a member of the group (organizer or participant).
     */
    private boolean isUserMemberOfGroup(GroupBooking group, Long userId) {
        // Check if organizer
        if (group.getOrganizer().getId().equals(userId)) {
            return true;
        }
        // Check if participant
        return group.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(userId));
    }
    
    /**
     * Get user's groups (as organizer or participant).
     */
    @Transactional(readOnly = true)
    public List<GroupBookingDto> getUserGroups(Long userId) {
        List<GroupBooking> organized = groupBookingRepository.findByOrganizer_IdOrderByCreatedAtDesc(userId);
        List<GroupBooking> participating = groupBookingRepository.findByParticipantUserId(userId);
        
        // Merge and deduplicate
        Set<Long> seenIds = new HashSet<>();
        return java.util.stream.Stream.concat(organized.stream(), participating.stream())
                .filter(g -> seenIds.add(g.getId()))
                .map(this::toDto)
                .toList();
    }
    
    // ==================== HELPERS ====================
    
    private GroupBooking getGroupAsOrganizer(Long groupId, Long userId) {
        GroupBooking group = groupBookingRepository.findByIdWithParticipants(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("GroupBooking", "id", groupId));
        
        if (!group.isOrganizer(userId)) {
            throw new BadRequestException("Only the organizer can perform this action");
        }
        
        return group;
    }
    
    private void broadcastGroupUpdate(GroupBooking group, String eventType, String message) {
        GroupBookingUpdateEvent event = GroupBookingUpdateEvent.builder()
                .groupId(group.getId())
                .groupCode(group.getGroupCode())
                .eventType(eventType)
                .message(message)
                .participantCount(group.getCurrentParticipantCount())
                .status(group.getStatus().name())
                .timestamp(LocalDateTime.now())
                .build();
        
        messagingTemplate.convertAndSend(
                "/topic/group/" + group.getGroupCode(),
                event
        );
    }
    
    private GroupBookingDto toDto(GroupBooking group) {
        return GroupBookingDto.builder()
                .id(group.getId())
                .groupCode(group.getGroupCode())
                .name(group.getName())
                .hotelId(group.getHotel().getId())
                .hotelName(group.getHotel().getName())
                .checkInDate(group.getCheckInDate())
                .checkOutDate(group.getCheckOutDate())
                .maxParticipants(group.getMaxParticipants())
                .currentParticipants(group.getCurrentParticipantCount())
                .status(group.getStatus().name())
                .notes(group.getNotes())
                .joinDeadline(group.getJoinDeadline())
                .totalPrice(group.getTotalPrice())
                .organizerId(group.getOrganizer().getId())
                .organizerName(group.getOrganizer().getFirstName() + " " + group.getOrganizer().getLastName())
                .canJoin(group.canJoin())
                .participants(group.getParticipants().stream()
                        .map(this::toParticipantDto)
                        .toList())
                .createdAt(group.getCreatedAt())
                .confirmedAt(group.getConfirmedAt())
                .build();
    }
    
    private GroupBookingDto.ParticipantDto toParticipantDto(GroupBookingParticipant p) {
        return GroupBookingDto.ParticipantDto.builder()
                .id(p.getId())
                .userId(p.getUser().getId())
                .userName(p.getUser().getFirstName() + " " + p.getUser().getLastName())
                .isOrganizer(p.getIsOrganizer())
                .status(p.getStatus().name())
                .roomId(p.getRoom() != null ? p.getRoom().getId() : null)
                .roomName(p.getRoom() != null ? p.getRoom().getName() : null)
                .numGuests(p.getNumGuests())
                .bookingId(p.getBooking() != null ? p.getBooking().getId() : null)
                .bookingReference(p.getBooking() != null ? p.getBooking().getBookingReference() : null)
                .joinedAt(p.getJoinedAt())
                .build();
    }
    
    // ==================== INNER CLASSES (Request/DTO) ====================
    
    // These would normally be in separate files, included here for completeness
}

package com.hotel.domain.dto.owner;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for admin to create a hotel owner account for an existing hotel.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateHotelOwnerRequest {

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be at most 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must be at most 100 characters")
    private String lastName;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String phone;

    /**
     * If true, a temporary password will be generated and the user
     * will be required to change it on first login.
     * If false, a password must be provided.
     */
    @Builder.Default
    private Boolean generateTemporaryPassword = true;

    /**
     * Optional password. Required if generateTemporaryPassword is false.
     */
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;
}

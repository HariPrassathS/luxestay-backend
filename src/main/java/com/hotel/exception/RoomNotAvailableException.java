package com.hotel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a room is not available for booking.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class RoomNotAvailableException extends RuntimeException {

    private final Long roomId;
    private final String checkIn;
    private final String checkOut;

    public RoomNotAvailableException(Long roomId, String checkIn, String checkOut) {
        super(String.format("Room %d is not available from %s to %s", roomId, checkIn, checkOut));
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public RoomNotAvailableException(String message) {
        super(message);
        this.roomId = null;
        this.checkIn = null;
        this.checkOut = null;
    }

    public Long getRoomId() {
        return roomId;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public String getCheckOut() {
        return checkOut;
    }
}

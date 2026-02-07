-- Add indexes to bookings table for faster lookups
CREATE INDEX idx_booking_user_id ON bookings(user_id);
CREATE INDEX idx_booking_room_id ON bookings(room_id);
CREATE INDEX idx_booking_status ON bookings(status);

-- Note: booking_reference should likely be unique, but if it isn't already, an index helps
CREATE INDEX idx_booking_reference ON bookings(booking_reference);

-- Add indexes to rooms table
CREATE INDEX idx_room_hotel_id ON rooms(hotel_id);

-- Add indexes to hotels table for search performance
CREATE INDEX idx_hotel_city ON hotels(city);
CREATE INDEX idx_hotel_country ON hotels(country);
CREATE INDEX idx_hotel_is_active ON hotels(is_active);

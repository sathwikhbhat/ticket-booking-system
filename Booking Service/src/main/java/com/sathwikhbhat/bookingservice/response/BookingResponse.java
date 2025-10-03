package com.sathwikhbhat.bookingservice.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponse {

    private Long bookingId;
    private Long eventId;
    private Long userId;
    private Long ticketCount;
    private String ticketPrice;

}
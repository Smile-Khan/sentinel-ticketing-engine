package com.sentinel.controller;

import com.sentinel.dto.BookingResponse;
import com.sentinel.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/reserve/{seatId}")
    public ResponseEntity<BookingResponse> reserve(@PathVariable Long seatId) {
        String result = bookingService.reserveSeat(seatId);

        return switch (result) {
            case "SUCCESS" -> ResponseEntity.status(HttpStatus.CREATED)
                    .body(new BookingResponse("Seat reserved successfully", seatId, "SUCCESS"));

            case "ALREADY_RESERVED" -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new BookingResponse("Seat is already taken", seatId, "TAKEN"));

            default -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new BookingResponse("System is busy, please try again", seatId, "RETRY"));
        };
    }
}
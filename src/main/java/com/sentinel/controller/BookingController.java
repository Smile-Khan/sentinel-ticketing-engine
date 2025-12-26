package com.sentinel.controller;

import com.sentinel.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/reserve/{seatId}")
    public String reserve(@PathVariable Long seatId) {
        return bookingService.reserveSeat(seatId);
    }
}
package com.sentinel.controller;

import com.sentinel.dto.BookingResponse;
import com.sentinel.model.Seat;
import com.sentinel.repository.SeatRepository;
import com.sentinel.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final SeatRepository seatRepository;

    @GetMapping("/available")
    public List<Seat> getAvailableSeats() {
        return seatRepository.findByIsReservedFalse();
    }

    @PostMapping("/reserve/{seatId}")
    public ResponseEntity<BookingResponse> reserve(@PathVariable Long seatId) {
        String result = bookingService.reserveSeat(seatId);

        return ResponseEntity.ok(new BookingResponse(
                "Request Processed",
                seatId,
                result
        ));
    }
}
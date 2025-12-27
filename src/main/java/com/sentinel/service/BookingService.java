package com.sentinel.service;

import com.sentinel.model.Seat;
import com.sentinel.producer.BookingEventProducer;
import com.sentinel.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final SeatRepository seatRepository;
    private final RedissonClient redissonClient;
    private final BookingEventProducer eventProducer;

    private static final String LOCK_PREFIX = "lock:seat:";

    /**
     * Coordinates the seat reservation process using a distributed lock.
     *
     * @param seatId Unique identifier of the seat
     * @return Execution status (SUCCESS, ALREADY_RESERVED, LOCK_FAILED)
     */
    public String reserveSeat(Long seatId) {
        String lockKey = LOCK_PREFIX + seatId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // Attempt to acquire lock to prevent race conditions during high-concurrency periods
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                try {
                    String result = persistReservation(seatId);

                    // Messaging is triggered outside the DB transaction to prevent
                    // connection pool exhaustion if the broker is slow/unavailable.
                    if ("SUCCESS".equals(result)) {
                        eventProducer.emitBookingEvent(seatId, "RESERVED");
                    }

                    return result;
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("Thread interrupted during lock acquisition for seat {}", seatId);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Reservation process interrupted");
        }
        return "LOCK_FAILED";
    }

    @Transactional
    protected String persistReservation(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Resource not found: " + seatId));

        if (seat.isReserved()) {
            return "ALREADY_RESERVED";
        }

        seat.setReserved(true);
        seatRepository.save(seat);
        return "SUCCESS";
    }
}
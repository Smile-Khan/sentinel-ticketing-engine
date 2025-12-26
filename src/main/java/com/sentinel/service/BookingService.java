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

    @Transactional
    public String reserveSeat(Long seatId) {
        String lockKey = "lock:seat:" + seatId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                try {
                    Seat seat = seatRepository.findById(seatId)
                            .orElseThrow(() -> new RuntimeException("Requested seat does not exist"));

                    if (seat.isReserved()) {
                        return "ALREADY_RESERVED";
                    }

                    seat.setReserved(true);
                    seatRepository.save(seat);

                    // Trigger asynchronous notification
                    eventProducer.emitBookingEvent(seatId, "RESERVED");

                    return "SUCCESS";
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Reservation process was interrupted");
        }

        return "LOCK_ACQUISITION_FAILED";
    }
}
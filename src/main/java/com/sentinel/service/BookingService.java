package com.sentinel.service;

import com.sentinel.model.Seat;
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

    @Transactional
    public String reserveSeat(Long seatId) {
        // 1. Create a unique lock key for this specific seat
        String lockKey = "lock:seat:" + seatId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 2. Try to acquire the lock (Wait 5s to get it, hold it for 10s)
            // This is the "Magic" that prevents two people from buying the same seat
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                try {
                    log.info("Lock acquired for seat: {}", seatId);

                    // 3. Check DB if seat is already reserved
                    Seat seat = seatRepository.findById(seatId)
                            .orElseThrow(() -> new RuntimeException("Seat not found"));

                    if (seat.isReserved()) {
                        return "FAILED: Seat already reserved.";
                    }

                    // 4. Perform the booking
                    seat.setReserved(true);
                    seatRepository.save(seat);

                    return "SUCCESS: Seat " + seatId + " reserved!";
                } finally {
                    // 5. Always release the lock so others can try
                    lock.unlock();
                    log.info("Lock released for seat: {}", seatId);
                }
            } else {
                return "FAILED: System busy (could not acquire lock).";
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "ERROR: Transaction interrupted.";
        }
    }
}
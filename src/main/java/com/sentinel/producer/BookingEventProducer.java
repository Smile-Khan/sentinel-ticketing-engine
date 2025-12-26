package com.sentinel.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class BookingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String BOOKING_TOPIC = "booking-events";

    public void emitBookingEvent(Long seatId, String status) {
        log.info("Emitting booking event for seatId: {} with status: {}", seatId, status);

        try {
            Map<String, Object> payload = Map.of(
                    "seatId", seatId,
                    "status", status,
                    "timestamp", System.currentTimeMillis()
            );

            kafkaTemplate.send(BOOKING_TOPIC, String.valueOf(seatId), payload);
        } catch (Exception e) {
            // Prevent event publishing failures from rolling back the main transaction
            log.error("Non-critical failure: Could not publish event to Kafka for seat {}", seatId, e);
        }
    }
}
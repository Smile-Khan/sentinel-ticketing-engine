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
    private static final String TOPIC = "booking-events";

    /**
     * Dispatches booking status updates to the message broker.
     * Implementation is non-blocking to ensure core service throughput.
     */
    public void emitBookingEvent(Long seatId, String status) {
        Map<String, Object> payload = Map.of(
                "seatId", seatId,
                "status", status,
                "timestamp", System.currentTimeMillis()
        );

        kafkaTemplate.send(TOPIC, String.valueOf(seatId), payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Asynchronous event dispatch failed for seat {}: {}", seatId, ex.getMessage());
                    } else {
                        log.debug("Event successfully published to topic {} for seat {}", TOPIC, seatId);
                    }
                });
    }
}
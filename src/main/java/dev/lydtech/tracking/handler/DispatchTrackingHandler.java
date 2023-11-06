package dev.lydtech.tracking.handler;

import dev.lydtech.dispatch.message.DispatchCompleted;
import dev.lydtech.dispatch.message.DispatchPreparing;
import dev.lydtech.tracking.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@KafkaListener(
        id = "dispatchTrackingConsumerClient",
        topics = "dispatch.tracking",
        groupId = "tracking.dispatch.tracking",
        containerFactory = "kafkaListenerContainerFactory"
)
public class DispatchTrackingHandler {

    private final TrackingService trackingService;

    @KafkaHandler
    public void listen(@Payload DispatchPreparing payload) {
        log.info("Received DispatchPreparing Message - payload: {}", payload);

        try {
            trackingService.process(payload);
        } catch (Exception e) {
            log.error("Processing failure", e);
        }
    }

    @KafkaHandler
    public void listen(@Payload DispatchCompleted payload) {
        log.info("Received DispatchCompleted Message - payload: {}", payload);
    }
}

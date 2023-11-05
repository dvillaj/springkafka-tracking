package dev.lydtech.tracking.integration;

import dev.lydtech.dispatch.message.DispatchPreparing;
import dev.lydtech.dispatch.message.TrackingStatusUpdated;
import dev.lydtech.tracking.config.TrackingConfiguration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.lydtech.tracking.util.TestEventData.buildDispatchPreparingEvent;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;


@Slf4j
@SpringBootTest(classes = {TrackingConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
@EmbeddedKafka(controlledShutdown = true)
public class DispatchTrackingIntegrationTest {

    private static final String DISPATCH_TRACKING_TOPIC = "dispatch.tracking";
    private static final String TRACKING_STATUS_TOPIC = "tracking.status";

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private KafkaTestListener testListener;

    @Configuration
    static class TestConfig {
        @Bean
        public KafkaTestListener testListener() {
            return new KafkaTestListener();
        }
    }

    public static class KafkaTestListener {
        AtomicInteger trackingStatusCounter = new AtomicInteger(0);

        @KafkaListener(groupId = "KafkaIntegrationTest",
                       topics = TRACKING_STATUS_TOPIC)
        void receiveDispatchStatus(@Payload TrackingStatusUpdated payload) {
            log.info("Receiving TrackingStatusUpdated Event: payload: {}", payload);

            assertThat(payload).isNotNull();
            trackingStatusCounter.incrementAndGet();
        }

    }

    @BeforeEach
    public void setUp() {
        testListener.trackingStatusCounter.set(0);

        registry.getListenerContainers().stream().forEach(container ->
                ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic()));
    }

    @Test
    @SneakyThrows
    public void testDispatchTrackingFlow() {
        DispatchPreparing dispatchPreparing = buildDispatchPreparingEvent(randomUUID());

        sendMessage(DISPATCH_TRACKING_TOPIC, dispatchPreparing);

        await().atMost(3, TimeUnit.SECONDS).pollDelay(100, TimeUnit.MILLISECONDS)
                .until(testListener.trackingStatusCounter::get, equalTo(1));
    }

    @SneakyThrows
    private void sendMessage(String topic, Object payload) {
        var message = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build();

        kafkaTemplate.send(message).get();
    }

}

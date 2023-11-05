package dev.lydtech.tracking.service;

import dev.lydtech.dispatch.message.DispatchPreparing;
import dev.lydtech.dispatch.message.TrackingStatusUpdated;
import dev.lydtech.tracking.util.TestEventData;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TrackingServiceTest {


    private TrackingService service;
    private KafkaTemplate kafkaProducerMock;

    @BeforeEach
    void setUp() {
        kafkaProducerMock = mock(KafkaTemplate.class);
        service = new TrackingService(kafkaProducerMock);
    }

    @Test
    @SneakyThrows
    void process_Sucess() {
        String key = randomUUID().toString();

        when(kafkaProducerMock.send(anyString(), any(TrackingStatusUpdated.class))).thenReturn(mock(CompletableFuture.class));

        UUID id = randomUUID();
        DispatchPreparing testEvent = TestEventData.buildDispatchPreparingEvent(id);

        service.process(testEvent);

        verify(kafkaProducerMock, times(1)).send(eq("tracking.status"), any(TrackingStatusUpdated.class));
    }

    @Test
    @SneakyThrows
    void testProcess_ProducerThrowsException() {
        DispatchPreparing testEvent = TestEventData.buildDispatchPreparingEvent(randomUUID());
        when(kafkaProducerMock.send(anyString(), any(TrackingStatusUpdated.class))).thenReturn(mock(CompletableFuture.class));
        doThrow(new RuntimeException("producer failure")).when(kafkaProducerMock).send(eq("tracking.status"), any(TrackingStatusUpdated.class));

        Exception exception = assertThrows(RuntimeException.class, ()-> service.process(testEvent));

        verify(kafkaProducerMock, times(1)).send(eq("tracking.status"), any(TrackingStatusUpdated.class));
        assertThat(exception.getMessage()).isEqualTo("producer failure");
    }
}

package dev.lydtech.tracking.handler;

import dev.lydtech.dispatch.message.DispatchPreparing;
import dev.lydtech.tracking.service.TrackingService;
import dev.lydtech.tracking.util.TestEventData;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.mockito.Mockito.*;
import static java.util.UUID.randomUUID;

class OrderCreatedHandlerTest {

    private DispatchTrackingHandler handler;
    private TrackingService dispatchServiceMock;

    @BeforeEach
    void setUp() {
        dispatchServiceMock = mock(TrackingService.class);
        handler = new DispatchTrackingHandler(dispatchServiceMock);
    }

    @Test
    @SneakyThrows
    void listen_Success()  {
        DispatchPreparing testEvent = TestEventData.buildDispatchPreparingEvent(randomUUID());
        handler.listen(testEvent);
        verify(dispatchServiceMock, times(1)).process(testEvent);
    }

    @Test
    @SneakyThrows
    void listen_ServiceThrowsException()  {
        DispatchPreparing testEvent = TestEventData.buildDispatchPreparingEvent(randomUUID());
        doThrow(new RuntimeException("Service failure")).when(dispatchServiceMock).process(testEvent);

        handler.listen(testEvent);
        verify(dispatchServiceMock, times(1)).process(testEvent);
    }
}
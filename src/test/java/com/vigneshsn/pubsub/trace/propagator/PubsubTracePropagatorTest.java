package com.vigneshsn.pubsub.trace.propagator;

import com.google.pubsub.v1.PubsubMessage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PubsubTracePropagatorTest {

    @Test
    @DisplayName("should be able to pass the context and reconstruct")
    void testTracerContext() {
        //todo: revisit
        SpanContext context = SpanContext.create("1213","3223", TraceFlags.getDefault(), TraceState.getDefault());
        Context root = Context.root();
        root = root.with(Span.wrap(context));

        try (Scope scope = root.makeCurrent()) {
            SpanContext current = Span.current().getSpanContext();

            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().build();
            PubsubMessage newPubsubMessage = PubsubTracePropagator.addTracerContext(pubsubMessage);

            String traceContext = newPubsubMessage.getAttributesMap().get("traceContext");
            assertNotNull(traceContext);

            SpanContext reconstructed = PubsubTracePropagator.getTracerContext(newPubsubMessage).get();
            assertEquals(current.getTraceId(), reconstructed.getTraceId());
            assertEquals(current.getSpanId(), reconstructed.getSpanId());
        }

    }

    @Test
    @DisplayName("passing invalid tracer context should return empty context")
    void invalidTracerContextText() {
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder().build();
        Optional<SpanContext> context =  PubsubTracePropagator.getTracerContext(pubsubMessage);
        assertTrue(context.isEmpty());

        PubsubMessage pubsubMessage1 = PubsubMessage.newBuilder().putAttributes("traceContext", "").build();
        Optional<SpanContext> emptyContext =  PubsubTracePropagator.getTracerContext(pubsubMessage1);
        assertTrue(emptyContext.isEmpty());

        PubsubMessage pubsubMessage2 = PubsubMessage.newBuilder().putAttributes("traceContext", "invalid format").build();
        Optional<SpanContext> invalidFormat =  PubsubTracePropagator.getTracerContext(pubsubMessage2);
        assertTrue(invalidFormat.isEmpty());

    }
}
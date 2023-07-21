package com.vigneshsn.pubsub.trace.propagator;

import com.google.pubsub.v1.PubsubMessage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;

@Slf4j
public class PubsubTracePropagator {

    public static final String TRACE_CONTEXT = "traceContext";

    public static PubsubMessage addTracerContext(PubsubMessage pubsubMessage) {
        SpanContext current = Span.current().getSpanContext();
        TraceContext traceContext = new TraceContext(current);
        return pubsubMessage.toBuilder()
                .putAttributes(TRACE_CONTEXT, traceContext.toString())
                .build();
    }

    public static Optional<SpanContext> getTracerContext(PubsubMessage pubsubMessage) {
        String contextString = pubsubMessage.getAttributesMap().get(TRACE_CONTEXT);

        log.debug("The context string in message {}", contextString);

        if(Objects.isNull(contextString) || "".equals(contextString)) {
            log.debug("no trace context in metadata");
            return Optional.empty();
        }
        try{
            return Optional.of( TraceContext.fromString(contextString) );
        } catch (Exception ex) {
            log.warn("invalid tracer string should not blocking flow, pass empty context caller");
        }
        return Optional.empty();
    }

}
package com.vigneshsn.pubsub.trace.propagator;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.PublisherInterface;
import com.google.pubsub.v1.PubsubMessage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@AllArgsConstructor
@Slf4j
public class TracingPublisher implements PublisherInterface {
    private final Publisher publisher;
    private final Tracer tracer;

    public ApiFuture<String> publish(PubsubMessage pubsubMessage) {
        Span producerSpan = tracer
                .spanBuilder("publish")
                .setSpanKind(SpanKind.PRODUCER)
                .setAttribute("service.name", "My Test sender app")
                .startSpan();
        try(Scope scope = producerSpan.makeCurrent()) {
            return publisher.publish( PubsubTracePropagator.addTracerContext(pubsubMessage) );
        } finally {
            producerSpan.end();
        }
    }

    public String getTopicNameString() {
        return publisher.getTopicNameString();
    }
}

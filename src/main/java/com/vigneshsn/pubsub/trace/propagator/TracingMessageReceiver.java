package com.vigneshsn.pubsub.trace.propagator;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.PubsubMessage;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@AllArgsConstructor
@Slf4j
public class TracingMessageReceiver implements MessageReceiver {
    private MessageReceiver originalListener;
    private Tracer tracer;

    @Override
    public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
        Optional<SpanContext> remoteContext = PubsubTracePropagator.getTracerContext( message );
        SpanBuilder spanBuilder = tracer
                .spanBuilder("processMessage")
                .setAttribute("service.name", "My Test receiver app")
                .setSpanKind(SpanKind.CONSUMER);
        if( remoteContext.isPresent() ) {
            SpanContext remoteContextObj = remoteContext.get();
            log.debug("remote span context trace: {} span: {}", remoteContextObj.getTraceId(), remoteContextObj.getSpanId());
            Context context = Context.current().with(Span.wrap(remoteContextObj));
            spanBuilder.setParent(context);
        }

        Span span = spanBuilder.startSpan();
        try ( Scope scope = span.makeCurrent() ) {
            originalListener.receiveMessage(message, consumer);
        } finally {
            span.end();
        }
    }
}

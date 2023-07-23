package com.vigneshsn.pubsub.trace.propagator;

import com.google.pubsub.v1.PubsubMessage;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.Collection;

@Slf4j
@ApplicationScoped
public class PubsubPropagator implements TextMapPropagator {
    @Override
    public String toString() {
        return "PubsubPropagator";
    }

    W3CTraceContextPropagator traceContextPropagator = W3CTraceContextPropagator.getInstance();
    public PubsubPropagator() {
    }

    @Override
    public Collection<String> fields() {
        return traceContextPropagator.fields();
    }

    @Override
    public <C> void inject(Context context, @Nullable C carrier, TextMapSetter<C> setter) {
        traceContextPropagator.inject(Context.current(), carrier, new PubSubTextMapSetter());
    }

    @Override
    public <C> Context extract(Context context, @Nullable C carrier, TextMapGetter<C> getter) {
        return traceContextPropagator.extract(context, carrier, new PubSubTextMapGetter());
    }

    static class PubSubTextMapSetter implements TextMapSetter {
        @Override
        public void set(@Nullable Object carrier, String key, String value) {
            log.info("pubsub sub text map setter key: {}  value: {}", key, value);
            PubsubMessage pubsubMessage = (PubsubMessage) carrier;
            pubsubMessage.toBuilder().putAttributes(key, value);
        }
    }

    static class PubSubTextMapGetter implements TextMapGetter {

        @Override
        public Iterable<String> keys(Object carrier) {
            PubsubMessage pubsubMessage = (PubsubMessage) carrier;
            return pubsubMessage.getAttributesMap().keySet();
        }

        @Nullable
        @Override
        public String get(@Nullable Object carrier, String key) {
            PubsubMessage pubsubMessage = (PubsubMessage) carrier;
            return pubsubMessage.getAttributesMap().get(key);
        }
    }
}


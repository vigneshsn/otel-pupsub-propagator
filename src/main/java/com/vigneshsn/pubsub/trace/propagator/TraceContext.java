package com.vigneshsn.pubsub.trace.propagator;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import lombok.NoArgsConstructor;

@NoArgsConstructor
class TraceContext {
    String traceId;
    String spanId;

    public TraceContext(SpanContext spanContext) {
        this.traceId = spanContext.getTraceId();
        this.spanId = spanContext.getSpanId();
    }

    public static SpanContext fromString(String contextString) {
        TraceContext context = parse(contextString);
        return SpanContext.createFromRemoteParent(context.traceId, context.spanId,
                TraceFlags.getSampled(), TraceState.getDefault());
    }

    private static TraceContext parse(String contextString) {
        String[] info = contextString.split(",");
        TraceContext context = new TraceContext();
        if ( info.length == 2 ) {
            context.traceId = info[0];
            context.spanId = info[1];
            return context;
        }
        throw new IllegalArgumentException("invalid trace context format");
    }


    @Override
    public String toString() {
        return String.join(",", traceId, spanId);
    }
}

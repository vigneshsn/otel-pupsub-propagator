package com.vigneshsn.pubsub.trace.controller;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.*;
import com.google.pubsub.v1.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static io.quarkiverse.loggingjson.providers.KeyValueStructuredArgument.kv;
import static io.quarkus.opentelemetry.runtime.config.build.OTelBuildConfig.INSTRUMENTATION_NAME;

@Slf4j
public class AppConfig {

    String projectId = "local";
    String subId = "demo-sub";
    String topicId = "mytesttopic";

    @Produces
    @Singleton
    public TransportChannelProvider getChannel() {
        ManagedChannel managed = ManagedChannelBuilder.forTarget("localhost:8085").usePlaintext().build();
        return FixedTransportChannelProvider.create(GrpcTransportChannel.create(managed));
    }

    @Produces
    @Singleton
    public Publisher getPublisher(TransportChannelProvider transportChannelProvider, Tracer tracer) {
        try {
            TopicName topicName = createTopic(transportChannelProvider);
            Publisher publisher = Publisher.newBuilder(topicName)
                    .setChannelProvider(transportChannelProvider)
                    .setCredentialsProvider(NoCredentialsProvider.create())
                    .build();
            return publisher;
            //return new TracingPublisher( publisher, tracer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    TopicName createTopic(TransportChannelProvider transportChannelProvider) throws IOException {
        TopicAdminClient topicClient =
                TopicAdminClient.create(
                        TopicAdminSettings.newBuilder()
                                .setTransportChannelProvider(transportChannelProvider)
                                .setCredentialsProvider(NoCredentialsProvider.create())
                                .build());

        TopicName topicName =  TopicName.of(projectId, topicId);
        Topic topic = topicClient.getTopic(topicName);
        if(topic == null) {
            topicClient.createTopic(topicName);
        }
        return topicName;
    }

    @Produces
    @Singleton
    public Tracer tracer() {
        return GlobalOpenTelemetry.getTracer(INSTRUMENTATION_NAME);
    }

    @Produces
    @Singleton
    public Subscriber getSubscriber(TransportChannelProvider transportChannelProvider, Tracer tracer) throws IOException {
        Subscription subscription = createSubscription(transportChannelProvider);
        CredentialsProvider credentialsProvider = NoCredentialsProvider.create();
        //TracingMessageReceiver tracingMessageReceiver = new TracingMessageReceiver(this::receive, tracer);
        return Subscriber
                .newBuilder(subscription.getName(), this::receive)
                .setChannelProvider(transportChannelProvider)
                .setCredentialsProvider(credentialsProvider)
                .build();
    }

    public Subscription createSubscription(TransportChannelProvider transportChannelProvider) throws IOException {
        SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create(SubscriptionAdminSettings.newBuilder()
                .setTransportChannelProvider(transportChannelProvider)
                .setCredentialsProvider(NoCredentialsProvider.create())
                .build());
        SubscriptionName name = SubscriptionName.of(projectId, subId);
        TopicName topic = TopicName.ofProjectTopicName(projectId, topicId);
        PushConfig pushConfig = PushConfig.newBuilder().build();
        int ackDeadlineSeconds = 5;

        Subscription subscription = subscriptionAdminClient.getSubscription(name);
        if( subscription == null ) {
            subscription = subscriptionAdminClient.createSubscription(name, topic, pushConfig, ackDeadlineSeconds);
        }
        return subscription;
    }

    void receive(final PubsubMessage message, final AckReplyConsumer consumer) {
        log.info("msg received  {} ", message.getData());
        consumer.ack();
    }


    void onStartup(@Observes StartupEvent event, Subscriber subscriber) {
        log.info("Starting subscription");
        subscriber.startAsync().awaitRunning();
        log.info("Now listening on subscription",
                kv("subscription", subscriber.getSubscriptionNameString()));
    }

    void onStop(@Observes ShutdownEvent event, Subscriber subscriber) {
        log.info("Destroying subscription");
        if (subscriber != null) {
            subscriber.stopAsync().awaitTerminated();
            log.info("subscription has been destroyed",
                    kv("subscription", subscriber.getSubscriptionNameString()));
        }
    }


}

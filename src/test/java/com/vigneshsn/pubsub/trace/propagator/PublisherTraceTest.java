//package gis.pubsub.trace.propagator;
//
//import com.google.api.gax.core.CredentialsProvider;
//import com.google.api.gax.core.NoCredentialsProvider;
//import com.google.api.gax.grpc.GrpcTransportChannel;
//import com.google.api.gax.rpc.FixedTransportChannelProvider;
//import com.google.api.gax.rpc.TransportChannelProvider;
//import com.google.cloud.pubsub.v1.*;
//import com.google.protobuf.ByteString;
//import com.google.pubsub.v1.*;
//import io.grpc.ManagedChannel;
//import io.grpc.ManagedChannelBuilder;
//import io.opentelemetry.api.GlobalOpenTelemetry;
//import io.opentelemetry.api.trace.Span;
//import io.opentelemetry.api.trace.SpanContext;
//import io.opentelemetry.api.trace.Tracer;
//import io.opentelemetry.context.Context;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.util.concurrent.ExecutionException;
//
//
//@Slf4j
//public class PublisherTraceTest {
//
//    String projectId = "local";
//    String subId = "demo-sub";
//    String topicId = "mytesttopic";
//    ManagedChannel managed = ManagedChannelBuilder.forTarget("localhost:8085").usePlaintext().build();
//
//    TopicName topicName;
//    Subscription subscription;
//
//    Subscriber subscriber;
//
//    Tracer tracer = GlobalOpenTelemetry.tracerBuilder("mytesttracer").build();
//
//
//    @Test
//    void testme() {
//        Class c = PubsubPropagator.class;
//        System.out.println(c.getName());
//    }
//
//
//    @Test
//    void pubsubTracer() throws ExecutionException, InterruptedException, IOException {
//
//        Publisher publisher = getPublisher(topicName);
//        TracingPublisher tracingPublisher = new TracingPublisher(publisher, tracer);
//
//        ByteString data = ByteString.copyFromUtf8("Hello pubsub");
//        var message = PubsubMessage.newBuilder().setData(data).build();
//        String msgId = tracingPublisher.publish(message).get();
//
//        System.out.println("Message has been published "+ msgId);
//        subscriber.startAsync().awaitTerminated();
//    }
//
//    @BeforeEach
//    void setUp() throws IOException {
//        topicName = createTopic();
//        subscription = createSubscription();
//        subscriber = getSubscriber(subscription);
//    }
//    @AfterEach
//    void tearDown() {
//        managed.shutdown();
//        subscriber.stopAsync();
//    }
//
//    Subscriber getSubscriber(Subscription subscription) {
//        CredentialsProvider credentialsProvider = NoCredentialsProvider.create();
//        TracingMessageReceiver tracingMessageReceiver = new TracingMessageReceiver(this::receive, tracer);
//        return Subscriber
//                .newBuilder(subscription.getName(), tracingMessageReceiver)
//                .setChannelProvider(getChannel())
//                .setCredentialsProvider(credentialsProvider)
//                .build();
//    }
//
//    public Subscription createSubscription() throws IOException {
//        SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create(SubscriptionAdminSettings.newBuilder()
//                .setTransportChannelProvider(getChannel())
//                .setCredentialsProvider(getCredentials())
//                .build());
//        SubscriptionName name = SubscriptionName.of(projectId, subId);
//        TopicName topic = TopicName.ofProjectTopicName(projectId, topicId);
//        PushConfig pushConfig = PushConfig.newBuilder().build();
//        int ackDeadlineSeconds = 5;
//
//        Subscription subscription = subscriptionAdminClient.getSubscription(name);
//        if( subscription == null ) {
//            subscription = subscriptionAdminClient.createSubscription(name, topic, pushConfig, ackDeadlineSeconds);
//        }
//        return subscription;
//    }
//
//    void receive(final PubsubMessage message, final AckReplyConsumer consumer) {
//        log.info("msg received  {} traceId {}", message.getData());
//        consumer.ack();
//    }
//
//    TransportChannelProvider getChannel() {
//        return FixedTransportChannelProvider.create(GrpcTransportChannel.create(managed));
//    }
//
//    CredentialsProvider getCredentials() {
//        return NoCredentialsProvider.create();
//    }
//
//    TopicName createTopic() throws IOException {
//        TopicAdminClient topicClient =
//                TopicAdminClient.create(
//                        TopicAdminSettings.newBuilder()
//                                .setTransportChannelProvider(getChannel())
//                                .setCredentialsProvider(NoCredentialsProvider.create())
//                                .build());
//
//        TopicName topicName =  TopicName.of(projectId, topicId);
//        Topic topic = topicClient.getTopic(topicName);
//        if(topic == null) {
//            topicClient.createTopic(topicName);
//        }
//        return topicName;
//    }
//
//    Publisher getPublisher(TopicName topicName) {
//        try {
//            Publisher publisher = Publisher.newBuilder(topicName)
//                            .setChannelProvider(getChannel())
//                            .setCredentialsProvider(getCredentials())
//                            .build();
//            return publisher;
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
//
//

package com.vigneshsn.pubsub.trace.controller;


import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;


@Slf4j
@Path("/greetings")
@RequiredArgsConstructor
public class GreetingController {

    public final Publisher publisher;

    @POST
    @Path("/{name}")
    public Response sendGreeting(@PathParam("name") String name) throws ExecutionException, InterruptedException {
        log.info("send invoked name: {}", name);
        ByteString data = ByteString.copyFromUtf8("Hello "+ name);
        var message = PubsubMessage.newBuilder().setData(data).build();
        var id = publisher.publish(message).get();
        log.info("greeting send {}", id);
        return Response.accepted().build();
    }

}

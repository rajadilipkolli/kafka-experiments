package com.example.quarkuskafkademo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.jboss.logging.Logger;

@Path("/produce")
public class ProducerResource {

    @Inject
    @Channel("demo-out")
    @OnOverflow(value = OnOverflow.Strategy.BUFFER, bufferSize = 1000)
    Emitter<String> emitter;

    @Inject
    ObjectMapper mapper;

    @ConfigProperty(name = "app.producer.send-timeout", defaultValue = "10s")
    Duration sendTimeout;

    private static final Logger LOG = Logger.getLogger(ProducerResource.class);

    /**
     * Accept a JSON payload and forward its raw string to the outgoing channel.
     * Tests assert the exact JSON string was consumed.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public CompletionStage<Response> produce(String jsonPayload) {
        try {
            return emitter.send(jsonPayload)
                    .toCompletableFuture()
                    .copy()
                    .orTimeout(sendTimeout.toMillis(), TimeUnit.MILLISECONDS)
                    .thenApply(v -> Response.accepted().build())
                    .exceptionally(t -> {
                        LOG.error("Failed to produce message", t);
                        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                                .entity("Service unavailable: Kafka producer failure")
                                .build();
                    });
        } catch (Exception e) {
            LOG.error("Synchronous overflow or failure producing message", e);
            return CompletableFuture.completedFuture(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Service unavailable: Kafka producer failure")
                    .build());
        }
    }

    /**
     * Typed endpoint that accepts a MessageDto as a POJO. It will be converted
     * to JSON using Jackson and the resulting JSON string will be emitted.
     */
    @POST
    @Path("/pojo")
    @Consumes(MediaType.APPLICATION_JSON)
    public CompletionStage<Response> producePojo(MessageDto dto) {
        String json;
        try {
            json = mapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize MessageDto", e);
            return CompletableFuture.completedFuture(
                    Response.serverError().entity("failed to serialize message").build());
        }

        try {
            return emitter.send(json)
                    .toCompletableFuture()
                    .copy()
                    .orTimeout(sendTimeout.toMillis(), TimeUnit.MILLISECONDS)
                    .thenApply(v -> Response.accepted().build())
                    .exceptionally(t -> {
                        LOG.error("Failed to produce message", t);
                        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                                .entity("Service unavailable: Kafka producer failure")
                                .build();
                    });
        } catch (Exception e) {
            LOG.error("Synchronous overflow or failure producing message", e);
            return CompletableFuture.completedFuture(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("Service unavailable: Kafka producer failure")
                    .build());
        }
    }
}

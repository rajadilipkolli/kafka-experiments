package com.example.quarkuskafkademo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProducerResourceTest {

    private ProducerResource producerResource;

    @BeforeEach
    void setUp() {
        producerResource = new ProducerResource();
        producerResource.mapper = new ObjectMapper();
    }

    static class TestEmitter implements Emitter<String> {
        boolean throwOnSend;
        boolean failFutureOnSend;
        boolean called;

        @Override
        public CompletionStage<Void> send(String msg) {
            called = true;
            if (throwOnSend) {
                throw new IllegalStateException("Simulated buffer overflow");
            }
            if (failFutureOnSend) {
                CompletableFuture<Void> failed = new CompletableFuture<>();
                failed.completeExceptionally(new RuntimeException("Simulated Kafka send failure / nack"));
                return failed;
            }
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public <M extends Message<? extends String>> void send(M msg) {}

        @Override
        public void complete() {}

        @Override
        public void error(Exception e) {}

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean hasRequests() {
            return true;
        }
    }

    @Test
    void testProduceSuccessReturns202() throws Exception {
        TestEmitter emitter = new TestEmitter();
        producerResource.emitter = emitter;

        Response response = producerResource
                .produce("{\"msg\":\"ok\"}")
                .toCompletableFuture()
                .get(5, TimeUnit.SECONDS);
        assertThat(response.getStatus(), equalTo(202));
    }

    @Test
    void testProduceNackOrAsyncFailureReturns503() throws Exception {
        TestEmitter emitter = new TestEmitter();
        emitter.failFutureOnSend = true;
        producerResource.emitter = emitter;

        Response response = producerResource
                .produce("{\"msg\":\"fail\"}")
                .toCompletableFuture()
                .get(5, TimeUnit.SECONDS);
        assertThat(response.getStatus(), equalTo(503));
    }

    @Test
    void testProduceSynchronousOverflowReturns503() throws Exception {
        TestEmitter emitter = new TestEmitter();
        emitter.throwOnSend = true;
        producerResource.emitter = emitter;

        Response response = producerResource
                .produce("{\"msg\":\"overflow\"}")
                .toCompletableFuture()
                .get(5, TimeUnit.SECONDS);
        assertThat(response.getStatus(), equalTo(503));
    }

    @Test
    void testProducePojoNackReturns503() throws Exception {
        TestEmitter emitter = new TestEmitter();
        emitter.failFutureOnSend = true;
        producerResource.emitter = emitter;

        Response response = producerResource
                .producePojo(new MessageDto("test", 10))
                .toCompletableFuture()
                .get(5, TimeUnit.SECONDS);
        assertThat(response.getStatus(), equalTo(503));
    }

    @Test
    void testProducePojoSynchronousOverflowReturns503() throws Exception {
        TestEmitter emitter = new TestEmitter();
        emitter.throwOnSend = true;
        producerResource.emitter = emitter;

        Response response = producerResource
                .producePojo(new MessageDto("test", 10))
                .toCompletableFuture()
                .get(5, TimeUnit.SECONDS);
        assertThat(response.getStatus(), equalTo(503));
    }

    @Test
    void testProducePojoSerializationFailureReturns500() throws Exception {
        TestEmitter emitter = new TestEmitter();
        producerResource.emitter = emitter;
        producerResource.mapper = new ObjectMapper() {
            @Override
            public String writeValueAsString(Object value) throws JsonProcessingException {
                throw new com.fasterxml.jackson.core.JsonParseException(null, "Serialization error");
            }
        };

        Response response = producerResource
                .producePojo(new MessageDto("test", 10))
                .toCompletableFuture()
                .get(5, TimeUnit.SECONDS);
        assertThat(response.getStatus(), equalTo(500));
    }
}

package com.example.quarkuskafkademo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

@Path("/produce")
public class ProducerResource {

    @Inject
    @Channel("demo-out")
    Emitter<String> emitter;

    @Inject
    ObjectMapper mapper;

    private static final Logger LOG = Logger.getLogger(ProducerResource.class);

    /**
     * Accept a JSON payload and forward its raw string to the outgoing channel.
     * Tests assert the exact JSON string was consumed.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response produce(String jsonPayload) {
        emitter.send(jsonPayload);
        return Response.accepted().build();
    }

    /**
     * Typed endpoint that accepts a MessageDto as a POJO. It will be converted
     * to JSON using Jackson and the resulting JSON string will be emitted.
     */
    @POST
    @Path("/pojo")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response producePojo(MessageDto dto) {
        try {
            String json = mapper.writeValueAsString(dto);
            emitter.send(json);
            return Response.accepted().build();
        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize MessageDto", e);
            return Response.serverError().entity("failed to serialize message").build();
        }
    }
}

/* Licensed under Apache-2.0 2025 */
package com.example.analytics.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

/** Utility class providing JSON serialization and deserialization for Kafka Streams testing. */
public class JsonSerdeUtils {

    /**
     * Returns the class of the JsonSerde implementation for use in configuration.
     *
     * @return Class of JsonSerde implementation
     */
    public static Class<?> getJsonClass() {
        return JsonSerde.class;
    }

    /**
     * Creates a JSON Serde for the specified class type.
     *
     * @param <T> The type for which to create the Serde
     * @param cls The class object for the type
     * @return A configured Serde
     */
    public static <T> Serde<T> jsonSerde(Class<T> cls) {
        return jsonSerde(cls, new ObjectMapper());
    }

    /**
     * Creates a JSON Serde for the specified class type using a provided ObjectMapper.
     *
     * @param <T> The type for which to create the Serde
     * @param cls The class object for the type
     * @param mapper The ObjectMapper to use
     * @return A configured Serde
     */
    public static <T> Serde<T> jsonSerde(Class<T> cls, ObjectMapper mapper) {
        return new JsonSerde<>(cls, mapper);
    }

    /**
     * Generic JSON implementation of the Kafka Serde interface.
     *
     * @param <T> The type handled by this Serde
     */
    public static class JsonSerde<T> implements Serde<T> {
        private final JsonSerializer<T> serializer;
        private final JsonDeserializer<T> deserializer;

        public JsonSerde(Class<T> cls) {
            this(cls, new ObjectMapper());
        }

        public JsonSerde(Class<T> cls, ObjectMapper mapper) {
            this.serializer = new JsonSerializer<>(cls, mapper);
            this.deserializer = new JsonDeserializer<>(cls, mapper);
        }

        @Override
        public Serializer<T> serializer() {
            return serializer;
        }

        @Override
        public Deserializer<T> deserializer() {
            return deserializer;
        }
    }

    /**
     * Serializer implementation that converts objects to JSON bytes.
     *
     * @param <T> The type to serialize
     */
    public static class JsonSerializer<T> implements Serializer<T> {
        private final ObjectMapper mapper;
        private final Class<T> cls;

        public JsonSerializer(Class<T> cls) {
            this(cls, new ObjectMapper());
        }

        public JsonSerializer(Class<T> cls, ObjectMapper mapper) {
            this.cls = cls;
            this.mapper = mapper;
        }

        @Override
        public byte[] serialize(String topic, T data) {
            if (data == null) return null;
            try {
                return mapper.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Deserializer implementation that converts JSON bytes to objects.
     *
     * @param <T> The type to deserialize into
     */
    public static class JsonDeserializer<T> implements Deserializer<T> {
        private final ObjectMapper mapper;
        private final Class<T> cls;

        public JsonDeserializer(Class<T> cls) {
            this(cls, new ObjectMapper());
        }

        public JsonDeserializer(Class<T> cls, ObjectMapper mapper) {
            this.cls = cls;
            this.mapper = mapper;
        }

        @Override
        public T deserialize(String topic, byte[] data) {
            if (data == null) return null;
            try {
                return mapper.readValue(data, cls);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}

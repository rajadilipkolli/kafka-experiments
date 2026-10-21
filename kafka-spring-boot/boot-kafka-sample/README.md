# Connecting to kafka using Spring Boot

This sample demonstrates sending message to topic (test_1), after listening to it will send the same message to (test_2).

test_2 topic is configured for validation and if it fails then it will be retried for 3 times and moved to deadletter queue using non blocking way

## Kafka Listener Management Endpoint

Exposed endpoint to start and stop kafka listener on demand

### Endpoint URL
`/listeners`

### HTTP Methods
- `GET`: Retrieve the current state of all Kafka listeners
- `POST`: Update the state (start/stop) of a specific Kafka listener

### Request/Response Formats

#### GET
Response Format (application/json):
```json
{
    "topic_2_Listener": true,
    "topic_2_Listener-retry": true,
    "topic_1_Listener": true,
    "topic_2_Listener-dlt": true
}
```
#### POST
Request Format (application/json):

```json
{
  "containerId": "topic_2_Listener",
  "operation": "STOP"  // Allowed values: START, STOP
}
```
 
Response Format: Same as GET response
 
### Example Usage

#### Get Listeners State

```shell
curl -X GET http://localhost:8080/listeners
```
 
#### Stop a Listener
```shell
curl -X POST http://localhost:8080/listeners \
-H "Content-Type: application/json" \
-d '{"containerId":"topic_2_Listener","operation":"STOP"}'
```

#### Start a Listener
```shell
curl -X POST http://localhost:8080/listeners \
-H "Content-Type: application/json" \
-d '{"containerId":"topic_2_Listener","operation":"START"}'
```
 
### Error Responses
- `400 Bad Request`: Invalid operation value or malformed request
- `404 Not Found`: Specified listener container ID not found

### URLS
* KafkaUI : http://localhost:8080/springwolf/asyncapi-ui.html

### Backpressure

| Boundary | Mechanism | Configured Limits | Behavior on Overload |
| :--- | :--- | :--- | :--- |
| HTTP Controller -> `Sender` | Timed future wait with ProblemDetail mapping | `app.producer.send-timeout-ms=12000`, `buffer.memory=33554432`, `max.block.ms=5000` | Waits up to 12s; Kafka send errors, timeouts, and buffer saturation map to 503 ProblemDetail (blank message DLT path returns 200) |
| Kafka Broker -> `Receiver2` | Bounded insertion-ordered tracking map | `app.receiver2.max-seen-messages=10000` | Synchronized `LinkedHashMap` overriding `removeEldestEntry` evicts oldest messages when size exceeds limit |

#### Known Limitations
- HTTP request latency now directly includes the broker acknowledgment latency.
- Global request-rate limiting belongs to the HTTP reverse proxy / gateway layer.

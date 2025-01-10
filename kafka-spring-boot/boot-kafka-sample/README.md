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
    "org.springframework.kafka.KafkaListenerEndpointContainer#0": true,
    "org.springframework.kafka.KafkaListenerEndpointContainer#1": true,
    "org.springframework.kafka.KafkaListenerEndpointContainer#1-retry": true,
    "org.springframework.kafka.KafkaListenerEndpointContainer#1-dlt": true
}
```
#### POST
Request Format (application/json):

```json
{
  "containerId": "org.springframework.kafka.KafkaListenerEndpointContainer#1",
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
-d '{"containerId":"org.springframework.kafka.KafkaListenerEndpointContainer#1","operation":"STOP"}'
```

#### Start a Listener
```shell
curl -X POST http://localhost:8080/listeners \
-H "Content-Type: application/json" \
-d '{"containerId":"org.springframework.kafka.KafkaListenerEndpointContainer#1","operation":"START"}'
```
 
### Error Responses
* `400 Bad Request`: Invalid operation value or malformed request
* `404 Not Found`: Specified listener container ID not found

# Connecting to kafka using Spring Boot

This sample demonstrates sending message to topic (test_1), after listening to it will send the same message to (test_2).

test_2 topic is configured for validation and if it fails then it will be retried for 3 times and moved to deadletter queue using non blocking way

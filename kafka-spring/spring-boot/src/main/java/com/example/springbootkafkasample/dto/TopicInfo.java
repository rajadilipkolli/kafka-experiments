package com.example.springbootkafkasample.dto;

public record TopicInfo(String topicName, int partitionCount, int replicationCount) {}

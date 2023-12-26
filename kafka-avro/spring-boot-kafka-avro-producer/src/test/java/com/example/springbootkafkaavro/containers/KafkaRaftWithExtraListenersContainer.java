package com.example.springbootkafkaavro.containers;

import com.github.dockerjava.api.command.InspectContainerResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

public class KafkaRaftWithExtraListenersContainer extends KafkaContainer {

    private List<Supplier<String>> listeners = new ArrayList<>();

    public KafkaRaftWithExtraListenersContainer(String image) {
        super(DockerImageName.parse(image));
    }

    @Override
    protected void configure() {
        super.configure();
        withEnv(
                "KAFKA_LISTENERS",
                String.format(
                        "%s,%s", "INTERNAL://0.0.0.0:19092", getEnvMap().get("KAFKA_LISTENERS")));
        withEnv(
                "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP",
                String.format(
                        "%s,%s",
                        "INTERNAL:PLAINTEXT",
                        getEnvMap().get("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP")));
        getEnvMap()
                .put(
                        "KAFKA_CONTROLLER_QUORUM_VOTERS",
                        String.format(
                                "%s@%s:9094",
                                getEnvMap().get("KAFKA_NODE_ID"),
                                getNetwork() != null
                                        ? listeners.get(0).get().split(":")[0]
                                        : "localhost"));
    }

    @Override
    protected void containerIsStarting(InspectContainerResponse containerInfo) {
        String command = "#!/bin/bash\n";
        // exporting KAFKA_ADVERTISED_LISTENERS with the container hostname
        command +=
                String.format(
                        "export KAFKA_ADVERTISED_LISTENERS=%s,%s,%s\n",
                        String.format("INTERNAL://%s", listeners.get(0).get()),
                        getBootstrapServers(),
                        brokerAdvertisedListener(containerInfo));

        command += "/etc/confluent/docker/run \n";
        copyFileToContainer(Transferable.of(command, 0777), "/testcontainers_start.sh");
    }

    public KafkaRaftWithExtraListenersContainer withAdditionalListener(Supplier<String> listener) {
        this.listeners.add(listener);
        return this;
    }
}

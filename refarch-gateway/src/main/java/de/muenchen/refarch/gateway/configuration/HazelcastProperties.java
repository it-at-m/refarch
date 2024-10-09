package de.muenchen.refarch.gateway.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@ConfigurationProperties("refarch.hazelcast")
@SuppressWarnings("PMD.ImmutableField")
public class HazelcastProperties {
    /**
     * Name of the hazelcast cluster.
     */
    private String clusterName = "session_replication_group";
    /**
     * Name of the hazelcast instance.
     */
    private String instanceName = "hazl_instance";
    /**
     * Kubernetes namespace name.
     * Required for running hazelcast inside kubernetes.
     */
    private String namespaceName;
    /**
     * Kubernetes service name.
     * Required for running hazelcast inside kubernetes.
     */
    private String serviceName;
}

package de.muenchen.oss.refarch.gateway.configuration;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties("refarch.hazelcast")
@Validated
public class HazelcastProperties {
    /**
     * Name of the hazelcast cluster.
     */
    @NotBlank private String clusterName = "session_replication_group";
    /**
     * Name of the hazelcast instance.
     */
    @NotBlank private String instanceName = "hazl_instance";
    /**
     * Kubernetes service name.
     * Required for running hazelcast inside kubernetes.
     */
    private String serviceName;
}

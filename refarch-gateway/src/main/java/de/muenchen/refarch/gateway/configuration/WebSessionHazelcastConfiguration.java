package de.muenchen.refarch.gateway.configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.session.MapSession;
import org.springframework.session.ReactiveMapSessionRepository;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;
import org.springframework.session.hazelcast.HazelcastIndexedSessionRepository;

/**
 * This class configures Hazelcast as the ReactiveSessionRepository.
 */
@Configuration
@EnableSpringWebSession
@Profile({ "hazelcast-local", "hazelcast-k8s" })
@RequiredArgsConstructor
public class WebSessionHazelcastConfiguration {

    private final HazelcastProperties hazelcastProperties;

    @Bean
    public ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new WebSessionServerOAuth2AuthorizedClientRepository();
    }

    @Bean
    public ReactiveSessionRepository<MapSession> reactiveSessionRepository(@Autowired final HazelcastInstance hazelcastInstance) {
        final IMap<String, Session> map = hazelcastInstance.getMap(HazelcastIndexedSessionRepository.DEFAULT_SESSION_MAP_NAME);
        return new ReactiveMapSessionRepository(map);
    }

    @Bean
    public HazelcastInstance hazelcastInstance(@Autowired final Config config) {
        return Hazelcast.getOrCreateHazelcastInstance(config);
    }

    @Bean
    @Profile({ "hazelcast-local" })
    public Config localConfig(@Value(
        "${spring.session.timeout}"
    ) final int timeout) {
        final Config hazelcastConfig = new Config();
        hazelcastConfig.setClusterName(hazelcastProperties.getClusterName());
        hazelcastConfig.setInstanceName(hazelcastProperties.getInstanceName());

        addSessionTimeoutToHazelcastConfig(hazelcastConfig, timeout);

        final NetworkConfig networkConfig = hazelcastConfig.getNetworkConfig();

        final JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig()
                .setEnabled(true)
                .addMember("localhost");

        return hazelcastConfig;
    }

    @Bean
    @Profile({ "hazelcast-k8s" })
    public Config config(@Value("${spring.session.timeout}") final int timeout) {
        final Config hazelcastConfig = new Config();
        hazelcastConfig.setClusterName(hazelcastProperties.getClusterName());
        hazelcastConfig.setInstanceName(hazelcastProperties.getInstanceName());

        addSessionTimeoutToHazelcastConfig(hazelcastConfig, timeout);

        hazelcastConfig.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        hazelcastConfig.getNetworkConfig().getJoin().getKubernetesConfig().setEnabled(true)
                //If we don't set a specific name, it would call -all- services within a namespace
                .setProperty("service-name", hazelcastProperties.getServiceName());

        return hazelcastConfig;
    }

    /**
     * Adds the session timeout in seconds to the hazelcast configuration.
     * <p>
     * Since we are creating the map it's important to evict sessions by setting a reasonable value for
     * time to live.
     *
     * @param hazelcastConfig to add the timeout.
     * @param sessionTimeout for security session.
     */
    private void addSessionTimeoutToHazelcastConfig(final Config hazelcastConfig, final int sessionTimeout) {
        final MapConfig sessionConfig = new MapConfig();
        sessionConfig.setName(HazelcastIndexedSessionRepository.DEFAULT_SESSION_MAP_NAME);
        sessionConfig.setTimeToLiveSeconds(sessionTimeout);
        sessionConfig.getEvictionConfig().setEvictionPolicy(EvictionPolicy.LRU);

        hazelcastConfig.addMapConfig(sessionConfig);
    }

}

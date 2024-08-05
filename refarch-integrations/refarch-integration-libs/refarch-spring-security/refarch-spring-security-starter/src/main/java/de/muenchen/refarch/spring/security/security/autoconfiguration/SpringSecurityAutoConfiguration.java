package de.muenchen.refarch.spring.security.security.autoconfiguration;

import de.muenchen.refarch.spring.security.security.SpringSecurityProperties;
import de.muenchen.refarch.spring.security.security.factory.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

/**
 * Auto configuration used to configure Spring security.
 */
@EnableConfigurationProperties(SpringSecurityProperties.class)
@PropertySource(value = "classpath:refarch-security-application.yaml", factory = YamlPropertySourceFactory.class)
public class SpringSecurityAutoConfiguration {
}

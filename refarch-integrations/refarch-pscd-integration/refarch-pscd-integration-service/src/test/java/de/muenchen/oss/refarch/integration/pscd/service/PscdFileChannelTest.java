package de.muenchen.oss.refarch.integration.pscd.service;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * A service running the file channel alone, configured as the tests driving the real sample batches
 * need it: the legacy archive names, notification switched on, and the mandatory inbound
 * credentials.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SpringBootTest(
        properties = {
                "refarch.pscd.inbound.file.enabled=true",
                "refarch.pscd.inbound.soap.enabled=false",
                "refarch.pscd.inbound.rest.enabled=false",
                "refarch.pscd.inbound.file.done-directory=archive/successful",
                "refarch.pscd.inbound.file.error-directory=archive/error",
                "refarch.pscd.inbound.file.stable-for=0",
                "refarch.pscd.notification.file.enabled=true",
                "refarch.pscd.notification.to=doesnotexist@muenchen.de",
                "refarch.pscd.inbound.security.username=pscd-sender",
                "refarch.pscd.inbound.security.password=s3cret"
        }
)
public @interface PscdFileChannelTest {
}

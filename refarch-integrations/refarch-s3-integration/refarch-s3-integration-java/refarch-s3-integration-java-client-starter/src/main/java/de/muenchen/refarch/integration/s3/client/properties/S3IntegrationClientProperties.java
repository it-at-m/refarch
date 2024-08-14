package de.muenchen.refarch.integration.s3.client.properties;

import de.muenchen.refarch.integration.s3.client.domain.model.SupportedFileExtensions;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

@Getter
@Setter
@ConfigurationProperties(prefix = "refarch.s3.client")
public class S3IntegrationClientProperties {
    /** Maximum allowed file size. Default is 0, which indicates that there is no limit. */
    private DataSize maxFileSize = DataSize.ofBytes(0L);
    /**
     * Maximum allowed size of a batch of files. Default is 0, which indicates that there is no limit.
     */
    private DataSize maxBatchSize = DataSize.ofBytes(0L);
    /** Supported file extensions. */
    private SupportedFileExtensions supportedFileExtensions;

}

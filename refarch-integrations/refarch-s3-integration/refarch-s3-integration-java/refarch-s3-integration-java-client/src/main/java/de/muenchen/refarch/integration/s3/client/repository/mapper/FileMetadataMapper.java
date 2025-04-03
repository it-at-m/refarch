package de.muenchen.refarch.integration.s3.client.repository.mapper;

import de.muenchen.refarch.integration.s3.client.domain.model.FileMetadata;
import de.muenchen.refarch.integration.s3.infrastructure.mapper.MapstructConfiguration;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfiguration.class)
public interface FileMetadataMapper {

    List<FileMetadata> map(List<de.muenchen.refarch.integration.s3.domain.model.FileMetadata> fileMetadata);

}

package de.muenchen.refarch.integration.s3.client.repository.mapper;

import de.muenchen.refarch.integration.s3.client.domain.model.FileMetadata;
import de.muenchen.refarch.integration.s3.client.model.FileMetadataDto;
import de.muenchen.refarch.integration.s3.client.repository.infrastructure.MapstructConfiguration;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = MapstructConfiguration.class)
public interface FileMetadataMapper {

    List<FileMetadata> dto2Model(List<FileMetadataDto> fileMetadata);

}

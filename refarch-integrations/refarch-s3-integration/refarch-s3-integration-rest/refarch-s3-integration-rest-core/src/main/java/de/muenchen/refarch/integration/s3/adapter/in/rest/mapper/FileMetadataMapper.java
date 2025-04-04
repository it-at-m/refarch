package de.muenchen.refarch.integration.s3.adapter.in.rest.mapper;

import de.muenchen.refarch.integration.s3.adapter.in.rest.dto.FilesMetadataInFolderDto;
import de.muenchen.refarch.integration.s3.domain.model.FilesMetadataInFolder;
import de.muenchen.refarch.integration.s3.infrastructure.mapper.MapstructConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfiguration.class)
public interface FileMetadataMapper {

    FilesMetadataInFolderDto model2Dto(FilesMetadataInFolder fileMetadata);

}

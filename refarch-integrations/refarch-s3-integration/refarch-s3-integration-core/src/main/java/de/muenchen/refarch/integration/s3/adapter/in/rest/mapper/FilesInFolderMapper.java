package de.muenchen.refarch.integration.s3.adapter.in.rest.mapper;

import de.muenchen.refarch.integration.s3.adapter.in.rest.dto.FilesInFolderDto;
import de.muenchen.refarch.integration.s3.domain.model.FilesInFolder;
import de.muenchen.refarch.integration.s3.infrastructure.mapper.MapstructConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfiguration.class)
public interface FilesInFolderMapper {

    FilesInFolderDto model2Dto(final FilesInFolder filesInFolder);

}

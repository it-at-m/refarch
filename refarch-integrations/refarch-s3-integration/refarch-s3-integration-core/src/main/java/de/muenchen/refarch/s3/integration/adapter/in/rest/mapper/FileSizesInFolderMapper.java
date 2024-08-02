package de.muenchen.refarch.s3.integration.adapter.in.rest.mapper;

import de.muenchen.refarch.s3.integration.adapter.in.rest.dto.FileSizesInFolderDto;
import de.muenchen.refarch.s3.integration.domain.model.FileSizesInFolder;
import de.muenchen.refarch.s3.integration.infrastructure.mapper.MapstructConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfiguration.class)
public interface FileSizesInFolderMapper {

    FileSizesInFolderDto model2Dto(FileSizesInFolder fileSizesInFolder);
}

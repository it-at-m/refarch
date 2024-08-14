package de.muenchen.refarch.integration.s3.adapter.in.rest.mapper;

import de.muenchen.refarch.integration.s3.adapter.in.rest.dto.FileSizesInFolderDto;
import de.muenchen.refarch.integration.s3.domain.model.FileSizesInFolder;
import de.muenchen.refarch.integration.s3.infrastructure.mapper.MapstructConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfiguration.class)
public interface FileSizesInFolderMapper {

    FileSizesInFolderDto model2Dto(FileSizesInFolder fileSizesInFolder);
}

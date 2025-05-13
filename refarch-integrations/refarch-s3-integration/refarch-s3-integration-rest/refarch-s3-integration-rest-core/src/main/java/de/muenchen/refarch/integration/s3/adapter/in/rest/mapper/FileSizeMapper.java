package de.muenchen.refarch.integration.s3.adapter.in.rest.mapper;

import de.muenchen.refarch.integration.s3.adapter.in.rest.dto.FileSizeDto;
import de.muenchen.refarch.integration.s3.domain.model.FileSize;
import de.muenchen.refarch.integration.s3.infrastructure.mapper.MapstructConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfiguration.class)
public interface FileSizeMapper {

    FileSizeDto model2dto(FileSize fileSize);
}

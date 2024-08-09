package de.muenchen.refarch.integration.s3.adapter.in.rest.mapper;

import de.muenchen.refarch.integration.s3.adapter.in.rest.dto.FileDataDto;
import de.muenchen.refarch.integration.s3.domain.model.FileData;
import de.muenchen.refarch.integration.s3.infrastructure.mapper.MapstructConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfiguration.class)
public interface FileDataMapper {

    FileData dto2Model(final FileDataDto fileDataDto);

}

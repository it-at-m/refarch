package de.muenchen.refarch.s3.integration.adapter.in.rest.mapper;

import de.muenchen.refarch.s3.integration.adapter.in.rest.dto.FileDataDto;
import de.muenchen.refarch.s3.integration.domain.model.FileData;
import de.muenchen.refarch.s3.integration.infrastructure.mapper.MapstructConfiguration;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfiguration.class)
public interface FileDataMapper {

    FileData dto2Model(final FileDataDto fileDataDto);

}

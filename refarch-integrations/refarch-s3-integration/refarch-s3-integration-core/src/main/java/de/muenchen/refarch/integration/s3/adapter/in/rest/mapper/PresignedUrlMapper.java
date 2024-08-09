package de.muenchen.refarch.integration.s3.adapter.in.rest.mapper;

import de.muenchen.refarch.integration.s3.adapter.in.rest.dto.PresignedUrlDto;
import de.muenchen.refarch.integration.s3.domain.model.PresignedUrl;
import de.muenchen.refarch.integration.s3.infrastructure.mapper.MapstructConfiguration;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfiguration.class)
public interface PresignedUrlMapper {

    PresignedUrlDto model2Dto(final PresignedUrl fileResponse);

    List<PresignedUrlDto> models2Dtos(final List<PresignedUrl> presignedUrls);

}

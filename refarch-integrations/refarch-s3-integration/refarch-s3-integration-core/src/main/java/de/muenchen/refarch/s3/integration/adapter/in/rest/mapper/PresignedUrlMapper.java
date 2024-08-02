package de.muenchen.refarch.s3.integration.adapter.in.rest.mapper;

import de.muenchen.refarch.s3.integration.adapter.in.rest.dto.PresignedUrlDto;
import de.muenchen.refarch.s3.integration.domain.model.PresignedUrl;
import de.muenchen.refarch.s3.integration.infrastructure.mapper.MapstructConfiguration;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(config = MapstructConfiguration.class)
public interface PresignedUrlMapper {

    PresignedUrlDto model2Dto(final PresignedUrl fileResponse);

    List<PresignedUrlDto> models2Dtos(final List<PresignedUrl> presignedUrls);

}

package com.communityplatform.content.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.communityplatform.content.dto.post.PostCreateDto;
import com.communityplatform.content.dto.post.PostResponseDto;
import com.communityplatform.content.dto.post.PostSummaryDto;
import com.communityplatform.content.dto.post.PostUpdateDto;
import com.communityplatform.content.entity.PostEntity;

/**
 * MapStruct mapper for Post entity and DTOs.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PostMapper {

    /**
     * Convert CreateDto to Entity.
     */
    PostEntity toEntity(PostCreateDto dto);

    /**
     * Convert Entity to ResponseDto.
     */
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "profilePictureUrl", ignore = true)
    @Mapping(target = "authorFullName", ignore = true)
    @Mapping(target = "likedByCurrentUser", ignore = true)
    PostResponseDto toResponseDto(PostEntity entity);

    /**
     * Convert Entity to SummaryDto.
     */
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "profilePictureUrl", ignore = true)
    PostSummaryDto toSummaryDto(PostEntity entity);

    /**
     * Update entity from UpdateDto (partial update).
     */
    void updateEntityFromDto(PostUpdateDto dto, @MappingTarget PostEntity entity);

}

package com.communityplatform.content.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.communityplatform.content.dto.comment.CommentCreateDto;
import com.communityplatform.content.dto.comment.CommentResponseDto;
import com.communityplatform.content.entity.CommentEntity;

/**
 * MapStruct mapper for Comment entity and DTOs.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CommentMapper {

    /**
     * Convert CreateDto to Entity.
     */
    CommentEntity toEntity(CommentCreateDto dto);

    /**
     * Convert Entity to ResponseDto.
     */
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "authorFullName", ignore = true)
    @Mapping(target = "profilePictureUrl", ignore = true)
    @Mapping(target = "likedByCurrentUser", ignore = true)
    CommentResponseDto toResponseDto(CommentEntity entity);
}

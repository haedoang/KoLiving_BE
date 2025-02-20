package com.koliving.api.room.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.koliving.api.user.domain.Gender;
import com.koliving.api.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Objects;

@Schema(description = "작성자 정보")
public record WriterResponse(
    @Schema(description = "작성자 이름")
    String firstName,

    @Schema(description = "작성자 성")
    String lastName,

    @Schema(description = "작성자 성별")
    Gender gender,

    @Schema(description = "작성자 생년월일")
    LocalDate birthDate,

    @Schema(description = "작성자 소개")
    String description,

    @Schema(description = "작성자 프로필 URL")
    String imageUrl
) {
    @JsonIgnore
    private static final String defaultProfile = "https://kr.object.ncloudstorage.com/backend-bucket/images/profiles/def_img.png";

    public static WriterResponse of(User entity) {
        String imageUrl = Objects.isNull(entity.getImageFile()) ? defaultProfile : entity.getImageFile().getPath();
        return new WriterResponse(entity.getFirstName(), entity.getLastName(), entity.getGender(), entity.getBirthDate(), entity.getDescription(), imageUrl);
    }
}

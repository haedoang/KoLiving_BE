package com.koliving.api.base;


import static org.springframework.http.HttpStatus.BAD_REQUEST;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ServiceError {
    RECORD_NOT_EXIST(BAD_REQUEST, "0001", "레코드 미존재"),
    INVALID_LOCATION(BAD_REQUEST, "0002", "유효하지 않은 Location 정보"),
    INVALID_MONEY(BAD_REQUEST, "0003", "금액이 유효하지 않음"),
    INVALID_MAINTENANCE_FEE(BAD_REQUEST, "0004", "관리비 금액이 유효하지 않음"),
    ILLEGAL_MAINTENANCE(BAD_REQUEST, "0005", "관리비 객체 생성 유효성 실패"),
    ILLEGAL_ROOM_INFO(BAD_REQUEST, "0006", "방 정보 객체 생성 유효성 실패"),
    UPLOAD_FAIL(BAD_REQUEST, "0007", "파일 업로드 실패"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "0008", "사용자 인증 실패"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "0009", "사용자 권한 없음");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

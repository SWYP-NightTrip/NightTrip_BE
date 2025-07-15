package com.nighttrip.core.global.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	USER_NOT_FOUND(HttpStatus.NOT_FOUND,"존재하지 않는 유저입니다."),
	PLACE_NOT_FOUND(HttpStatus.NOT_FOUND,"존재하지 않는 장소입니다."),

	INVALID_OAUTH_CODE_MISSING(HttpStatus.BAD_REQUEST, "인가 코드가 누락되었습니다."),
	INVALID_OAUTH_CODE_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않은 인가 코드입니다."),
	UNSUPPORTED_SOCIAL_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인입니다."),
	EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "해당 이메일은 이미 다른 소셜 계정으로 가입되어 있습니다."),
	OAUTH_PROVIDER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "OAuth 공급자에서 에러가 발생했습니다.");

	private final HttpStatus errorCode;
	private final String message;

}

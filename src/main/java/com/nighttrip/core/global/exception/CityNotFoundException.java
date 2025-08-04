package com.nighttrip.core.global.exception;


import com.nighttrip.core.global.enums.ErrorCode;

import com.nighttrip.core.global.enums.ErrorCode;

public class CityNotFoundException extends BusinessException {

    public CityNotFoundException() {
        super(ErrorCode.CITY_NOT_FOUND);
    }
    public CityNotFoundException(String message) {
        super(ErrorCode.CITY_NOT_FOUND.getErrorCode(), message);
    }
}
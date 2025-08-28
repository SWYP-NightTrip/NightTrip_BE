package com.nighttrip.core.domain.triporder;

import lombok.Getter;
import lombok.NoArgsConstructor;

public record ArrivalTimeUpdateRequest(
        String arrivalTime
) {
    // 레코드는 별도의 코드를 작성할 필요가 없습니다.
}
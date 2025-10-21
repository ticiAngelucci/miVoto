package com.mivoto.api.dto;

import java.util.Map;

public record TallyResponse(String ballotId, Map<String, Long> counts) {
}

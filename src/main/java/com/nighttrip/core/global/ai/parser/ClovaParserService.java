package com.nighttrip.core.global.ai.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClovaParserService {
    public List<RecommendationResult> parseClovaResponse(String clovaText) {
        List<RecommendationResult> resultList = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\d+\\.\\s(.+?)\\s-\\s(.+)");
        Matcher matcher = pattern.matcher(clovaText);

        while (matcher.find()) {
            String name = matcher.group(1).trim();
            String reason = matcher.group(2).trim();
            resultList.add(new RecommendationResult(name, reason));
        }

        return resultList;
    }

}

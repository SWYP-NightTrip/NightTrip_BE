package com.nighttrip.core.global.ai.recommend;

public class RecommendationScorer {

    public double calculateScore(double visitRate, double spendRate, double categoryMatch, String companionType) {
        double alpha, beta, gamma;

        switch (companionType.toLowerCase()) {
            case "가족":
                alpha = 0.3;
                beta = 0.3;
                gamma = 0.4; // 시설/환경 적합도
                break;
            case "커플":
                alpha = 0.4;
                beta = 0.4;
                gamma = 0.2; // 감성/야경
                break;
            case "혼자":
                alpha = 0.5;
                beta = 0.3;
                gamma = 0.2; // 접근성, 가성비
                break;
            default:
                alpha = 0.4;
                beta = 0.4;
                gamma = 0.2;
        }

        return (visitRate * alpha) + (spendRate * beta) + (categoryMatch * gamma);
    }
}


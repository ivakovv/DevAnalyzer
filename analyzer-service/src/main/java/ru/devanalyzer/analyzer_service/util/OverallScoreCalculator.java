package ru.devanalyzer.analyzer_service.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.devanalyzer.analyzer_service.config.properties.ScoreComplexityProperties;
import ru.devanalyzer.analyzer_service.config.properties.ScorePenaltiesProperties;
import ru.devanalyzer.analyzer_service.config.properties.ScoreWeightsProperties;
import ru.devanalyzer.analyzer_service.dto.AnalysisSummary;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverallScoreCalculator {

    private final ScoreWeightsProperties weights;
    private final ScorePenaltiesProperties penalties;
    private final ScoreComplexityProperties complexityProps;

    public int calculateScore(AnalysisSummary summary) {
        int totalScanned = summary.passedQualityGate() + summary.failedQualityGate();
        if (totalScanned == 0) {
            log.info("No repositories scanned, returning score 0");
            return 0;
        }

        double score = 0.0;

        double qualityGateScore = calculateQualityGateScore(
                summary.passedQualityGate(),
                summary.failedQualityGate()
        );
        score += qualityGateScore * weights.getQualityGate();
        log.debug("Quality Gate: {} * {} = {}", qualityGateScore, weights.getQualityGate(),
                qualityGateScore * weights.getQualityGate());

        double securityScore = calculateSecurityScore(
                summary.medianVulnerabilities(),
                summary.medianSecurityRating()
        );
        score += securityScore * weights.getSecurity();
        log.debug("Security: {} * {} = {}", securityScore, weights.getSecurity(),
                securityScore * weights.getSecurity());

        double reliabilityScore = calculateReliabilityScore(
                summary.medianBugs(),
                summary.medianReliabilityRating()
        );
        score += reliabilityScore * weights.getReliability();
        log.debug("Reliability: {} * {} = {}", reliabilityScore, weights.getReliability(),
                reliabilityScore * weights.getReliability());

        double maintainabilityScore = calculateMaintainabilityScore(
                summary.medianCodeSmells(),
                summary.medianMaintainabilityRating()
        );
        score += maintainabilityScore * weights.getMaintainability();
        log.debug("Maintainability: {} * {} = {}", maintainabilityScore, weights.getMaintainability(),
                maintainabilityScore * weights.getMaintainability());

        double coverageScore = summary.medianCoverage();
        score += coverageScore * weights.getCoverage();
        log.debug("Coverage: {} * {} = {}", coverageScore, weights.getCoverage(),
                coverageScore * weights.getCoverage());

        double duplicationScore = calculateDuplicationScore(summary.medianDuplications());
        score += duplicationScore * weights.getDuplication();
        log.debug("Duplication: {} * {} = {}", duplicationScore, weights.getDuplication(),
                duplicationScore * weights.getDuplication());

        double complexityScore = calculateComplexityScore(
                summary.medianLinesOfCode(),
                totalScanned
        );
        score += complexityScore * weights.getComplexity();
        log.debug("Complexity: {} * {} = {}", complexityScore, weights.getComplexity(),
                complexityScore * weights.getComplexity());

        int finalScore = (int) Math.round(Math.min(100, Math.max(0, score)));
        log.info("Overall score calculated: {}", finalScore);

        return finalScore;
    }

    private double calculateQualityGateScore(int passed, int failed) {
        int total = passed + failed;
        if (total == 0) {
            return 0.0;
        }
        return (passed * 100.0) / total;
    }

    private double calculateSecurityScore(double medianVulnerabilities, String rating) {
        double ratingScore = ratingToScore(rating);
        double vulnerabilityPenalty = Math.min(
                penalties.getVulnerabilityMaxPenalty(),
                medianVulnerabilities * penalties.getVulnerabilityPenalty()
        );
        return Math.max(0, ratingScore - vulnerabilityPenalty);
    }

    private double calculateReliabilityScore(double medianBugs, String rating) {
        double ratingScore = ratingToScore(rating);
        double bugPenalty = Math.min(
                penalties.getBugMaxPenalty(),
                medianBugs * penalties.getBugPenalty()
        );
        return Math.max(0, ratingScore - bugPenalty);
    }

    private double calculateMaintainabilityScore(double medianCodeSmells, String rating) {
        double ratingScore = ratingToScore(rating);
        double smellPenalty = Math.min(
                penalties.getCodeSmellMaxPenalty(),
                medianCodeSmells / penalties.getCodeSmellDivisor()
        );
        return Math.max(0, ratingScore - smellPenalty);
    }

    private double calculateDuplicationScore(double medianDuplications) {
        return Math.max(0, 100 - medianDuplications * penalties.getDuplicationMultiplier());
    }

    private double ratingToScore(String rating) {
        if (rating == null) {
            return 0;
        }
        return switch (rating.toUpperCase()) {
            case "A" -> 100.0;
            case "B" -> 80.0;
            case "C" -> 60.0;
            case "D" -> 40.0;
            case "E" -> 20.0;
            default -> 0;
        };
    }

    private double calculateComplexityScore(double medianLinesOfCode, int repositoryCount) {
        double projectSizeScore = calculateProjectSizeScore(medianLinesOfCode);
        double repositoryCountScore = calculateRepositoryCountScore(repositoryCount);

        double totalWeight = complexityProps.getProjectSizeWeight() + complexityProps.getRepositoryCountWeight();
        double normalizedProjectSizeWeight = complexityProps.getProjectSizeWeight() / totalWeight;
        double normalizedRepoCountWeight = complexityProps.getRepositoryCountWeight() / totalWeight;

        double complexityScore = (projectSizeScore * normalizedProjectSizeWeight) +
                (repositoryCountScore * normalizedRepoCountWeight);

        log.debug("Complexity breakdown: projectSize={} (weight={}), repoCount={} (weight={}), total={}",
                projectSizeScore, normalizedProjectSizeWeight,
                repositoryCountScore, normalizedRepoCountWeight,
                complexityScore);

        return Math.min(100, Math.max(0, complexityScore));
    }


    private double calculateProjectSizeScore(double medianLinesOfCode) {
        if (medianLinesOfCode <= 0) {
            return 0;
        }

        int minThreshold = complexityProps.getMinLinesThreshold();
        int maxThreshold = complexityProps.getMaxLinesThreshold();

        if (medianLinesOfCode <= minThreshold) {
            return (medianLinesOfCode / minThreshold) * 50;
        } else if (medianLinesOfCode >= maxThreshold) {
            return 100;
        } else {
            double range = maxThreshold - minThreshold;
            double position = medianLinesOfCode - minThreshold;
            return 50 + (position / range) * 50;
        }
    }


    private double calculateRepositoryCountScore(int repositoryCount) {
        if (repositoryCount <= 0) {
            return 0;
        }

        int minThreshold = complexityProps.getMinReposThreshold();
        int maxThreshold = complexityProps.getMaxReposThreshold();

        if (repositoryCount <= minThreshold) {
            return ((double) repositoryCount / minThreshold) * 50;
        } else if (repositoryCount >= maxThreshold) {
            return 100;
        } else {
            double range = maxThreshold - minThreshold;
            double position = repositoryCount - minThreshold;
            return 50 + (position / range) * 50;
        }
    }
}

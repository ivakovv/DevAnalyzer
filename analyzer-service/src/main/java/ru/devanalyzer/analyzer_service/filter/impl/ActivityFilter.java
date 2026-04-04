package ru.devanalyzer.analyzer_service.filter.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;
import ru.devanalyzer.analyzer_service.filter.RepositoryFilter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;


@Component
public class ActivityFilter implements RepositoryFilter {
    
    @Value("${analyzer.filter.max-inactivity-years}")
    private int maxInactivityYears;
    
    @Override
    public boolean test(GitHubRepository repository) {
        if (repository.pushedAt() == null) {
            return false;
        }
        
        Instant threshold = Instant.now().minus(maxInactivityYears * 365L, ChronoUnit.DAYS);
        return repository.pushedAt().isAfter(threshold);
    }
    
    @Override
    public String getRejectionReason() {
        return "Repository is inactive (last push > " + maxInactivityYears + " years ago)";
    }
}

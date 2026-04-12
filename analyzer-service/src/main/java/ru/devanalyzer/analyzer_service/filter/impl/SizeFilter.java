package ru.devanalyzer.analyzer_service.filter.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;
import ru.devanalyzer.analyzer_service.filter.RepositoryFilter;


@Component
public class SizeFilter implements RepositoryFilter {
    
    @Value("${analyzer.filter.min-size-kb}")
    private int minSizeKb;
    
    @Value("${analyzer.filter.max-size-mb}")
    private int maxSizeMb;
    
    @Override
    public boolean test(GitHubRepository repository) {
        int sizeKb = repository.size();
        int maxSizeKb = maxSizeMb * 1024;
        return sizeKb >= minSizeKb && sizeKb <= maxSizeKb;
    }
    
    @Override
    public String getRejectionReason() {
        return "Repository size is out of range (must be between " + minSizeKb + " KB and " + maxSizeMb + " MB)";
    }
}

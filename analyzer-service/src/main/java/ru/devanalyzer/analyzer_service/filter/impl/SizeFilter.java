package ru.devanalyzer.analyzer_service.filter.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;
import ru.devanalyzer.analyzer_service.filter.RepositoryFilter;


@Component
public class SizeFilter implements RepositoryFilter {
    
    @Value("${analyzer.filter.min-size-kb}")
    private int minSizeKb;
    
    @Override
    public boolean test(GitHubRepository repository) {
        return repository.size() >= minSizeKb;
    }
    
    @Override
    public String getRejectionReason() {
        return "Repository size is too small (< " + minSizeKb + " KB)";
    }
}

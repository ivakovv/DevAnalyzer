package ru.devanalyzer.analyzer_service.filter.impl;

import org.springframework.stereotype.Component;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;
import ru.devanalyzer.analyzer_service.filter.RepositoryFilter;

/**
 * Фильтрует форки без значительных изменений
 */
@Component
public class ForkFilter implements RepositoryFilter {
    
    @Override
    public boolean test(GitHubRepository repository) {
        if (!repository.isFork()) {
            return true;
        }
        return false;
    }
    

    @Override
    public String getRejectionReason() {
        return "Repository is a fork";
    }
}

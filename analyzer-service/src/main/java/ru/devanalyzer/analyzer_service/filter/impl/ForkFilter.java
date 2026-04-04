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
        
        // TODO: Добавить проверку % изменений через GitHub API
        return false;
    }
    

    @Override
    public String getRejectionReason() {
        return "Repository is a fork without significant changes";
    }
}

package ru.devanalyzer.analyzer_service.filter.impl;

import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;
import ru.devanalyzer.analyzer_service.filter.RepositoryFilter;

import java.util.List;

/**
 * Фильтрует репозитории по языку программирования.
 * Пропускает репозитории, если хотя бы один из их языков есть в требуемом стеке
 */
public class LanguageFilter implements RepositoryFilter {
    
    private final List<String> requiredTechStack;
    
    public LanguageFilter(List<String> requiredTechStack) {
        this.requiredTechStack = requiredTechStack;
    }
    
    @Override
    public boolean test(GitHubRepository repository) {
        if (requiredTechStack == null || requiredTechStack.isEmpty()) {
            return true;
        }
        
        List<String> repoLanguages = repository.languages();
        if (repoLanguages == null || repoLanguages.isEmpty()) {
            return true;
        }
        
        return repoLanguages.stream()
                .anyMatch(repoLang -> requiredTechStack.stream()
                        .anyMatch(tech -> tech.equalsIgnoreCase(repoLang)));
    }
    
    @Override
    public String getRejectionReason() {
        return "Repository languages not in required tech stack: " + requiredTechStack;
    }
}

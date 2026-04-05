package ru.devanalyzer.analyzer_service.filter.impl;

import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;
import ru.devanalyzer.analyzer_service.filter.RepositoryFilter;

import java.util.List;

/**
 * Фильтрует репозитории по языку программирования.
 * Пропускает репозитории, если хотя бы один из их языков есть в требуемом стеке
 */
public class LanguageFilter implements RepositoryFilter {
    
    private final List<String> requiredLanguages;
    
    public LanguageFilter(List<String> requiredLanguages) {
        this.requiredLanguages = requiredLanguages;
    }
    
    @Override
    public boolean test(GitHubRepository repository) {
        List<String> repoLanguages = repository.languages();

        if (requiredLanguages != null && !requiredLanguages.isEmpty()) {
            if (repoLanguages == null || repoLanguages.isEmpty()) {
                return false;
            }
            return repoLanguages.stream()
                    .anyMatch(repoLang -> requiredLanguages.stream()
                            .anyMatch(reqLang -> reqLang.equalsIgnoreCase(repoLang)));
        }
        
        return repoLanguages != null && !repoLanguages.isEmpty();
    }
    
    @Override
    public String getRejectionReason() {
        if (requiredLanguages != null && !requiredLanguages.isEmpty()) {
            return "Repository languages not in required languages: " + requiredLanguages;
        }
        return "Repository has no detected languages";
    }
}

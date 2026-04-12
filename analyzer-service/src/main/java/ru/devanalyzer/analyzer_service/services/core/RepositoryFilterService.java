package ru.devanalyzer.analyzer_service.services.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.devanalyzer.analyzer_service.dto.FilterResult;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;
import ru.devanalyzer.analyzer_service.filter.RepositoryFilter;
import ru.devanalyzer.analyzer_service.filter.impl.LanguageFilter;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryFilterService {
    
    private final List<RepositoryFilter> filters;


    public List<GitHubRepository> filterRepositories(List<GitHubRepository> repositories, List<String> languages) {
        log.info("Filtering {} repositories with languages: {}", repositories.size(), languages);
        
        List<RepositoryFilter> allFilters = new ArrayList<>(filters);
        allFilters.add(new LanguageFilter(languages));
        
        List<GitHubRepository> filtered = repositories.stream()
                .map(repo -> applyFilters(repo, allFilters))
                .filter(FilterResult::passed)
                .map(FilterResult::repository)
                .toList();
        
        log.info("Filtered: {} passed, {} rejected", 
                filtered.size(), 
                repositories.size() - filtered.size());
        
        return filtered;
    }

    private FilterResult applyFilters(GitHubRepository repository, List<RepositoryFilter> filtersToApply) {
        for (RepositoryFilter filter : filtersToApply) {
            if (!filter.test(repository)) {
                log.debug("Repository '{}' rejected by {}: {}", 
                        repository.name(), 
                        filter.getClass().getSimpleName(),
                        filter.getRejectionReason());
                return FilterResult.rejected(repository, filter.getRejectionReason());
            }
        }
        
        return FilterResult.passed(repository);
    }
}

package ru.devanalyzer.analyzer_service.filter;

import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;

/**
 * Определяет контракт для фильтрации репозиториев
 */
public interface RepositoryFilter {
    
    /**
     * Проверяет, проходит ли репозиторий фильтр
     * 
     * @param repository репозиторий для проверки
     * @return true если репозиторий проходит фильтр
     */
    boolean test(GitHubRepository repository);
    
    /**
     * Возвращает причину отклонения репозитория
     * 
     * @return описание причины фильтрации
     */
    String getRejectionReason();
}

package ru.devanalyzer.analyzer_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.devanalyzer.analyzer_service.filter.RepositoryFilter;
import ru.devanalyzer.analyzer_service.filter.impl.ActivityFilter;
import ru.devanalyzer.analyzer_service.filter.impl.ForkFilter;
import ru.devanalyzer.analyzer_service.filter.impl.SizeFilter;

import java.util.List;

@Configuration
public class FilterConfig {

    @Bean
    public List<RepositoryFilter> repositoryFilters() {
        return List.of(
                new ForkFilter(),
                new SizeFilter(),
                new ActivityFilter()
        );
    }
}

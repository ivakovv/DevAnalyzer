package ru.devanalyzer.analyzer_service.services.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.devanalyzer.analyzer_service.clients.GitHubClient;
import ru.devanalyzer.analyzer_service.dto.github.GitHubRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitHubRepositoryServiceTest {

    @Mock
    private GitHubClient gitHubClient;

    @InjectMocks
    private GitHubRepositoryService service;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "repositoriesQuery", "query { user { repositories { ... } } }");
        objectMapper = new ObjectMapper();
    }

    @Test
    void getUserRepositories_shouldReturnRepositories_whenSuccessful() throws Exception {
        String jsonResponse = """
                {
                    "data": {
                        "user": {
                            "repositories": {
                                "nodes": [
                                    {
                                        "name": "repo1",
                                        "nameWithOwner": "testuser/repo1",
                                        "isFork": false,
                                        "diskUsage": 1024,
                                        "stargazerCount": 10,
                                        "forkCount": 5,
                                        "primaryLanguage": {"name": "Java"},
                                        "languages": {
                                            "edges": [
                                                {"node": {"name": "Java"}},
                                                {"node": {"name": "Kotlin"}}
                                            ]
                                        },
                                        "pushedAt": "2024-01-15T10:00:00Z",
                                        "createdAt": "2023-01-01T00:00:00Z",
                                        "description": "Test repo 1",
                                        "hasIssuesEnabled": true,
                                        "issues": {"totalCount": 3},
                                        "defaultBranchRef": {"name": "main"},
                                        "owner": {"login": "testuser"},
                                        "repositoryTopics": {
                                            "nodes": [
                                                {"topic": {"name": "java"}},
                                                {"topic": {"name": "spring-boot"}}
                                            ]
                                        }
                                    },
                                    {
                                        "name": "repo2",
                                        "nameWithOwner": "testuser/repo2",
                                        "isFork": true,
                                        "diskUsage": 2048,
                                        "stargazerCount": 20,
                                        "forkCount": 10,
                                        "primaryLanguage": {"name": "Python"},
                                        "languages": {
                                            "edges": [
                                                {"node": {"name": "Python"}}
                                            ]
                                        },
                                        "pushedAt": "2024-01-10T10:00:00Z",
                                        "createdAt": "2023-06-01T00:00:00Z",
                                        "description": null,
                                        "hasIssuesEnabled": false,
                                        "issues": {"totalCount": 0},
                                        "defaultBranchRef": {"name": "master"},
                                        "owner": {"login": "testuser"},
                                        "repositoryTopics": {"nodes": []}
                                    }
                                ],
                                "pageInfo": {
                                    "hasNextPage": false,
                                    "endCursor": null
                                }
                            }
                        }
                    }
                }
                """;

        JsonNode responseNode = objectMapper.readTree(jsonResponse);
        when(gitHubClient.executeGraphQLQuery(anyString(), anyMap()))
                .thenReturn(responseNode);

        List<GitHubRepository> result = service.getUserRepositories("testuser");

        assertThat(result).hasSize(2);

        GitHubRepository repo1 = result.getFirst();
        assertThat(repo1.name()).isEqualTo("repo1");
        assertThat(repo1.fullName()).isEqualTo("testuser/repo1");
        assertThat(repo1.cloneUrl()).isEqualTo("https://github.com/testuser/repo1.git");
        assertThat(repo1.isFork()).isFalse();
        assertThat(repo1.size()).isEqualTo(1024);
        assertThat(repo1.stargazersCount()).isEqualTo(10);
        assertThat(repo1.forksCount()).isEqualTo(5);
        assertThat(repo1.language()).isEqualTo("Java");
        assertThat(repo1.languages()).containsExactly("Java", "Kotlin");
        assertThat(repo1.description()).isEqualTo("Test repo 1");
        assertThat(repo1.hasIssues()).isTrue();
        assertThat(repo1.openIssuesCount()).isEqualTo(3);
        assertThat(repo1.defaultBranch()).isEqualTo("main");
        assertThat(repo1.owner().login()).isEqualTo("testuser");
        assertThat(repo1.topics()).containsExactly("java", "spring-boot");

        GitHubRepository repo2 = result.get(1);
        assertThat(repo2.name()).isEqualTo("repo2");
        assertThat(repo2.isFork()).isTrue();
        assertThat(repo2.description()).isNull();
        assertThat(repo2.defaultBranch()).isEqualTo("master");
        assertThat(repo2.topics()).isEmpty();
    }

    @Test
    void getUserRepositories_shouldHandlePagination() throws Exception {
        String firstPage = """
                {
                    "data": {
                        "user": {
                            "repositories": {
                                "nodes": [
                                    {
                                        "name": "repo1",
                                        "nameWithOwner": "testuser/repo1",
                                        "isFork": false,
                                        "diskUsage": 1024,
                                        "stargazerCount": 10,
                                        "forkCount": 5,
                                        "primaryLanguage": null,
                                        "languages": {"edges": []},
                                        "pushedAt": null,
                                        "createdAt": null,
                                        "description": null,
                                        "hasIssuesEnabled": true,
                                        "issues": {"totalCount": 0},
                                        "defaultBranchRef": null,
                                        "owner": {"login": "testuser"},
                                        "repositoryTopics": {"nodes": []}
                                    }
                                ],
                                "pageInfo": {
                                    "hasNextPage": true,
                                    "endCursor": "cursor123"
                                }
                            }
                        }
                    }
                }
                """;

        String secondPage = """
                {
                    "data": {
                        "user": {
                            "repositories": {
                                "nodes": [
                                    {
                                        "name": "repo2",
                                        "nameWithOwner": "testuser/repo2",
                                        "isFork": false,
                                        "diskUsage": 2048,
                                        "stargazerCount": 20,
                                        "forkCount": 10,
                                        "primaryLanguage": null,
                                        "languages": {"edges": []},
                                        "pushedAt": null,
                                        "createdAt": null,
                                        "description": null,
                                        "hasIssuesEnabled": true,
                                        "issues": {"totalCount": 0},
                                        "defaultBranchRef": null,
                                        "owner": {"login": "testuser"},
                                        "repositoryTopics": {"nodes": []}
                                    }
                                ],
                                "pageInfo": {
                                    "hasNextPage": false,
                                    "endCursor": null
                                }
                            }
                        }
                    }
                }
                """;

        JsonNode firstPageNode = objectMapper.readTree(firstPage);
        JsonNode secondPageNode = objectMapper.readTree(secondPage);

        when(gitHubClient.executeGraphQLQuery(anyString(), anyMap()))
                .thenReturn(firstPageNode)
                .thenReturn(secondPageNode);

        List<GitHubRepository> result = service.getUserRepositories("testuser");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(GitHubRepository::name)
                .containsExactly("repo1", "repo2");
    }

    @Test
    void getUserRepositories_shouldReturnEmptyList_whenNoRepositories() throws Exception {
        String jsonResponse = """
                {
                    "data": {
                        "user": {
                            "repositories": {
                                "nodes": [],
                                "pageInfo": {
                                    "hasNextPage": false,
                                    "endCursor": null
                                }
                            }
                        }
                    }
                }
                """;

        JsonNode responseNode = objectMapper.readTree(jsonResponse);
        when(gitHubClient.executeGraphQLQuery(anyString(), anyMap()))
                .thenReturn(responseNode);

        List<GitHubRepository> result = service.getUserRepositories("testuser");

        assertThat(result).isEmpty();
    }

    @Test
    void getUserRepositories_shouldThrowException_whenApiCallFails() {
        when(gitHubClient.executeGraphQLQuery(anyString(), anyMap()))
                .thenThrow(new RuntimeException("API error"));

        assertThatThrownBy(() -> service.getUserRepositories("testuser"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch repositories");
    }

    @Test
    void getUserRepositories_shouldHandleMissingFields() throws Exception {
        String jsonResponse = """
                {
                    "data": {
                        "user": {
                            "repositories": {
                                "nodes": [
                                    {
                                        "name": "minimal-repo",
                                        "nameWithOwner": "testuser/minimal-repo",
                                        "isFork": false,
                                        "diskUsage": 0,
                                        "stargazerCount": 0,
                                        "forkCount": 0,
                                        "primaryLanguage": null,
                                        "languages": null,
                                        "pushedAt": null,
                                        "createdAt": null,
                                        "description": null,
                                        "hasIssuesEnabled": false,
                                        "issues": null,
                                        "defaultBranchRef": null,
                                        "owner": null,
                                        "repositoryTopics": null
                                    }
                                ],
                                "pageInfo": {
                                    "hasNextPage": false,
                                    "endCursor": null
                                }
                            }
                        }
                    }
                }
                """;

        JsonNode responseNode = objectMapper.readTree(jsonResponse);
        when(gitHubClient.executeGraphQLQuery(anyString(), anyMap()))
                .thenReturn(responseNode);

        List<GitHubRepository> result = service.getUserRepositories("testuser");

        assertThat(result).hasSize(1);
        GitHubRepository repo = result.getFirst();
        assertThat(repo.name()).isEqualTo("minimal-repo");
        assertThat(repo.cloneUrl()).isEqualTo("https://github.com/testuser/minimal-repo.git");
        assertThat(repo.language()).isNull();
        assertThat(repo.languages()).isEmpty();
        assertThat(repo.defaultBranch()).isEqualTo("main");
        assertThat(repo.owner()).isNull();
        assertThat(repo.topics()).isEmpty();
    }

    @Test
    void getUserRepositories_shouldExtractTotalCommits() throws Exception {
        String jsonResponse = """
                {
                    "data": {
                        "user": {
                            "repositories": {
                                "nodes": [
                                    {
                                        "name": "repo-with-commits",
                                        "nameWithOwner": "testuser/repo-with-commits",
                                        "isFork": false,
                                        "diskUsage": 1024,
                                        "stargazerCount": 10,
                                        "forkCount": 5,
                                        "primaryLanguage": null,
                                        "languages": {"edges": []},
                                        "pushedAt": null,
                                        "createdAt": null,
                                        "description": null,
                                        "hasIssuesEnabled": true,
                                        "issues": {"totalCount": 0},
                                        "defaultBranchRef": {
                                            "name": "main",
                                            "target": {
                                                "history": {
                                                    "totalCount": 42
                                                }
                                            }
                                        },
                                        "owner": {"login": "testuser"},
                                        "repositoryTopics": {"nodes": []}
                                    }
                                ],
                                "pageInfo": {
                                    "hasNextPage": false,
                                    "endCursor": null
                                }
                            }
                        }
                    }
                }
                """;

        JsonNode responseNode = objectMapper.readTree(jsonResponse);
        when(gitHubClient.executeGraphQLQuery(anyString(), anyMap()))
                .thenReturn(responseNode);

        List<GitHubRepository> result = service.getUserRepositories("testuser");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().totalCommits()).isEqualTo(42);
    }
}

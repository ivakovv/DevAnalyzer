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

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorshipVerificationServiceTest {

    @Mock
    private GitHubClient gitHubClient;

    @InjectMocks
    private AuthorshipVerificationService service;

    private GitHubRepository repository;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "minOwnershipPercentage", 50.0);
        ReflectionTestUtils.setField(service, "authorshipQuery", "query { repository { ... } }");

        repository = new GitHubRepository(
                "test-repo",
                "testuser/test-repo",
                "https://github.com/testuser/test-repo.git",
                new GitHubRepository.Owner("testuser"),
                false,
                1024,
                10,
                5,
                "Java",
                List.of("Java"),
                Instant.now(),
                Instant.now().minus(365, java.time.temporal.ChronoUnit.DAYS),
                "Test repo",
                true,
                3,
                "main",
                100,
                List.of()
        );

        objectMapper = new ObjectMapper();
    }

    @Test
    void verifyOwnership_shouldReturnTrue_whenOwnerHasMajorityOfCommits() throws Exception {

        String jsonResponse = """
                {
                    "data": {
                        "repository": {
                            "defaultBranchRef": {
                                "target": {
                                    "history": {
                                        "totalCount": 10,
                                        "nodes": [
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}}
                                        ],
                                        "pageInfo": {
                                            "hasNextPage": false,
                                            "endCursor": null
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                """;

        JsonNode responseNode = objectMapper.readTree(jsonResponse);
        when(gitHubClient.executeGraphQLQuery(anyString(), anyMap()))
                .thenReturn(responseNode);

        boolean result = service.verifyOwnership(repository, "testuser");

        assertThat(result).isTrue();
    }

    @Test
    void verifyOwnership_shouldReturnFalse_whenOwnerHasMinorityOfCommits() throws Exception {

        String jsonResponse = """
                {
                    "data": {
                        "repository": {
                            "defaultBranchRef": {
                                "target": {
                                    "history": {
                                        "totalCount": 10,
                                        "nodes": [
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}}
                                        ],
                                        "pageInfo": {
                                            "hasNextPage": false,
                                            "endCursor": null
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                """;

        JsonNode responseNode = objectMapper.readTree(jsonResponse);
        when(gitHubClient.executeGraphQLQuery(anyString(), anyMap()))
                .thenReturn(responseNode);

        boolean result = service.verifyOwnership(repository, "testuser");

        assertThat(result).isFalse();
    }

    @Test
    void verifyOwnership_shouldReturnTrue_whenRepositoryHasNoCommits() throws Exception {
        String jsonResponse = """
                {
                    "data": {
                        "repository": {
                            "defaultBranchRef": {
                                "target": {
                                    "history": {
                                        "totalCount": 0,
                                        "nodes": [],
                                        "pageInfo": {
                                            "hasNextPage": false,
                                            "endCursor": null
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                """;

        JsonNode responseNode = objectMapper.readTree(jsonResponse);
        when(gitHubClient.executeGraphQLQuery(anyString(), anyMap()))
                .thenReturn(responseNode);

        boolean result = service.verifyOwnership(repository, "testuser");

        assertThat(result).isTrue();
    }

    @Test
    void verifyOwnership_shouldReturnTrue_whenApiCallFails() {
        when(gitHubClient.executeGraphQLQuery(anyString(), anyMap()))
                .thenThrow(new RuntimeException("API error"));

        boolean result = service.verifyOwnership(repository, "testuser");

        assertThat(result).isTrue();
    }

    @Test
    void verifyOwnership_shouldBeCaseInsensitive() throws Exception {

        String jsonResponse = """
                {
                    "data": {
                        "repository": {
                            "defaultBranchRef": {
                                "target": {
                                    "history": {
                                        "totalCount": 10,
                                        "nodes": [
                                            {"author": {"user": {"login": "TestUser"}}},
                                            {"author": {"user": {"login": "TESTUSER"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "TestUser"}}},
                                            {"author": {"user": {"login": "TESTUSER"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "TestUser"}}},
                                            {"author": {"user": {"login": "TESTUSER"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}}
                                        ],
                                        "pageInfo": {
                                            "hasNextPage": false,
                                            "endCursor": null
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                """;

        JsonNode responseNode = objectMapper.readTree(jsonResponse);
        when(gitHubClient.executeGraphQLQuery(anyString(), anyMap()))
                .thenReturn(responseNode);

        boolean result = service.verifyOwnership(repository, "testuser");

        assertThat(result).isTrue();
    }

    @Test
    void verifyOwnership_shouldHandlePagination() throws Exception {

        String firstPage = """
                {
                    "data": {
                        "repository": {
                            "defaultBranchRef": {
                                "target": {
                                    "history": {
                                        "totalCount": 10,
                                        "nodes": [
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "otheruser"}}}
                                        ],
                                        "pageInfo": {
                                            "hasNextPage": true,
                                            "endCursor": "cursor123"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                """;


        String secondPage = """
                {
                    "data": {
                        "repository": {
                            "defaultBranchRef": {
                                "target": {
                                    "history": {
                                        "totalCount": 10,
                                        "nodes": [
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}}
                                        ],
                                        "pageInfo": {
                                            "hasNextPage": false,
                                            "endCursor": null
                                        }
                                    }
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

        boolean result = service.verifyOwnership(repository, "testuser");

        // 4 (первая) + 2 (вторая) = 6 коммитов владельца из 10 = 60% > 50%
        assertThat(result).isTrue();
    }

    @Test
    void verifyOwnership_shouldHandleNullAuthor() throws Exception {

        String jsonResponse = """
                {
                    "data": {
                        "repository": {
                            "defaultBranchRef": {
                                "target": {
                                    "history": {
                                        "totalCount": 10,
                                        "nodes": [
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": null}},
                                            {"author": null},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}}
                                        ],
                                        "pageInfo": {
                                            "hasNextPage": false,
                                            "endCursor": null
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                """;

        JsonNode responseNode = objectMapper.readTree(jsonResponse);
        when(gitHubClient.executeGraphQLQuery(anyString(), anyMap()))
                .thenReturn(responseNode);

        boolean result = service.verifyOwnership(repository, "testuser");

        assertThat(result).isTrue();
    }

    @Test
    void verifyOwnership_shouldReturnFalse_whenExactlyAtThreshold() throws Exception {

        ReflectionTestUtils.setField(service, "minOwnershipPercentage", 50.0);

        String jsonResponse = """
                {
                    "data": {
                        "repository": {
                            "defaultBranchRef": {
                                "target": {
                                    "history": {
                                        "totalCount": 10,
                                        "nodes": [
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}}
                                        ],
                                        "pageInfo": {
                                            "hasNextPage": false,
                                            "endCursor": null
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                """;

        JsonNode responseNode = objectMapper.readTree(jsonResponse);
        when(gitHubClient.executeGraphQLQuery(anyString(), anyMap()))
                .thenReturn(responseNode);

        boolean result = service.verifyOwnership(repository, "testuser");

        assertThat(result).isTrue();
    }

    @Test
    void verifyOwnership_shouldReturnFalse_whenBelowThreshold() throws Exception {

        ReflectionTestUtils.setField(service, "minOwnershipPercentage", 50.0);

        String jsonResponse = """
                {
                    "data": {
                        "repository": {
                            "defaultBranchRef": {
                                "target": {
                                    "history": {
                                        "totalCount": 10,
                                        "nodes": [
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "testuser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}},
                                            {"author": {"user": {"login": "otheruser"}}}
                                        ],
                                        "pageInfo": {
                                            "hasNextPage": false,
                                            "endCursor": null
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                """;

        JsonNode responseNode = objectMapper.readTree(jsonResponse);
        when(gitHubClient.executeGraphQLQuery(anyString(), anyMap()))
                .thenReturn(responseNode);

        boolean result = service.verifyOwnership(repository, "testuser");

        assertThat(result).isFalse();
    }
}

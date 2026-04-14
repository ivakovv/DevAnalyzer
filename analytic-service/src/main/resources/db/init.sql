CREATE TABLE IF NOT EXISTS devanalyzer.analysis_results
(
    request_id                   String,
    user_id                      Int64,
    github_username              String,
    total_repositories           Int32,
    filtered_repositories        Int32,
    verified_repositories        Int32,
    successful_scans             Int64,
    failed_scans                 Int64,
    overall_score                Int32,
    total_bugs                   Int32,
    total_vulnerabilities        Int32,
    total_code_smells            Int32,
    average_coverage             Float64,
    passed_quality_gate          Int32,
    failed_quality_gate          Int32,
    median_security_rating       Nullable(String),
    median_reliability_rating    Nullable(String),
    median_maintainability_rating Nullable(String),
    repositories_json            String,
    tech_stack_json              String,
    github_stats_json            String,
    github_repo_json             String,
    created_at                   DateTime
)
ENGINE = MergeTree()
ORDER BY (created_at, user_id, github_username);

CREATE TABLE optimization_result
(
    planning_problem_id   VARCHAR(255) PRIMARY KEY,
    fitness               DOUBLE PRECISION NOT NULL,
    feasible              BOOLEAN          NOT NULL,
    hard_penalty          DOUBLE PRECISION NOT NULL,
    soft_penalty          DOUBLE PRECISION NOT NULL,
    generations_executed  BIGINT           NOT NULL,
    execution_time_millis BIGINT           NOT NULL,
    random_seed           BIGINT           NOT NULL,
    assignments           JSONB            NOT NULL,
    constraint_results    JSONB            NOT NULL,

    CONSTRAINT fk_optimization_result_planning
        FOREIGN KEY (planning_problem_id)
            REFERENCES planning_problem (id)
            ON DELETE CASCADE
);
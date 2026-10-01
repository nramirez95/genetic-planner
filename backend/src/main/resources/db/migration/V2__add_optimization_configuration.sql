CREATE TABLE optimization_configuration
(
    planning_problem_id   VARCHAR(255) PRIMARY KEY,
    population_size       INTEGER          NOT NULL,
    generation_limit      INTEGER          NOT NULL,
    mutation_probability  DOUBLE PRECISION NOT NULL,
    crossover_probability DOUBLE PRECISION NOT NULL,
    elite_count           INTEGER          NOT NULL,
    random_seed           BIGINT,

    CONSTRAINT fk_optimization_configuration_planning_problem
        FOREIGN KEY (planning_problem_id)
            REFERENCES planning_problem (id)
            ON DELETE CASCADE
);
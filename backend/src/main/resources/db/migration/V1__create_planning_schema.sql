CREATE TABLE planning_problem
(
    id                     VARCHAR(255) PRIMARY KEY,
    name                   VARCHAR(255) NOT NULL,
    template_type          VARCHAR(100),
    planning_horizon_start TIMESTAMP    NOT NULL,
    planning_horizon_end   TIMESTAMP    NOT NULL
);

CREATE TABLE resource_type
(
    id                  VARCHAR(255) PRIMARY KEY,
    planning_problem_id VARCHAR(255) NOT NULL,
    name                VARCHAR(255) NOT NULL,

    CONSTRAINT fk_resource_type_planning_problem
        FOREIGN KEY (planning_problem_id)
            REFERENCES planning_problem (id)
            ON DELETE CASCADE
);

CREATE TABLE resource
(
    id                  VARCHAR(255) PRIMARY KEY,
    planning_problem_id VARCHAR(255) NOT NULL,
    type_id             VARCHAR(255) NOT NULL,
    name                VARCHAR(255) NOT NULL,
    attributes          JSONB        NOT NULL DEFAULT '{}'::jsonb,

    CONSTRAINT fk_resource_planning_problem
        FOREIGN KEY (planning_problem_id)
            REFERENCES planning_problem (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_resource_type
        FOREIGN KEY (type_id)
            REFERENCES resource_type (id)
);

CREATE TABLE time_slot
(
    id                  VARCHAR(255) PRIMARY KEY,
    planning_problem_id VARCHAR(255) NOT NULL,
    start_time          TIMESTAMP    NOT NULL,
    end_time            TIMESTAMP    NOT NULL,
    label               VARCHAR(255),

    CONSTRAINT fk_time_slot_planning_problem
        FOREIGN KEY (planning_problem_id)
            REFERENCES planning_problem (id)
            ON DELETE CASCADE
);

CREATE TABLE location
(
    id                  VARCHAR(255) PRIMARY KEY,
    planning_problem_id VARCHAR(255) NOT NULL,
    name                VARCHAR(255) NOT NULL,
    type                VARCHAR(100),
    capacity            INTEGER,
    attributes          JSONB        NOT NULL DEFAULT '{}'::jsonb,

    CONSTRAINT fk_location_planning_problem
        FOREIGN KEY (planning_problem_id)
            REFERENCES planning_problem (id)
            ON DELETE CASCADE
);

CREATE TABLE activity
(
    id                         VARCHAR(255) PRIMARY KEY,
    planning_problem_id        VARCHAR(255) NOT NULL,
    name                       VARCHAR(255) NOT NULL,
    type                       VARCHAR(100),
    time_slots_restricted      BOOLEAN      NOT NULL DEFAULT FALSE,
    locations_restricted       BOOLEAN      NOT NULL DEFAULT FALSE,
    required_location_capacity INTEGER,
    attributes                 JSONB        NOT NULL DEFAULT '{}'::jsonb,

    CONSTRAINT fk_activity_planning_problem
        FOREIGN KEY (planning_problem_id)
            REFERENCES planning_problem (id)
            ON DELETE CASCADE
);

CREATE TABLE activity_allowed_time_slot
(
    activity_id  VARCHAR(255) NOT NULL,
    time_slot_id VARCHAR(255) NOT NULL,

    PRIMARY KEY (activity_id, time_slot_id),

    CONSTRAINT fk_activity_allowed_time_slot_activity
        FOREIGN KEY (activity_id)
            REFERENCES activity (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_activity_allowed_time_slot_time_slot
        FOREIGN KEY (time_slot_id)
            REFERENCES time_slot (id)
            ON DELETE CASCADE
);

CREATE TABLE activity_allowed_location
(
    activity_id VARCHAR(255) NOT NULL,
    location_id VARCHAR(255) NOT NULL,

    PRIMARY KEY (activity_id, location_id),

    CONSTRAINT fk_activity_allowed_location_activity
        FOREIGN KEY (activity_id)
            REFERENCES activity (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_activity_allowed_location_location
        FOREIGN KEY (location_id)
            REFERENCES location (id)
            ON DELETE CASCADE
);

CREATE TABLE resource_requirement
(
    id                             VARCHAR(255) PRIMARY KEY,
    activity_id                    VARCHAR(255) NOT NULL,
    resource_type_id               VARCHAR(255) NOT NULL,
    quantity                       INTEGER      NOT NULL,
    candidate_resources_restricted BOOLEAN      NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_resource_requirement_activity
        FOREIGN KEY (activity_id)
            REFERENCES activity (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_resource_requirement_resource_type
        FOREIGN KEY (resource_type_id)
            REFERENCES resource_type (id),

    CONSTRAINT chk_resource_requirement_quantity
        CHECK (quantity > 0)
);

CREATE TABLE requirement_candidate_resource
(
    requirement_id VARCHAR(255) NOT NULL,
    resource_id    VARCHAR(255) NOT NULL,

    PRIMARY KEY (requirement_id, resource_id),

    CONSTRAINT fk_requirement_candidate_requirement
        FOREIGN KEY (requirement_id)
            REFERENCES resource_requirement (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_requirement_candidate_resource
        FOREIGN KEY (resource_id)
            REFERENCES resource (id)
            ON DELETE CASCADE
);

CREATE TABLE planning_constraint
(
    id                  VARCHAR(255) PRIMARY KEY,
    planning_problem_id VARCHAR(255)     NOT NULL,
    constraint_key      VARCHAR(100)     NOT NULL,
    constraint_type     VARCHAR(20)      NOT NULL,
    name                VARCHAR(255)     NOT NULL,
    weight              DOUBLE PRECISION NOT NULL,
    configuration       JSONB            NOT NULL DEFAULT '{}'::jsonb,

    CONSTRAINT fk_constraint_planning_problem
        FOREIGN KEY (planning_problem_id)
            REFERENCES planning_problem (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_constraint_type
        CHECK (constraint_type IN ('HARD', 'SOFT')),

    CONSTRAINT chk_constraint_weight
        CHECK (weight >= 0)
);

CREATE INDEX idx_resource_planning_problem
    ON resource (planning_problem_id);

CREATE INDEX idx_time_slot_planning_problem
    ON time_slot (planning_problem_id);

CREATE INDEX idx_location_planning_problem
    ON location (planning_problem_id);

CREATE INDEX idx_activity_planning_problem
    ON activity (planning_problem_id);

CREATE INDEX idx_resource_requirement_activity
    ON resource_requirement (activity_id);

CREATE INDEX idx_constraint_planning_problem
    ON planning_constraint (planning_problem_id);
CREATE TABLE terminal (
    id              INTEGER PRIMARY KEY,
    name            VARCHAR(64),
    max_length      DOUBLE PRECISION NOT NULL,
    max_draft       DOUBLE PRECISION NOT NULL
);

CREATE TABLE terminal_allowed_cargo (
    terminal_id INTEGER REFERENCES terminal(id) ON DELETE CASCADE,
    cargo_type  VARCHAR(32) NOT NULL
);

CREATE TABLE ship (
    id               VARCHAR(64) PRIMARY KEY,
    arrival_time     TIMESTAMPTZ NOT NULL,
    length           DOUBLE PRECISION NOT NULL,
    draft            DOUBLE PRECISION NOT NULL,
    cargo_type       VARCHAR(32) NOT NULL,
    est_duration_hours DOUBLE PRECISION NOT NULL
);

CREATE TABLE terminal_closure (
    id          BIGSERIAL PRIMARY KEY,
    terminal_id INTEGER REFERENCES terminal(id),
    start_time  TIMESTAMPTZ NOT NULL,
    end_time    TIMESTAMPTZ NOT NULL,
    reason      VARCHAR(128)
);

CREATE TABLE weather_event (
    id          BIGSERIAL PRIMARY KEY,
    start_time  TIMESTAMPTZ NOT NULL,
    end_time    TIMESTAMPTZ NOT NULL,
    description VARCHAR(256)
);

CREATE TABLE pairwise_feedback (
    id              BIGSERIAL PRIMARY KEY,
    comparison_id   VARCHAR(64),
    timestamp       TIMESTAMPTZ,
    plan_a_algorithm VARCHAR(32),
    plan_b_algorithm VARCHAR(32),
    plan_a_waiting   DOUBLE PRECISION,
    plan_b_waiting   DOUBLE PRECISION,
    chosen_plan     CHAR(1)
);

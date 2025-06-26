CREATE TABLE terminal (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(64),
    max_length      DOUBLE PRECISION NOT NULL,
    max_draft       DOUBLE PRECISION NOT NULL
);

CREATE TABLE terminal_allowed_cargo (
    terminal_id BIGINT REFERENCES terminal(id) ON DELETE CASCADE,
    cargo_type  VARCHAR(32) NOT NULL
);

CREATE TABLE ships (
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR(64),
    arrival_time       TIMESTAMPTZ NOT NULL,
    length             DOUBLE PRECISION NOT NULL,
    draft              DOUBLE PRECISION NOT NULL,
    cargo_type         VARCHAR(32) NOT NULL,
    est_duration_hours DOUBLE PRECISION NOT NULL,
    priority           VARCHAR(16)
);

CREATE TABLE terminal_closure (
    id          BIGSERIAL PRIMARY KEY,
    terminal_id BIGINT REFERENCES terminal(id),
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
    id                BIGSERIAL PRIMARY KEY,
    comparison_id     VARCHAR(64),
    timestamp         TIMESTAMPTZ,
    plan_a_algorithm  VARCHAR(32),
    plan_b_algorithm  VARCHAR(32),
    plan_a_waiting    DOUBLE PRECISION,
    plan_b_waiting    DOUBLE PRECISION,
    chosen_plan       CHAR(1)
);

CREATE TABLE terminal_fuel_supported (
    terminal_id BIGINT REFERENCES terminal(id) ON DELETE CASCADE,
    fuel_type   VARCHAR(32) NOT NULL
);

ALTER TABLE ships
    ADD COLUMN deadweight               DOUBLE PRECISION,
    ADD COLUMN flag_country             VARCHAR(64),
    ADD COLUMN imo_number               VARCHAR(16),
    ADD COLUMN ship_type                VARCHAR(32),
    ADD COLUMN requires_customs_clearance BOOLEAN,
    ADD COLUMN hazard_class             VARCHAR(32),
    ADD COLUMN temperature_controlled   BOOLEAN,
    ADD COLUMN fuel_type                VARCHAR(32),
    ADD COLUMN emission_rating          VARCHAR(32),
    ADD COLUMN arrival_port             VARCHAR(32),
    ADD COLUMN next_port                VARCHAR(32),
    ADD COLUMN requires_pilot           BOOLEAN,
    ADD COLUMN arrival_window_start     TIMESTAMPTZ,
    ADD COLUMN arrival_window_end       TIMESTAMPTZ,
    ADD COLUMN expected_delay_hours     DOUBLE PRECISION;
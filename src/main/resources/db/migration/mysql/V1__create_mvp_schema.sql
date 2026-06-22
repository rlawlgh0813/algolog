CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE problems (
    id BIGINT NOT NULL AUTO_INCREMENT,
    platform VARCHAR(30) NOT NULL,
    problem_number VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    difficulty VARCHAR(50),
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_problems_platform_problem_number UNIQUE (platform, problem_number)
);

CREATE TABLE solution_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    author_id BIGINT NOT NULL,
    problem_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    solution_memo LONGTEXT,
    mistake_note LONGTEXT,
    solving_status VARCHAR(30) NOT NULL,
    review_needed BOOLEAN NOT NULL,
    visibility VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_solution_records_author FOREIGN KEY (author_id) REFERENCES users (id),
    CONSTRAINT fk_solution_records_problem FOREIGN KEY (problem_id) REFERENCES problems (id),
    CONSTRAINT ck_solution_records_solving_status CHECK (solving_status IN ('NOT_SOLVED', 'SOLVED', 'NEED_RETRY')),
    CONSTRAINT ck_solution_records_visibility CHECK (visibility IN ('PUBLIC', 'PRIVATE'))
);

CREATE INDEX idx_solution_records_author ON solution_records (author_id);
CREATE INDEX idx_solution_records_problem ON solution_records (problem_id);
CREATE INDEX idx_solution_records_visibility ON solution_records (visibility);

CREATE TABLE counter_examples (
    id BIGINT NOT NULL AUTO_INCREMENT,
    solution_record_id BIGINT NOT NULL,
    input_example TEXT NOT NULL,
    expected_behavior LONGTEXT,
    wrong_reason LONGTEXT,
    fix_memo LONGTEXT,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_counter_examples_solution_record FOREIGN KEY (solution_record_id) REFERENCES solution_records (id)
);

CREATE INDEX idx_counter_examples_solution_record ON counter_examples (solution_record_id);

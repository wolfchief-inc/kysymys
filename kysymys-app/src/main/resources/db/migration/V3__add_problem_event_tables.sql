CREATE TABLE problem_updated_events (
  id VARCHAR(21) NOT NULL,
  problem_lifecycle_id VARCHAR(21),
  occurred_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  updater_id VARCHAR(21) NOT NULL,
  CONSTRAINT pk_problem_updated_events PRIMARY KEY (id)
);

CREATE TABLE problem_archived_events (
  id VARCHAR(21) NOT NULL,
  problem_lifecycle_id VARCHAR(21),
  occurred_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  archiver_id VARCHAR(21) NOT NULL,
  CONSTRAINT pk_problem_archived_events PRIMARY KEY (id)
);

ALTER TABLE problem_updated_events ADD CONSTRAINT FK_PROBLEM_UPDATED_EVENTS_ON_PROBLEM_LIFECYCLE
  FOREIGN KEY (problem_lifecycle_id) REFERENCES problem_lifecycles (id);

ALTER TABLE problem_archived_events ADD CONSTRAINT FK_PROBLEM_ARCHIVED_EVENTS_ON_PROBLEM_LIFECYCLE
  FOREIGN KEY (problem_lifecycle_id) REFERENCES problem_lifecycles (id);

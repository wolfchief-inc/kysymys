CREATE TABLE activity_events (
  id VARCHAR(21) NOT NULL,
  participant_id VARCHAR(255) NOT NULL,
  problem_id VARCHAR(64),
  kind VARCHAR(20) NOT NULL,
  detail VARCHAR(2000),
  occurred_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  CONSTRAINT pk_activity_events PRIMARY KEY (id)
);

CREATE INDEX idx_activity_events_participant ON activity_events (participant_id, occurred_at);

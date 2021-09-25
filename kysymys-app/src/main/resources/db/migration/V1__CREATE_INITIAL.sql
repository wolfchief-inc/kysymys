CREATE TABLE answers (
  id VARCHAR(21) NOT NULL,
  problem_id VARCHAR(21) NOT NULL,
  answerer_id VARCHAR(255) NOT NULL,
  repository_url VARCHAR(255),
  CONSTRAINT pk_answers PRIMARY KEY (id)
);

CREATE TABLE connections (
  followee_id VARCHAR(255) NOT NULL,
  follower_id VARCHAR(255) NOT NULL,
  CONSTRAINT pk_connections PRIMARY KEY (followee_id, follower_id)
);

CREATE TABLE offers (
  id VARCHAR(255) NOT NULL,
  offering_user_id VARCHAR(255),
  target_user_id VARCHAR(255),
  offered_at TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT pk_offers PRIMARY KEY (id)
);

CREATE TABLE problem_created_events (
  id VARCHAR(255) NOT NULL,
  problem_lifecycle_id VARCHAR(255),
  occurred_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  creator_id VARCHAR(255) NOT NULL,
  CONSTRAINT pk_problem_created_events PRIMARY KEY (id)
);

CREATE TABLE problem_lifecycles (
  id VARCHAR(255) NOT NULL,
  problem_id VARCHAR(21) NOT NULL,
  status INTEGER NOT NULL,
  CONSTRAINT pk_problem_lifecycles PRIMARY KEY (id)
);

CREATE TABLE problems (
  id VARCHAR(21) NOT NULL,
  name VARCHAR(255) NOT NULL,
  repository_url VARCHAR(255) NOT NULL,
  branch VARCHAR(255) NOT NULL,
  readme_path VARCHAR(255),
  runner VARCHAR(255),
  problem_lifecycle_id VARCHAR(255),
  CONSTRAINT pk_problems PRIMARY KEY (id)
);

CREATE TABLE review_comments (
  id VARCHAR(255) NOT NULL,
  answer_id VARCHAR(21) NOT NULL,
  commenter_id VARCHAR(255) NOT NULL,
  description VARCHAR(255) NOT NULL,
  posted_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  CONSTRAINT pk_review_comments PRIMARY KEY (id)
);

CREATE TABLE submissions (
  id VARCHAR(255) NOT NULL,
  answer_id VARCHAR(21),
  commit_hash VARCHAR(255),
  submitted_at TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT pk_submissions PRIMARY KEY (id)
);

CREATE TABLE latest_submissions(
  answer_id VARCHAR(21),
  submission_id VARCHAR(255),
  CONSTRAINT pk_latest_submissions PRIMARY KEY(answer_id, submission_id)
);

CREATE TABLE user_avatars (
  user_id VARCHAR(255) NOT NULL,
  avatar_code BIGINT,
  image_content OID,
  CONSTRAINT pk_user_avatars PRIMARY KEY (user_id)
);

CREATE TABLE user_roles (user_id VARCHAR(255) NOT NULL, roles VARCHAR(255));

CREATE TABLE users (
  id VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  password VARCHAR(255),
  CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE TABLE whats_news (
  id VARCHAR(255) NOT NULL,
  user_id VARCHAR(255) NOT NULL,
  template_path VARCHAR(255) NOT NULL,
  params TEXT,
  posted_at TIMESTAMP WITHOUT TIME ZONE,
  CONSTRAINT pk_whats_news PRIMARY KEY (id)
);

ALTER TABLE users ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE answers ADD CONSTRAINT FK_ANSWERS_ON_PROBLEM FOREIGN KEY (problem_id) REFERENCES problems (id);

ALTER TABLE offers ADD CONSTRAINT FK_OFFERS_ON_OFFERING_USER FOREIGN KEY (offering_user_id) REFERENCES users (id);

ALTER TABLE offers ADD CONSTRAINT FK_OFFERS_ON_TARGET_USER FOREIGN KEY (target_user_id) REFERENCES users (id);

ALTER TABLE problems ADD CONSTRAINT FK_PROBLEMS_ON_PROBLEMLIFECYCLE FOREIGN KEY (problem_lifecycle_id) REFERENCES problem_lifecycles (id);

ALTER TABLE problem_created_events ADD CONSTRAINT FK_PROBLEM_CREATED_EVENTS_ON_PROBLEM_LIFECYCLE FOREIGN KEY (problem_lifecycle_id) REFERENCES problem_lifecycles (id);

ALTER TABLE problem_lifecycles ADD CONSTRAINT FK_PROBLEM_LIFECYCLES_ON_PROBLEM FOREIGN KEY (problem_id) REFERENCES problems (id);

ALTER TABLE review_comments ADD CONSTRAINT FK_REVIEW_COMMENTS_ON_ANSWER FOREIGN KEY (answer_id) REFERENCES answers (id);

ALTER TABLE submissions ADD CONSTRAINT FK_SUBMISSIONS_ON_ANSWER FOREIGN KEY (answer_id) REFERENCES answers (id);

ALTER TABLE connections ADD CONSTRAINT fk_con_on_followee FOREIGN KEY (followee_id) REFERENCES users (id);

ALTER TABLE connections ADD CONSTRAINT fk_con_on_follower FOREIGN KEY (follower_id) REFERENCES users (id);

ALTER TABLE user_roles ADD CONSTRAINT fk_user_roles_on_user_jpa_entity FOREIGN KEY (user_id) REFERENCES users (id);
# Sub-B: Lesson Context Migration — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the Lesson bounded context (Problem / Answer / Submission / ReviewComment) on the new Enkan/Kotowari/Raoh/JOOQ stack, with full ADR 003 long-term event persistence.

**Architecture:** Each lesson aggregate gets a `data/` (record + sealed), `dao/` (hand-written jOOQ DSL), `behavior/` (pure functions for mutations) and `resource/` (kotowari-restful `@Decision` classes + Raoh decoders). Read-only queries skip `behavior/` and call dao directly from resources. All ID columns are unified to nanoid 21 chars; `ProblemStatus` is persisted as VARCHAR; the three `ProblemEvent` subtypes each get their own table.

**Tech Stack:** Java 25, Enkan 0.15.0, Kotowari-restful 0.15.0, Raoh 0.5.0 (raoh-json + raoh-jooq), jOOQ (hand-written DSL, no codegen), Flyway, Bouncr (HMAC), JUnit 5, AssertJ, Hurl (E2E).

**PR strategy:** 5 internal PRs, each merged to `develop` before the next starts.

- **Sub-B-1**: V1 edits + V3 migration + UserId + lesson `data/`
- **Sub-B-2**: lesson `dao/` + dao tests
- **Sub-B-3**: Problem CRUD behavior + resource + Hurl scenario start
- **Sub-B-4**: Answer/Submission behavior + resource + Hurl extension
- **Sub-B-5**: ReviewComment + Answer-with-comments encoder + Hurl finalisation

---

## File Map

### New files (Sub-B-1)

```
kysymys-app/src/main/resources/db/migration/V3__add_problem_event_tables.sql

kysymys-app/src/main/java/net/unit8/kysymys/user/data/UserId.java

kysymys-app/src/main/java/net/unit8/kysymys/lesson/data/
  ProblemId.java                  ProblemName.java
  ProblemRepository.java          GitHubProblemRepository.java
  BitBucketProblemRepository.java GenericProblemRepository.java
  Problem.java
  ProblemStatus.java              ProblemLifecycle.java          ProblemLifecycleId.java
  ProblemEvent.java               ProblemEventId.java
  ProblemCreatedEvent.java        ProblemUpdatedEvent.java       ProblemArchivedEvent.java
  AnswerId.java
  AnswerRepository.java           GitHubAnswerRepository.java
  BitBucketAnswerRepository.java  GenericAnswerRepository.java
  Answer.java
  SubmissionId.java               CommitHash.java                Submission.java
  CommentId.java                  Description.java               ReviewComment.java
  IdGenerator.java                -- jnanoid wrapper, 21 chars

kysymys-app/src/test/java/net/unit8/kysymys/lesson/data/
  ProblemRepositoryUrlTest.java
  AnswerRepositoryUrlTest.java
  IdGeneratorTest.java
```

### Modified files (Sub-B-1)

```
kysymys-app/src/main/resources/db/migration/V1__CREATE_INITIAL.sql   -- column type fixes
kysymys-app/pom.xml                                                  -- jnanoid dependency
```

### New files (Sub-B-2)

```
kysymys-app/src/main/java/net/unit8/kysymys/lesson/dao/
  ProblemDao.java
  ProblemEventDao.java
  AnswerDao.java
  SubmissionDao.java
  ReviewCommentDao.java
  RecordDecoders.java             -- raoh-jooq JooqRecordDecoder<T> table

kysymys-app/src/test/java/net/unit8/kysymys/lesson/dao/
  ProblemDaoTest.java             AnswerDaoTest.java
  SubmissionDaoTest.java          ReviewCommentDaoTest.java
  DaoTestSupport.java             -- shared H2 + Flyway setup
```

### New files (Sub-B-3)

```
kysymys-app/src/main/java/net/unit8/kysymys/lesson/behavior/
  CreateProblem.java              UpdateProblem.java   ArchiveProblem.java

kysymys-app/src/main/java/net/unit8/kysymys/lesson/resource/
  ProblemsResource.java           ProblemResource.java
  ProblemJsonDecoders.java        ProblemJsonEncoders.java

kysymys-app/src/main/java/net/unit8/kysymys/inject/
  UserIdInjector.java

kysymys-app/src/test/java/net/unit8/kysymys/lesson/behavior/
  CreateProblemTest.java          UpdateProblemTest.java   ArchiveProblemTest.java

kysymys-app/src/test/hurl/
  lesson.hurl                     -- start: scenarios 1-3, 10, 11
```

### Modified files (Sub-B-3)

```
kysymys-app/src/main/java/net/unit8/kysymys/KysymysApplicationFactory.java
  -- register UserIdInjector + Lesson routes
```

### New files (Sub-B-4)

```
kysymys-app/src/main/java/net/unit8/kysymys/lesson/behavior/SubmitAnswer.java

kysymys-app/src/main/java/net/unit8/kysymys/lesson/resource/
  AnswersResource.java            AnswerResource.java        MyAnswersResource.java
  AnswerJsonDecoders.java         AnswerJsonEncoders.java

kysymys-app/src/test/java/net/unit8/kysymys/lesson/behavior/SubmitAnswerTest.java
```

### Modified files (Sub-B-4)

```
kysymys-app/src/main/java/net/unit8/kysymys/KysymysApplicationFactory.java
  -- add /problems/:id/answers, /answers, /answers/:id routes
kysymys-app/src/test/hurl/lesson.hurl                                -- scenarios 4-7
```

### New files (Sub-B-5)

```
kysymys-app/src/main/java/net/unit8/kysymys/lesson/behavior/PostComment.java

kysymys-app/src/main/java/net/unit8/kysymys/lesson/resource/
  CommentsResource.java           CommentJsonDecoders.java

kysymys-app/src/test/java/net/unit8/kysymys/lesson/behavior/PostCommentTest.java
```

### Modified files (Sub-B-5)

```
kysymys-app/src/main/java/net/unit8/kysymys/KysymysApplicationFactory.java
  -- add /answers/:id/comments route
kysymys-app/src/main/java/net/unit8/kysymys/lesson/resource/AnswerJsonEncoders.java
  -- include comments[] in the response body
kysymys-app/src/test/hurl/lesson.hurl                                -- scenarios 8-9 (final)
doc/adr/003_problem_status.md                                        -- update file refs
```

---

## Phase 1 (Sub-B-1): Schema + data layer

### Task 1.1: Edit V1 migration column types

**Files:**
- Modify: `kysymys-app/src/main/resources/db/migration/V1__CREATE_INITIAL.sql`

- [ ] **Step 1:** Open `V1__CREATE_INITIAL.sql` and apply the table from the spec (`doc/specs/2026-05-03-sub-b-lesson-design.md` § "V1 編集") in place. Replacements:

```sql
-- answers
answerer_id VARCHAR(21) NOT NULL,                  -- was VARCHAR(255)

-- problems
problem_lifecycle_id VARCHAR(21),                  -- was VARCHAR(255)

-- problem_lifecycles
id VARCHAR(21) NOT NULL,
problem_id VARCHAR(21) NOT NULL,
status VARCHAR(20) NOT NULL,                       -- was INTEGER

-- problem_created_events
id VARCHAR(21) NOT NULL,
problem_lifecycle_id VARCHAR(21),
creator_id VARCHAR(21) NOT NULL,

-- submissions
id VARCHAR(21) NOT NULL,                           -- was VARCHAR(255)
answer_id VARCHAR(21),                             -- already 21
commit_hash VARCHAR(40),                           -- was VARCHAR(255)

-- latest_submissions: new structure
DROP table latest_submissions inline; replace with:
CREATE TABLE latest_submissions(
  answer_id VARCHAR(21) NOT NULL,
  submission_id VARCHAR(21) NOT NULL,
  CONSTRAINT pk_latest_submissions PRIMARY KEY(answer_id)
);

-- review_comments
id VARCHAR(21) NOT NULL,
commenter_id VARCHAR(21) NOT NULL,
description VARCHAR(4000) NOT NULL,                -- was VARCHAR(255)
```

The final V1 file content is the existing file with the column-by-column edits above. Do NOT touch user/avatar/notification tables (those are Sub-C).

- [ ] **Step 2:** Build to confirm Flyway file is still valid SQL.

```bash
mvn -pl kysymys-app -am compile -q
```

Expected: BUILD SUCCESS (Flyway is not invoked at compile, but mvn validates resources copy).

- [ ] **Step 3:** Start dev to verify V1 migration applies cleanly on fresh H2.

```bash
cd kysymys-app && timeout 20 mvn exec:java 2>&1 | grep -E "Successfully applied|ERROR|FlywayException" | head -5
```

Expected: a line `Successfully applied 1 migration to schema "PUBLIC", now at version v1`.

- [ ] **Step 4:** Commit.

```bash
git add kysymys-app/src/main/resources/db/migration/V1__CREATE_INITIAL.sql
git commit -m "Sub-B-1: tighten V1 column types for Lesson tables"
```

### Task 1.2: Add V3 migration for updated/archived event tables

**Files:**
- Create: `kysymys-app/src/main/resources/db/migration/V3__add_problem_event_tables.sql`

- [ ] **Step 1:** Create the file with this exact content (matches spec §V3):

```sql
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
```

- [ ] **Step 2:** Restart dev to verify V3 applies on top of V1.

```bash
cd kysymys-app && timeout 20 mvn exec:java 2>&1 | grep -E "version v[123]|ERROR" | head -10
```

Expected: lines for v1 and v3 (V2 already exists for whats_news from previous develop).

- [ ] **Step 3:** Commit.

```bash
git add kysymys-app/src/main/resources/db/migration/V3__add_problem_event_tables.sql
git commit -m "Sub-B-1: add V3 migration for problem updated/archived event tables"
```

### Task 1.3: Add jnanoid dependency and IdGenerator

**Files:**
- Modify: `kysymys-app/pom.xml`
- Create: `kysymys-app/src/main/java/net/unit8/kysymys/lesson/data/IdGenerator.java`
- Create: `kysymys-app/src/test/java/net/unit8/kysymys/lesson/data/IdGeneratorTest.java`

- [ ] **Step 1:** Add jnanoid to `kysymys-app/pom.xml` (parent already has the dependencyManagement entry from Sub-A; just declare without version):

```xml
        <dependency>
            <groupId>com.aventrix.jnanoid</groupId>
            <artifactId>jnanoid</artifactId>
        </dependency>
```

Insert it in the `<dependencies>` block, near the top (alongside other domain-level deps).

- [ ] **Step 2:** Create `IdGeneratorTest.java`:

```java
package net.unit8.kysymys.lesson.data;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdGeneratorTest {
    @Test
    void generates21CharNanoid() {
        String id = IdGenerator.newId();
        assertThat(id).hasSize(21);
        assertThat(id).matches("[A-Za-z0-9_-]{21}");
    }

    @Test
    void generatesUniqueIds() {
        String a = IdGenerator.newId();
        String b = IdGenerator.newId();
        assertThat(a).isNotEqualTo(b);
    }
}
```

- [ ] **Step 3:** Run, confirm fail (class missing):

```bash
mvn -pl kysymys-app test -Dtest=IdGeneratorTest -q 2>&1 | tail -10
```

Expected: COMPILATION ERROR or test fails because `IdGenerator` doesn't exist.

- [ ] **Step 4:** Create `IdGenerator.java`:

```java
package net.unit8.kysymys.lesson.data;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

/** Generates 21-character nanoids for all Lesson aggregate ids. */
public final class IdGenerator {
    private IdGenerator() {}

    public static String newId() {
        return NanoIdUtils.randomNanoId();
    }
}
```

- [ ] **Step 5:** Run, confirm pass.

```bash
mvn -pl kysymys-app test -Dtest=IdGeneratorTest -q 2>&1 | tail -5
```

Expected: `Tests run: 2, Failures: 0`.

- [ ] **Step 6:** Commit.

```bash
git add kysymys-app/pom.xml \
        kysymys-app/src/main/java/net/unit8/kysymys/lesson/data/IdGenerator.java \
        kysymys-app/src/test/java/net/unit8/kysymys/lesson/data/IdGeneratorTest.java
git commit -m "Sub-B-1: add jnanoid + IdGenerator (21-char nanoid)"
```

### Task 1.4: UserId record (cross-context preview)

**Files:**
- Create: `kysymys-app/src/main/java/net/unit8/kysymys/user/data/UserId.java`

- [ ] **Step 1:** Create:

```java
package net.unit8.kysymys.user.data;

import java.util.Objects;

/**
 * Stable identifier for a Kysymys user. Other bounded contexts (Lesson,
 * Avatar, Notification) reference users only via this record per ADR 001.
 *
 * <p>The full User aggregate is built in Sub-C; this record is hoisted into
 * Sub-B because Lesson aggregates carry {@code answererId} / {@code commenterId}
 * fields that need a typed identifier today.
 */
public record UserId(String value) {
    public UserId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("UserId value must not be blank");
        }
    }

    public static UserId of(String value) {
        return new UserId(value);
    }
}
```

- [ ] **Step 2:** Verify it compiles.

```bash
mvn -pl kysymys-app -am compile -q 2>&1 | tail -3
```

Expected: BUILD SUCCESS.

- [ ] **Step 3:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/user/data/UserId.java
git commit -m "Sub-B-1: add user.data.UserId (cross-context identifier)"
```

### Task 1.5: ProblemId / ProblemName / Description / CommitHash / *Id records

**Files:**
- Create:
  - `lesson/data/ProblemId.java`
  - `lesson/data/ProblemName.java`
  - `lesson/data/ProblemLifecycleId.java`
  - `lesson/data/ProblemEventId.java`
  - `lesson/data/AnswerId.java`
  - `lesson/data/SubmissionId.java`
  - `lesson/data/CommentId.java`
  - `lesson/data/CommitHash.java`
  - `lesson/data/Description.java`

These are simple value records with constructor validation. They have no tests of their own — their constraints are exercised by Raoh decoders in Sub-B-3+ and by domain tests.

- [ ] **Step 1:** Create the seven id records by template. Each follows the same structure; example for `ProblemId.java`:

```java
package net.unit8.kysymys.lesson.data;

import java.util.Objects;

public record ProblemId(String value) {
    public ProblemId {
        Objects.requireNonNull(value, "value");
        if (value.length() != 21) {
            throw new IllegalArgumentException("ProblemId must be 21 chars (nanoid)");
        }
    }
    public static ProblemId of(String value) { return new ProblemId(value); }
    public static ProblemId newId() { return new ProblemId(IdGenerator.newId()); }
}
```

Apply the same pattern verbatim — only the type name changes — to:

- `ProblemLifecycleId`
- `ProblemEventId`
- `AnswerId`
- `SubmissionId`
- `CommentId`

- [ ] **Step 2:** Create `ProblemName.java`:

```java
package net.unit8.kysymys.lesson.data;

import java.util.Objects;

public record ProblemName(String value) {
    public ProblemName {
        Objects.requireNonNull(value, "value");
        String trimmed = value.strip();
        if (trimmed.isEmpty() || trimmed.length() > 100) {
            throw new IllegalArgumentException("ProblemName must be 1..100 chars");
        }
    }
}
```

- [ ] **Step 3:** Create `Description.java`:

```java
package net.unit8.kysymys.lesson.data;

import java.util.Objects;

public record Description(String value) {
    public Description {
        Objects.requireNonNull(value, "value");
        String trimmed = value.strip();
        if (trimmed.isEmpty() || trimmed.length() > 4000) {
            throw new IllegalArgumentException("Description must be 1..4000 chars");
        }
    }
}
```

- [ ] **Step 4:** Create `CommitHash.java`:

```java
package net.unit8.kysymys.lesson.data;

import java.util.Objects;
import java.util.regex.Pattern;

public record CommitHash(String value) {
    private static final Pattern HEX40 = Pattern.compile("^[0-9a-fA-F]{40}$");

    public CommitHash {
        Objects.requireNonNull(value, "value");
        if (!HEX40.matcher(value).matches()) {
            throw new IllegalArgumentException("CommitHash must be 40 hex chars");
        }
    }
}
```

- [ ] **Step 5:** Verify compile.

```bash
mvn -pl kysymys-app -am compile -q 2>&1 | tail -3
```

Expected: BUILD SUCCESS.

- [ ] **Step 6:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/lesson/data/{ProblemId,ProblemName,ProblemLifecycleId,ProblemEventId,AnswerId,SubmissionId,CommentId,CommitHash,Description}.java
git commit -m "Sub-B-1: add lesson value object records (ids, names, hash, description)"
```

### Task 1.6: ProblemRepository sealed hierarchy

**Files:**
- Create:
  - `lesson/data/ProblemRepository.java` (sealed interface)
  - `lesson/data/GitHubProblemRepository.java`
  - `lesson/data/BitBucketProblemRepository.java`
  - `lesson/data/GenericProblemRepository.java`
- Test: `lesson/data/ProblemRepositoryUrlTest.java`

- [ ] **Step 1:** Write the failing test first.

```java
package net.unit8.kysymys.lesson.data;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProblemRepositoryUrlTest {
    @Test
    void githubProblemUrlBuildsBlobLink() {
        ProblemRepository repo = new GitHubProblemRepository(
                "https://github.com/foo/bar.git", "main", "/README.md");
        assertThat(repo.problemUrl())
                .isEqualTo("https://github.com/foo/bar/blob/main/README.md");
    }

    @Test
    void githubProblemUrlAllowsNoDotGitSuffix() {
        ProblemRepository repo = new GitHubProblemRepository(
                "https://github.com/foo/bar", "main", "/README.md");
        assertThat(repo.problemUrl())
                .isEqualTo("https://github.com/foo/bar/blob/main/README.md");
    }

    @Test
    void bitbucketProblemUrlBuildsSrcLink() {
        ProblemRepository repo = new BitBucketProblemRepository(
                "https://bitbucket.org/foo/bar", "main", "/README.md");
        assertThat(repo.problemUrl())
                .isEqualTo("https://bitbucket.org/foo/bar/src/main/README.md");
    }

    @Test
    void genericProblemUrlReturnsTheBaseUrl() {
        ProblemRepository repo = new GenericProblemRepository(
                "https://example.com/lessons/kafka", "main");
        assertThat(repo.problemUrl())
                .isEqualTo("https://example.com/lessons/kafka");
    }

    @Test
    void rejectsBlankUrl() {
        assertThatThrownBy(() ->
                new GitHubProblemRepository("", "main", "/README.md"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBranchWithDoubleDots() {
        assertThatThrownBy(() ->
                new GitHubProblemRepository("https://github.com/x/y", "ma..in", "/README.md"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBranchEndingWithSlash() {
        assertThatThrownBy(() ->
                new GitHubProblemRepository("https://github.com/x/y", "main/", "/README.md"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 2:** Run, confirm FAIL.

```bash
mvn -pl kysymys-app test -Dtest=ProblemRepositoryUrlTest -q 2>&1 | tail -10
```

Expected: COMPILATION ERROR.

- [ ] **Step 3:** Create the sealed interface `ProblemRepository.java`:

```java
package net.unit8.kysymys.lesson.data;

/**
 * Sealed hierarchy describing where a Problem's source lives. Each subtype
 * knows how to build the public URL of its README.
 *
 * <p>The branch name pattern is shared via {@link BranchNamePattern}.
 */
public sealed interface ProblemRepository
        permits GitHubProblemRepository, BitBucketProblemRepository, GenericProblemRepository {

    String url();

    /** Returns a public URL pointing to the readme/landing page. */
    String problemUrl();
}
```

- [ ] **Step 4:** Create `BranchNamePattern.java` (package-private helper used by both Problem and Answer repos in Task 1.7):

```java
package net.unit8.kysymys.lesson.data;

import java.util.regex.Pattern;

/**
 * Validation helpers shared by repository value objects. The branch regex
 * comes from git's ref-format rules — it is the same pattern the legacy
 * Yavi-based code used.
 */
final class BranchNamePattern {
    /** See https://www.spinics.net/lists/git/msg133704.html */
    static final Pattern BRANCH = Pattern.compile(
            "^(?!(^\\.|.*(\\.\\.|\\p{Space}|\\p{Cntrl}))).*(?<!(/|\\.lock))$");

    private BranchNamePattern() {}

    static String validateBranch(String branch) {
        if (branch == null || branch.isEmpty() || branch.length() > 100) {
            throw new IllegalArgumentException("branch must be 1..100 chars");
        }
        if (!BRANCH.matcher(branch).matches()) {
            throw new IllegalArgumentException("branch is not a valid git ref: " + branch);
        }
        return branch;
    }

    static String validateUrl(String url) {
        if (url == null || url.isBlank() || url.length() > 255) {
            throw new IllegalArgumentException("url must be 1..255 chars");
        }
        return url;
    }

    static String chopDotGit(String url) {
        return url.endsWith(".git") ? url.substring(0, url.length() - 4) : url;
    }

    static String validateReadmePath(String path) {
        if (path == null || path.isEmpty() || path.length() > 100) {
            throw new IllegalArgumentException("readmePath must be 1..100 chars");
        }
        return path;
    }
}
```

- [ ] **Step 5:** Create `GitHubProblemRepository.java`:

```java
package net.unit8.kysymys.lesson.data;

import static net.unit8.kysymys.lesson.data.BranchNamePattern.chopDotGit;
import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateBranch;
import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateReadmePath;
import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateUrl;

public record GitHubProblemRepository(String url, String branch, String readmePath)
        implements ProblemRepository {
    public GitHubProblemRepository {
        url = validateUrl(url);
        branch = validateBranch(branch);
        readmePath = validateReadmePath(readmePath);
    }

    @Override
    public String problemUrl() {
        return chopDotGit(url) + "/blob/" + branch + readmePath;
    }
}
```

- [ ] **Step 6:** Create `BitBucketProblemRepository.java`:

```java
package net.unit8.kysymys.lesson.data;

import static net.unit8.kysymys.lesson.data.BranchNamePattern.chopDotGit;
import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateBranch;
import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateReadmePath;
import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateUrl;

public record BitBucketProblemRepository(String url, String branch, String readmePath)
        implements ProblemRepository {
    public BitBucketProblemRepository {
        url = validateUrl(url);
        branch = validateBranch(branch);
        readmePath = validateReadmePath(readmePath);
    }

    @Override
    public String problemUrl() {
        return chopDotGit(url) + "/src/" + branch + readmePath;
    }
}
```

- [ ] **Step 7:** Create `GenericProblemRepository.java`:

```java
package net.unit8.kysymys.lesson.data;

import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateBranch;
import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateUrl;

public record GenericProblemRepository(String url, String branch)
        implements ProblemRepository {
    public GenericProblemRepository {
        url = validateUrl(url);
        branch = validateBranch(branch);
    }

    @Override
    public String problemUrl() {
        return url;
    }
}
```

- [ ] **Step 8:** Run, confirm PASS.

```bash
mvn -pl kysymys-app test -Dtest=ProblemRepositoryUrlTest -q 2>&1 | tail -5
```

Expected: `Tests run: 7, Failures: 0`.

- [ ] **Step 9:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/lesson/data/{ProblemRepository,GitHubProblemRepository,BitBucketProblemRepository,GenericProblemRepository,BranchNamePattern}.java \
        kysymys-app/src/test/java/net/unit8/kysymys/lesson/data/ProblemRepositoryUrlTest.java
git commit -m "Sub-B-1: ProblemRepository sealed hierarchy with URL builders"
```

### Task 1.7: AnswerRepository sealed hierarchy

**Files:**
- Create:
  - `lesson/data/AnswerRepository.java`
  - `lesson/data/GitHubAnswerRepository.java`
  - `lesson/data/BitBucketAnswerRepository.java`
  - `lesson/data/GenericAnswerRepository.java`
- Test: `lesson/data/AnswerRepositoryUrlTest.java`

The structure mirrors Task 1.6, but each subtype carries only `url`. The commit hash lives on `Submission`, not on the repository value object (per spec §"Answer / Submission").

- [ ] **Step 1:** Write the failing test:

```java
package net.unit8.kysymys.lesson.data;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnswerRepositoryUrlTest {
    @Test
    void githubAnswerUrlForCommit() {
        AnswerRepository repo = new GitHubAnswerRepository("https://github.com/me/fizzbuzz.git");
        String hash = "0123456789012345678901234567890123456789";
        assertThat(repo.commitUrl(new CommitHash(hash)))
                .isEqualTo("https://github.com/me/fizzbuzz/tree/" + hash);
    }

    @Test
    void bitbucketAnswerUrlForCommit() {
        AnswerRepository repo = new BitBucketAnswerRepository("https://bitbucket.org/me/fizzbuzz");
        String hash = "0123456789012345678901234567890123456789";
        assertThat(repo.commitUrl(new CommitHash(hash)))
                .isEqualTo("https://bitbucket.org/me/fizzbuzz/commits/" + hash);
    }

    @Test
    void genericAnswerUrlReturnsTheBaseUrl() {
        AnswerRepository repo = new GenericAnswerRepository("https://example.com/u/me");
        String hash = "0123456789012345678901234567890123456789";
        assertThat(repo.commitUrl(new CommitHash(hash)))
                .isEqualTo("https://example.com/u/me");
    }
}
```

- [ ] **Step 2:** Run, confirm FAIL.

```bash
mvn -pl kysymys-app test -Dtest=AnswerRepositoryUrlTest -q 2>&1 | tail -5
```

- [ ] **Step 3:** Create `AnswerRepository.java`:

```java
package net.unit8.kysymys.lesson.data;

public sealed interface AnswerRepository
        permits GitHubAnswerRepository, BitBucketAnswerRepository, GenericAnswerRepository {

    String url();

    /** Returns the public URL for a specific commit on this repository. */
    String commitUrl(CommitHash hash);
}
```

- [ ] **Step 4:** Create the three subtypes:

```java
package net.unit8.kysymys.lesson.data;

import static net.unit8.kysymys.lesson.data.BranchNamePattern.chopDotGit;
import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateUrl;

public record GitHubAnswerRepository(String url) implements AnswerRepository {
    public GitHubAnswerRepository { url = validateUrl(url); }

    @Override
    public String commitUrl(CommitHash hash) {
        return chopDotGit(url) + "/tree/" + hash.value();
    }
}
```

```java
package net.unit8.kysymys.lesson.data;

import static net.unit8.kysymys.lesson.data.BranchNamePattern.chopDotGit;
import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateUrl;

public record BitBucketAnswerRepository(String url) implements AnswerRepository {
    public BitBucketAnswerRepository { url = validateUrl(url); }

    @Override
    public String commitUrl(CommitHash hash) {
        return chopDotGit(url) + "/commits/" + hash.value();
    }
}
```

```java
package net.unit8.kysymys.lesson.data;

import static net.unit8.kysymys.lesson.data.BranchNamePattern.validateUrl;

public record GenericAnswerRepository(String url) implements AnswerRepository {
    public GenericAnswerRepository { url = validateUrl(url); }

    @Override
    public String commitUrl(CommitHash hash) {
        return url;
    }
}
```

- [ ] **Step 5:** Run, confirm PASS.

```bash
mvn -pl kysymys-app test -Dtest=AnswerRepositoryUrlTest -q 2>&1 | tail -5
```

Expected: `Tests run: 3, Failures: 0`.

- [ ] **Step 6:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/lesson/data/{AnswerRepository,GitHubAnswerRepository,BitBucketAnswerRepository,GenericAnswerRepository}.java \
        kysymys-app/src/test/java/net/unit8/kysymys/lesson/data/AnswerRepositoryUrlTest.java
git commit -m "Sub-B-1: AnswerRepository sealed hierarchy with commit URL builders"
```

### Task 1.8: Aggregate records (Problem, Lifecycle, Events, Answer, Submission, ReviewComment)

**Files:**
- Create:
  - `lesson/data/Problem.java`
  - `lesson/data/ProblemStatus.java`
  - `lesson/data/ProblemLifecycle.java`
  - `lesson/data/ProblemEvent.java`
  - `lesson/data/ProblemCreatedEvent.java`
  - `lesson/data/ProblemUpdatedEvent.java`
  - `lesson/data/ProblemArchivedEvent.java`
  - `lesson/data/Answer.java`
  - `lesson/data/Submission.java`
  - `lesson/data/ReviewComment.java`

These are pure records with constructor null-checks. No business logic / no tests at this layer.

- [ ] **Step 1:** `ProblemStatus.java`:

```java
package net.unit8.kysymys.lesson.data;

public enum ProblemStatus {
    ACTIVE,
    ARCHIVED
}
```

- [ ] **Step 2:** `Problem.java`:

```java
package net.unit8.kysymys.lesson.data;

import java.util.Objects;

public record Problem(
        ProblemId id,
        ProblemName name,
        ProblemRepository repository,
        ProblemLifecycleId lifecycleId
) {
    public Problem {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(repository, "repository");
        Objects.requireNonNull(lifecycleId, "lifecycleId");
    }
}
```

- [ ] **Step 3:** `ProblemLifecycle.java`:

```java
package net.unit8.kysymys.lesson.data;

import java.util.Objects;

public record ProblemLifecycle(
        ProblemLifecycleId id,
        ProblemId problemId,
        ProblemStatus status
) {
    public ProblemLifecycle {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(problemId, "problemId");
        Objects.requireNonNull(status, "status");
    }
}
```

- [ ] **Step 4:** `ProblemEvent.java`:

```java
package net.unit8.kysymys.lesson.data;

import java.time.LocalDateTime;

public sealed interface ProblemEvent
        permits ProblemCreatedEvent, ProblemUpdatedEvent, ProblemArchivedEvent {

    ProblemEventId id();
    ProblemLifecycleId lifecycleId();
    LocalDateTime occurredAt();
}
```

- [ ] **Step 5:** Three event subtypes:

```java
package net.unit8.kysymys.lesson.data;

import net.unit8.kysymys.user.data.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

public record ProblemCreatedEvent(
        ProblemEventId id,
        ProblemLifecycleId lifecycleId,
        LocalDateTime occurredAt,
        UserId creatorId
) implements ProblemEvent {
    public ProblemCreatedEvent {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(lifecycleId, "lifecycleId");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(creatorId, "creatorId");
    }
}
```

```java
package net.unit8.kysymys.lesson.data;

import net.unit8.kysymys.user.data.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

public record ProblemUpdatedEvent(
        ProblemEventId id,
        ProblemLifecycleId lifecycleId,
        LocalDateTime occurredAt,
        UserId updaterId
) implements ProblemEvent {
    public ProblemUpdatedEvent {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(lifecycleId, "lifecycleId");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(updaterId, "updaterId");
    }
}
```

```java
package net.unit8.kysymys.lesson.data;

import net.unit8.kysymys.user.data.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

public record ProblemArchivedEvent(
        ProblemEventId id,
        ProblemLifecycleId lifecycleId,
        LocalDateTime occurredAt,
        UserId archiverId
) implements ProblemEvent {
    public ProblemArchivedEvent {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(lifecycleId, "lifecycleId");
        Objects.requireNonNull(occurredAt, "occurredAt");
        Objects.requireNonNull(archiverId, "archiverId");
    }
}
```

- [ ] **Step 6:** `Answer.java`:

```java
package net.unit8.kysymys.lesson.data;

import net.unit8.kysymys.user.data.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

public record Answer(
        AnswerId id,
        ProblemId problemId,
        UserId answererId,
        AnswerRepository repository,
        LocalDateTime lastAnsweredAt
) {
    public Answer {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(problemId, "problemId");
        Objects.requireNonNull(answererId, "answererId");
        Objects.requireNonNull(repository, "repository");
        Objects.requireNonNull(lastAnsweredAt, "lastAnsweredAt");
    }
}
```

- [ ] **Step 7:** `Submission.java`:

```java
package net.unit8.kysymys.lesson.data;

import java.time.LocalDateTime;
import java.util.Objects;

public record Submission(
        SubmissionId id,
        AnswerId answerId,
        CommitHash commitHash,
        LocalDateTime submittedAt
) {
    public Submission {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(answerId, "answerId");
        Objects.requireNonNull(commitHash, "commitHash");
        Objects.requireNonNull(submittedAt, "submittedAt");
    }
}
```

- [ ] **Step 8:** `ReviewComment.java`:

```java
package net.unit8.kysymys.lesson.data;

import net.unit8.kysymys.user.data.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

public record ReviewComment(
        CommentId id,
        AnswerId answerId,
        UserId commenterId,
        Description description,
        LocalDateTime postedAt
) {
    public ReviewComment {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(answerId, "answerId");
        Objects.requireNonNull(commenterId, "commenterId");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(postedAt, "postedAt");
    }
}
```

- [ ] **Step 9:** Verify compile.

```bash
mvn -pl kysymys-app -am compile -q 2>&1 | tail -3
```

Expected: BUILD SUCCESS.

- [ ] **Step 10:** Run all lesson data tests one more time.

```bash
mvn -pl kysymys-app test -Dtest='net.unit8.kysymys.lesson.data.*' -q 2>&1 | tail -5
```

Expected: all tests pass.

- [ ] **Step 11:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/lesson/data/{Problem,ProblemStatus,ProblemLifecycle,ProblemEvent,ProblemCreatedEvent,ProblemUpdatedEvent,ProblemArchivedEvent,Answer,Submission,ReviewComment}.java
git commit -m "Sub-B-1: Lesson aggregate records (Problem/Lifecycle/Event/Answer/Submission/Comment)"
```

### Task 1.9: Open Sub-B-1 PR

- [ ] **Step 1:** Push.

```bash
git push -u origin feature/sub-b-lesson
```

- [ ] **Step 2:** Open PR.

```bash
gh pr create --base develop --head feature/sub-b-lesson \
  --title "Sub-B-1: Lesson schema + data layer (V1 fix, V3 events, records)" \
  --body "$(cat <<'EOF'
## Summary

First slice of Sub-B (Lesson context migration). No behavior or routes yet — just the schema fixes and the pure-record `data/` layer that everything else builds on.

- **V1 edits**: nanoid 21-char id columns unified across lesson tables, `problem_lifecycles.status` switched from INTEGER to VARCHAR(20), `review_comments.description` widened to VARCHAR(4000), `latest_submissions` PK fixed to `(answer_id)`. Per spec §"V1 編集".
- **V3 migration** (new): `problem_updated_events` + `problem_archived_events` tables, completing ADR 003's three-event hierarchy.
- **`user/data/UserId`** preview record (Sub-C will own the rest of the User context).
- **`lesson/data/`**: ids, names, hash, description, sealed `ProblemRepository` / `AnswerRepository` hierarchies (GitHub/BitBucket/Generic), Problem aggregate, ProblemLifecycle, three ProblemEvent subtypes, Answer, Submission, ReviewComment. Validation lives inside compact constructors; URL building lives on the sealed subtypes.

## Test plan

- [x] `mvn -pl kysymys-app -am clean compile`
- [x] `mvn -pl kysymys-app test -Dtest='net.unit8.kysymys.lesson.data.*'`
- [x] `cd kysymys-app && mvn exec:java` — verifies V1+V2+V3 apply on a fresh H2

Subsequent PRs (Sub-B-2 dao, B-3 Problem CRUD, B-4 Answer, B-5 Comment) build on this.
EOF
)"
```

---

## Phase 2 (Sub-B-2): dao layer with tests

### Task 2.1: Shared DaoTestSupport

**Files:**
- Create: `kysymys-app/src/test/java/net/unit8/kysymys/lesson/dao/DaoTestSupport.java`

A small helper that gives each dao test a fresh H2 + Flyway-applied DSLContext. JUnit 5 lifecycle (`@BeforeAll` / `@AfterAll`).

- [ ] **Step 1:** Create:

```java
package net.unit8.kysymys.lesson.dao;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.util.UUID;

/**
 * Test-only helper. Creates a private in-memory H2 database, runs Flyway,
 * and exposes a jOOQ {@link DSLContext}. Each dao test class instantiates
 * one of these in {@code @BeforeAll}.
 */
public final class DaoTestSupport implements AutoCloseable {
    private final DataSource dataSource;
    private final DSLContext dsl;

    public DaoTestSupport() {
        JdbcDataSource ds = new JdbcDataSource();
        // Unique URL per instance keeps parallel-running tests isolated.
        ds.setUrl("jdbc:h2:mem:test-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        ds.setUser("sa");
        ds.setPassword("");
        Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")
                .load()
                .migrate();
        this.dataSource = ds;
        this.dsl = DSL.using(ds, SQLDialect.H2);
    }

    public DSLContext dsl() {
        return dsl;
    }

    @Override
    public void close() {
        // H2 in-mem with DB_CLOSE_DELAY=-1 lives until JVM exit; nothing to do.
    }
}
```

- [ ] **Step 2:** Verify compile.

```bash
mvn -pl kysymys-app test-compile -q 2>&1 | tail -3
```

Expected: BUILD SUCCESS (no test runs yet — just compile).

- [ ] **Step 3:** Commit.

```bash
git add kysymys-app/src/test/java/net/unit8/kysymys/lesson/dao/DaoTestSupport.java
git commit -m "Sub-B-2: DaoTestSupport (H2 + Flyway harness)"
```

### Task 2.2: ProblemDao + ProblemEventDao + tests

**Files:**
- Create: `lesson/dao/ProblemDao.java`, `lesson/dao/ProblemEventDao.java`
- Test: `lesson/dao/ProblemDaoTest.java`

`ProblemDao` writes the (problems, problem_lifecycles) pair atomically; `ProblemEventDao` writes the per-subtype event rows. Both are constructor-injected with `DSLContext`. **The caller is responsible for opening the transaction** — the daos do not call `dsl.transaction(...)`. Behaviors call them inside a single transaction.

- [ ] **Step 1:** Write `ProblemDaoTest.java`:

```java
package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemDaoTest {
    private static DaoTestSupport support;
    private static ProblemDao dao;
    private static ProblemEventDao events;

    @BeforeAll
    static void setUp() {
        support = new DaoTestSupport();
        dao = new ProblemDao(support.dsl());
        events = new ProblemEventDao(support.dsl());
    }

    @Test
    void saveAndFindById() {
        ProblemId pid = ProblemId.newId();
        ProblemLifecycleId lid = new ProblemLifecycleId(IdGenerator.newId());
        Problem p = new Problem(
                pid,
                new ProblemName("FizzBuzz"),
                new GitHubProblemRepository(
                        "https://github.com/kysymys/fizzbuzz.git", "main", "/README.md"),
                lid);
        ProblemLifecycle lc = new ProblemLifecycle(lid, pid, ProblemStatus.ACTIVE);

        support.dsl().transaction(cfg -> {
            new ProblemDao(cfg.dsl()).insert(p, lc);
        });

        Optional<Problem> loaded = dao.findById(pid);
        assertThat(loaded).hasValueSatisfying(found -> {
            assertThat(found.id()).isEqualTo(pid);
            assertThat(found.name().value()).isEqualTo("FizzBuzz");
            assertThat(found.repository()).isInstanceOf(GitHubProblemRepository.class);
        });
    }

    @Test
    void updateLifecycleStatus() {
        ProblemId pid = ProblemId.newId();
        ProblemLifecycleId lid = new ProblemLifecycleId(IdGenerator.newId());
        Problem p = new Problem(pid, new ProblemName("Echo"),
                new GenericProblemRepository("https://example.com/echo", "main"), lid);

        support.dsl().transaction(cfg -> {
            new ProblemDao(cfg.dsl()).insert(p,
                    new ProblemLifecycle(lid, pid, ProblemStatus.ACTIVE));
            new ProblemDao(cfg.dsl()).updateStatus(lid, ProblemStatus.ARCHIVED);
        });

        ProblemStatus status = dao.findStatus(lid).orElseThrow();
        assertThat(status).isEqualTo(ProblemStatus.ARCHIVED);
    }

    @Test
    void persistCreatedEvent() {
        ProblemId pid = ProblemId.newId();
        ProblemLifecycleId lid = new ProblemLifecycleId(IdGenerator.newId());
        Problem p = new Problem(pid, new ProblemName("Echo"),
                new GenericProblemRepository("https://example.com/echo", "main"), lid);
        UserId teacher = UserId.of(IdGenerator.newId());

        support.dsl().transaction(cfg -> {
            new ProblemDao(cfg.dsl()).insert(p,
                    new ProblemLifecycle(lid, pid, ProblemStatus.ACTIVE));
            new ProblemEventDao(cfg.dsl()).insert(new ProblemCreatedEvent(
                    new ProblemEventId(IdGenerator.newId()),
                    lid,
                    LocalDateTime.now(),
                    teacher));
        });

        long count = events.countByLifecycle(lid);
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void listAllReturnsInsertedProblems() {
        ProblemId pid = ProblemId.newId();
        ProblemLifecycleId lid = new ProblemLifecycleId(IdGenerator.newId());
        support.dsl().transaction(cfg -> {
            new ProblemDao(cfg.dsl()).insert(
                    new Problem(pid, new ProblemName("ListTest"),
                            new GenericProblemRepository("https://example.com/x", "main"), lid),
                    new ProblemLifecycle(lid, pid, ProblemStatus.ACTIVE));
        });

        assertThat(dao.listActive())
                .extracting(Problem::id)
                .contains(pid);
    }
}
```

- [ ] **Step 2:** Run, confirm FAIL (compilation).

```bash
mvn -pl kysymys-app test -Dtest=ProblemDaoTest -q 2>&1 | tail -10
```

Expected: COMPILATION ERROR (`ProblemDao` / `ProblemEventDao` missing).

- [ ] **Step 3:** Implement `ProblemDao.java`:

```java
package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.*;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;

import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * jOOQ-backed repository for {@code problems} and {@code problem_lifecycles}.
 * Callers pass the {@link DSLContext} they want to run on; the dao itself
 * never opens transactions.
 */
public class ProblemDao {

    // ---- problems ----
    private static final Field<String> ID = field("id", String.class);
    private static final Field<String> NAME = field("name", String.class);
    private static final Field<String> REPOSITORY_URL = field("repository_url", String.class);
    private static final Field<String> BRANCH = field("branch", String.class);
    private static final Field<String> README_PATH = field("readme_path", String.class);
    private static final Field<String> RUNNER = field("runner", String.class);
    private static final Field<String> PROBLEM_LIFECYCLE_ID = field("problem_lifecycle_id", String.class);

    // ---- problem_lifecycles ----
    private static final Field<String> LC_ID = field("id", String.class);
    private static final Field<String> LC_PROBLEM_ID = field("problem_id", String.class);
    private static final Field<String> LC_STATUS = field("status", String.class);

    private final DSLContext dsl;

    public ProblemDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void insert(Problem p, ProblemLifecycle lc) {
        // problems first, then lifecycles, but the FK in V1 is problems -> lifecycles,
        // so the lifecycle must exist before the problem row. Insert lifecycle first.
        dsl.insertInto(table("problem_lifecycles"), LC_ID, LC_PROBLEM_ID, LC_STATUS)
                .values(lc.id().value(), lc.problemId().value(), lc.status().name())
                .execute();
        dsl.insertInto(table("problems"),
                        ID, NAME, REPOSITORY_URL, BRANCH, README_PATH, RUNNER, PROBLEM_LIFECYCLE_ID)
                .values(p.id().value(),
                        p.name().value(),
                        p.repository().url(),
                        branchOf(p.repository()),
                        readmePathOrNull(p.repository()),
                        repositoryTypeKey(p.repository()),
                        p.lifecycleId().value())
                .execute();
    }

    public void update(Problem p) {
        dsl.update(table("problems"))
                .set(NAME, p.name().value())
                .set(REPOSITORY_URL, p.repository().url())
                .set(BRANCH, branchOf(p.repository()))
                .set(README_PATH, readmePathOrNull(p.repository()))
                .set(RUNNER, repositoryTypeKey(p.repository()))
                .where(ID.eq(p.id().value()))
                .execute();
    }

    public void updateStatus(ProblemLifecycleId lifecycleId, ProblemStatus status) {
        dsl.update(table("problem_lifecycles"))
                .set(LC_STATUS, status.name())
                .where(LC_ID.eq(lifecycleId.value()))
                .execute();
    }

    public Optional<Problem> findById(ProblemId id) {
        Record rec = dsl.select(ID, NAME, REPOSITORY_URL, BRANCH, README_PATH, RUNNER, PROBLEM_LIFECYCLE_ID)
                .from(table("problems"))
                .where(ID.eq(id.value()))
                .fetchOne();
        return Optional.ofNullable(rec).map(ProblemDao::mapProblem);
    }

    public Optional<ProblemStatus> findStatus(ProblemLifecycleId lifecycleId) {
        return Optional.ofNullable(
                dsl.select(LC_STATUS).from(table("problem_lifecycles"))
                        .where(LC_ID.eq(lifecycleId.value()))
                        .fetchOne(LC_STATUS))
                .map(ProblemStatus::valueOf);
    }

    public List<Problem> listActive() {
        return dsl.select(ID, NAME, REPOSITORY_URL, BRANCH, README_PATH, RUNNER, PROBLEM_LIFECYCLE_ID)
                .from(table("problems"))
                .join(table("problem_lifecycles"))
                .on(field("problem_lifecycle_id", String.class)
                        .eq(field("problem_lifecycles.id", String.class)))
                .where(field("problem_lifecycles.status", String.class).eq(ProblemStatus.ACTIVE.name()))
                .fetch(ProblemDao::mapProblem);
    }

    // ---- mapping helpers ----

    private static Problem mapProblem(Record r) {
        String runner = r.get(RUNNER);
        String url = r.get(REPOSITORY_URL);
        String branch = r.get(BRANCH);
        String readmePath = r.get(README_PATH);
        ProblemRepository repo = switch (runner) {
            case "github" -> new GitHubProblemRepository(url, branch, readmePath);
            case "bitbucket" -> new BitBucketProblemRepository(url, branch, readmePath);
            case "generic", null -> new GenericProblemRepository(url, branch);
            default -> throw new IllegalStateException("unknown repository type: " + runner);
        };
        return new Problem(
                new ProblemId(r.get(ID)),
                new ProblemName(r.get(NAME)),
                repo,
                new ProblemLifecycleId(r.get(PROBLEM_LIFECYCLE_ID))
        );
    }

    private static String branchOf(ProblemRepository repo) {
        return switch (repo) {
            case GitHubProblemRepository g -> g.branch();
            case BitBucketProblemRepository b -> b.branch();
            case GenericProblemRepository g -> g.branch();
        };
    }

    private static String readmePathOrNull(ProblemRepository repo) {
        return switch (repo) {
            case GitHubProblemRepository g -> g.readmePath();
            case BitBucketProblemRepository b -> b.readmePath();
            case GenericProblemRepository g -> null;
        };
    }

    /** The {@code runner} column doubles as the discriminator for {@link ProblemRepository}. */
    private static String repositoryTypeKey(ProblemRepository repo) {
        return switch (repo) {
            case GitHubProblemRepository g -> "github";
            case BitBucketProblemRepository b -> "bitbucket";
            case GenericProblemRepository g -> "generic";
        };
    }
}
```

- [ ] **Step 4:** Implement `ProblemEventDao.java`:

```java
package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.*;
import org.jooq.DSLContext;
import org.jooq.Field;

import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class ProblemEventDao {
    private static final Field<String> ID = field("id", String.class);
    private static final Field<String> LIFECYCLE_ID = field("problem_lifecycle_id", String.class);
    private static final Field<LocalDateTime> OCCURRED_AT = field("occurred_at", LocalDateTime.class);
    private static final Field<String> CREATOR_ID = field("creator_id", String.class);
    private static final Field<String> UPDATER_ID = field("updater_id", String.class);
    private static final Field<String> ARCHIVER_ID = field("archiver_id", String.class);

    private final DSLContext dsl;

    public ProblemEventDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void insert(ProblemEvent event) {
        switch (event) {
            case ProblemCreatedEvent c -> dsl.insertInto(table("problem_created_events"),
                            ID, LIFECYCLE_ID, OCCURRED_AT, CREATOR_ID)
                    .values(c.id().value(), c.lifecycleId().value(), c.occurredAt(), c.creatorId().value())
                    .execute();
            case ProblemUpdatedEvent u -> dsl.insertInto(table("problem_updated_events"),
                            ID, LIFECYCLE_ID, OCCURRED_AT, UPDATER_ID)
                    .values(u.id().value(), u.lifecycleId().value(), u.occurredAt(), u.updaterId().value())
                    .execute();
            case ProblemArchivedEvent a -> dsl.insertInto(table("problem_archived_events"),
                            ID, LIFECYCLE_ID, OCCURRED_AT, ARCHIVER_ID)
                    .values(a.id().value(), a.lifecycleId().value(), a.occurredAt(), a.archiverId().value())
                    .execute();
        }
    }

    public long countByLifecycle(ProblemLifecycleId lifecycleId) {
        long created = countOn("problem_created_events", lifecycleId);
        long updated = countOn("problem_updated_events", lifecycleId);
        long archived = countOn("problem_archived_events", lifecycleId);
        return created + updated + archived;
    }

    private long countOn(String tableName, ProblemLifecycleId lifecycleId) {
        return dsl.selectCount().from(table(tableName))
                .where(LIFECYCLE_ID.eq(lifecycleId.value()))
                .fetchOne(0, Long.class);
    }
}
```

- [ ] **Step 5:** Run.

```bash
mvn -pl kysymys-app test -Dtest=ProblemDaoTest -q 2>&1 | tail -10
```

Expected: `Tests run: 4, Failures: 0`.

- [ ] **Step 6:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/lesson/dao/{ProblemDao,ProblemEventDao}.java \
        kysymys-app/src/test/java/net/unit8/kysymys/lesson/dao/ProblemDaoTest.java
git commit -m "Sub-B-2: ProblemDao + ProblemEventDao with tests"
```

### Task 2.3: AnswerDao + SubmissionDao + tests

**Files:**
- Create: `lesson/dao/AnswerDao.java`, `lesson/dao/SubmissionDao.java`
- Test: `lesson/dao/AnswerDaoTest.java`, `lesson/dao/SubmissionDaoTest.java`

`AnswerDao` owns `answers` (one row per `(problemId, answererId)` pair — uniqueness enforced at the dao level via "find or insert" `upsert`-style).
`SubmissionDao` owns `submissions` and `latest_submissions`. The "two submissions, one Answer, one latest" invariant from the spec lives here.

- [ ] **Step 1:** Write `AnswerDaoTest.java`:

```java
package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AnswerDaoTest {
    private static DaoTestSupport support;
    private static AnswerDao dao;

    @BeforeAll
    static void setUp() {
        support = new DaoTestSupport();
        dao = new AnswerDao(support.dsl());
    }

    @Test
    void upsertReturnsExistingIdForSameAnswerer() {
        // seed a Problem first to satisfy FK
        ProblemId pid = seedProblem();
        UserId answerer = UserId.of(IdGenerator.newId());
        AnswerRepository repo = new GitHubAnswerRepository("https://github.com/me/x");

        AnswerId first = dao.upsert(pid, answerer, repo, LocalDateTime.now());
        AnswerId again = dao.upsert(pid, answerer, repo, LocalDateTime.now().plusMinutes(1));

        assertThat(first).isEqualTo(again);
        assertThat(dao.listByAnswerer(answerer))
                .extracting(Answer::id)
                .containsExactly(first);
    }

    private static ProblemId seedProblem() {
        ProblemId pid = ProblemId.newId();
        ProblemLifecycleId lid = new ProblemLifecycleId(IdGenerator.newId());
        Problem p = new Problem(pid, new ProblemName("seed"),
                new GenericProblemRepository("https://example.com/p", "main"), lid);
        support.dsl().transaction(cfg -> {
            new ProblemDao(cfg.dsl()).insert(p,
                    new ProblemLifecycle(lid, pid, ProblemStatus.ACTIVE));
        });
        return pid;
    }
}
```

- [ ] **Step 2:** Write `SubmissionDaoTest.java`:

```java
package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SubmissionDaoTest {
    private static DaoTestSupport support;
    private static AnswerDao answers;
    private static SubmissionDao submissions;

    @BeforeAll
    static void setUp() {
        support = new DaoTestSupport();
        answers = new AnswerDao(support.dsl());
        submissions = new SubmissionDao(support.dsl());
    }

    @Test
    void twoSubmissionsKeepOneAnswerAndUpdateLatest() {
        ProblemId pid = seedProblem();
        UserId user = UserId.of(IdGenerator.newId());
        AnswerId aid = answers.upsert(pid, user,
                new GitHubAnswerRepository("https://github.com/me/x"),
                LocalDateTime.now());

        CommitHash h1 = new CommitHash("0".repeat(40));
        CommitHash h2 = new CommitHash("1".repeat(40));

        support.dsl().transaction(cfg -> {
            SubmissionDao d = new SubmissionDao(cfg.dsl());
            d.insertAndMarkLatest(new Submission(
                    new SubmissionId(IdGenerator.newId()), aid, h1, LocalDateTime.now()));
            d.insertAndMarkLatest(new Submission(
                    new SubmissionId(IdGenerator.newId()), aid, h2, LocalDateTime.now()));
        });

        assertThat(submissions.countByAnswer(aid)).isEqualTo(2L);

        Optional<Submission> latest = submissions.findLatest(aid);
        assertThat(latest).hasValueSatisfying(s -> assertThat(s.commitHash()).isEqualTo(h2));
    }

    private static ProblemId seedProblem() {
        ProblemId pid = ProblemId.newId();
        ProblemLifecycleId lid = new ProblemLifecycleId(IdGenerator.newId());
        support.dsl().transaction(cfg -> {
            new ProblemDao(cfg.dsl()).insert(
                    new Problem(pid, new ProblemName("seed"),
                            new GenericProblemRepository("https://example.com/p", "main"), lid),
                    new ProblemLifecycle(lid, pid, ProblemStatus.ACTIVE));
        });
        return pid;
    }
}
```

- [ ] **Step 3:** Run, confirm FAIL.

```bash
mvn -pl kysymys-app test -Dtest='AnswerDaoTest,SubmissionDaoTest' -q 2>&1 | tail -10
```

Expected: COMPILATION ERROR.

- [ ] **Step 4:** Implement `AnswerDao.java`:

```java
package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class AnswerDao {
    private static final Field<String> ID = field("id", String.class);
    private static final Field<String> PROBLEM_ID = field("problem_id", String.class);
    private static final Field<String> ANSWERER_ID = field("answerer_id", String.class);
    private static final Field<String> REPOSITORY_URL = field("repository_url", String.class);

    private final DSLContext dsl;

    public AnswerDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * Returns the AnswerId for {@code (problemId, answererId)}, inserting a row
     * if none exists. The legacy invariant is "one Answer per (problem, answerer)".
     * Uses two queries (find then insert) inside the caller-provided transaction.
     */
    public AnswerId upsert(ProblemId problemId, UserId answererId,
                           AnswerRepository repository, LocalDateTime when) {
        Optional<String> existing = Optional.ofNullable(
                dsl.select(ID).from(table("answers"))
                        .where(PROBLEM_ID.eq(problemId.value()))
                        .and(ANSWERER_ID.eq(answererId.value()))
                        .fetchOne(ID));
        if (existing.isPresent()) {
            return new AnswerId(existing.get());
        }
        AnswerId fresh = new AnswerId(IdGenerator.newId());
        dsl.insertInto(table("answers"), ID, PROBLEM_ID, ANSWERER_ID, REPOSITORY_URL)
                .values(fresh.value(), problemId.value(), answererId.value(), repository.url())
                .execute();
        return fresh;
    }

    public Optional<Answer> findById(AnswerId id) {
        Record rec = dsl.select(ID, PROBLEM_ID, ANSWERER_ID, REPOSITORY_URL)
                .from(table("answers"))
                .where(ID.eq(id.value()))
                .fetchOne();
        return Optional.ofNullable(rec).map(AnswerDao::mapAnswer);
    }

    public List<Answer> listByAnswerer(UserId answererId) {
        return dsl.select(ID, PROBLEM_ID, ANSWERER_ID, REPOSITORY_URL)
                .from(table("answers"))
                .where(ANSWERER_ID.eq(answererId.value()))
                .fetch(AnswerDao::mapAnswer);
    }

    private static Answer mapAnswer(Record r) {
        // For Sub-B we don't store a per-Answer "type" column — recover the
        // sealed subtype from the URL prefix, mirroring the legacy autodetection.
        String url = r.get(REPOSITORY_URL);
        AnswerRepository repo;
        if (url.startsWith("https://github.com/")) repo = new GitHubAnswerRepository(url);
        else if (url.startsWith("https://bitbucket.org/")) repo = new BitBucketAnswerRepository(url);
        else repo = new GenericAnswerRepository(url);

        // lastAnsweredAt is not persisted on the answers table in V1; we surface
        // the minimum representable timestamp here. Sub-B-4's resource layer
        // overlays the latest submission's submitted_at when serialising.
        return new Answer(
                new AnswerId(r.get(ID)),
                new ProblemId(r.get(PROBLEM_ID)),
                UserId.of(r.get(ANSWERER_ID)),
                repo,
                LocalDateTime.MIN
        );
    }
}
```

- [ ] **Step 5:** Implement `SubmissionDao.java`:

```java
package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.*;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

/**
 * Manages {@code submissions} (history) and {@code latest_submissions}
 * (a single-row pointer per Answer).
 */
public class SubmissionDao {
    private static final Field<String> ID = field("id", String.class);
    private static final Field<String> ANSWER_ID = field("answer_id", String.class);
    private static final Field<String> COMMIT_HASH = field("commit_hash", String.class);
    private static final Field<LocalDateTime> SUBMITTED_AT = field("submitted_at", LocalDateTime.class);
    private static final Field<String> SUBMISSION_ID = field("submission_id", String.class);

    private final DSLContext dsl;

    public SubmissionDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void insertAndMarkLatest(Submission s) {
        dsl.insertInto(table("submissions"), ID, ANSWER_ID, COMMIT_HASH, SUBMITTED_AT)
                .values(s.id().value(), s.answerId().value(), s.commitHash().value(), s.submittedAt())
                .execute();

        // upsert into latest_submissions (PK is answer_id alone)
        int updated = dsl.update(table("latest_submissions"))
                .set(SUBMISSION_ID, s.id().value())
                .where(ANSWER_ID.eq(s.answerId().value()))
                .execute();
        if (updated == 0) {
            dsl.insertInto(table("latest_submissions"), ANSWER_ID, SUBMISSION_ID)
                    .values(s.answerId().value(), s.id().value())
                    .execute();
        }
    }

    public long countByAnswer(AnswerId answerId) {
        return dsl.selectCount().from(table("submissions"))
                .where(ANSWER_ID.eq(answerId.value()))
                .fetchOne(0, Long.class);
    }

    public Optional<Submission> findLatest(AnswerId answerId) {
        Record rec = dsl.select(ID, ANSWER_ID, COMMIT_HASH, SUBMITTED_AT)
                .from(table("submissions"))
                .where(ID.eq(
                        dsl.select(SUBMISSION_ID).from(table("latest_submissions"))
                                .where(ANSWER_ID.eq(answerId.value()))
                ))
                .fetchOne();
        return Optional.ofNullable(rec).map(r -> new Submission(
                new SubmissionId(r.get(ID)),
                new AnswerId(r.get(ANSWER_ID)),
                new CommitHash(r.get(COMMIT_HASH)),
                r.get(SUBMITTED_AT)
        ));
    }
}
```

- [ ] **Step 6:** Run.

```bash
mvn -pl kysymys-app test -Dtest='AnswerDaoTest,SubmissionDaoTest' -q 2>&1 | tail -10
```

Expected: all tests pass.

- [ ] **Step 7:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/lesson/dao/{AnswerDao,SubmissionDao}.java \
        kysymys-app/src/test/java/net/unit8/kysymys/lesson/dao/{AnswerDaoTest,SubmissionDaoTest}.java
git commit -m "Sub-B-2: AnswerDao + SubmissionDao (latest-pointer pattern)"
```

### Task 2.4: ReviewCommentDao + tests

**Files:**
- Create: `lesson/dao/ReviewCommentDao.java`
- Test: `lesson/dao/ReviewCommentDaoTest.java`

- [ ] **Step 1:** Write `ReviewCommentDaoTest.java`:

```java
package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewCommentDaoTest {
    private static DaoTestSupport support;
    private static ReviewCommentDao comments;

    @BeforeAll
    static void setUp() {
        support = new DaoTestSupport();
        comments = new ReviewCommentDao(support.dsl());
    }

    @Test
    void insertAndListByAnswerInPostedOrder() {
        AnswerId aid = seedAnswer();
        UserId user = UserId.of(IdGenerator.newId());

        support.dsl().transaction(cfg -> {
            ReviewCommentDao d = new ReviewCommentDao(cfg.dsl());
            d.insert(new ReviewComment(
                    new CommentId(IdGenerator.newId()), aid, user,
                    new Description("first"), LocalDateTime.now()));
            d.insert(new ReviewComment(
                    new CommentId(IdGenerator.newId()), aid, user,
                    new Description("second"), LocalDateTime.now().plusSeconds(1)));
        });

        List<ReviewComment> list = comments.listByAnswer(aid);
        assertThat(list).hasSize(2);
        assertThat(list.get(0).description().value()).isEqualTo("first");
        assertThat(list.get(1).description().value()).isEqualTo("second");
    }

    private static AnswerId seedAnswer() {
        ProblemId pid = ProblemId.newId();
        ProblemLifecycleId lid = new ProblemLifecycleId(IdGenerator.newId());
        UserId user = UserId.of(IdGenerator.newId());
        AnswerId[] holder = new AnswerId[1];
        support.dsl().transaction(cfg -> {
            new ProblemDao(cfg.dsl()).insert(
                    new Problem(pid, new ProblemName("seed"),
                            new GenericProblemRepository("https://example.com/p", "main"), lid),
                    new ProblemLifecycle(lid, pid, ProblemStatus.ACTIVE));
            holder[0] = new AnswerDao(cfg.dsl()).upsert(pid, user,
                    new GenericAnswerRepository("https://example.com/me"), LocalDateTime.now());
        });
        return holder[0];
    }
}
```

- [ ] **Step 2:** Run, confirm FAIL.

```bash
mvn -pl kysymys-app test -Dtest=ReviewCommentDaoTest -q 2>&1 | tail -5
```

- [ ] **Step 3:** Implement `ReviewCommentDao.java`:

```java
package net.unit8.kysymys.lesson.dao;

import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;
import org.jooq.Field;

import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class ReviewCommentDao {
    private static final Field<String> ID = field("id", String.class);
    private static final Field<String> ANSWER_ID = field("answer_id", String.class);
    private static final Field<String> COMMENTER_ID = field("commenter_id", String.class);
    private static final Field<String> DESCRIPTION = field("description", String.class);
    private static final Field<LocalDateTime> POSTED_AT = field("posted_at", LocalDateTime.class);

    private final DSLContext dsl;

    public ReviewCommentDao(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void insert(ReviewComment c) {
        dsl.insertInto(table("review_comments"),
                        ID, ANSWER_ID, COMMENTER_ID, DESCRIPTION, POSTED_AT)
                .values(c.id().value(), c.answerId().value(), c.commenterId().value(),
                        c.description().value(), c.postedAt())
                .execute();
    }

    public List<ReviewComment> listByAnswer(AnswerId answerId) {
        return dsl.select(ID, ANSWER_ID, COMMENTER_ID, DESCRIPTION, POSTED_AT)
                .from(table("review_comments"))
                .where(ANSWER_ID.eq(answerId.value()))
                .orderBy(POSTED_AT.asc())
                .fetch(r -> new ReviewComment(
                        new CommentId(r.get(ID)),
                        new AnswerId(r.get(ANSWER_ID)),
                        UserId.of(r.get(COMMENTER_ID)),
                        new Description(r.get(DESCRIPTION)),
                        r.get(POSTED_AT)
                ));
    }
}
```

- [ ] **Step 4:** Run, confirm PASS.

```bash
mvn -pl kysymys-app test -Dtest=ReviewCommentDaoTest -q 2>&1 | tail -5
```

- [ ] **Step 5:** Run *all* lesson dao tests as a sanity check.

```bash
mvn -pl kysymys-app test -Dtest='net.unit8.kysymys.lesson.dao.*' -q 2>&1 | tail -5
```

Expected: all pass.

- [ ] **Step 6:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/lesson/dao/ReviewCommentDao.java \
        kysymys-app/src/test/java/net/unit8/kysymys/lesson/dao/ReviewCommentDaoTest.java
git commit -m "Sub-B-2: ReviewCommentDao + test"
```

### Task 2.5: Open Sub-B-2 PR

- [ ] **Step 1:** Push and open.

```bash
git push
gh pr create --base develop --head feature/sub-b-lesson \
  --title "Sub-B-2: Lesson dao layer with H2 tests" \
  --body "$(cat <<'EOF'
## Summary

- `ProblemDao` + `ProblemEventDao` (sealed `ProblemEvent` dispatch on insert)
- `AnswerDao` (find-or-insert: one Answer per `(problemId, answererId)`)
- `SubmissionDao` (history + latest-pointer upsert)
- `ReviewCommentDao` (insert + list by answer in posted order)
- `DaoTestSupport`: in-memory H2 + Flyway harness shared by all dao tests

All daos take a `DSLContext` via constructor; transactions are the caller's responsibility (behaviors in Sub-B-3..5 will set them).

## Test plan

- [x] `mvn -pl kysymys-app test -Dtest='net.unit8.kysymys.lesson.dao.*'`

Builds on Sub-B-1.
EOF
)"
```

The PR replaces the previous one if `--head` is already pushed; otherwise this opens PR #N. Wait for merge before starting Phase 3 in a new branch checked out from updated `develop`.

---

## Phase 3 (Sub-B-3): Problem CRUD behaviour + resource

### Task 3.1: UserIdInjector

**Files:**
- Create: `kysymys-app/src/main/java/net/unit8/kysymys/inject/UserIdInjector.java`

- [ ] **Step 1:** Create:

```java
package net.unit8.kysymys.inject;

import enkan.security.bouncr.UserPermissionPrincipal;
import enkan.web.data.HttpRequest;
import kotowari.inject.ParameterInjector;
import net.unit8.kysymys.user.data.UserId;

/**
 * Resolves {@code UserId} parameters on resource methods. Reads the principal
 * (set by {@code AuthenticationMiddleware}) and constructs a {@link UserId}
 * from its {@code sub} claim, exposed via {@code getName()}.
 */
public class UserIdInjector implements ParameterInjector<UserId> {
    @Override
    public String getName() {
        return "userId";
    }

    @Override
    public boolean isApplicable(Class<?> type) {
        return UserId.class.isAssignableFrom(type);
    }

    @Override
    public UserId getInjectObject(HttpRequest request) {
        if (request.getPrincipal() instanceof UserPermissionPrincipal p) {
            return UserId.of(p.getName());
        }
        if (request.getPrincipal() != null) {
            return UserId.of(request.getPrincipal().getName());
        }
        return null;
    }
}
```

- [ ] **Step 2:** Wire it into `KysymysApplicationFactory.java`. Open the file, locate the `parameterInjectors` list, add the new entry:

```java
        List<ParameterInjector<?>> parameterInjectors = List.of(
                new HttpRequestInjector(),
                new ParametersInjector(),
                new PrincipalInjector(),
                new DSLContextInjector(),
                new UserIdInjector()                 // <-- add
        );
```

Add the import:

```java
import net.unit8.kysymys.inject.UserIdInjector;
```

- [ ] **Step 3:** Compile.

```bash
mvn -pl kysymys-app -am compile -q 2>&1 | tail -3
```

Expected: BUILD SUCCESS.

- [ ] **Step 4:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/inject/UserIdInjector.java \
        kysymys-app/src/main/java/net/unit8/kysymys/KysymysApplicationFactory.java
git commit -m "Sub-B-3: UserIdInjector + register in middleware stack"
```

### Task 3.2: CreateProblem behaviour + test

**Files:**
- Create: `kysymys-app/src/main/java/net/unit8/kysymys/lesson/behavior/CreateProblem.java`
- Test: `kysymys-app/src/test/java/net/unit8/kysymys/lesson/behavior/CreateProblemTest.java`

`CreateProblem` is a function: `(ProblemRepository, ProblemName, UserId, LocalDateTime) -> Problem` and writes the (Problem, Lifecycle, CreatedEvent) trio inside a transaction. It depends on a `DSLContext` (passed at construction) and on the daos.

- [ ] **Step 1:** Write `CreateProblemTest.java`:

```java
package net.unit8.kysymys.lesson.behavior;

import net.unit8.kysymys.lesson.dao.DaoTestSupport;
import net.unit8.kysymys.lesson.dao.ProblemDao;
import net.unit8.kysymys.lesson.dao.ProblemEventDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CreateProblemTest {
    private static DaoTestSupport support;
    private static CreateProblem behavior;

    @BeforeAll
    static void setUp() {
        support = new DaoTestSupport();
        behavior = new CreateProblem(support.dsl());
    }

    @Test
    void insertsProblemLifecycleAndCreatedEvent() {
        UserId teacher = UserId.of(IdGenerator.newId());
        Problem p = behavior.apply(
                new CreateProblem.Input(
                        new ProblemName("Loop unrolling"),
                        new GitHubProblemRepository(
                                "https://github.com/k/loop", "main", "/README.md"),
                        teacher,
                        LocalDateTime.now()));

        ProblemDao problems = new ProblemDao(support.dsl());
        ProblemEventDao events = new ProblemEventDao(support.dsl());

        assertThat(problems.findById(p.id())).isPresent();
        assertThat(problems.findStatus(p.lifecycleId())).hasValue(ProblemStatus.ACTIVE);
        assertThat(events.countByLifecycle(p.lifecycleId())).isEqualTo(1L);
    }
}
```

- [ ] **Step 2:** Run, confirm FAIL.

```bash
mvn -pl kysymys-app test -Dtest=CreateProblemTest -q 2>&1 | tail -5
```

Expected: COMPILATION ERROR.

- [ ] **Step 3:** Implement `CreateProblem.java`:

```java
package net.unit8.kysymys.lesson.behavior;

import net.unit8.kysymys.lesson.dao.ProblemDao;
import net.unit8.kysymys.lesson.dao.ProblemEventDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.time.LocalDateTime;

/**
 * Atomically inserts a new Problem along with its lifecycle and a
 * {@link ProblemCreatedEvent}. Returns the persisted Problem.
 */
public class CreateProblem {
    private final DSLContext dsl;

    public CreateProblem(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Problem apply(Input input) {
        ProblemId pid = ProblemId.newId();
        ProblemLifecycleId lid = new ProblemLifecycleId(IdGenerator.newId());
        Problem problem = new Problem(pid, input.name(), input.repository(), lid);
        ProblemLifecycle lifecycle = new ProblemLifecycle(lid, pid, ProblemStatus.ACTIVE);
        ProblemCreatedEvent event = new ProblemCreatedEvent(
                new ProblemEventId(IdGenerator.newId()),
                lid,
                input.now(),
                input.creatorId());

        dsl.transaction(cfg -> {
            new ProblemDao(cfg.dsl()).insert(problem, lifecycle);
            new ProblemEventDao(cfg.dsl()).insert(event);
        });
        return problem;
    }

    public record Input(
            ProblemName name,
            ProblemRepository repository,
            UserId creatorId,
            LocalDateTime now
    ) {}
}
```

- [ ] **Step 4:** Run, confirm PASS.

```bash
mvn -pl kysymys-app test -Dtest=CreateProblemTest -q 2>&1 | tail -5
```

- [ ] **Step 5:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/lesson/behavior/CreateProblem.java \
        kysymys-app/src/test/java/net/unit8/kysymys/lesson/behavior/CreateProblemTest.java
git commit -m "Sub-B-3: CreateProblem behaviour + test"
```

### Task 3.3: UpdateProblem + ArchiveProblem + tests

**Files:**
- Create: `lesson/behavior/UpdateProblem.java`, `lesson/behavior/ArchiveProblem.java`
- Test: `lesson/behavior/UpdateProblemTest.java`, `lesson/behavior/ArchiveProblemTest.java`

- [ ] **Step 1:** Write `UpdateProblemTest.java`:

```java
package net.unit8.kysymys.lesson.behavior;

import net.unit8.kysymys.lesson.dao.DaoTestSupport;
import net.unit8.kysymys.lesson.dao.ProblemDao;
import net.unit8.kysymys.lesson.dao.ProblemEventDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateProblemTest {
    private static DaoTestSupport support;
    private static CreateProblem create;
    private static UpdateProblem update;

    @BeforeAll
    static void setUp() {
        support = new DaoTestSupport();
        create = new CreateProblem(support.dsl());
        update = new UpdateProblem(support.dsl());
    }

    @Test
    void updateRenamesAndAppendsEvent() {
        UserId teacher = UserId.of(IdGenerator.newId());
        Problem original = create.apply(new CreateProblem.Input(
                new ProblemName("Old name"),
                new GenericProblemRepository("https://example.com/x", "main"),
                teacher,
                LocalDateTime.now()));

        Optional<Problem> updated = update.apply(new UpdateProblem.Input(
                original.id(),
                new ProblemName("New name"),
                new GenericProblemRepository("https://example.com/x", "main"),
                teacher,
                LocalDateTime.now()));

        assertThat(updated).hasValueSatisfying(p ->
                assertThat(p.name().value()).isEqualTo("New name"));
        assertThat(new ProblemEventDao(support.dsl()).countByLifecycle(original.lifecycleId()))
                .isEqualTo(2L);  // created + updated
    }

    @Test
    void updateOnMissingReturnsEmpty() {
        Optional<Problem> result = update.apply(new UpdateProblem.Input(
                ProblemId.newId(),
                new ProblemName("ghost"),
                new GenericProblemRepository("https://example.com/x", "main"),
                UserId.of(IdGenerator.newId()),
                LocalDateTime.now()));
        assertThat(result).isEmpty();
    }
}
```

- [ ] **Step 2:** Write `ArchiveProblemTest.java`:

```java
package net.unit8.kysymys.lesson.behavior;

import net.unit8.kysymys.lesson.dao.DaoTestSupport;
import net.unit8.kysymys.lesson.dao.ProblemDao;
import net.unit8.kysymys.lesson.dao.ProblemEventDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ArchiveProblemTest {
    private static DaoTestSupport support;
    private static CreateProblem create;
    private static ArchiveProblem archive;

    @BeforeAll
    static void setUp() {
        support = new DaoTestSupport();
        create = new CreateProblem(support.dsl());
        archive = new ArchiveProblem(support.dsl());
    }

    @Test
    void archiveSwitchesStatusAndAppendsEvent() {
        UserId teacher = UserId.of(IdGenerator.newId());
        Problem original = create.apply(new CreateProblem.Input(
                new ProblemName("X"),
                new GenericProblemRepository("https://example.com/x", "main"),
                teacher,
                LocalDateTime.now()));

        boolean ok = archive.apply(new ArchiveProblem.Input(
                original.id(), teacher, LocalDateTime.now()));

        assertThat(ok).isTrue();
        assertThat(new ProblemDao(support.dsl()).findStatus(original.lifecycleId()))
                .hasValue(ProblemStatus.ARCHIVED);
        assertThat(new ProblemEventDao(support.dsl()).countByLifecycle(original.lifecycleId()))
                .isEqualTo(2L);  // created + archived
    }
}
```

- [ ] **Step 3:** Run, confirm FAIL.

```bash
mvn -pl kysymys-app test -Dtest='UpdateProblemTest,ArchiveProblemTest' -q 2>&1 | tail -5
```

- [ ] **Step 4:** Implement `UpdateProblem.java`:

```java
package net.unit8.kysymys.lesson.behavior;

import net.unit8.kysymys.lesson.dao.ProblemDao;
import net.unit8.kysymys.lesson.dao.ProblemEventDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.time.LocalDateTime;
import java.util.Optional;

public class UpdateProblem {
    private final DSLContext dsl;

    public UpdateProblem(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<Problem> apply(Input in) {
        ProblemDao problems = new ProblemDao(dsl);
        Optional<Problem> existing = problems.findById(in.problemId());
        if (existing.isEmpty()) return Optional.empty();

        Problem updated = new Problem(
                existing.get().id(),
                in.name(),
                in.repository(),
                existing.get().lifecycleId());
        ProblemUpdatedEvent event = new ProblemUpdatedEvent(
                new ProblemEventId(IdGenerator.newId()),
                existing.get().lifecycleId(),
                in.now(),
                in.updaterId());

        dsl.transaction(cfg -> {
            new ProblemDao(cfg.dsl()).update(updated);
            new ProblemEventDao(cfg.dsl()).insert(event);
        });
        return Optional.of(updated);
    }

    public record Input(
            ProblemId problemId,
            ProblemName name,
            ProblemRepository repository,
            UserId updaterId,
            LocalDateTime now
    ) {}
}
```

- [ ] **Step 5:** Implement `ArchiveProblem.java`:

```java
package net.unit8.kysymys.lesson.behavior;

import net.unit8.kysymys.lesson.dao.ProblemDao;
import net.unit8.kysymys.lesson.dao.ProblemEventDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.time.LocalDateTime;
import java.util.Optional;

public class ArchiveProblem {
    private final DSLContext dsl;

    public ArchiveProblem(DSLContext dsl) {
        this.dsl = dsl;
    }

    public boolean apply(Input in) {
        Optional<Problem> existing = new ProblemDao(dsl).findById(in.problemId());
        if (existing.isEmpty()) return false;

        ProblemArchivedEvent event = new ProblemArchivedEvent(
                new ProblemEventId(IdGenerator.newId()),
                existing.get().lifecycleId(),
                in.now(),
                in.archiverId());

        dsl.transaction(cfg -> {
            new ProblemDao(cfg.dsl()).updateStatus(existing.get().lifecycleId(), ProblemStatus.ARCHIVED);
            new ProblemEventDao(cfg.dsl()).insert(event);
        });
        return true;
    }

    public record Input(
            ProblemId problemId,
            UserId archiverId,
            LocalDateTime now
    ) {}
}
```

- [ ] **Step 6:** Run, confirm PASS.

```bash
mvn -pl kysymys-app test -Dtest='UpdateProblemTest,ArchiveProblemTest' -q 2>&1 | tail -5
```

- [ ] **Step 7:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/lesson/behavior/{UpdateProblem,ArchiveProblem}.java \
        kysymys-app/src/test/java/net/unit8/kysymys/lesson/behavior/{UpdateProblemTest,ArchiveProblemTest}.java
git commit -m "Sub-B-3: UpdateProblem + ArchiveProblem behaviours + tests"
```

### Task 3.4: ProblemJsonDecoders + ProblemJsonEncoders

**Files:**
- Create: `lesson/resource/ProblemJsonDecoders.java`, `lesson/resource/ProblemJsonEncoders.java`

These are pure Raoh decoders / Jackson-friendly Map producers. No tests of their own at this layer — they get exercised by the resource and the Hurl scenarios.

- [ ] **Step 1:** `ProblemJsonDecoders.java`:

```java
package net.unit8.kysymys.lesson.resource;

import net.unit8.kysymys.lesson.behavior.CreateProblem;
import net.unit8.kysymys.lesson.behavior.UpdateProblem;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.raoh.Decoder;
import net.unit8.raoh.json.JsonDecoder;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static net.unit8.raoh.json.JsonDecoders.*;

public final class ProblemJsonDecoders {
    private ProblemJsonDecoders() {}

    private static final Decoder<JsonNode, ProblemRepository> REPOSITORY = discriminate("type", Map.of(
            "github", combine(
                    field("url", string().maxLength(255)),
                    field("branch", string().maxLength(100)),
                    field("readmePath", optional(string().maxLength(100))
                            .map(o -> o.orElse("/README.md")))
            ).map(GitHubProblemRepository::new),
            "bitbucket", combine(
                    field("url", string().maxLength(255)),
                    field("branch", string().maxLength(100)),
                    field("readmePath", optional(string().maxLength(100))
                            .map(o -> o.orElse("/README.md")))
            ).map(BitBucketProblemRepository::new),
            "generic", combine(
                    field("url", string().maxLength(255)),
                    field("branch", string().maxLength(100))
            ).map(GenericProblemRepository::new)
    ));

    public static final JsonDecoder<CreateInput> CREATE = combine(
            field("name", string().minLength(1).maxLength(100)).map(ProblemName::new),
            field("repository", REPOSITORY)
    ).map(CreateInput::new)::decode;

    public static final JsonDecoder<UpdateInput> UPDATE = combine(
            field("name", string().minLength(1).maxLength(100)).map(ProblemName::new),
            field("repository", REPOSITORY)
    ).map(UpdateInput::new)::decode;

    /** Decoder output for POST /problems before behaviour input is constructed. */
    public record CreateInput(ProblemName name, ProblemRepository repository) {}

    /** Decoder output for PUT /problems/:id. */
    public record UpdateInput(ProblemName name, ProblemRepository repository) {}
}
```

> **Note on Raoh API:** the exact static-import names (`string`, `field`, `combine`, `discriminate`, `optional`, `maxLength`, etc.) follow `validation-modeling/raoh/src/main/java/com/example/raoh/joboffer/web/JobOfferJsonDecoders.java`. If a method name differs, mirror that file.

- [ ] **Step 2:** `ProblemJsonEncoders.java`:

```java
package net.unit8.kysymys.lesson.resource;

import net.unit8.kysymys.lesson.data.*;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ProblemJsonEncoders {
    private ProblemJsonEncoders() {}

    public static Map<String, Object> encode(Problem p, ProblemStatus status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", p.id().value());
        body.put("name", p.name().value());
        body.put("repository", encodeRepository(p.repository()));
        body.put("status", status.name());
        body.put("problemUrl", p.repository().problemUrl());
        return body;
    }

    private static Map<String, Object> encodeRepository(ProblemRepository repo) {
        Map<String, Object> m = new LinkedHashMap<>();
        switch (repo) {
            case GitHubProblemRepository g -> {
                m.put("type", "github");
                m.put("url", g.url());
                m.put("branch", g.branch());
                m.put("readmePath", g.readmePath());
            }
            case BitBucketProblemRepository b -> {
                m.put("type", "bitbucket");
                m.put("url", b.url());
                m.put("branch", b.branch());
                m.put("readmePath", b.readmePath());
            }
            case GenericProblemRepository g -> {
                m.put("type", "generic");
                m.put("url", g.url());
                m.put("branch", g.branch());
            }
        }
        return m;
    }
}
```

- [ ] **Step 3:** Compile.

```bash
mvn -pl kysymys-app -am compile -q 2>&1 | tail -3
```

Expected: BUILD SUCCESS. (If a Raoh static method name differs from the assumed one, fix here by inspecting `validation-modeling`'s `JobOfferJsonDecoders.java`.)

- [ ] **Step 4:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/lesson/resource/{ProblemJsonDecoders,ProblemJsonEncoders}.java
git commit -m "Sub-B-3: ProblemJsonDecoders + ProblemJsonEncoders"
```

### Task 3.5: ProblemsResource (POST /problems, GET /problems)

**Files:**
- Create: `lesson/resource/ProblemsResource.java`

- [ ] **Step 1:** Create:

```java
package net.unit8.kysymys.lesson.resource;

import kotowari.restful.Decision;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.lesson.behavior.CreateProblem;
import net.unit8.kysymys.lesson.dao.ProblemDao;
import net.unit8.kysymys.lesson.data.IdGenerator;
import net.unit8.kysymys.lesson.data.ProblemStatus;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.raoh.Err;
import net.unit8.raoh.Issue;
import net.unit8.raoh.Ok;
import org.jooq.DSLContext;
import tools.jackson.databind.JsonNode;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static kotowari.restful.DecisionPoint.*;

@AllowedMethods({"GET", "POST"})
public class ProblemsResource {

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal) {
        return principal != null;
    }

    @Decision(value = MALFORMED, method = {"POST"})
    public Problem validatePost(JsonNode body, RestContext context) {
        return switch (ProblemJsonDecoders.CREATE.decode(body)) {
            case Ok<ProblemJsonDecoders.CreateInput> ok -> {
                context.putValue(ok.value());
                yield null;
            }
            case Err<ProblemJsonDecoders.CreateInput> err -> {
                List<Problem.Violation> violations = err.issues().asList().stream()
                        .map(ProblemsResource::toViolation)
                        .toList();
                yield Problem.fromViolationList(violations);
            }
        };
    }

    @Decision(POST)
    public boolean create(ProblemJsonDecoders.CreateInput input,
                          DSLContext dsl, UserId caller, RestContext context) {
        net.unit8.kysymys.lesson.data.Problem created = new CreateProblem(dsl).apply(
                new CreateProblem.Input(input.name(), input.repository(), caller, LocalDateTime.now()));
        context.putValue(created);
        return true;
    }

    @Decision(HANDLE_CREATED)
    public Map<String, Object> handleCreated(net.unit8.kysymys.lesson.data.Problem problem) {
        return ProblemJsonEncoders.encode(problem, ProblemStatus.ACTIVE);
    }

    @Decision(HANDLE_OK)
    public List<Map<String, Object>> list(DSLContext dsl) {
        ProblemDao dao = new ProblemDao(dsl);
        return dao.listActive().stream()
                .map(p -> ProblemJsonEncoders.encode(p, ProblemStatus.ACTIVE))
                .toList();
    }

    private static Problem.Violation toViolation(Issue issue) {
        return new Problem.Violation(
                issue.path().toString(), issue.code(), issue.message());
    }
}
```

> **Cross-check:** the helper `Problem.Violation`, `Problem.fromViolationList`, `Ok` / `Err` types, and the `RestContext.putValue` API match `kotowari-restful/example/.../resource/CustomersResource.java`. The `CreateInput` is stored on `RestContext` and re-injected to `create(...)` as a typed parameter (kotowari-restful's body-on-context pattern).

- [ ] **Step 2:** Compile.

```bash
mvn -pl kysymys-app -am compile -q 2>&1 | tail -3
```

Expected: BUILD SUCCESS.

- [ ] **Step 3:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/lesson/resource/ProblemsResource.java
git commit -m "Sub-B-3: ProblemsResource (POST + GET /problems)"
```

### Task 3.6: ProblemResource (GET/PUT/DELETE /problems/:id)

**Files:**
- Create: `lesson/resource/ProblemResource.java`

- [ ] **Step 1:** Create:

```java
package net.unit8.kysymys.lesson.resource;

import enkan.collection.Parameters;
import kotowari.restful.Decision;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.lesson.behavior.ArchiveProblem;
import net.unit8.kysymys.lesson.behavior.UpdateProblem;
import net.unit8.kysymys.lesson.dao.ProblemDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.raoh.Err;
import net.unit8.raoh.Issue;
import net.unit8.raoh.Ok;
import org.jooq.DSLContext;
import tools.jackson.databind.JsonNode;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static kotowari.restful.DecisionPoint.*;

@AllowedMethods({"GET", "PUT", "DELETE"})
public class ProblemResource {

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal) {
        return principal != null;
    }

    @Decision(EXISTS)
    public boolean exists(Parameters params, DSLContext dsl, RestContext context) {
        ProblemId id;
        try {
            id = new ProblemId(params.get("id"));
        } catch (IllegalArgumentException ex) {
            return false;
        }
        Optional<net.unit8.kysymys.lesson.data.Problem> p = new ProblemDao(dsl).findById(id);
        if (p.isEmpty()) return false;
        ProblemStatus status = new ProblemDao(dsl).findStatus(p.get().lifecycleId())
                .orElse(ProblemStatus.ACTIVE);
        context.putValue(p.get());
        context.putValue(status);
        return true;
    }

    @Decision(value = MALFORMED, method = {"PUT"})
    public Problem validatePut(JsonNode body, RestContext context) {
        return switch (ProblemJsonDecoders.UPDATE.decode(body)) {
            case Ok<ProblemJsonDecoders.UpdateInput> ok -> {
                context.putValue(ok.value());
                yield null;
            }
            case Err<ProblemJsonDecoders.UpdateInput> err -> Problem.fromViolationList(
                    err.issues().asList().stream()
                            .map(ProblemResource::toViolation).toList());
        };
    }

    @Decision(PUT)
    public boolean update(ProblemJsonDecoders.UpdateInput input,
                          net.unit8.kysymys.lesson.data.Problem existing,
                          DSLContext dsl, UserId caller, RestContext context) {
        Optional<net.unit8.kysymys.lesson.data.Problem> updated = new UpdateProblem(dsl).apply(
                new UpdateProblem.Input(existing.id(), input.name(), input.repository(),
                        caller, LocalDateTime.now()));
        updated.ifPresent(context::putValue);
        return updated.isPresent();
    }

    @Decision(DELETE)
    public boolean delete(net.unit8.kysymys.lesson.data.Problem existing,
                          DSLContext dsl, UserId caller, RestContext context) {
        boolean ok = new ArchiveProblem(dsl).apply(
                new ArchiveProblem.Input(existing.id(), caller, LocalDateTime.now()));
        if (ok) {
            // refresh status for HANDLE_OK
            context.putValue(ProblemStatus.ARCHIVED);
        }
        return ok;
    }

    @Decision(NEW)
    public boolean isNew() { return false; }   // PUT/DELETE return 200, not 201

    @Decision(RESPOND_WITH_ENTITY)
    public boolean respondWithEntity() { return true; }

    @Decision(HANDLE_OK)
    public Map<String, Object> show(net.unit8.kysymys.lesson.data.Problem problem,
                                    ProblemStatus status) {
        return ProblemJsonEncoders.encode(problem, status);
    }

    private static Problem.Violation toViolation(Issue issue) {
        return new Problem.Violation(issue.path().toString(), issue.code(), issue.message());
    }
}
```

- [ ] **Step 2:** Compile.

```bash
mvn -pl kysymys-app -am compile -q 2>&1 | tail -3
```

Expected: BUILD SUCCESS.

- [ ] **Step 3:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/lesson/resource/ProblemResource.java
git commit -m "Sub-B-3: ProblemResource (GET/PUT/DELETE /problems/:id)"
```

### Task 3.7: Routes wiring + Hurl smoke

**Files:**
- Modify: `KysymysApplicationFactory.java` (add 5 routes)
- Create: `kysymys-app/src/test/hurl/lesson.hurl`

- [ ] **Step 1:** Open `KysymysApplicationFactory.java`, find the `Routes.define` block, replace with:

```java
        Routes routes = Routes.define(r -> {
            r.get("/health").to(HealthResource.class);
            r.get("/me").to(MeResource.class);

            // Lesson — Problem
            r.get("/problems").to(ProblemsResource.class);
            r.post("/problems").to(ProblemsResource.class);
            r.get("/problems/:id").to(ProblemResource.class);
            r.put("/problems/:id").to(ProblemResource.class);
            r.delete("/problems/:id").to(ProblemResource.class);
        }).compile();
```

Add the imports:

```java
import net.unit8.kysymys.lesson.resource.ProblemResource;
import net.unit8.kysymys.lesson.resource.ProblemsResource;
```

- [ ] **Step 2:** Create `kysymys-app/src/test/hurl/lesson.hurl` (initial scenarios — ones not depending on Answer/Comment):

```hurl
# Sub-B-3 portion: scenarios 1, 2, 3, 10, 11
# Reuses the Sub-A test JWT signed with KYSYMYS_JWT_SECRET=kysymys-dev-jwt-secret-not-for-production

POST {{host}}/problems
x-bouncr-credential: {{token}}
Content-Type: application/json
{
  "name": "FizzBuzz",
  "repository": {
    "type": "github",
    "url": "https://github.com/example/fizzbuzz",
    "branch": "main"
  }
}
HTTP 201
[Captures]
problem_id: jsonpath "$.id"

GET {{host}}/problems
x-bouncr-credential: {{token}}
Accept: application/json
HTTP 200
[Asserts]
jsonpath "$" count >= 1

GET {{host}}/problems/{{problem_id}}
x-bouncr-credential: {{token}}
Accept: application/json
HTTP 200
[Asserts]
jsonpath "$.name" == "FizzBuzz"
jsonpath "$.status" == "ACTIVE"

# scenario 10: rename
PUT {{host}}/problems/{{problem_id}}
x-bouncr-credential: {{token}}
Content-Type: application/json
{
  "name": "FizzBuzz Renamed",
  "repository": {
    "type": "github",
    "url": "https://github.com/example/fizzbuzz",
    "branch": "main"
  }
}
HTTP 200
[Asserts]
jsonpath "$.name" == "FizzBuzz Renamed"

# scenario 11: archive
DELETE {{host}}/problems/{{problem_id}}
x-bouncr-credential: {{token}}
HTTP 200
[Asserts]
jsonpath "$.status" == "ARCHIVED"
```

- [ ] **Step 3:** Manual smoke test.

```bash
# Start dev server in background
cd kysymys-app && mvn exec:java &
sleep 5

# Generate JWT (Sub-A's GenToken)
TOKEN=$(java /tmp/GenToken.java)

# Run hurl
hurl --variable host=http://localhost:3000 --variable token="$TOKEN" \
  src/test/hurl/lesson.hurl
```

Expected: all 5 scenarios pass.

Stop the server (`fg` then Ctrl-C, or `pkill -f exec:java`).

- [ ] **Step 4:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/KysymysApplicationFactory.java \
        kysymys-app/src/test/hurl/lesson.hurl
git commit -m "Sub-B-3: wire Problem routes + Hurl smoke (CRUD)"
```

### Task 3.8: Open Sub-B-3 PR

- [ ] **Step 1:**

```bash
git push
gh pr create --base develop --head feature/sub-b-lesson \
  --title "Sub-B-3: Problem CRUD on the new stack" \
  --body "$(cat <<'EOF'
## Summary

- `UserIdInjector` registered in `KysymysApplicationFactory.parameterInjectors`
- Behaviours: `CreateProblem`, `UpdateProblem`, `ArchiveProblem` (each opens its own transaction; persist Problem + Lifecycle + matching ProblemEvent)
- `ProblemJsonDecoders` (Raoh `discriminate("type", ...)` for the sealed `ProblemRepository` hierarchy) + `ProblemJsonEncoders`
- `ProblemsResource` (POST + GET /problems) and `ProblemResource` (GET/PUT/DELETE /problems/:id)
- Hurl scenarios 1–3, 10, 11 from spec

## Test plan

- [x] Behaviour tests (`mvn -pl kysymys-app test -Dtest='net.unit8.kysymys.lesson.behavior.*'`)
- [x] Hurl: create → list → get → update → archive on dev server

Builds on Sub-B-2.
EOF
)"
```

---

## Phase 4 (Sub-B-4): Answer + Submission

### Task 4.1: SubmitAnswer behaviour + test

**Files:**
- Create: `lesson/behavior/SubmitAnswer.java`
- Test: `lesson/behavior/SubmitAnswerTest.java`

- [ ] **Step 1:** Write `SubmitAnswerTest.java`:

```java
package net.unit8.kysymys.lesson.behavior;

import net.unit8.kysymys.lesson.dao.AnswerDao;
import net.unit8.kysymys.lesson.dao.DaoTestSupport;
import net.unit8.kysymys.lesson.dao.SubmissionDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SubmitAnswerTest {
    private static DaoTestSupport support;
    private static CreateProblem create;
    private static SubmitAnswer submit;

    @BeforeAll
    static void setUp() {
        support = new DaoTestSupport();
        create = new CreateProblem(support.dsl());
        submit = new SubmitAnswer(support.dsl());
    }

    @Test
    void twoSubmitsKeepOneAnswerAndAdvanceLatest() {
        UserId teacher = UserId.of(IdGenerator.newId());
        UserId student = UserId.of(IdGenerator.newId());
        Problem p = create.apply(new CreateProblem.Input(
                new ProblemName("X"),
                new GenericProblemRepository("https://example.com/x", "main"),
                teacher, LocalDateTime.now()));

        AnswerRepository repo = new GitHubAnswerRepository("https://github.com/me/x");
        CommitHash h1 = new CommitHash("0".repeat(40));
        CommitHash h2 = new CommitHash("1".repeat(40));

        Optional<SubmitAnswer.Output> first = submit.apply(new SubmitAnswer.Input(
                p.id(), student, repo, h1, LocalDateTime.now()));
        Optional<SubmitAnswer.Output> second = submit.apply(new SubmitAnswer.Input(
                p.id(), student, repo, h2, LocalDateTime.now().plusMinutes(1)));

        assertThat(first).isPresent();
        assertThat(second).isPresent();
        assertThat(first.get().answer().id()).isEqualTo(second.get().answer().id());

        SubmissionDao subDao = new SubmissionDao(support.dsl());
        assertThat(subDao.countByAnswer(first.get().answer().id())).isEqualTo(2L);
        assertThat(subDao.findLatest(first.get().answer().id()))
                .hasValueSatisfying(s -> assertThat(s.commitHash()).isEqualTo(h2));
    }

    @Test
    void submitOnUnknownProblemReturnsEmpty() {
        Optional<SubmitAnswer.Output> result = submit.apply(new SubmitAnswer.Input(
                ProblemId.newId(),
                UserId.of(IdGenerator.newId()),
                new GenericAnswerRepository("https://example.com/me"),
                new CommitHash("a".repeat(40)),
                LocalDateTime.now()));
        assertThat(result).isEmpty();
    }
}
```

- [ ] **Step 2:** Run, confirm FAIL.

```bash
mvn -pl kysymys-app test -Dtest=SubmitAnswerTest -q 2>&1 | tail -5
```

- [ ] **Step 3:** Implement `SubmitAnswer.java`:

```java
package net.unit8.kysymys.lesson.behavior;

import net.unit8.kysymys.lesson.dao.AnswerDao;
import net.unit8.kysymys.lesson.dao.ProblemDao;
import net.unit8.kysymys.lesson.dao.SubmissionDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.time.LocalDateTime;
import java.util.Optional;

public class SubmitAnswer {
    private final DSLContext dsl;

    public SubmitAnswer(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<Output> apply(Input in) {
        // 1. Confirm Problem exists (outside the writing transaction so an unknown
        //    problemId returns empty without leaving a partial answer).
        if (new ProblemDao(dsl).findById(in.problemId()).isEmpty()) {
            return Optional.empty();
        }

        // 2. Atomic write: ensure Answer row, append Submission, advance latest pointer.
        Output[] holder = new Output[1];
        dsl.transaction(cfg -> {
            DSLContext tx = cfg.dsl();
            AnswerId aid = new AnswerDao(tx).upsert(
                    in.problemId(), in.answererId(), in.repository(), in.now());
            Submission submission = new Submission(
                    new SubmissionId(IdGenerator.newId()), aid, in.commitHash(), in.now());
            new SubmissionDao(tx).insertAndMarkLatest(submission);

            Answer ans = new Answer(
                    aid, in.problemId(), in.answererId(), in.repository(), in.now());
            holder[0] = new Output(ans, submission);
        });
        return Optional.of(holder[0]);
    }

    public record Input(
            ProblemId problemId,
            UserId answererId,
            AnswerRepository repository,
            CommitHash commitHash,
            LocalDateTime now
    ) {}

    public record Output(Answer answer, Submission submission) {}
}
```

- [ ] **Step 4:** Run, confirm PASS.

```bash
mvn -pl kysymys-app test -Dtest=SubmitAnswerTest -q 2>&1 | tail -5
```

- [ ] **Step 5:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/lesson/behavior/SubmitAnswer.java \
        kysymys-app/src/test/java/net/unit8/kysymys/lesson/behavior/SubmitAnswerTest.java
git commit -m "Sub-B-4: SubmitAnswer behaviour + test"
```

### Task 4.2: AnswerJsonDecoders + AnswerJsonEncoders

**Files:**
- Create: `lesson/resource/AnswerJsonDecoders.java`, `lesson/resource/AnswerJsonEncoders.java`

- [ ] **Step 1:** `AnswerJsonDecoders.java`:

```java
package net.unit8.kysymys.lesson.resource;

import net.unit8.kysymys.lesson.data.*;
import net.unit8.raoh.Decoder;
import net.unit8.raoh.json.JsonDecoder;
import tools.jackson.databind.JsonNode;

import java.util.Map;

import static net.unit8.raoh.json.JsonDecoders.*;

public final class AnswerJsonDecoders {
    private AnswerJsonDecoders() {}

    private static final Decoder<JsonNode, AnswerRepository> REPOSITORY = discriminate("type", Map.of(
            "github",    field("url", string().maxLength(255)).map(GitHubAnswerRepository::new),
            "bitbucket", field("url", string().maxLength(255)).map(BitBucketAnswerRepository::new),
            "generic",   field("url", string().maxLength(255)).map(GenericAnswerRepository::new)
    ));

    public static final JsonDecoder<SubmitInput> SUBMIT = combine(
            field("repository", REPOSITORY),
            field("commitHash", string().pattern("^[0-9a-fA-F]{40}$")).map(CommitHash::new)
    ).map(SubmitInput::new)::decode;

    public record SubmitInput(AnswerRepository repository, CommitHash commitHash) {}
}
```

- [ ] **Step 2:** `AnswerJsonEncoders.java` (without comments yet — Sub-B-5 extends):

```java
package net.unit8.kysymys.lesson.resource;

import net.unit8.kysymys.lesson.data.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class AnswerJsonEncoders {
    private AnswerJsonEncoders() {}

    public static Map<String, Object> encode(Answer a, Optional<Submission> latest) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", a.id().value());
        body.put("problemId", a.problemId().value());
        body.put("answererId", a.answererId().value());
        body.put("repository", encodeRepository(a.repository()));
        latest.ifPresent(s -> {
            body.put("latestCommitHash", s.commitHash().value());
            body.put("latestSubmittedAt", s.submittedAt().toString());
            body.put("answerUrl", a.repository().commitUrl(s.commitHash()));
        });
        return body;
    }

    private static Map<String, Object> encodeRepository(AnswerRepository repo) {
        Map<String, Object> m = new LinkedHashMap<>();
        switch (repo) {
            case GitHubAnswerRepository g -> { m.put("type", "github"); m.put("url", g.url()); }
            case BitBucketAnswerRepository b -> { m.put("type", "bitbucket"); m.put("url", b.url()); }
            case GenericAnswerRepository g -> { m.put("type", "generic"); m.put("url", g.url()); }
        }
        return m;
    }
}
```

- [ ] **Step 3:** Compile.

```bash
mvn -pl kysymys-app -am compile -q 2>&1 | tail -3
```

- [ ] **Step 4:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/lesson/resource/{AnswerJsonDecoders,AnswerJsonEncoders}.java
git commit -m "Sub-B-4: AnswerJsonDecoders + AnswerJsonEncoders (no comments yet)"
```

### Task 4.3: AnswersResource + AnswerResource + MyAnswersResource

**Files:**
- Create:
  - `lesson/resource/AnswersResource.java` (POST /problems/:id/answers)
  - `lesson/resource/AnswerResource.java` (GET /answers/:id)
  - `lesson/resource/MyAnswersResource.java` (GET /answers)

- [ ] **Step 1:** `AnswersResource.java`:

```java
package net.unit8.kysymys.lesson.resource;

import enkan.collection.Parameters;
import kotowari.restful.Decision;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.lesson.behavior.SubmitAnswer;
import net.unit8.kysymys.lesson.data.ProblemId;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.raoh.Err;
import net.unit8.raoh.Issue;
import net.unit8.raoh.Ok;
import org.jooq.DSLContext;
import tools.jackson.databind.JsonNode;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static kotowari.restful.DecisionPoint.*;

@AllowedMethods({"POST"})
public class AnswersResource {

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal) { return principal != null; }

    @Decision(value = MALFORMED, method = {"POST"})
    public Problem validate(JsonNode body, RestContext context) {
        return switch (AnswerJsonDecoders.SUBMIT.decode(body)) {
            case Ok<AnswerJsonDecoders.SubmitInput> ok -> {
                context.putValue(ok.value());
                yield null;
            }
            case Err<AnswerJsonDecoders.SubmitInput> err -> Problem.fromViolationList(
                    err.issues().asList().stream().map(AnswersResource::v).toList());
        };
    }

    @Decision(POST)
    public boolean submit(AnswerJsonDecoders.SubmitInput input,
                          Parameters params, DSLContext dsl, UserId caller, RestContext context) {
        ProblemId pid;
        try { pid = new ProblemId(params.get("id")); }
        catch (IllegalArgumentException ex) { return false; }

        Optional<SubmitAnswer.Output> out = new SubmitAnswer(dsl).apply(
                new SubmitAnswer.Input(
                        pid, caller, input.repository(), input.commitHash(),
                        LocalDateTime.now()));
        if (out.isEmpty()) {
            // Problem doesn't exist -> 422 Unprocessable; encoded as 400 here for simplicity.
            return false;
        }
        context.putValue(out.get());
        return true;
    }

    @Decision(HANDLE_CREATED)
    public Map<String, Object> handleCreated(SubmitAnswer.Output out) {
        return AnswerJsonEncoders.encode(out.answer(), Optional.of(out.submission()));
    }

    private static Problem.Violation v(Issue i) {
        return new Problem.Violation(i.path().toString(), i.code(), i.message());
    }
}
```

- [ ] **Step 2:** `AnswerResource.java`:

```java
package net.unit8.kysymys.lesson.resource;

import enkan.collection.Parameters;
import kotowari.restful.Decision;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.lesson.dao.AnswerDao;
import net.unit8.kysymys.lesson.dao.SubmissionDao;
import net.unit8.kysymys.lesson.data.*;
import org.jooq.DSLContext;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import static kotowari.restful.DecisionPoint.*;

@AllowedMethods({"GET"})
public class AnswerResource {

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal) { return principal != null; }

    @Decision(EXISTS)
    public boolean exists(Parameters params, DSLContext dsl, RestContext context) {
        AnswerId id;
        try { id = new AnswerId(params.get("id")); }
        catch (IllegalArgumentException ex) { return false; }
        Optional<Answer> a = new AnswerDao(dsl).findById(id);
        if (a.isEmpty()) return false;
        Optional<Submission> latest = new SubmissionDao(dsl).findLatest(id);
        context.putValue(a.get());
        latest.ifPresent(context::putValue);
        return true;
    }

    @Decision(HANDLE_OK)
    public Map<String, Object> show(Answer a, RestContext context) {
        Optional<Submission> latest = Optional.ofNullable(context.getValue(Submission.class));
        return AnswerJsonEncoders.encode(a, latest);
    }
}
```

- [ ] **Step 3:** `MyAnswersResource.java`:

```java
package net.unit8.kysymys.lesson.resource;

import kotowari.restful.Decision;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.lesson.dao.AnswerDao;
import net.unit8.kysymys.lesson.dao.SubmissionDao;
import net.unit8.kysymys.lesson.data.Answer;
import net.unit8.kysymys.lesson.data.Submission;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static kotowari.restful.DecisionPoint.AUTHORIZED;
import static kotowari.restful.DecisionPoint.HANDLE_OK;

@AllowedMethods({"GET"})
public class MyAnswersResource {

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal) { return principal != null; }

    @Decision(HANDLE_OK)
    public List<Map<String, Object>> mine(UserId caller, DSLContext dsl) {
        AnswerDao answers = new AnswerDao(dsl);
        SubmissionDao submissions = new SubmissionDao(dsl);
        return answers.listByAnswerer(caller).stream()
                .map(a -> AnswerJsonEncoders.encode(a, submissions.findLatest(a.id())))
                .toList();
    }
}
```

- [ ] **Step 4:** Compile.

```bash
mvn -pl kysymys-app -am compile -q 2>&1 | tail -3
```

- [ ] **Step 5:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/lesson/resource/{AnswersResource,AnswerResource,MyAnswersResource}.java
git commit -m "Sub-B-4: Answer resources (POST submit, GET show, GET mine)"
```

### Task 4.4: Wire Answer routes + extend Hurl

**Files:**
- Modify: `KysymysApplicationFactory.java`
- Modify: `kysymys-app/src/test/hurl/lesson.hurl`

- [ ] **Step 1:** In `KysymysApplicationFactory.java` extend the `Routes.define` block:

```java
            // Lesson — Answer
            r.post("/problems/:id/answers").to(AnswersResource.class);
            r.get("/answers").to(MyAnswersResource.class);
            r.get("/answers/:id").to(AnswerResource.class);
```

Add imports:

```java
import net.unit8.kysymys.lesson.resource.AnswerResource;
import net.unit8.kysymys.lesson.resource.AnswersResource;
import net.unit8.kysymys.lesson.resource.MyAnswersResource;
```

- [ ] **Step 2:** Append to `lesson.hurl` (between scenario 3 GET problem and scenario 10 PUT):

```hurl
# scenario 4: submit first answer
POST {{host}}/problems/{{problem_id}}/answers
x-bouncr-credential: {{token}}
Content-Type: application/json
{
  "repository": { "type": "github", "url": "https://github.com/student/fizzbuzz-mine" },
  "commitHash": "0123456789012345678901234567890123456789"
}
HTTP 201
[Captures]
answer_id: jsonpath "$.id"

# scenario 5: submit second commit on same answer
POST {{host}}/problems/{{problem_id}}/answers
x-bouncr-credential: {{token}}
Content-Type: application/json
{
  "repository": { "type": "github", "url": "https://github.com/student/fizzbuzz-mine" },
  "commitHash": "1111111111111111111111111111111111111111"
}
HTTP 201
[Asserts]
jsonpath "$.id" == "{{answer_id}}"
jsonpath "$.latestCommitHash" == "1111111111111111111111111111111111111111"

# scenario 6: my answers list
GET {{host}}/answers
x-bouncr-credential: {{token}}
Accept: application/json
HTTP 200
[Asserts]
jsonpath "$" count == 1
jsonpath "$[0].id" == "{{answer_id}}"
jsonpath "$[0].latestCommitHash" == "1111111111111111111111111111111111111111"

# scenario 7: show answer (no comments yet)
GET {{host}}/answers/{{answer_id}}
x-bouncr-credential: {{token}}
Accept: application/json
HTTP 200
[Asserts]
jsonpath "$.latestCommitHash" == "1111111111111111111111111111111111111111"
```

(Make sure scenarios 4-7 sit *between* the create/list/get of Phase 3 and the PUT/DELETE of Phase 3 — re-arrange as needed so that PUT/DELETE happen *after* answer scenarios; the spec ordering is 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11.)

- [ ] **Step 3:** Smoke test.

```bash
cd kysymys-app && pkill -f exec:java; mvn exec:java &
sleep 5
TOKEN=$(java /tmp/GenToken.java)
hurl --variable host=http://localhost:3000 --variable token="$TOKEN" \
  src/test/hurl/lesson.hurl
pkill -f exec:java
```

Expected: all 9 scenarios run so far pass (10 + 11 still expected, 8 + 9 deferred to Sub-B-5).

- [ ] **Step 4:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/KysymysApplicationFactory.java \
        kysymys-app/src/test/hurl/lesson.hurl
git commit -m "Sub-B-4: wire Answer routes + Hurl scenarios 4-7"
```

### Task 4.5: Open Sub-B-4 PR

```bash
git push
gh pr create --base develop --head feature/sub-b-lesson \
  --title "Sub-B-4: Answer + Submission flow" \
  --body "Behaviour + resources for POST /problems/:id/answers, GET /answers, GET /answers/:id. Latest pointer flips on subsequent submissions; one Answer per (problem, answerer)."
```

---

## Phase 5 (Sub-B-5): ReviewComment + Answer-with-comments + Hurl finale

### Task 5.1: PostComment behaviour + test

**Files:**
- Create: `lesson/behavior/PostComment.java`
- Test: `lesson/behavior/PostCommentTest.java`

- [ ] **Step 1:** `PostCommentTest.java`:

```java
package net.unit8.kysymys.lesson.behavior;

import net.unit8.kysymys.lesson.dao.AnswerDao;
import net.unit8.kysymys.lesson.dao.DaoTestSupport;
import net.unit8.kysymys.lesson.dao.ReviewCommentDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PostCommentTest {
    private static DaoTestSupport support;
    private static CreateProblem create;
    private static SubmitAnswer submit;
    private static PostComment post;

    @BeforeAll
    static void setUp() {
        support = new DaoTestSupport();
        create = new CreateProblem(support.dsl());
        submit = new SubmitAnswer(support.dsl());
        post = new PostComment(support.dsl());
    }

    @Test
    void postsCommentForExistingAnswer() {
        UserId teacher = UserId.of(IdGenerator.newId());
        UserId student = UserId.of(IdGenerator.newId());
        Problem p = create.apply(new CreateProblem.Input(
                new ProblemName("X"),
                new GenericProblemRepository("https://example.com/x", "main"),
                teacher, LocalDateTime.now()));
        SubmitAnswer.Output answer = submit.apply(new SubmitAnswer.Input(
                p.id(), student,
                new GenericAnswerRepository("https://example.com/me"),
                new CommitHash("0".repeat(40)),
                LocalDateTime.now())).orElseThrow();

        Optional<ReviewComment> c = post.apply(new PostComment.Input(
                answer.answer().id(), teacher,
                new Description("looks good"),
                LocalDateTime.now()));

        assertThat(c).isPresent();
        assertThat(new ReviewCommentDao(support.dsl())
                .listByAnswer(answer.answer().id())).hasSize(1);
    }

    @Test
    void postingOnUnknownAnswerReturnsEmpty() {
        Optional<ReviewComment> c = post.apply(new PostComment.Input(
                AnswerId.newId(),
                UserId.of(IdGenerator.newId()),
                new Description("ghost"),
                LocalDateTime.now()));
        assertThat(c).isEmpty();
    }
}
```

(Add `AnswerId.newId()` only if it isn't already present — see Task 1.5: yes, `AnswerId` follows the same pattern as `ProblemId`, so `newId()` exists.)

- [ ] **Step 2:** Run, confirm FAIL.

```bash
mvn -pl kysymys-app test -Dtest=PostCommentTest -q 2>&1 | tail -5
```

- [ ] **Step 3:** Implement `PostComment.java`:

```java
package net.unit8.kysymys.lesson.behavior;

import net.unit8.kysymys.lesson.dao.AnswerDao;
import net.unit8.kysymys.lesson.dao.ReviewCommentDao;
import net.unit8.kysymys.lesson.data.*;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

import java.time.LocalDateTime;
import java.util.Optional;

public class PostComment {
    private final DSLContext dsl;

    public PostComment(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<ReviewComment> apply(Input in) {
        if (new AnswerDao(dsl).findById(in.answerId()).isEmpty()) {
            return Optional.empty();
        }
        ReviewComment c = new ReviewComment(
                new CommentId(IdGenerator.newId()),
                in.answerId(),
                in.commenterId(),
                in.description(),
                in.now());
        dsl.transaction(cfg -> new ReviewCommentDao(cfg.dsl()).insert(c));
        return Optional.of(c);
    }

    public record Input(
            AnswerId answerId,
            UserId commenterId,
            Description description,
            LocalDateTime now
    ) {}
}
```

- [ ] **Step 4:** Run, confirm PASS.

```bash
mvn -pl kysymys-app test -Dtest=PostCommentTest -q 2>&1 | tail -5
```

- [ ] **Step 5:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/lesson/behavior/PostComment.java \
        kysymys-app/src/test/java/net/unit8/kysymys/lesson/behavior/PostCommentTest.java
git commit -m "Sub-B-5: PostComment behaviour + test"
```

### Task 5.2: CommentJsonDecoders + CommentsResource

**Files:**
- Create: `lesson/resource/CommentJsonDecoders.java`, `lesson/resource/CommentsResource.java`

- [ ] **Step 1:** `CommentJsonDecoders.java`:

```java
package net.unit8.kysymys.lesson.resource;

import net.unit8.kysymys.lesson.data.Description;
import net.unit8.raoh.json.JsonDecoder;

import static net.unit8.raoh.json.JsonDecoders.*;

public final class CommentJsonDecoders {
    private CommentJsonDecoders() {}

    public static final JsonDecoder<PostInput> POST = combine(
            field("description", string().minLength(1).maxLength(4000)).map(Description::new)
    ).map(PostInput::new)::decode;

    public record PostInput(Description description) {}
}
```

- [ ] **Step 2:** `CommentsResource.java`:

```java
package net.unit8.kysymys.lesson.resource;

import enkan.collection.Parameters;
import kotowari.restful.Decision;
import kotowari.restful.data.Problem;
import kotowari.restful.data.RestContext;
import kotowari.restful.resource.AllowedMethods;
import net.unit8.kysymys.lesson.behavior.PostComment;
import net.unit8.kysymys.lesson.data.AnswerId;
import net.unit8.kysymys.lesson.data.ReviewComment;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.raoh.Err;
import net.unit8.raoh.Issue;
import net.unit8.raoh.Ok;
import org.jooq.DSLContext;
import tools.jackson.databind.JsonNode;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static kotowari.restful.DecisionPoint.*;

@AllowedMethods({"POST"})
public class CommentsResource {

    @Decision(AUTHORIZED)
    public boolean authorized(Principal principal) { return principal != null; }

    @Decision(value = MALFORMED, method = {"POST"})
    public Problem validate(JsonNode body, RestContext context) {
        return switch (CommentJsonDecoders.POST.decode(body)) {
            case Ok<CommentJsonDecoders.PostInput> ok -> {
                context.putValue(ok.value());
                yield null;
            }
            case Err<CommentJsonDecoders.PostInput> err -> Problem.fromViolationList(
                    err.issues().asList().stream().map(CommentsResource::v).toList());
        };
    }

    @Decision(POST)
    public boolean create(CommentJsonDecoders.PostInput input,
                          Parameters params, DSLContext dsl, UserId caller, RestContext context) {
        AnswerId aid;
        try { aid = new AnswerId(params.get("id")); }
        catch (IllegalArgumentException ex) { return false; }

        Optional<ReviewComment> created = new PostComment(dsl).apply(
                new PostComment.Input(aid, caller, input.description(), LocalDateTime.now()));
        created.ifPresent(context::putValue);
        return created.isPresent();
    }

    @Decision(HANDLE_CREATED)
    public Map<String, Object> handleCreated(ReviewComment c) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", c.id().value());
        body.put("answerId", c.answerId().value());
        body.put("commenterId", c.commenterId().value());
        body.put("description", c.description().value());
        body.put("postedAt", c.postedAt().toString());
        return body;
    }

    private static Problem.Violation v(Issue i) {
        return new Problem.Violation(i.path().toString(), i.code(), i.message());
    }
}
```

- [ ] **Step 3:** Compile + commit.

```bash
mvn -pl kysymys-app -am compile -q 2>&1 | tail -3
git add kysymys-app/src/main/java/net/unit8/kysymys/lesson/resource/{CommentJsonDecoders,CommentsResource}.java
git commit -m "Sub-B-5: CommentJsonDecoders + CommentsResource"
```

### Task 5.3: Embed comments in AnswerJsonEncoders + update AnswerResource

**Files:**
- Modify: `lesson/resource/AnswerJsonEncoders.java`
- Modify: `lesson/resource/AnswerResource.java`

- [ ] **Step 1:** Replace `AnswerJsonEncoders.encode(...)` with a 3-arg variant that takes the comment list (keep the 2-arg version for places that don't have comments yet):

```java
    public static Map<String, Object> encode(Answer a, Optional<Submission> latest) {
        return encode(a, latest, java.util.List.of());
    }

    public static Map<String, Object> encode(Answer a, Optional<Submission> latest,
                                             java.util.List<ReviewComment> comments) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", a.id().value());
        body.put("problemId", a.problemId().value());
        body.put("answererId", a.answererId().value());
        body.put("repository", encodeRepository(a.repository()));
        latest.ifPresent(s -> {
            body.put("latestCommitHash", s.commitHash().value());
            body.put("latestSubmittedAt", s.submittedAt().toString());
            body.put("answerUrl", a.repository().commitUrl(s.commitHash()));
        });
        body.put("comments", comments.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.id().value());
            m.put("commenterId", c.commenterId().value());
            m.put("description", c.description().value());
            m.put("postedAt", c.postedAt().toString());
            return m;
        }).toList());
        return body;
    }
```

Add the import:

```java
import net.unit8.kysymys.lesson.data.ReviewComment;
```

- [ ] **Step 2:** Update `AnswerResource.java`'s `show(...)` method to fetch and pass comments. Replace the method body:

```java
    @Decision(HANDLE_OK)
    public Map<String, Object> show(Answer a, DSLContext dsl, RestContext context) {
        Optional<Submission> latest = Optional.ofNullable(context.getValue(Submission.class));
        java.util.List<ReviewComment> comments = new ReviewCommentDao(dsl).listByAnswer(a.id());
        return AnswerJsonEncoders.encode(a, latest, comments);
    }
```

Add imports:

```java
import net.unit8.kysymys.lesson.dao.ReviewCommentDao;
import net.unit8.kysymys.lesson.data.ReviewComment;
```

- [ ] **Step 3:** Compile + commit.

```bash
mvn -pl kysymys-app -am compile -q 2>&1 | tail -3
git add kysymys-app/src/main/java/net/unit8/kysymys/lesson/resource/{AnswerJsonEncoders,AnswerResource}.java
git commit -m "Sub-B-5: include comments[] in GET /answers/:id"
```

### Task 5.4: Wire comment route + extend Hurl

**Files:**
- Modify: `KysymysApplicationFactory.java`
- Modify: `kysymys-app/src/test/hurl/lesson.hurl`

- [ ] **Step 1:** Add to `Routes.define`:

```java
            r.post("/answers/:id/comments").to(CommentsResource.class);
```

Add import:

```java
import net.unit8.kysymys.lesson.resource.CommentsResource;
```

- [ ] **Step 2:** Add scenarios 8 and 9 to `lesson.hurl` (between scenario 7 and 10):

```hurl
# scenario 8: post a comment
POST {{host}}/answers/{{answer_id}}/comments
x-bouncr-credential: {{token}}
Content-Type: application/json
{
  "description": "Looks good"
}
HTTP 201

# scenario 9: show answer (with one comment)
GET {{host}}/answers/{{answer_id}}
x-bouncr-credential: {{token}}
Accept: application/json
HTTP 200
[Asserts]
jsonpath "$.comments" count == 1
jsonpath "$.comments[0].description" == "Looks good"
```

- [ ] **Step 3:** Final smoke.

```bash
cd kysymys-app && pkill -f exec:java; mvn exec:java &
sleep 5
TOKEN=$(java /tmp/GenToken.java)
hurl --variable host=http://localhost:3000 --variable token="$TOKEN" \
  src/test/hurl/lesson.hurl
pkill -f exec:java
```

Expected: all 11 scenarios pass.

- [ ] **Step 4:** Commit.

```bash
git add kysymys-app/src/main/java/net/unit8/kysymys/KysymysApplicationFactory.java \
        kysymys-app/src/test/hurl/lesson.hurl
git commit -m "Sub-B-5: wire comment route + finalise Hurl scenarios"
```

### Task 5.5: Update ADR 003 file refs

**Files:**
- Modify: `doc/adr/003_problem_status.md`

- [ ] **Step 1:** Replace the two paths that point to `ProblemJpaEntity.java` (now removed) with the new sealed/record locations. Open the file, find the link block, replace with:

```markdown
[Problem](../../kysymys-app/src/main/java/net/unit8/kysymys/lesson/data/Problem.java) に対して、[ProblemLifecycle](../../kysymys-app/src/main/java/net/unit8/kysymys/lesson/data/ProblemLifecycle.java) のロングタームイベントをOneToOneの関連として作り、ProblemLifecycleがステータスを持つ。

...

ProblemLifecycleのステータスを変えるイベントを、[ProblemEvent](../../kysymys-app/src/main/java/net/unit8/kysymys/lesson/data/ProblemEvent.java) (sealed interface, permits ProblemCreated/Updated/ArchivedEvent) として記録する。
```

(Match the exact existing prose; only the file paths and the "JpaEntity" → record mention change.)

- [ ] **Step 2:** Commit.

```bash
git add doc/adr/003_problem_status.md
git commit -m "Sub-B-5: update ADR 003 file references for new sealed records"
```

### Task 5.6: Open Sub-B-5 PR

```bash
git push
gh pr create --base develop --head feature/sub-b-lesson \
  --title "Sub-B-5: ReviewComment + answer-with-comments encoder" \
  --body "PostComment behaviour, CommentsResource, comments[] embedded in GET /answers/:id, Hurl scenarios 8-9, ADR 003 link refresh. Closes the Sub-B series."
```

---

## Self-review

Going back through the spec sections to verify coverage:

- §"全体方針" rows: V1 edits (Tasks 1.1-1.2), V3 (1.2), nanoid IDs (1.3, 1.5), `UserId` preview (1.4), Problem/Answer sealed hierarchies (1.6, 1.7), aggregate records (1.8), Long-term events (2.2, 3.2-3.3), `ProblemStatus` enum + VARCHAR persistence (1.8 + 2.2), Submission separation (2.3, 4.1), Description widening (1.5 + 1.1), ListFollowerAnswers explicitly excluded ✓, Principal→UserId injector (3.1), Authorization principal-only (every resource's `authorized()`), RFC 9457 errors (resources via `Problem.fromViolationList`), `dao` directory naming (2.x), `behavior/` separation (3.x, 4.1, 5.1) ✓
- §"完了条件" 1-8: Flyway (1.1-1.2), Problem CRUD (3.5-3.7), Answer (4.x), Comment (5.x), Bouncr-protected writes (every `authorized()`), JUnit tests (every Task with `*Test`), Hurl scenario (3.7 + 4.4 + 5.4), ADR 003 link fix (5.5) ✓
- §"DDL 変更" V1 row table: every row mapped to Task 1.1 ✓
- §"ルーティング" 9 endpoints: covered across 3.7, 4.4, 5.4 ✓

**Placeholder scan**: no "TBD", "TODO", "implement later" remain in the plan. Two soft warnings remain that are *intentional*:

- "Note on Raoh API" (Task 3.4) — points to a concrete reference file the engineer will inspect when writing the Raoh decoder; this is unavoidable because we don't have a full Raoh javadoc to embed inline.
- The Hurl PUT/DELETE ordering note (Task 4.4 step 2) — this is an **explicit** instruction, not a TODO.

**Type consistency**: cross-task names are stable: `IdGenerator.newId()`, `ProblemId.newId()`, `AnswerId.newId()`, `ProblemDao.insert(Problem, ProblemLifecycle)`, `ProblemDao.update(Problem)`, `ProblemDao.updateStatus(ProblemLifecycleId, ProblemStatus)`, `ProblemDao.findById(ProblemId)`, `ProblemDao.findStatus(ProblemLifecycleId)`, `ProblemDao.listActive()`, `ProblemEventDao.insert(ProblemEvent)`, `ProblemEventDao.countByLifecycle(ProblemLifecycleId)`, `AnswerDao.upsert(...)`/`findById`/`listByAnswerer`, `SubmissionDao.insertAndMarkLatest`/`countByAnswer`/`findLatest`, `ReviewCommentDao.insert`/`listByAnswer`, behaviour `Input` records sit on each behaviour, `AnswerJsonEncoders.encode(Answer, Optional<Submission>)` (2-arg) and `encode(Answer, Optional<Submission>, List<ReviewComment>)` (3-arg) are both defined — the 2-arg is used in Sub-B-4 and remains backwards-compatible after Sub-B-5 adds the 3-arg overload.

No issues found.

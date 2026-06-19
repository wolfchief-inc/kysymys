package net.unit8.kysymys.activity;

import net.unit8.kysymys.activity.behavior.ListParticipantStatus;
import net.unit8.kysymys.activity.behavior.RecordActivity;
import net.unit8.kysymys.activity.data.ActivityKind;
import net.unit8.kysymys.activity.data.ParticipantStatus;
import net.unit8.kysymys.lesson.dao.DaoTestSupport;
import net.unit8.kysymys.user.data.UserId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityStatusTest {
    private static DaoTestSupport support;
    private static RecordActivity record;
    private static ListParticipantStatus list;

    @BeforeAll
    static void setUp() {
        support = new DaoTestSupport();
        record = new RecordActivity(support.dsl());
        list = new ListParticipantStatus(support.dsl());
    }

    private static Optional<ParticipantStatus> statusOf(UserId who) {
        return list.apply().stream().filter(s -> s.participantId().equals(who)).findFirst();
    }

    @Test
    void derivesLatestBuildAndActivityFromEventStream() {
        UserId u = UserId.of("alice-" + System.nanoTime());
        LocalDateTime t0 = LocalDateTime.of(2026, 6, 19, 10, 0, 0);

        record.apply(new RecordActivity.Input(u, "p1", ActivityKind.BUILD_FAILURE, "compile error: ;", t0));
        record.apply(new RecordActivity.Input(u, "p1", ActivityKind.HEARTBEAT, null, t0.plusMinutes(1)));
        record.apply(new RecordActivity.Input(u, "p1", ActivityKind.BUILD_SUCCESS, null, t0.plusMinutes(2)));

        ParticipantStatus s = statusOf(u).orElseThrow();
        assertThat(s.problemId()).isEqualTo("p1");
        assertThat(s.lastActivityAt()).isEqualTo(t0.plusMinutes(2));
        assertThat(s.lastBuildKind()).isEqualTo(ActivityKind.BUILD_SUCCESS);
        assertThat(s.lastBuildAt()).isEqualTo(t0.plusMinutes(2));
        assertThat(s.stuck()).isFalse();
    }

    @Test
    void stuckIsTrueUntilResolvedArrivesLater() {
        UserId u = UserId.of("bob-" + System.nanoTime());
        LocalDateTime t0 = LocalDateTime.of(2026, 6, 19, 11, 0, 0);

        record.apply(new RecordActivity.Input(u, "p2", ActivityKind.STUCK, null, t0));
        assertThat(statusOf(u).orElseThrow().stuck()).isTrue();

        record.apply(new RecordActivity.Input(u, "p2", ActivityKind.RESOLVED, null, t0.plusMinutes(5)));
        assertThat(statusOf(u).orElseThrow().stuck()).isFalse();

        // Getting stuck again after resolving flips it back.
        record.apply(new RecordActivity.Input(u, "p2", ActivityKind.STUCK, null, t0.plusMinutes(10)));
        assertThat(statusOf(u).orElseThrow().stuck()).isTrue();
    }

    @Test
    void listsOneStatusPerParticipant() {
        UserId u1 = UserId.of("carol-" + System.nanoTime());
        UserId u2 = UserId.of("dave-" + System.nanoTime());
        LocalDateTime t0 = LocalDateTime.of(2026, 6, 19, 12, 0, 0);

        record.apply(new RecordActivity.Input(u1, "p3", ActivityKind.HEARTBEAT, null, t0));
        record.apply(new RecordActivity.Input(u1, "p3", ActivityKind.HEARTBEAT, null, t0.plusSeconds(30)));
        record.apply(new RecordActivity.Input(u2, "p3", ActivityKind.BUILD_SUCCESS, null, t0));

        List<ParticipantStatus> all = list.apply();
        assertThat(all.stream().filter(s -> s.participantId().equals(u1)).count()).isEqualTo(1);
        assertThat(all.stream().filter(s -> s.participantId().equals(u2)).count()).isEqualTo(1);
    }
}

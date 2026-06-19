package net.unit8.kysymys.activity.resource;

import net.unit8.kysymys.activity.data.ParticipantStatus;

import java.util.LinkedHashMap;
import java.util.Map;

/** JSON response shapes for the Activity context. */
public final class ActivityJsonEncoders {
    private ActivityJsonEncoders() {}

    public static Map<String, Object> encodeStatus(ParticipantStatus s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("participantId", s.participantId().value());
        m.put("problemId", s.problemId());
        m.put("lastActivityAt", s.lastActivityAt() == null ? null : s.lastActivityAt().toString());
        m.put("lastBuildKind", s.lastBuildKind() == null ? null : s.lastBuildKind().name());
        m.put("lastBuildAt", s.lastBuildAt() == null ? null : s.lastBuildAt().toString());
        m.put("lastBuildDetail", s.lastBuildDetail());
        m.put("stuck", s.stuck());
        return m;
    }
}

package net.unit8.kysymys.activity.resource;

import net.unit8.kysymys.activity.data.ParticipantStatus;
import net.unit8.raoh.encode.Encoder;

import java.util.Map;

import static net.unit8.raoh.encode.MapEncoders.nullableProperty;
import static net.unit8.raoh.encode.MapEncoders.object;
import static net.unit8.raoh.encode.MapEncoders.property;
import static net.unit8.raoh.encode.ObjectEncoders.bool;
import static net.unit8.raoh.encode.ObjectEncoders.dateTime;
import static net.unit8.raoh.encode.ObjectEncoders.enumOf;
import static net.unit8.raoh.encode.ObjectEncoders.string;

/** JSON response shapes for the Activity context, built with raoh-encode. */
public final class ActivityJsonEncoders {
    private ActivityJsonEncoders() {}

    private static final Encoder<ParticipantStatus, Map<String, Object>> STATUS = object(
            property("participantId", s -> s.participantId().value(), string()),
            nullableProperty("problemId", ParticipantStatus::problemId, string()),
            property("lastActivityAt", ParticipantStatus::lastActivityAt, dateTime()),
            nullableProperty("lastBuildKind", ParticipantStatus::lastBuildKind, enumOf()),
            nullableProperty("lastBuildAt", ParticipantStatus::lastBuildAt, dateTime()),
            nullableProperty("lastBuildDetail", ParticipantStatus::lastBuildDetail, string()),
            property("stuck", s -> s.stuck(), bool()));

    public static Map<String, Object> encodeStatus(ParticipantStatus s) {
        return STATUS.encode(s);
    }
}

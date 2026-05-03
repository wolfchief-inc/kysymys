package net.unit8.kysymys.notification.behavior;

import net.unit8.kysymys.notification.dao.WhatsNewDao;
import net.unit8.kysymys.notification.data.WhatsNewId;
import net.unit8.kysymys.user.data.UserId;
import org.jooq.DSLContext;

public class MarkAsRead {
    private final DSLContext dsl;

    public MarkAsRead(DSLContext dsl) {
        this.dsl = dsl;
    }

    /** Returns true if an unread row was deleted. */
    public boolean apply(WhatsNewId whatsNewId, UserId userId) {
        int[] holder = new int[1];
        dsl.transaction(cfg -> holder[0] = new WhatsNewDao(cfg.dsl()).markRead(whatsNewId, userId));
        return holder[0] > 0;
    }
}

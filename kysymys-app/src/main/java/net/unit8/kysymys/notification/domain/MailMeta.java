package net.unit8.kysymys.notification.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MailMeta {
    @Getter
    String from;

    @Getter
    List<String> to;

    @Getter
    List<String> cc;

    @Getter
    String subject;

    public static MailMetaBuilder builder() {
        return new MailMetaBuilder();
    }

    public static class MailMetaBuilder {
        private final MailMeta meta;

        MailMetaBuilder() {
            meta = new MailMeta();
        }
        public MailMetaBuilder from(String from) {
            meta.from = from;
            return this;
        }
        public MailMetaBuilder subject(String subject) {
            meta.subject = subject;
            return this;
        }

        public MailMetaBuilder to(String to) {
            if (meta.to == null) meta.to = new ArrayList<>();
            meta.to.add(to);
            return this;
        }

        public MailMetaBuilder to(String... to) {
            if (meta.to == null) meta.to = new ArrayList<>();
            meta.to.addAll(Arrays.asList(to));
            return this;
        }

        public MailMetaBuilder cc(String cc) {
            if (meta.cc == null) meta.cc = new ArrayList<>();
            meta.cc.add(cc);
            return this;
        }

        public MailMetaBuilder cc(String... cc) {
            if (meta.cc == null) meta.cc = new ArrayList<>();
            meta.cc.addAll(Arrays.asList(cc));
            return this;
        }

        public MailMeta build() {
            return meta;
        }
    }
}

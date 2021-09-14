package net.unit8.kysymys.lesson.domain.repos;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;

public class GenericRepositoryUrlBuilder extends AbstractRepositoryUrlBuilder {
    public GenericRepositoryUrlBuilder(String url) {
        super(url);
    }

    @Override
    public URL build() {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }}

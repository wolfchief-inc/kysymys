package net.unit8.kysymys.lesson.data;

import java.util.regex.Pattern;

/**
 * Validation helpers shared by repository value objects. The branch regex
 * comes from git's ref-format rules — the same pattern the legacy
 * Yavi-based code used.
 */
final class BranchNamePattern {
    /** See https://www.spinics.net/lists/git/msg133704.html */
    static final Pattern BRANCH = Pattern.compile(
            "^(?!(^\\.|.*(\\.\\.|\\p{Space}|\\p{Cntrl}))).*(?<!(/|\\.lock))$");

    private BranchNamePattern() {}

    static String validateBranch(String branch) {
        if (branch.isEmpty() || branch.length() > 100) {
            throw new IllegalArgumentException("branch must be 1..100 chars");
        }
        if (!BRANCH.matcher(branch).matches()) {
            throw new IllegalArgumentException("branch is not a valid git ref: " + branch);
        }
        return branch;
    }

    static String validateUrl(String url) {
        if (url.isBlank() || url.length() > 255) {
            throw new IllegalArgumentException("url must be 1..255 chars");
        }
        return url;
    }

    static String chopDotGit(String url) {
        return url.endsWith(".git") ? url.substring(0, url.length() - 4) : url;
    }

    static String validateReadmePath(String path) {
        if (path.isEmpty() || path.length() > 100) {
            throw new IllegalArgumentException("readmePath must be 1..100 chars");
        }
        return path;
    }
}

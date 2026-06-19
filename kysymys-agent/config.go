package main

import (
	"bufio"
	"os"
	"path/filepath"
	"strings"
)

// Config holds the agent's connection settings.
type Config struct {
	URL       string
	Token     string
	ProblemID string
}

func (c Config) complete() bool {
	return c.URL != "" && c.Token != ""
}

const defaultURL = "http://localhost:3000"

// resolveConfig reads settings from the environment first, then from a
// kysymys.properties file in projectDir, then applies the default URL. This
// mirrors how SubmitMojo and the Java extension resolve the same keys.
func resolveConfig(projectDir string) Config {
	props := readProps(filepath.Join(projectDir, "kysymys.properties"))

	c := Config{
		URL:       firstNonEmpty(os.Getenv("KYSYMYS_URL"), props["kysymys.url"], defaultURL),
		Token:     firstNonEmpty(os.Getenv("KYSYMYS_TOKEN"), props["kysymys.token"]),
		ProblemID: firstNonEmpty(os.Getenv("KYSYMYS_PROBLEM_ID"), props["kysymys.problemId"]),
	}
	return c
}

// readProps parses a minimal java.util.Properties file (key=value, # comments).
// A missing or unreadable file yields an empty map — settings just fall through.
func readProps(path string) map[string]string {
	out := map[string]string{}
	f, err := os.Open(path)
	if err != nil {
		return out
	}
	defer f.Close()

	sc := bufio.NewScanner(f)
	for sc.Scan() {
		line := strings.TrimSpace(sc.Text())
		if line == "" || strings.HasPrefix(line, "#") || strings.HasPrefix(line, "!") {
			continue
		}
		k, v, ok := strings.Cut(line, "=")
		if !ok {
			continue
		}
		out[strings.TrimSpace(k)] = strings.TrimSpace(v)
	}
	return out
}

func firstNonEmpty(values ...string) string {
	for _, v := range values {
		if strings.TrimSpace(v) != "" {
			return v
		}
	}
	return ""
}

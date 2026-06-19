package main

import "testing"

func TestBuildJSONOmitsOptionalFields(t *testing.T) {
	got := buildJSON("BUILD_SUCCESS", "", "")
	want := `{"kind":"BUILD_SUCCESS"}`
	if got != want {
		t.Errorf("got %s want %s", got, want)
	}
}

func TestBuildJSONIncludesProblemAndDetail(t *testing.T) {
	got := buildJSON("BUILD_FAILURE", "p1", "compile error")
	want := `{"kind":"BUILD_FAILURE","problemId":"p1","detail":"compile error"}`
	if got != want {
		t.Errorf("got %s want %s", got, want)
	}
}

func TestBuildJSONEscapesAndCollapsesToFirstLine(t *testing.T) {
	got := buildJSON("BUILD_FAILURE", "", "error: \"x\" expected\nstack two\nstack three")
	want := `{"kind":"BUILD_FAILURE","detail":"error: \"x\" expected"}`
	if got != want {
		t.Errorf("got %s want %s", got, want)
	}
}

func TestResolveConfigEnvWinsOverDefault(t *testing.T) {
	t.Setenv("KYSYMYS_URL", "http://h:3000")
	t.Setenv("KYSYMYS_TOKEN", "tok")
	t.Setenv("KYSYMYS_PROBLEM_ID", "p9")
	cfg := resolveConfig(t.TempDir())
	if cfg.URL != "http://h:3000" || cfg.Token != "tok" || cfg.ProblemID != "p9" {
		t.Errorf("unexpected config: %+v", cfg)
	}
	if !cfg.complete() {
		t.Error("expected complete config")
	}
}

func TestResolveConfigDefaultsUrlAndIsIncompleteWithoutToken(t *testing.T) {
	t.Setenv("KYSYMYS_URL", "")
	t.Setenv("KYSYMYS_TOKEN", "")
	t.Setenv("KYSYMYS_PROBLEM_ID", "")
	cfg := resolveConfig(t.TempDir())
	if cfg.URL != defaultURL {
		t.Errorf("expected default url, got %s", cfg.URL)
	}
	if cfg.complete() {
		t.Error("expected incomplete config without token")
	}
}

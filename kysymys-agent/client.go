package main

import (
	"net/http"
	"strings"
	"time"
)

// postEvent sends one activity event to {url}/activity. Best-effort: any error
// is returned but callers treat telemetry as fire-and-forget.
func postEvent(cfg Config, kind, detail string) error {
	body := buildJSON(kind, cfg.ProblemID, detail)
	req, err := http.NewRequest(http.MethodPost, strings.TrimRight(cfg.URL, "/")+"/activity",
		strings.NewReader(body))
	if err != nil {
		return err
	}
	req.Header.Set("content-type", "application/json")
	req.Header.Set("x-bouncr-credential", cfg.Token)

	client := &http.Client{Timeout: 10 * time.Second}
	resp, err := client.Do(req)
	if err != nil {
		return err
	}
	defer resp.Body.Close()
	return nil
}

// buildJSON renders the request body, omitting optional fields when empty and
// collapsing detail to a single escaped line within the server's length cap.
func buildJSON(kind, problemID, detail string) string {
	var b strings.Builder
	b.WriteString(`{"kind":"`)
	b.WriteString(kind)
	b.WriteString(`"`)
	if strings.TrimSpace(problemID) != "" {
		b.WriteString(`,"problemId":"`)
		b.WriteString(escape(problemID))
		b.WriteString(`"`)
	}
	if d := firstLine(detail); d != "" {
		b.WriteString(`,"detail":"`)
		b.WriteString(escape(d))
		b.WriteString(`"`)
	}
	b.WriteString("}")
	return b.String()
}

func firstLine(s string) string {
	s = strings.TrimSpace(s)
	if s == "" {
		return ""
	}
	if i := strings.IndexAny(s, "\r\n"); i >= 0 {
		s = s[:i]
	}
	s = strings.TrimSpace(s)
	if len(s) > 500 {
		s = s[:500]
	}
	return s
}

func escape(s string) string {
	var b strings.Builder
	for _, r := range s {
		switch r {
		case '"':
			b.WriteString(`\"`)
		case '\\':
			b.WriteString(`\\`)
		case '\n':
			b.WriteString(`\n`)
		case '\r':
			b.WriteString(`\r`)
		case '\t':
			b.WriteString(`\t`)
		default:
			if r < 0x20 {
				b.WriteString("\\u00")
				const hex = "0123456789abcdef"
				b.WriteByte(hex[(r>>4)&0xf])
				b.WriteByte(hex[r&0xf])
			} else {
				b.WriteRune(r)
			}
		}
	}
	return b.String()
}

package main

import (
	"errors"
	"flag"
	"os"
)

// cmdEvent posts a single activity event:
//
//	kysymys-agent event BUILD_FAILURE --detail "compile error: ; expected"
//
// Config comes from the environment (and the current directory's
// kysymys.properties as a fallback).
func cmdEvent(args []string) error {
	fs := flag.NewFlagSet("event", flag.ContinueOnError)
	detail := fs.String("detail", "", "free-text detail (e.g. first build error line)")
	if err := fs.Parse(args); err != nil {
		return err
	}
	if fs.NArg() < 1 {
		return errors.New("missing KIND (e.g. BUILD_SUCCESS, BUILD_FAILURE, STUCK, RESOLVED)")
	}
	kind := fs.Arg(0)

	wd, _ := os.Getwd()
	cfg := resolveConfig(wd)
	if !cfg.complete() {
		return errors.New("KYSYMYS_URL/KYSYMYS_TOKEN not set")
	}
	return postEvent(cfg, kind, *detail)
}

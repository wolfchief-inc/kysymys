package main

import (
	"errors"
	"hash/fnv"
	"io/fs"
	"os"
	"path/filepath"
	"strconv"
	"time"
)

const (
	pollInterval      = 5 * time.Second
	heartbeatInterval = 30 * time.Second
	idleExit          = 15 * time.Minute
	lockFreshWindow   = 90 * time.Second
)

// cmdWatch runs the background heartbeat daemon for one project. It posts a
// HEARTBEAT when files change, so the instructor dashboard can tell "actively
// editing" from "idle and possibly stuck" between builds.
//
// Only one watcher runs per project: a lock file in the temp dir, kept fresh
// while the watcher lives, makes a second `watch` invocation exit immediately.
// The watcher exits on its own after idleExit without any file change.
func cmdWatch(args []string) error {
	if len(args) < 1 {
		return errors.New("missing <projectDir>")
	}
	project, err := filepath.Abs(args[0])
	if err != nil {
		return err
	}
	cfg := resolveConfig(project)
	if !cfg.complete() {
		// No token: nothing to report. Exit quietly so the build is unaffected.
		return nil
	}

	lock := lockFileFor(project)
	if lockFresh(lock) {
		return nil // a watcher is already running for this project
	}
	if err := touch(lock); err != nil {
		return nil // cannot claim the lock; give up rather than risk a duplicate
	}
	defer os.Remove(lock)

	lastMod := maxModTime(project)
	lastChange := time.Now()
	var lastHeartbeat time.Time

	for {
		time.Sleep(pollInterval)
		_ = touch(lock) // keep the lock fresh so duplicates don't spawn

		now := maxModTime(project)
		if now.After(lastMod) {
			lastMod = now
			lastChange = time.Now()
			if time.Since(lastHeartbeat) >= heartbeatInterval {
				_ = postEvent(cfg, "HEARTBEAT", "")
				lastHeartbeat = time.Now()
			}
		} else if time.Since(lastChange) >= idleExit {
			return nil // gone quiet; the dashboard already shows them idle
		}
	}
}

// maxModTime returns the newest modification time among the project's source
// files, skipping build output and VCS/IDE noise. A bump means the participant
// edited something.
func maxModTime(root string) time.Time {
	var newest time.Time
	_ = filepath.WalkDir(root, func(path string, d fs.DirEntry, err error) error {
		if err != nil {
			return nil
		}
		if d.IsDir() {
			if isIgnoredDir(d.Name()) {
				return filepath.SkipDir
			}
			return nil
		}
		if info, err := d.Info(); err == nil && info.ModTime().After(newest) {
			newest = info.ModTime()
		}
		return nil
	})
	return newest
}

func isIgnoredDir(name string) bool {
	switch name {
	case "target", ".git", "node_modules", ".idea":
		return true
	default:
		return false
	}
}

func lockFileFor(project string) string {
	h := fnv.New32a()
	_, _ = h.Write([]byte(project))
	return filepath.Join(os.TempDir(), "kysymys-watcher-"+strconv.FormatUint(uint64(h.Sum32()), 16)+".lock")
}

func lockFresh(lock string) bool {
	info, err := os.Stat(lock)
	if err != nil {
		return false
	}
	return time.Since(info.ModTime()) < lockFreshWindow
}

func touch(lock string) error {
	now := time.Now()
	if err := os.Chtimes(lock, now, now); err == nil {
		return nil
	}
	f, err := os.Create(lock)
	if err != nil {
		return err
	}
	return f.Close()
}

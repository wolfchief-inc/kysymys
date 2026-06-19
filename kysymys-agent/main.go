// Command kysymys-agent is the participant-side work-activity agent for kysymys
// training sessions. It is a single static binary (one per OS/arch) so it needs
// no JVM and is trivial to drop onto any participant machine.
//
// Subcommands:
//
//	kysymys-agent watch <projectDir>   run the background heartbeat daemon
//	kysymys-agent event <KIND> [flags] post a single activity event
//
// The Java Maven extension (kysymys-activity-agent) extracts the matching binary
// for the host and runs `watch` at the start of every build; it reports build
// outcomes itself. Configuration (server URL, Bouncr token, problem id) comes
// from the KYSYMYS_URL / KYSYMYS_TOKEN / KYSYMYS_PROBLEM_ID environment variables,
// falling back to a kysymys.properties file in the project directory.
package main

import (
	"fmt"
	"os"
)

func main() {
	if len(os.Args) < 2 {
		usage()
		os.Exit(2)
	}
	switch os.Args[1] {
	case "watch":
		if err := cmdWatch(os.Args[2:]); err != nil {
			fmt.Fprintln(os.Stderr, "kysymys-agent watch:", err)
			os.Exit(1)
		}
	case "event":
		if err := cmdEvent(os.Args[2:]); err != nil {
			fmt.Fprintln(os.Stderr, "kysymys-agent event:", err)
			os.Exit(1)
		}
	case "-h", "--help", "help":
		usage()
	default:
		fmt.Fprintf(os.Stderr, "unknown subcommand %q\n", os.Args[1])
		usage()
		os.Exit(2)
	}
}

func usage() {
	fmt.Fprintln(os.Stderr, "usage: kysymys-agent <watch|event> ...")
}

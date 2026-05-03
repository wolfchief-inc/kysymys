package net.unit8.kysymys;

import enkan.system.EnkanSystem;

/**
 * Production entry point. Wires components via {@link KysymysSystemFactory},
 * which reads database / JWT settings from environment variables
 * ({@code KYSYMYS_DB_URL}, {@code KYSYMYS_DB_USER}, {@code KYSYMYS_DB_PASSWORD},
 * {@code KYSYMYS_JWT_SECRET}). Blocks the main thread until shutdown.
 */
public class KysymysMain {
    public static void main(String[] args) throws InterruptedException {
        EnkanSystem system = new KysymysSystemFactory().create();
        system.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            system.stop();
        }, "shutdown-hook"));

        System.out.println("Server started on http://localhost:3000. Press Ctrl-C to stop.");
        Thread.currentThread().join();
    }
}

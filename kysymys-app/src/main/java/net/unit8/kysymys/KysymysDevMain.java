package net.unit8.kysymys;

import enkan.system.EnkanSystem;

/**
 * Development entry point. Wires components via {@link KysymysDevSystemFactory}
 * (H2 in-memory, Bouncr HMAC with a fixed dev secret) and blocks the main
 * thread until the JVM receives a shutdown signal (Ctrl-C).
 */
public class KysymysDevMain {
    public static void main(String[] args) throws InterruptedException {
        EnkanSystem system = new KysymysDevSystemFactory().create();
        system.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            system.stop();
        }, "shutdown-hook"));

        System.out.println("Server started on http://localhost:3000 (dev). Press Ctrl-C to stop.");
        Thread.currentThread().join();
    }
}

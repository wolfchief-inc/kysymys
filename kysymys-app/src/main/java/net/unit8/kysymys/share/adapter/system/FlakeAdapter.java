package net.unit8.kysymys.share.adapter.system;

import net.unit8.kysymys.share.application.GenerateCursorPort;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;

import java.io.UncheckedIOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.time.Clock;
import java.util.Collections;
import java.util.Objects;

@Component
class FlakeAdapter implements GenerateCursorPort {
    private final Clock clock;
    private int sequence;
    private long lastTime;
    private final byte[] macAddress;

    public FlakeAdapter() {
        clock = Clock.systemUTC();
        lastTime = clock.millis();
        macAddress = getMacAddress();
        sequence = 0;
    }

    @Override
    public String generateId() {
        ByteBuffer buf = ByteBuffer.allocate(16);
        long current = clock.millis();
        if (current != lastTime) {
            lastTime = current;
            sequence = 0;
        } else {
            sequence++;
        }
        return new String(Hex.encode(buf.putLong(lastTime).put(macAddress).putShort((short) sequence)
                .array()));
    }

    private byte[] getMacAddress() {
        byte[] defaultAddress = new byte[6];
        try {
            return Collections.list(NetworkInterface.getNetworkInterfaces())
                    .stream()
                    .filter(ni -> {
                        try {
                            return !ni.isLoopback() && ni.isUp();
                        } catch (SocketException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .map(ni -> {
                        try {
                            return ni.getHardwareAddress();
                        } catch (SocketException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .filter(Objects::nonNull)
                    .findAny()
                    .orElse(defaultAddress);
        } catch (SocketException e) {
            throw new UncheckedIOException(e);
        }
    }
}

package com.edwardjones.drift.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.security.MessageDigest;

public final class ChecksumUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final MessageDigest SHA256;

    static {
        try { SHA256 = MessageDigest.getInstance("SHA-256"); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }

    public static String sha256Hex(Object dto) {
        try {
            byte[] json = MAPPER.writeValueAsBytes(dto);               // canonical JSON
            byte[] hash = SHA256.digest(json);
            StringBuilder sb = new StringBuilder(64);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash DTO", e);
        }
    }
}

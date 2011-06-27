package com.sqt001.ipcall.util;

import java.util.UUID;

public class GuidGenerator {
    public static String generate() {
        UUID key = UUID.randomUUID();
        return key.toString();
    }
}

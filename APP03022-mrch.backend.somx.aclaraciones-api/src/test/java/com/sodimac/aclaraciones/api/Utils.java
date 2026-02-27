package com.sodimac.aclaraciones.api;

import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;

public class Utils {

    public static String createRandomString() {
        byte[] randomBytes = new byte[16];
        SecureRandom randomizer = new SecureRandom();
        randomizer.nextBytes(randomBytes);
        return Base64.encodeBase64String(randomBytes);
    }
}

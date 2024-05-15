package com.example.googledrivemerge.util;

import org.apache.tika.Tika;

public class MimeTypeUtil {

    private static final Tika tika = new Tika();

    public static String getMimeType(String fileName) {
        return tika.detect(fileName);
    }
}

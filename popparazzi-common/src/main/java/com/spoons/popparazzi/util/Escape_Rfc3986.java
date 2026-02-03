package com.spoons.popparazzi.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
public class Escape_Rfc3986 {

    public static String Encode(String input) throws UnsupportedEncodingException {
        if (input == null || input.isEmpty())
            return "";

        return new String(EncodeToBytes(input, "UTF-8"), StandardCharsets.US_ASCII);
    }

    private static byte[] EncodeToBytes(String input, String enc) throws UnsupportedEncodingException {

        if (input == null || input.isEmpty())
            return new byte[0];

        byte[] inbytes = input.getBytes(enc);

        // Count unsafe characters
        int unsafeChars = 0;
        char c;
        for (byte b : inbytes) {

            c = (char)b;

            if (NeedsEscaping(c))
                unsafeChars++;
        }

        // Check if we need to do any encoding
        if (unsafeChars == 0)
            return inbytes;

        byte[] outbytes = new byte[inbytes.length + (unsafeChars * 2)];
        int pos = 0;

        for (int i = 0; i < inbytes.length; i++) {
            byte b = inbytes[i];

            if (NeedsEscaping((char)b)) {
                outbytes[pos++] = (byte)'%';
                outbytes[pos++] = (byte)IntToHex((b >> 4) & 0xf);
                outbytes[pos++] = (byte)IntToHex(b & 0x0f);

                //byte.ToString("X2")
            }
            else
                outbytes[pos++] = b;
        }

        return outbytes;
    }

    private static boolean NeedsEscaping(char c) {
        return !((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                || c == '-' || c == '_' || c == '.' || c == '~');
    }

    private static char IntToHex(int n) {
        if (n < 0 || n >= 16)
            throw new IllegalArgumentException("n");

        if (n <= 9)
            return (char)(n + (int)'0');
        else
            return (char)(n - 10 + (int)'A');
    }

    public static String Decode(String input) throws UnsupportedEncodingException {
        if (input == null || input.isEmpty())
            return "";

        byte[] inputBytes = input.getBytes(StandardCharsets.US_ASCII);
        char[] inChars = new char[inputBytes.length];

        for(int i = 0; i < inputBytes.length; i++) {
            inChars[i] = (char)(inputBytes[i] & 0xFF);
        }

        int n4PercentCount = 0;

        for (char inChar : inChars) {
            if (inChar == '%')
                n4PercentCount++;
        }


        byte[] inBytes = new byte[inChars.length + (n4PercentCount * -2)];
        int n4InBytesIndex = 0;

        try {
            for (int i = 0; i < inChars.length; i++) {
                if (inChars[i] == '%') {
                    inBytes[n4InBytesIndex++] = Byte.parseByte(String.valueOf(new char[] {inChars[i + 1], inChars[i + 2]}), 16);
                    i = i + 2;
                }
                else {

                    inBytes[n4InBytesIndex++] = (byte) inChars[i];
                }
            }
            return new String(inBytes, StandardCharsets.UTF_8);
        }
        catch(Exception ex) {
            log.error("Exception", ex);
            throw new UnsupportedEncodingException("Could not RFC 3986 decode String");
        }
    }

    public static String Encode2(String strURL) throws UnsupportedEncodingException {
        return URLEncoder.encode(strURL, StandardCharsets.UTF_8)
                .replace("!", "%21")
                .replace("*", "%2A")
                .replace("(", "%28")
                .replace(")", "%29")
                .replace("'", "%27");
    }

    public static String Decode2(String strURL) throws UnsupportedEncodingException {
        return URLDecoder.decode(strURL, StandardCharsets.UTF_8)
                .replace("!", "%21")
                .replace("*", "%2A")
                .replace("(", "%28")
                .replace(")", "%29")
                .replace("'", "%27");
    }
}

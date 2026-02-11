package com.flg.data;

import java.util.*;

public class Utils {
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();
    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    
    // ============== HEX ==============
    
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX[v >>> 4];
            hexChars[i * 2 + 1] = HEX[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
    
    // ============== BINARY ==============
    
    public static String binToText(String bin) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bin.length(); i += 8) {
            if (i + 8 <= bin.length()) {
                String byteStr = bin.substring(i, i + 8);
                sb.append((char) Integer.parseInt(byteStr, 2));
            }
        }
        return sb.toString();
    }
    
    // ============== BASE32 ==============
    
    public static String toBase32(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;
        
        for (byte b : bytes) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                bitsLeft -= 5;
                int index = (buffer >> bitsLeft) & 0x1F;
                sb.append(BASE32_ALPHABET.charAt(index));
            }
        }
        
        if (bitsLeft > 0) {
            int index = (buffer << (5 - bitsLeft)) & 0x1F;
            sb.append(BASE32_ALPHABET.charAt(index));
        }
        
        return sb.toString();
    }
    
    public static byte[] base32ToBytes(String base32) {
        List<Byte> bytes = new ArrayList<>();
        int buffer = 0;
        int bitsLeft = 0;
        
        for (char c : base32.toCharArray()) {
            int value = BASE32_ALPHABET.indexOf(Character.toUpperCase(c));
            if (value == -1) continue;
            
            buffer = (buffer << 5) | value;
            bitsLeft += 5;
            
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                bytes.add((byte) ((buffer >> bitsLeft) & 0xFF));
            }
        }
        
        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            result[i] = bytes.get(i);
        }
        return result;
    }
    
    // ============== ROT ==============
    
    public static String rot12(String input) {
        return rot(input, 12);
    }
    
    public static String rot(String input, int shift) {
        shift = shift % 26;
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                sb.append((char) ('a' + (c - 'a' + shift) % 26));
            } else if (c >= 'A' && c <= 'Z') {
                sb.append((char) ('A' + (c - 'A' + shift) % 26));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
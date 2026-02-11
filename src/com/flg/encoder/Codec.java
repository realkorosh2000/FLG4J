package com.flg.encoder;

import com.flg.data.*;
import java.util.Base64;

public class Codec {
    
    public static String encode(FLG data, Format format) {
        switch (format) {
            case HEX: return data.toHex();
            case BIN: return data.toBin();
            case BASE64: return data.toBase64();
            case BASE32: return data.toBase32();
            case ROT12: return data.toRot12();
            default: return data.toString();
        }
    }
    
    public static enum Format {
        RAW, HEX, BIN, BASE64, BASE32, ROT12
    }
    
    // Decode from encoded format back to FLG
    public static FLG decode(String encoded, Format format) {
        String decoded;
        switch (format) {
            case HEX:
                decoded = new String(Utils.hexToBytes(encoded));
                break;
            case BASE64:
                decoded = new String(Base64.getDecoder().decode(encoded));
                break;
            case ROT12:
                decoded = Utils.rot12(encoded); // ROT12 twice = original
                break;
            default:
                decoded = encoded;
        }
        return FLG.parse(decoded);
    }
}
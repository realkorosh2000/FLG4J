package com.flg.data;

import java.util.*;
import java.io.*;
import java.nio.file.*;

public class FLG {
    // The entire database - just variables with names in < >
    private Map<String, Object> variables;
    private boolean overridden;
    
    public FLG() {
        this.variables = new LinkedHashMap<>(); // Preserves order
        this.overridden = false;
    }
    
    // ============== DATABASE OPERATIONS ==============
    
    // Store ANY variable with name in < >
    public FLG set(String name, Object value) {
        variables.put(formatName(name), value);
        return this;
    }
    
    // Get ANY variable
    public Object get(String name) {
        return variables.get(formatName(name));
    }
    
    // Get with type casting
    public String getString(String name) {
        Object val = variables.get(formatName(name));
        return val != null ? val.toString() : null;
    }
    
    public Integer getInt(String name) {
        Object val = variables.get(formatName(name));
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof String) return Integer.parseInt((String) val);
        return null;
    }
    
    public Boolean getBoolean(String name) {
        Object val = variables.get(formatName(name));
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof String) return Boolean.parseBoolean((String) val);
        return null;
    }
    
    // Check if variable exists
    public boolean has(String name) {
        return variables.containsKey(formatName(name));
    }
    
    // Delete variable
    public FLG delete(String name) {
        variables.remove(formatName(name));
        return this;
    }
    
    // Clear entire database
    public FLG clear() {
        variables.clear();
        return this;
    }
    
    // Get all variable names
    public Set<String> getKeys() {
        return variables.keySet();
    }
    
    // Get size (how many variables stored)
    public int size() {
        return variables.size();
    }
    
    // ============== ARRAY SUPPORT ==============
    
    // Store arrays
    public FLG setArray(String name, Object... items) {
        variables.put(formatName(name), new ArrayList<>(Arrays.asList(items)));
        return this;
    }
    
    // Get array
    @SuppressWarnings("unchecked")
    public List<Object> getArray(String name) {
        Object val = variables.get(formatName(name));
        if (val instanceof List) return (List<Object>) val;
        return null;
    }
    
    // ============== LAMBDA SUPPORT ==============
    
    // Store lambda code
    public FLG setLambda(String name, String code) {
        variables.put(formatName(name), new Lambda(code));
        return this;
    }
    
    // ============== OVERRIDE SUPPORT ==============
    
    public FLG setOverride(boolean ov) {
        this.overridden = ov;
        return this;
    }
    
    // ============== SAVE OPERATIONS ==============
    
    // Save as normal FLG
    public void save(String path) throws IOException {
        Files.write(Paths.get(path), toString().getBytes());
    }
    
    // Save as Hex
    public void saveHex(String path) throws IOException {
        Files.write(Paths.get(path), toHex().getBytes());
    }
    
    // Save as Binary
    public void saveBin(String path) throws IOException {
        Files.write(Paths.get(path), toBin().getBytes());
    }
    
    // Save as Base64
    public void saveBase64(String path) throws IOException {
        Files.write(Paths.get(path), toBase64().getBytes());
    }
    
    // Save as Base32
    public void saveBase32(String path) throws IOException {
        Files.write(Paths.get(path), toBase32().getBytes());
    }
    
    // Save as Rot12
    public void saveRot12(String path) throws IOException {
        Files.write(Paths.get(path), toRot12().getBytes());
    }
    
    // Save as any Rot level
    public void saveRot(String path, int level) throws IOException {
        Files.write(Paths.get(path), toRot(level).getBytes());
    }
    
    // ============== LOAD OPERATIONS ==============
    
    // Load normal FLG
    public static FLG load(String path) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(path)));
        return parse(content);
    }
    
    // Load from Hex
    public static FLG loadHex(String path) throws IOException {
        String hex = new String(Files.readAllBytes(Paths.get(path))).trim();
        String decoded = new String(Utils.hexToBytes(hex));
        return parse(decoded);
    }
    
    // Load from Binary
    public static FLG loadBin(String path) throws IOException {
        String bin = new String(Files.readAllBytes(Paths.get(path))).trim();
        String decoded = Utils.binToText(bin);
        return parse(decoded);
    }
    
    // Load from Base64
    public static FLG loadBase64(String path) throws IOException {
        String b64 = new String(Files.readAllBytes(Paths.get(path))).trim();
        String decoded = new String(Base64.getDecoder().decode(b64));
        return parse(decoded);
    }
    
    // Load from Base32
    public static FLG loadBase32(String path) throws IOException {
        String b32 = new String(Files.readAllBytes(Paths.get(path))).trim();
        String decoded = new String(Utils.base32ToBytes(b32));
        return parse(decoded);
    }
    
    // Load from Rot12
    public static FLG loadRot12(String path) throws IOException {
        String rot = new String(Files.readAllBytes(Paths.get(path))).trim();
        String decoded = Utils.rot12(rot); // Rot12 twice = original
        return parse(decoded);
    }
    
    // Load from any Rot level
    public static FLG loadRot(String path, int level) throws IOException {
        String rot = new String(Files.readAllBytes(Paths.get(path))).trim();
        String decoded = Utils.rot(rot, 26 - (level % 26)); // Reverse rotation
        return parse(decoded);
    }
    
    // ============== ENCODING FUNCTIONS ==============
    
    public String toHex() {
        return Utils.bytesToHex(toString().getBytes());
    }
    
    public String toBin() {
        byte[] bytes = toString().getBytes();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        return sb.toString();
    }
    
    public String toBase64() {
        return Base64.getEncoder().encodeToString(toString().getBytes());
    }
    
    public String toBase32() {
        return Utils.toBase32(toString().getBytes());
    }
    
    public String toRot12() {
        return Utils.rot12(toString());
    }
    
    public String toRot(int level) {
        return Utils.rot(toString(), level);
    }
    
    // ============== PARSING ==============
    
    // Parse FLG format
    public static FLG parse(String content) {
        FLG db = new FLG();
        String[] lines = content.split("\n");
        Stack<String> context = new Stack<>();
        boolean nextOverride = false;
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // Comment
            if (line.startsWith("#")) continue;
            
            // Override marker
            if (line.startsWith("@Override")) {
                nextOverride = true;
                continue;
            }
            
            // Variable definition (starts with <)
            if (line.startsWith("<") && !line.startsWith("</")) {
                int end = line.indexOf('>');
                if (end != -1) {
                    String varName = line.substring(0, end + 1);
                    context.push(varName);
                    
                    // Check if this line has inline value
                    String rest = line.substring(end + 1).trim();
                    if (!rest.isEmpty()) {
                        db.set(varName, parseValue(rest));
                        if (nextOverride) {
                            // Handle override logic
                            db.setOverride(true);
                            nextOverride = false;
                        }
                        context.pop();
                    }
                }
            }
            // Closing tag
            else if (line.startsWith("/<")) {
                if (!context.isEmpty()) {
                    context.pop();
                }
            }
            // Value line
            else if (!context.isEmpty()) {
                String currentVar = context.peek();
                Object value = parseValue(line);
                db.set(currentVar, value);
                
                if (nextOverride) {
                    db.setOverride(true);
                    nextOverride = false;
                }
            }
        }
        
        return db;
    }
    
    private static Object parseValue(String val) {
        val = val.trim();
        
        // Array
        if (val.startsWith("[") && val.endsWith("]")) {
            List<Object> list = new ArrayList<>();
            String inner = val.substring(1, val.length() - 1);
            String[] items = inner.split(",");
            for (String item : items) {
                list.add(parsePrimitive(item.trim()));
            }
            return list;
        }
        
        // Lambda
        if (val.startsWith("{") && val.endsWith("}")) {
            return new Lambda(val.substring(1, val.length() - 1));
        }
        
        // Primitive
        return parsePrimitive(val);
    }
    
    private static Object parsePrimitive(String val) {
        // String
        if (val.startsWith("\"") && val.endsWith("\"")) {
            return val.substring(1, val.length() - 1);
        }
        // Boolean
        if (val.equals("true")) return true;
        if (val.equals("false")) return false;
        // Null
        if (val.equals("Null") || val.equals("null")) return null;
        // Number
        try {
            if (val.contains(".")) {
                return Double.parseDouble(val);
            } else {
                return Integer.parseInt(val);
            }
        } catch (NumberFormatException e) {
            // Fallback to string
            return val;
        }
    }
    
    private String formatName(String name) {
        if (!name.startsWith("<")) name = "<" + name;
        if (!name.endsWith(">")) name = name + ">";
        return name;
    }
    
    // ============== OUTPUT ==============
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            if (overridden) {
                sb.append("@Override\n");
            }
            
            sb.append(entry.getKey()).append("\n");
            
            Object val = entry.getValue();
            if (val instanceof List) {
                sb.append("    ");
                sb.append(val.toString()).append("\n");
            } else if (val instanceof Lambda) {
                sb.append("    {").append(((Lambda) val).code).append("}\n");
            } else if (val != null) {
                sb.append("    ");
                if (val instanceof String) {
                    sb.append("\"").append(val).append("\"");
                } else {
                    sb.append(val);
                }
                sb.append("\n");
            }
            
            sb.append("/").append(entry.getKey()).append("\n\n");
        }
        
        return sb.toString();
    }
    
    // Lambda holder
    static class Lambda {
        String code;
        Lambda(String code) { this.code = code; }
    }
}
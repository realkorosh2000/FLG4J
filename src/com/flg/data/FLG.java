package com.flg.data;

import java.util.*;
import java.io.*;
import java.nio.file.*;

public class FLG {
    private Map<String, Object> variables;
    private boolean overridden;
    
    public FLG() {
        this.variables = new LinkedHashMap<>();
        this.overridden = false;
    }
    public FLG set(String name, Object value) {
        variables.put(formatName(name), value);
        return this;
    }
    
    public Object get(String name) {
        return variables.get(formatName(name));
    }
    
   
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
    
    
    public boolean has(String name) {
        return variables.containsKey(formatName(name));
    }
    
    
    public FLG delete(String name) {
        variables.remove(formatName(name));
        return this;
    }
    
    
    public FLG clear() {
        variables.clear();
        return this;
    }
    
  
    public Set<String> getKeys() {
        return variables.keySet();
    }
    
   
    public int size() {
        return variables.size();
    }
    
    // -------------- ARRAY SUPPORT --------------
    
   
    public FLG setArray(String name, Object... items) {
        variables.put(formatName(name), new ArrayList<>(Arrays.asList(items)));
        return this;
    }
    
  
    @SuppressWarnings("unchecked")
    public List<Object> getArray(String name) {
        Object val = variables.get(formatName(name));
        if (val instanceof List) return (List<Object>) val;
        return null;
    }
    
    // -------------- LAMBDA SUPPORT --------------
    
    
    public FLG setLambda(String name, String code) {
        variables.put(formatName(name), new Lambda(code));
        return this;
    }
    
    // -------------- OVERRIDE SUPPORT --------------
    
    public FLG setOverride(boolean ov) {
        this.overridden = ov;
        return this;
    }
    
    // -------------- SAVE OPERATIONS --------------
    
    
    public void save(String path) throws IOException {
        Files.write(Paths.get(path), toString().getBytes());
    }
    
    
    public void saveHex(String path) throws IOException {
        Files.write(Paths.get(path), toHex().getBytes());
    }
    
   
    public void saveBin(String path) throws IOException {
        Files.write(Paths.get(path), toBin().getBytes());
    }
    
  
    public void saveBase64(String path) throws IOException {
        Files.write(Paths.get(path), toBase64().getBytes());
    }
    
 
    public void saveBase32(String path) throws IOException {
        Files.write(Paths.get(path), toBase32().getBytes());
    }
    
   
    public void saveRot12(String path) throws IOException {
        Files.write(Paths.get(path), toRot12().getBytes());
    }
    
  
    public void saveRot(String path, int level) throws IOException {
        Files.write(Paths.get(path), toRot(level).getBytes());
    }
    
    // -------------- LOAD OPERATIONS --------------
    
  
    public static FLG load(String path) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(path)));
        return parse(content);
    }
    
  
    public static FLG loadHex(String path) throws IOException {
        String hex = new String(Files.readAllBytes(Paths.get(path))).trim();
        String decoded = new String(Utils.hexToBytes(hex));
        return parse(decoded);
    }
    
 
    public static FLG loadBin(String path) throws IOException {
        String bin = new String(Files.readAllBytes(Paths.get(path))).trim();
        String decoded = Utils.binToText(bin);
        return parse(decoded);
    }
    
 
    public static FLG loadBase64(String path) throws IOException {
        String b64 = new String(Files.readAllBytes(Paths.get(path))).trim();
        String decoded = new String(Base64.getDecoder().decode(b64));
        return parse(decoded);
    }
    

    public static FLG loadBase32(String path) throws IOException {
        String b32 = new String(Files.readAllBytes(Paths.get(path))).trim();
        String decoded = new String(Utils.base32ToBytes(b32));
        return parse(decoded);
    }
    
   
    public static FLG loadRot12(String path) throws IOException {
        String rot = new String(Files.readAllBytes(Paths.get(path))).trim();
        String decoded = Utils.rot12(rot); // Rot12 twice = original
        return parse(decoded);
    }
    
    
    public static FLG loadRot(String path, int level) throws IOException {
        String rot = new String(Files.readAllBytes(Paths.get(path))).trim();
        String decoded = Utils.rot(rot, 26 - (level % 26)); // Reverse rotation
        return parse(decoded);
    }
    
    // -------------- ENCODING FUNCTIONS --------------
    
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
    
    // -------------- PARSING --------------
    
   
    public static FLG parse(String content) {
        FLG db = new FLG();
        String[] lines = content.split("\n");
        Stack<String> context = new Stack<>();
        boolean nextOverride = false;
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            
            if (line.startsWith("#")) continue;
            
            if (line.startsWith("@Override")) {
                nextOverride = true;
                continue;
            }
            
            
            if (line.startsWith("<") && line.contains("</")) {
                int openEnd = line.indexOf('>');
                int closeStart = line.indexOf("</");
                
                if (openEnd != -1 && closeStart != -1) {
                    String varName = line.substring(0, openEnd + 1);
                    String value = line.substring(openEnd + 1, closeStart).trim();
                    
                    if (value.contains("#")) {
                        value = value.substring(0, value.indexOf('#')).trim();
                    }
                    
                    db.set(varName, parseValue(value));
                    if (nextOverride) {
                        db.setOverride(true);
                        nextOverride = false;
                    }
                }
            }
            
            else if (line.startsWith("<") && !line.startsWith("</")) {
                int end = line.indexOf('>');
                if (end != -1) {
                    String varName = line.substring(0, end + 1);
                    context.push(varName);
                    
                    String rest = line.substring(end + 1).trim();
                    
                    if (rest.contains("#")) {
                        rest = rest.substring(0, rest.indexOf('#')).trim();
                    }
                    
                    if (!rest.isEmpty()) {
                        db.set(varName, parseValue(rest));
                        if (nextOverride) {
                            db.setOverride(true);
                            nextOverride = false;
                        }
                        context.pop();
                    }
                }
            }
            
            else if (line.startsWith("</")) {
                if (!context.isEmpty()) {
                    context.pop();
                }
            }
            
            else if (!context.isEmpty()) {
               
                String value = line;
                if (value.contains("#")) {
                    value = value.substring(0, value.indexOf('#')).trim();
                }
                
                if (!value.isEmpty()) {
                    Object parsed = parseValue(value);
                    db.set(context.peek(), parsed);
                    
                    if (nextOverride) {
                        db.setOverride(true);
                        nextOverride = false;
                    }
                }
            }
        }
        
        return db;
    }
    
    private static Object parseValue(String val) {
    	if (val.startsWith("[") && val.endsWith("]")) {
    	    String inner = val.substring(1, val.length() - 1).trim();
    	    List<Object> list = new ArrayList<>();
    	    
    	    boolean inQuote = false;
    	    StringBuilder current = new StringBuilder();
    	    for (char c : inner.toCharArray()) {
    	        if (c == '"') {
    	            inQuote = !inQuote;
    	            current.append(c);
    	        } else if (c == ',' && !inQuote) {
    	            list.add(parsePrimitive(current.toString().trim()));
    	            current = new StringBuilder();
    	        } else {
    	            current.append(c);
    	        }
    	    }
    	    if (current.length() > 0) {
    	        list.add(parsePrimitive(current.toString().trim()));
    	    }
    	    return list;
    	}
		return val;
    }
    
    private static Object parsePrimitive(String val) {
        // Quoted strings
        if (val.startsWith("\"") && val.endsWith("\"")) {
            return val.substring(1, val.length() - 1);
        }
       
        
        if (val.equals("true")) return true;
        if (val.equals("false")) return false;
        
        
        if (val.equals("Null") || val.equals("null")) return null;
        
        
        try {
            if (val.contains(".")) {
                return Double.parseDouble(val);
            } else {
                return Integer.parseInt(val);
            }
        } catch (NumberFormatException e) {
            
            return val;
        }
    }
    
    private String formatName(String name) {
        if (!name.startsWith("<")) name = "<" + name;
        if (!name.endsWith(">")) name = name + ">";
        return name;
    }
    
    // -------------- OUTPUT --------------
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            if (overridden) {
                sb.append("@Override\n");
            }
            
            sb.append(entry.getKey()).append("\n");
            
            Object val = entry.getValue();
            
           
            sb.append("    ");
            
            if (val == null) {
                sb.append("null");
            } else if (val instanceof List) {
                sb.append(val.toString());
            } else if (val instanceof Lambda) {
                sb.append("{").append(((Lambda) val).code).append("}");
            } else if (val instanceof String) {
                sb.append("\"").append(val).append("\"");
            } else {
                sb.append(val);
            }
            
            sb.append("\n");
            
            
            String key = entry.getKey();
            sb.append("<").append(key.substring(1));
            
            sb.setLength(sb.length() - 1); 
            sb.append("</").append(key.substring(1)); 
            sb.append("\n\n");
        }
        
        return sb.toString();
    }
    
    static class Lambda {
        String code;
        Lambda(String code) { this.code = code; }
    }
}
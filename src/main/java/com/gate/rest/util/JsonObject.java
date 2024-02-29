package com.gate.rest.util;

import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.core.convert.ConversionService;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JsonObject extends HashMap<String, Object> {

    static final ConversionService conversionService =
            ApplicationConversionService.getSharedInstance();

    @SuppressWarnings("unchecked")
    public static JsonObject convert(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof JsonObject) {
            return (JsonObject) object;
        }
        if (object instanceof Map) {
            return new JsonObject((Map<String, ?>) object);
        }
        return null;
    }

    public JsonObject() {
        super(32);
    }

    private JsonObject(Map<String, ?> map) {
        super(map);
    }

    public String getValue(String key) {
        return conversionService.convert(get(key), String.class);
    }

    public boolean checkKey(String key, String expecting) {
        return Objects.equals(getValue(key), expecting);
    }

    public Integer getInteger(String key) {
        return conversionService.convert(get(key), Integer.class);
    }

    public boolean checkKey(String key, int expecting) {
        return Objects.equals(getInteger(key), expecting);
    }

    public Long getLong(String key) {
        return conversionService.convert(get(key), Long.class);
    }

    public boolean checkKey(String key, long expecting) {
        return Objects.equals(getLong(key), expecting);
    }

    public Double getDouble(String key) {
        return conversionService.convert(get(key), Double.class);
    }

    public boolean checkKey(String key, double expecting, double delta) {
        return Math.abs(getDouble(key) - expecting) < delta;
    }

    public Boolean getBoolean(String key) {
        return conversionService.convert(String.valueOf(get(key)), Boolean.class);
    }

    public boolean checkKey(String key, boolean expecting) {
        return Objects.equals(getBoolean(key), expecting);
    }

    public JsonArray getArray(String key) {
        Object object = this.get(key);
        return JsonArray.convert(object);
    }

    public JsonObject getObject(String key) {
        Object object = this.get(key);
        return JsonObject.convert(object);
    }

    public JsonObject fluentPut(String key, Object value) {
        super.put(key, value);
        return this;
    }

    @Override
    public String toString() {
        return JsonTool.mapToJson(this);
    }
}

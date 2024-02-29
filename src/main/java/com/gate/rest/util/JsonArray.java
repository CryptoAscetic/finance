package com.gate.rest.util;

import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.core.convert.ConversionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class JsonArray extends ArrayList<Object> {

    static final ConversionService conversionService =
            ApplicationConversionService.getSharedInstance();

    public static JsonArray convert(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof JsonArray) {
            return (JsonArray) object;
        }
        if (object instanceof List) {
            return new JsonArray((List<?>) object);
        }
        return null;
    }

    public JsonArray() {
    }

    private JsonArray(List<?> list) {
        super(list);
    }

    public String getValueAt(int index) {
        return conversionService.convert(get(index), String.class);
    }

    public boolean checkIndex(int index, String expecting) {
        return Objects.equals(getValueAt(index), expecting);
    }

    public Integer getIntegerAt(int index) {
        return conversionService.convert(get(index), Integer.class);
    }

    public boolean checkIndex(int index, int expecting) {
        return Objects.equals(getIntegerAt(index), expecting);
    }

    public Long getLongAt(int index) {
        return conversionService.convert(get(index), Long.class);
    }

    public boolean checkIndex(int key, long expecting) {
        return Objects.equals(getLongAt(key), expecting);
    }

    public Double getDoubleAt(int index) {
        return conversionService.convert(get(index), Double.class);
    }

    public boolean checkIndex(int index, double expecting, double delta) {
        return Math.abs(getDoubleAt(index) - expecting) < delta;
    }

    public Boolean getBooleanAt(int index) {
        return conversionService.convert(String.valueOf(get(index)), Boolean.class);
    }


    public boolean checkIndex(int index, boolean expecting) {
        return Objects.equals(getBooleanAt(index), expecting);
    }

    public JsonArray getArrayAt(int index) {
        Object object = get(index);
        return JsonArray.convert(object);
    }

    public JsonObject getObjectAt(int index) {
        Object object = get(index);
        return JsonObject.convert(object);
    }

    public <T> void forEachWith(Consumer<T> consumer, Function<Integer, T> indexConverter) {
        for (int i = 0; i < size(); i++) {
            consumer.accept(indexConverter.apply(i));
        }
    }

    public void forEachArray(Consumer<JsonArray> consumer) {
        forEachWith(consumer, this::getArrayAt);
    }

    public void forEachInteger(Consumer<Integer> consumer) {
        forEachWith(consumer, this::getIntegerAt);
    }

    public void forEachObject(Consumer<JsonObject> consumer) {
        forEachWith(consumer, this::getObjectAt);
    }

    public void forEachValue(Consumer<String> consumer) {
        forEachWith(consumer, this::getValueAt);
    }

    public void forEachLong(Consumer<Long> consumer) {
        forEachWith(consumer, this::getLongAt);
    }


    public void forEachBoolean(Consumer<Boolean> consumer) {
        forEachWith(consumer, this::getBooleanAt);
    }

    public void forEachDouble(Consumer<Double> consumer) {
        forEachWith(consumer, this::getDoubleAt);
    }

    @Override
    public String toString() {
        return JsonTool.mapToJson(this);
    }
}

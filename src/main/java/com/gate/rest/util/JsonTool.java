package com.gate.rest.util;

import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.internal.bind.JsonTreeWriter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Description: Json转换工具
 * @Author: Administrator
 * @UpdateUser: Li Shuang
 * @UpdateDate: 20-11-9 下午3:33
 * @UpdateRemark: 类及方法的注释描述
 */
public class JsonTool {

    /**
     * 大量参考了 #{@link JsonTreeWriter}, 用于将一个Bean对象转换为一个Map
     */
    static class JsonMapWriter extends JsonWriter {
        private static final Writer UNWRITABLE_WRITER = new Writer() {
            @Override
            public void write(char[] buffer, int offset, int counter) {
                throw new AssertionError();
            }

            @Override
            public void flush() throws IOException {
                throw new AssertionError();
            }

            @Override
            public void close() throws IOException {
                throw new AssertionError();
            }
        };
        /** Added to the top of the stack when this writer is closed to cause following ops to fail. */
        private static final Object SENTINEL_CLOSED = new Object();

        /** The JsonElements and JsonArrays under modification, outermost to innermost. */
        private final List<Object> stack = new ArrayList<>();

        /** The name for the next JSON object value. If non-null, the top of the stack is a JsonObject. */
        private String pendingName;

        /** the JSON element constructed by this writer. */
        private Object product = new HashMap<>();

        public JsonMapWriter() {
            super(UNWRITABLE_WRITER);
        }

        /**
         * Returns the top level object produced by this writer.
         */
        public Map<String, Object> getMap() {
            if (!stack.isEmpty()) {
                throw new IllegalStateException("Expected one JSON element but was " + stack);
            }
            if (product instanceof JsonObject) {
                return (JsonObject) product;
            }
            throw new IllegalStateException();
        }

        public List<Object> getArray() {
            if (!stack.isEmpty()) {
                throw new IllegalStateException("Expected one JSON element but was " + stack);
            }

            if (product instanceof JsonArray) {
                return (JsonArray) product;
            }
            throw new IllegalStateException();
        }

        private Object peek() {
            return stack.get(stack.size() - 1);
        }

        private void put(Object value) {
            if (pendingName != null) {
                if (value != null || getSerializeNulls()) {
                    JsonObject object = (JsonObject) peek();
                    object.put(pendingName, value);
                }
                pendingName = null;
            } else if (stack.isEmpty()) {

                if (value instanceof JsonObject) {
                    product = value;
                } else if (value instanceof JsonArray) {
                    product = value;
                } else {
                    throw new IllegalStateException();
                }
            } else {
                Object element = peek();
                if (element instanceof JsonArray) {
                    ((JsonArray) element).add(value);
                } else {
                    throw new IllegalStateException();
                }
            }
        }

        @Override
        public JsonWriter beginArray() throws IOException {
            JsonArray array = new JsonArray();
            put(array);
            stack.add(array);
            return this;
        }

        @Override
        public JsonWriter endArray() throws IOException {
            if (stack.isEmpty() || pendingName != null) {
                throw new IllegalStateException();
            }
            Object element = peek();
            if (element instanceof JsonArray) {
                stack.remove(stack.size() - 1);
                return this;
            }
            throw new IllegalStateException();
        }

        @Override
        public JsonWriter beginObject() throws IOException {
            JsonObject object = new JsonObject();
            put(object);
            stack.add(object);
            return this;
        }

        @Override
        public JsonWriter endObject() throws IOException {
            if (stack.isEmpty() || pendingName != null) {
                throw new IllegalStateException();
            }
            Object element = peek();
            if (element instanceof JsonObject) {
                stack.remove(stack.size() - 1);
                return this;
            }
            throw new IllegalStateException();
        }

        @Override
        public JsonWriter name(String name) throws IOException {
            if (stack.isEmpty() || pendingName != null) {
                throw new IllegalStateException();
            }
            Object element = peek();
            if (element instanceof JsonObject) {
                pendingName = name;
                return this;
            }
            throw new IllegalStateException();
        }

        @Override
        public JsonWriter value(String value) throws IOException {
            if (value == null) {
                return nullValue();
            }
            put(value);
            return this;
        }

        @Override
        public JsonWriter nullValue() throws IOException {
            put(null);
            return this;
        }

        @Override
        public JsonWriter value(boolean value) throws IOException {
            put(value);
            return this;
        }

        @Override
        public JsonWriter value(Boolean value) throws IOException {
            if (value == null) {
                return nullValue();
            }
            put(value);
            return this;
        }

        @Override
        public JsonWriter value(double value) throws IOException {
            if (!isLenient() && (Double.isNaN(value) || Double.isInfinite(value))) {
                throw new IllegalArgumentException("JSON forbids NaN and infinities: " + value);
            }
            put(value);
            return this;
        }

        @Override
        public JsonWriter value(long value) throws IOException {
            put(value);
            return this;
        }

        @Override
        public JsonWriter value(Number value) throws IOException {
            if (value == null) {
                return nullValue();
            }

            if (!isLenient()) {
                double d = value.doubleValue();
                if (Double.isNaN(d) || Double.isInfinite(d)) {
                    throw new IllegalArgumentException("JSON forbids NaN and infinities: " + value);
                }
            }

            put(value);
            return this;
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
            if (!stack.isEmpty()) {
                throw new IOException("Incomplete document");
            }
            stack.add(SENTINEL_CLOSED);
        }
    }

    static class DateDeserializer implements JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            String str = je.getAsString();
            return new Date(Long.parseLong(str));
        }
    }

    static class BoolDeserializer implements JsonDeserializer<Boolean> {

        @Override
        public Boolean deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            String str = je.getAsString();
            return "1".equals(str) || "true".equals(str);
        }
    }

    static class EmptyStringToNullDeserializer<T> implements JsonDeserializer<T> {

        Function<String, T> converter;

        public EmptyStringToNullDeserializer(Function<String, T> converter) {
            this.converter = converter;
        }

        @Override
        public T deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            String str = je.getAsString();
            if (StringUtils.hasText(str)) {
                return converter.apply(str);
            } else {
                return null;
            }
        }
    }


//    static class NumberDeserializer implements JsonDeserializer<Number> {
//
//        @Override
//        public Number deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
//            Number num = je.getAsNumber();
//            if(true)return num;
//            if (Math.ceil(num.doubleValue()) == num.longValue()) {
//                return num.longValue();
//            } else {
//                return num.doubleValue();
//            }
//        }
//    }

    /**
     解决方案来源
     https://stackoverflow.com/questions/36508323/how-can-i-prevent-gson-from-converting-integers-to-doubles
     */
    static class MapDeserializerDoubleAsIntFix implements JsonDeserializer<Map<String, Object>> {

        @Override
        @SuppressWarnings("unchecked")
        public Map<String, Object> deserialize(JsonElement json, Type typeOfT,
                                               JsonDeserializationContext context) throws JsonParseException {
            if (!json.isJsonObject()) {
                throw new JsonParseException("必须是jsonObject: " + json);
            }
            return (Map<String, Object>) read(json);
        }

        public Object read(JsonElement in) {

            if (in.isJsonArray()) {
                List<Object> list = new ArrayList<>();
                for (JsonElement anArr : in.getAsJsonArray()) {
                    list.add(read(anArr));
                }
                return list;
            } else if (in.isJsonObject()) {
                Map<String, Object> map = new LinkedTreeMap<>();
                Set<Map.Entry<String, JsonElement>> entitySet = in.getAsJsonObject().entrySet();
                for (Map.Entry<String, JsonElement> entry : entitySet) {
                    map.put(entry.getKey(), read(entry.getValue()));
                }
                return map;
            } else if (in.isJsonPrimitive()) {
                JsonPrimitive prim = in.getAsJsonPrimitive();
                if (prim.isBoolean()) {
                    return prim.getAsBoolean();
                } else if (prim.isString()) {
                    return prim.getAsString();
                } else if (prim.isNumber()) {

                    Number num = prim.getAsNumber();
                    // here you can handle double int/long values
                    // and return any type you want
                    // this solution will transform 3.0 float to long values
                    if (Math.ceil(num.doubleValue()) == num.longValue()) {
                        return num.longValue();
                    } else {
                        return num.doubleValue();
                    }
                }
            }
            return null;
        }
    }

    static final Supplier<GsonBuilder> BASIC_BUILDER_SUPPLIER = () -> {
        return new GsonBuilder()
                .enableComplexMapKeySerialization()
                .registerTypeAdapter(
                        new TypeToken<Map<String, Object>>() {
                        }.getType(), new MapDeserializerDoubleAsIntFix()
                ).registerTypeAdapter(
                        Boolean.class, new BoolDeserializer()
                ).registerTypeAdapter(
                        Date.class, new DateDeserializer()
                ).registerTypeAdapter(
                        boolean.class, new BoolDeserializer()
                ).registerTypeAdapter(
                        BigDecimal.class, new EmptyStringToNullDeserializer<>(BigDecimal::new)
                ).registerTypeAdapter(
                        Byte.class, new EmptyStringToNullDeserializer<>(Byte::valueOf)
                );
    };
    static final Gson GSON = BASIC_BUILDER_SUPPLIER.get().create();

    static final Gson GSON_SERIALIZE_NULLS = BASIC_BUILDER_SUPPLIER.get().serializeNulls().create();

    static final Gson GSON_DATE_TO_TIMESTAMP = BASIC_BUILDER_SUPPLIER.get()
            .registerTypeAdapter(
                    Date.class, (JsonSerializer<Date>) (src, typeOfSrc, context)
                            -> new JsonPrimitive(src.getTime())
            )
            .registerTypeAdapter(
                    LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context)
                            -> new JsonPrimitive(DateUtil.fromUTCLocalDate(src).getTime())
            )
            .registerTypeAdapter(
                    LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context)
                            -> new JsonPrimitive(DateUtil.fromUTCLocalDateTime(src).getTime())
            )
            .registerTypeAdapter(
                    Duration.class, (JsonSerializer<Duration>) (src, typeOfSrc, context)
                            -> new JsonPrimitive(src.toMillis())
            )
            .registerTypeAdapter(
                    Instant.class, (JsonSerializer<Instant>) (src, typeOfSrc, context)
                            -> new JsonPrimitive(src.toEpochMilli())
            )
            .create();

    static final Map<FieldNamingPolicy, Gson> POLICY_MAP = Stream.of(FieldNamingPolicy.values()).collect(
            Collectors.toMap(p -> p, p -> BASIC_BUILDER_SUPPLIER.get().setFieldNamingPolicy(p).create())
    );


    /**
     检查一个字符串是否是json 格式
     @param string
     @return
     */
    public static boolean isJson(String string) {
        try {
            JsonParser.parseString(string);
            return true;
        } catch (JsonParseException ex) {
            return false;
        }
    }

    public static boolean isJsonObject(String string) {
        try {
            return JsonParser.parseString(string).isJsonObject();
        } catch (JsonParseException ex) {
            return false;
        }
    }

    public static JsonObject toObject(String jsonString) {
        return JsonObject.convert(jsonToObject(jsonString));
    }

    public static JsonArray toArray(String jsonString) {
        Type type = new TypeToken<List<Object>>() {
        }.getType();
        List<Object> list = jsonToObject(jsonString, type);
        return JsonArray.convert(list);
    }

    /**
     * @Description json 转换为范型. 此方法为一个特化方法, 返回Map<String, String>
     * @param jsonString
     * @return Map
     *
     */
    @Deprecated
    public static Map<String, Object> jsonStringToObject(String jsonString) {
        try {
            return GSON.fromJson(jsonString, new TypeToken<Map<String, String>>() {
            }.getType());
        } catch (JsonParseException ex) {
            throw new IllegalStateException("解析Json 失败: " + jsonString, ex);
        }
    }

    /**
     * @Description json 转换为范型. 此方法为一个特化方法, 返回Map<String, String>
     * @param jsonString
     * @return Map
     */
    public static Map<String, String> jsonStringToMapStr(String jsonString) {
        try {
            return GSON.fromJson(jsonString, new TypeToken<Map<String, String>>() {
            }.getType());
        } catch (JsonParseException ex) {
            throw new IllegalStateException("解析Json 失败: " + jsonString, ex);
        }
    }

    /**
     * @Description json 转换为map  TypeToken，它是gson提供的数据类型转换器，可以支持各种数据集合类型转换
     * @param jsonString
     * @return Map
     */
    public static Map<String, Object> jsonToObject(String jsonString) {
        try {
            return GSON.fromJson(jsonString, new TypeToken<Map<String, Object>>() {
            }.getType());
        } catch (JsonParseException ex) {
            throw new IllegalStateException("解析Json 失败: " + jsonString, ex);
        }
    }

    /**
     * @Description 将json字符串转换为map集合  TypeToken，它是gson提供的数据类型转换器，可以支持各种数据集合类型转换
     * @param jsonString
     * @return
     */
    public static List<Map<String, Object>> jsonToListMap(String jsonString) {
        try {
            List<Map<String, Object>> retMap = GSON.fromJson(jsonString,
                    new TypeToken<List<Map<String, Object>>>() {
                    }.getType());
            return retMap;
        } catch (JsonParseException ex) {
            throw new IllegalStateException("解析Json 失败: " + jsonString, ex);
        }
    }

    /**
     * @Description 对象 转换为Json Map
     * @param object
     * @return String
     */
    public static Map<String, Object> mapToObject(Object object) {
        return mapToJsonMap(GSON, object);
    }

    /**
     * @Description 对象 转换为Json 字符串
     * @param object
     * @return String
     */
    public static String mapToJson(Object object) {
        // 默认直接使用日期转换
        return mapToJsonWithDateToTimestamp(object);
    }

    /**
     * @Description 对象 转换为Json 字符串, 属性以下划线标准输出
     * @param object
     * @return String
     */
    public static String mapToJsonUnderscore(Object object) {
        return POLICY_MAP.get(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).toJson(object);
    }

    /**
     * @Description 对象 转换为Json 字符串 (特化方法. 会将value = null 的key输出)
     * @param object
     * @return String
     */
    public static String mapWithNullToJson(Object object) {
        return GSON_SERIALIZE_NULLS.toJson(object);
    }

    /**
     * @Description 对象 转换为Json 字符串 (特化方法. 会将Date 对象转换为时间戳输出)
     * @param object
     * @return String
     */
    public static String mapToJsonWithDateToTimestamp(Object object) {
        return GSON_DATE_TO_TIMESTAMP.toJson(object);
    }


    /**
     * @Description 对象 转换为Json Map
     * @param object
     * @return String
     */
    public static Map<String, Object> mapToJsonMap(Object object) {
        return mapToJsonMap(GSON, object);
    }

    /**
     * @Description 对象 转换为Json Map (特化方法. 会将value = null 的key输出)
     * @param object
     * @return String
     */
    public static Map<String, Object> mapWithNullToJsonMap(Object object) {
        return mapToJsonMap(GSON_SERIALIZE_NULLS, object);
    }

    /**
     * @Description 对象 转换为Json Map (特化方法. 会将Date 对象转换为时间戳输出)
     * @param object
     * @return String
     */
    public static Map<String, Object> mapToJsonMapWithDateToTimestamp(Object object) {
        return mapToJsonMap(GSON_DATE_TO_TIMESTAMP, object);
    }


    /**
     * @Description 对象转换为Map
     * @param object
     * @return String
     */
    static Map<String, Object> mapToJsonMap(Gson gson, Object object) {
        JsonMapWriter jsonMapWriter = new JsonMapWriter();
        gson.toJson(object, object.getClass(), jsonMapWriter);
        return jsonMapWriter.getMap();
    }


    /**
     * @Description 对象 转换为Json List
     * @param object
     * @return String
     */
    public static <T> List<T> mapToJsonArray(Object object) {
        return mapToJsonArray(GSON, object);
    }

    /**
     * @Description 对象 转换为Json List (特化方法. 会将value = null 的key输出)
     * @param object
     * @return String
     */
    public static <T> List<T> mapWithNullToJsonArray(Object object) {
        return mapToJsonArray(GSON_SERIALIZE_NULLS, object);
    }

    /**
     * @Description 对象 转换为Json List (特化方法. 会将Date 对象转换为时间戳输出)
     * @param object
     * @return String
     */
    public static <T> List<T> mapToJsonArrayWithDateToTimestamp(Object object) {
        return mapToJsonArray(GSON_DATE_TO_TIMESTAMP, object);
    }

    /**
     * @Description 对象转换为Map
     * @param object
     * @return String
     */
    static <T> List<T> mapToJsonArray(Gson gson, Object object) {
        JsonMapWriter jsonMapWriter = new JsonMapWriter();
        gson.toJson(object, object.getClass(), jsonMapWriter);
        return (List<T>) jsonMapWriter.getArray();
    }

    /**
     * @Description 将Json 字符串 转换成列表类型, 可以使用new ParameterizedTypeReference 对象产生泛型返回
     * @param <T>
     * @param jsonString
     * @param type
     * @see org.springframework.core.ParameterizedTypeReference
     * @return
     */
    public static <T> T jsonToObject(String jsonString, Type type) {
        try {
            return GSON.fromJson(jsonString, type);
        } catch (JsonParseException ex) {
            throw new IllegalStateException("解析Json 失败: " + jsonString, ex);
        }
    }

    public static <T> T jsonToObjectByUnderscore(String jsonString, Type type) {
        try {
            return POLICY_MAP.get(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).fromJson(jsonString, type);
        } catch (JsonParseException ex) {
            throw new IllegalStateException("解析Json 失败: " + jsonString, ex);
        }
    }


    public static Object getObject(String path, Map<String, ?> map) {
        return search(map, path.split("\\."), 0);
    }

    public static Integer getInt(String path, Map<String, Object> map) {
        Object searched = search(map, path.split("\\."), 0);
        return ApplicationConversionService.getSharedInstance().convert(searched, Integer.class);
    }

    public static String getValue(String path, Map<String, Object> map) {
        Object searched = search(map, path.split("\\."), 0);
        return ApplicationConversionService.getSharedInstance().convert(searched, String.class);
    }

    public static Boolean getBoolean(String path, Map<String, Object> map) {
        Object searched = search(map, path.split("\\."), 0);
        return ApplicationConversionService.getSharedInstance().convert(searched, Boolean.class);
    }

    public static Double getDouble(String path, Map<String, Object> map) {
        Object searched = search(map, path.split("\\."), 0);
        return ApplicationConversionService.getSharedInstance().convert(searched, Double.class);
    }

    @SuppressWarnings("unchecked")
    static Object search(Map<String, ?> map, String[] keys, int searchFrom) {
        String key = keys[searchFrom];
        if (searchFrom == keys.length - 1) {
            // 最后的key
            if (map.containsKey(key)) {
                // 找到最终的结果
                return map.get(key);
            } else {
                // 无法找到
                return null;
            }
        } else {
            // 不是最后的key
            if (!map.containsKey(key)) {
                return null;
            }
            Object value = map.get(key);
            if (value instanceof Map) {
                // 向后一个搜索
                return search((Map<String, Object>) value, keys, searchFrom + 1);
            } else if (value instanceof List) {
                return search((List<Object>) value, keys, searchFrom + 1);
            } else {
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    static Object search(List<Object> list, String[] keys, int searchFrom) {
        String key = keys[searchFrom];
        int index;
        try {
            index = Integer.parseInt(key);
        } catch (NumberFormatException ex) {
            return null;
        }
        if (index < 0 || index >= list.size()) {
            return null;
        }
        if (searchFrom == keys.length - 1) {
            // 最后的key
            return list.get(index);
        } else {
            // 不是最后的key
            Object value = list.get(index);
            if (value instanceof Map) {
                // 向后一个搜索
                return search((Map<String, Object>) value, keys, searchFrom + 1);
            } else if (value instanceof List) {
                return search((List<Object>) value, keys, searchFrom + 1);
            } else {
                return null;
            }
        }
    }
}

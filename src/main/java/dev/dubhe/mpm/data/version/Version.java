package dev.dubhe.mpm.data.version;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import dev.dubhe.mpm.exceptions.VersionSyntaxErrorException;
import io.micrometer.common.util.StringUtils;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class Version {
    public static final Version MAX = new Version(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    public static final Version MIN = new Version(0);
    public final int major;
    public final int minor;
    public final int patch;

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public Version(int major, int minor) {
        this(major, minor, 0);
    }

    public Version(int major) {
        this(major, 0);
    }

    public Version(String version) {
        if (!Pattern.matches("^[0-9]+[.]?[0-9]*[.]?[0-9]*$", version)) {
            throw new VersionSyntaxErrorException(version);
        }
        String[] versions = version.split("[.]");
        int temp;
        this.major = versions.length >= 1 ? Integer.parseInt(versions[0]) : 0;
        this.minor = versions.length >= 2 ? Integer.parseInt(versions[1]) : 0;
        this.patch = versions.length >= 3 ? Integer.parseInt(versions[2]) : 0;
        if (compare(this, Version.MIN) < 0) {
            throw new VersionSyntaxErrorException(version);
        }
    }

    /**
     * Compares two {@code version} values numerically.
     *
     * @param a the first {@code version} to compare
     * @param b the second {@code version} to compare
     * @return the value {@code 0} if {@code x == y};
     * a value less than {@code 0} if {@code x < y}; and
     * a value greater than {@code 0} if {@code x > y}
     */
    public static int compare(Version a, Version b) {
        if (a.major > b.major) return 1;
        else if (a.major < b.major) return -1;
        else if (a.minor > b.minor) return 1;
        else if (a.minor < b.minor) return -1;
        else return Integer.compare(a.patch, b.patch);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Version version && Version.compare(version, this) == 0;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public String toString() {
        return "%s.%s.%s".formatted(this.major, this.minor, this.patch);
    }

    public static final class Serializer extends JsonSerializer<Version> implements ContextualSerializer {

        @Override
        public void serialize(Version value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) gen.writeNull();
            else gen.writeString(value.toString());
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
            if (property == null) {
                return prov.findNullValueSerializer(property);
            }
            if (Objects.equals(property.getType().getRawClass(), Version.class)) {
                Formatter annotation = property.getAnnotation(Formatter.class);
                if (annotation != null) {
                    // 这里可以获取注解中的一些参数
                    String pattern = annotation.pattern();
                    return this;
                }
            }
            return prov.findValueSerializer(property.getType(), property);
        }
    }

    public static final class Deserializer extends JsonDeserializer<Version> implements ContextualDeserializer {

        @Override
        public Version deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            try {
                if (p != null && StringUtils.isNotEmpty(p.getText())) {
                    return new Version(p.getText());
                } else {
                    return null;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
            //判断beanProperty是不是空
            if (property == null) {
                return ctxt.findNonContextualValueDeserializer(property.getType());
            }
            //判断类型是否是String
            if (Objects.equals(property.getType().getRawClass(), Version.class)) {
                Formatter annotation = property.getAnnotation(Formatter.class);
                if (annotation != null) {
                    // 这里可以获取注解中的一些参数
                    String pattern = annotation.pattern();
                    return this;
                }
            }
            return ctxt.findContextualValueDeserializer(property.getType(), property);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonSerialize(using = Serializer.class)
    @JsonDeserialize(using = Deserializer.class)
    public @interface Formatter {
        // todo 可以定义格式化方式
        String pattern() default "";
    }
}

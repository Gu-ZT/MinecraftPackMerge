package dev.dubhe.mpm.data.version;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import dev.dubhe.mpm.exceptions.VersionsSyntaxErrorException;
import io.micrometer.common.util.StringUtils;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class Versions {
    public final Version max;
    public final boolean hasMax;
    public final Version min;
    public final boolean hasMin;

    public Versions(Version max, boolean hasMax, Version min, boolean hasMin) {
        this.max = max;
        this.hasMax = hasMax;
        this.min = min;
        this.hasMin = hasMin;
    }

    public Versions(String versions) {
        // 1.0.0
        Pattern versionRegex = Pattern.compile("[0-9]+[.]?[0-9]*[.]?[0-9]*");
        // [1.0.0,2.0.0]
        Pattern versionsRegex1 = Pattern.compile("^[\\[(]([0-9]+[.]?[0-9]*[.]?[0-9]*)?,([0-9]+[.]?[0-9]*[.]?[0-9]*)?[])]$");
        // >=1.0.0
        Pattern versionsRegex2 = Pattern.compile("^((>=)|(<=)|>|<|(==))?[0-9]+[.]?[0-9]*[.]?[0-9]*$");
        versions = versions.replace(" ", "");
        if (versionsRegex1.matcher(versions).matches()) {
            String[] temps = versions.split(",");
            String temp;
            this.min = !"".equals(temp = temps[0].substring(1)) ? new Version(temp) : Version.MIN;
            this.hasMin = temps[0].charAt(0) == '[';
            this.max = !"".equals(temp = temps[1].substring(0, temps[1].length() - 1)) ? new Version(temp) : Version.MAX;
            this.hasMax = temps[1].charAt(temps[1].length() - 1) == ']';
            if (Version.compare(max, min) >= 0) return;
        } else if (versionsRegex2.matcher(versions).matches()) {
            Matcher matcher = versionRegex.matcher(versions);
            if (matcher.find()) {
                String v = matcher.group(0);
                Version version = new Version(matcher.group(0));
                String compare = versions.replace(v, "");
                switch (compare) {
                    case ">" -> {
                        this.min = version;
                        this.hasMin = false;
                        this.max = Version.MAX;
                        this.hasMax = true;
                    }
                    case ">=" -> {
                        this.min = version;
                        this.hasMin = true;
                        this.max = Version.MAX;
                        this.hasMax = true;
                    }
                    case "<" -> {
                        this.min = Version.MIN;
                        this.hasMin = true;
                        this.max = version;
                        this.hasMax = false;
                    }
                    case "<=" -> {
                        this.min = Version.MIN;
                        this.hasMin = true;
                        this.max = version;
                        this.hasMax = true;
                    }
                    case "==" -> {
                        this.min = version;
                        this.hasMin = true;
                        this.max = version;
                        this.hasMax = true;
                    }
                    default -> throw new VersionsSyntaxErrorException(versions);
                }
                return;
            }
        }
        throw new VersionsSyntaxErrorException(versions);
    }

    public boolean contains(Version version) {
        boolean a = Version.compare(version, this.min) > 0;
        boolean b = this.hasMin && Version.compare(version, this.min) == 0;
        boolean c = Version.compare(version, this.max) < 0;
        boolean d = this.hasMax && Version.compare(version, this.max) == 0;
        return a && b && c && d;
    }

    @Override
    public String toString() {
        return "%s%s,%s%s".formatted(hasMin ? "[" : "(", min.toString(), max.toString(), hasMax ? "]" : ")");
    }

    public static final class Serializer extends JsonSerializer<Versions> implements ContextualSerializer {

        @Override
        public void serialize(Versions value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) gen.writeNull();
            else gen.writeString(value.toString());
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
            if (property == null) {
                return prov.findNullValueSerializer(property);
            }
            if (Objects.equals(property.getType().getRawClass(), Versions.class)) {
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

    public static final class Deserializer extends JsonDeserializer<Versions> implements ContextualDeserializer {

        @Override
        public Versions deserialize(JsonParser p, DeserializationContext ctxt) {
            try {
                if (p != null && StringUtils.isNotEmpty(p.getText())) {
                    return new Versions(p.getText());
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
            if (Objects.equals(property.getType().getRawClass(), Versions.class)) {
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

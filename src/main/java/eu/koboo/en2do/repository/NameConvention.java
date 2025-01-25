package eu.koboo.en2do.repository;

import eu.koboo.en2do.utility.NameUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.function.Function;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum NameConvention {

    CAMEL_CASE_LOWER(NameUtils::toCamelCaseLower),
    CAMEL_CASE_UPPER(NameUtils::toCamelCaseUpper),
    SNAKE_CASE(NameUtils::toSnakeCase),
    ;

    Function<String, String> generateFunction;

    public String generate(Class<?> clazz) {
        if (clazz == null) {
            throw new NullPointerException("clazz is null");
        }
        String clazzName = clazz.getSimpleName();
        return generateFunction.apply(clazzName);
    }
}

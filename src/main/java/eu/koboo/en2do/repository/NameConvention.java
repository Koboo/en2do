package eu.koboo.en2do.repository;

import eu.koboo.en2do.utility.NameCasingUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.function.Function;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum NameConvention {

    CAMEL_CASE_LOWER(NameCasingUtils::toCamelCase),
    CAMEL_CASE_UPPER(NameCasingUtils::toPascalCase),
    SNAKE_CASE(NameCasingUtils::toSnakeCase),
    FLAT_CASE(NameCasingUtils::toFlatCase),
    MACRO_CASE(NameCasingUtils::toMacroCase),
    KEBAB_CASE(NameCasingUtils::toKebabCase),
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

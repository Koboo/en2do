package eu.koboo.en2do.misc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public enum MethodOperator {

    FIND("findBy",List.class),
    DELETE("deleteBy", boolean.class),
    EXISTS("existsBy", boolean.class),
    COUNT("countBy", long.class);

    public static final MethodOperator[] VALUES = MethodOperator.values();

    String keyword;
    Class<?> returnTypes;

    public String removeOperatorFrom(String textWithOperator) {
        return textWithOperator.replaceFirst(getKeyword(), "");
    }

    public static MethodOperator parseMethodStartsWith(String methodNamePart) {
        for (MethodOperator operator : VALUES) {
            if(!methodNamePart.startsWith(operator.getKeyword())) {
                continue;
            }
            return operator;
        }
        return null;
    }
}
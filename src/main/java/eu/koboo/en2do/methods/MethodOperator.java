package eu.koboo.en2do.methods;

import eu.koboo.en2do.exception.*;
import eu.koboo.en2do.utility.GenericUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public enum MethodOperator {

    FIND_FIRST("findFirstBy", (method, returnType, entityClass, repoClass) -> {
        if (!GenericUtils.isTypeOf(entityClass, returnType)) {
            throw new MethodFindReturnTypeException(method, entityClass, repoClass);
        }
    }),
    FIND_MANY("findManyBy", (method, returnType, entityClass, repoClass) -> {
        if (!GenericUtils.isTypeOf(List.class, returnType)) {
            throw new MethodFindListReturnTypeException(method, entityClass, repoClass);
        }
        Class<?> listType = GenericUtils.getGenericTypeOfReturnList(method);
        if (!listType.isAssignableFrom(entityClass)) {
            throw new MethodFindListTypeException(method, repoClass, listType);
        }
    }),
    DELETE("deleteBy", (method, returnType, entityClass, repoClass) -> {
        if (!GenericUtils.isTypeOf(Boolean.class, returnType)) {
            throw new MethodBooleanReturnTypeException(method, repoClass);
        }
    }),
    EXISTS("existsBy", (method, returnType, entityClass, repoClass) -> {
        if (!GenericUtils.isTypeOf(Boolean.class, returnType)) {
            throw new MethodBooleanReturnTypeException(method, repoClass);
        }
    }),
    COUNT("countBy", ((method, returnType, entityClass, repoClass) -> {
        if (!GenericUtils.isTypeOf(Long.class, returnType)) {
            throw new MethodLongReturnTypeException(method, repoClass);
        }
    }));

    public static final MethodOperator[] VALUES = MethodOperator.values();

    String keyword;
    ReturnTypeValidator returnTypeValidator;

    public String removeOperatorFrom(String textWithOperator) {
        return textWithOperator.replaceFirst(getKeyword(), "");
    }

    public static MethodOperator parseMethodStartsWith(String methodNamePart) {
        for (MethodOperator operator : VALUES) {
            if (!methodNamePart.startsWith(operator.getKeyword())) {
                continue;
            }
            return operator;
        }
        return null;
    }
}

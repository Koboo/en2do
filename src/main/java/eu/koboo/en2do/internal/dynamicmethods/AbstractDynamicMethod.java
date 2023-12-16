package eu.koboo.en2do.internal.dynamicmethods;

import eu.koboo.en2do.internal.operators.MethodOperator;
import eu.koboo.en2do.repository.Repository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Method;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractDynamicMethod<E, ID, R extends Repository<E, ID>> {

    @Getter
    Method method;

    @Getter
    MethodOperator methodOperator;

    boolean multipleFilter;
    boolean andFilter;

    List<MethodFilterPart> filterPartList;

    public abstract <B> B createFilter(Object[] arguments) throws Exception;
}
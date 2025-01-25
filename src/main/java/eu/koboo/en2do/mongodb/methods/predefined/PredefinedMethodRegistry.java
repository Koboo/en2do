package eu.koboo.en2do.mongodb.methods.predefined;

import eu.koboo.en2do.mongodb.methods.predefined.impl.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PredefinedMethodRegistry {

    Map<String, GlobalPredefinedMethod> predefinedMethodMap;

    public PredefinedMethodRegistry() {
        this.predefinedMethodMap = new HashMap<>();

        // Register the default predefined methods, which can get executed
        // on every created repository.
        registerPredefinedMethod(new MethodCountAll());
        registerPredefinedMethod(new MethodDelete());
        registerPredefinedMethod(new MethodDeleteAll());
        registerPredefinedMethod(new MethodDeleteById());
        registerPredefinedMethod(new MethodDeleteMany());
        registerPredefinedMethod(new MethodDeleteManyById());
        registerPredefinedMethod(new MethodDrop());
        registerPredefinedMethod(new MethodEquals());
        registerPredefinedMethod(new MethodExists());
        registerPredefinedMethod(new MethodExistsById());
        registerPredefinedMethod(new MethodFindAll());
        registerPredefinedMethod(new MethodFindFirstById());
        registerPredefinedMethod(new MethodGetClass());
        registerPredefinedMethod(new MethodGetCollectionName());
        registerPredefinedMethod(new MethodGetEntityClass());
        registerPredefinedMethod(new MethodGetEntityUniqueIdClass());
        registerPredefinedMethod(new MethodGetNativeCollection());
        registerPredefinedMethod(new MethodGetUniqueId());
        registerPredefinedMethod(new MethodHashCode());
        registerPredefinedMethod(new MethodInsertAll());
        registerPredefinedMethod(new MethodPageAll());
        registerPredefinedMethod(new MethodSave());
        registerPredefinedMethod(new MethodSaveAll());
        registerPredefinedMethod(new MethodSetUniqueId());
        registerPredefinedMethod(new MethodSortAll());
        registerPredefinedMethod(new MethodToString());
        registerPredefinedMethod(new MethodUpdateAllFields());
    }

    private void registerPredefinedMethod(GlobalPredefinedMethod predefinedMethod) {
        String methodName = predefinedMethod.getMethodName();
        if (predefinedMethodMap.containsKey(methodName)) {
            throw new RuntimeException("Already registered method with name \"" + methodName + "\".");
        }
        predefinedMethodMap.put(methodName, predefinedMethod);
    }

    public GlobalPredefinedMethod getPredefinedMethod(String methodName) {
        return predefinedMethodMap.get(methodName);
    }

    public boolean isPredefinedMethod(String methodName) {
        return predefinedMethodMap.containsKey(methodName);
    }

    public void close() {
        predefinedMethodMap.clear();
    }
}

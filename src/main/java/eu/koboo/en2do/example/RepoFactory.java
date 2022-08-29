package eu.koboo.en2do.example;

import com.mongodb.client.MongoCollection;
import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.annotation.Entity;
import eu.koboo.en2do.annotation.Id;
import eu.koboo.en2do.annotation.Repository;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

public class RepoFactory {

    private final MongoManager manager;
    private final Map<Class<Repo<?, ?>>, Repo<?, ?>> repoRegistry;

    public RepoFactory(MongoManager manager) {
        this.manager = manager;
        this.repoRegistry = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <E, ID> Repo<E, ID> create(Class<? extends Repo<E, ID>> repoClass) {
        if(repoRegistry.containsKey(repoClass)) {
            return (Repo<E, ID>) repoRegistry.get(repoClass);
        }
        if(!repoClass.isAnnotationPresent(Repository.class)) {
            throw new NullPointerException("No \"@Repository\" annotation in repository class " + repoClass.getName());
        }
        String repositoryName = repoClass.getAnnotation(Repository.class).value();

        Type[] repoInterfaceTypes = repoClass.getGenericInterfaces();
        Type repoType = null;
        for (Type type : repoInterfaceTypes) {
            if(type.getTypeName().split("<")[0].equalsIgnoreCase(Repo.class.getName())) {
                repoType = type;
                break;
            }
        }
        if(repoType == null) {
            throw new NullPointerException("Invalid repository type parameters");
        }
        String entityClassName = repoType.getTypeName().split("<")[1].split(",")[0];
        Class<E> entityClass;
        Class<ID> entityIdClass = null;
        try {
            entityClass = (Class<E>) Class.forName(entityClassName);
            for (Field field : entityClass.getDeclaredFields()) {
                if(!field.isAnnotationPresent(Id.class)) {
                    continue;
                }
                entityIdClass = (Class<ID>) field.getType();
            }
            if(entityIdClass == null) {
                throw new NullPointerException("No field for entity id found");
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        MongoCollection<E> collection = manager.getDatabase().getCollection(repositoryName, entityClass);
        ClassLoader classLoader = repoClass.getClassLoader();
        Class<?>[] interfaces = new Class[]{repoClass};
        Repo<E, ID> repo = (Repo<E, ID>) Proxy.newProxyInstance(classLoader, interfaces, new RepoInvocationHandler<>(collection));
        repoRegistry.put((Class<Repo<?, ?>>) repoClass, repo);
        return repo;
    }
}
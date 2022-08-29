package eu.koboo.en2do.example;

public interface Repo<E, ID> {

    E findById(ID identifier);

    boolean deleteById(ID identifier);

    boolean save(E entity);

    boolean existsById(ID identifier);
}
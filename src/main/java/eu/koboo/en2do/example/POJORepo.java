package eu.koboo.en2do.example;

public interface POJORepo extends Repo<POJO, String> {

    POJO findFirst();
}
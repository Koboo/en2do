package eu.koboo.en2do.example;

import eu.koboo.en2do.MongoManager;

public class Test {

    public static void main(String[] args) {
        MongoManager manager = new MongoManager();
        RepoFactory repoFactory = new RepoFactory(manager);

        POJORepo repo = (POJORepo) repoFactory.create(POJORepo.class);
        POJO first = repo.findFirst();
        if(first != null) {
            System.out.println(first);
        } else {
            System.out.println("Is null");
        }
    }


}
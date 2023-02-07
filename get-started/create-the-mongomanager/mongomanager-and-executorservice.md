# MongoManager and ExecutorService

Since there is also an `AsyncRepository` and asynchronous methods that return `CompletableFuture`, the MongoManager can also be passed its own `ExecutorService` that is responsible for executing the `CompletableFuture`.

_Example of `MongoManager` with `ExecutorService`:_

```java
public class Application {

    public static void main(String[] args) {
        MongoManager manager = new MongoManager(
                    Credentials.of("yourConnectString", "yourDatabase"), 
                    Executors.newSingleThreadExecutor()
            );
    }
}
```

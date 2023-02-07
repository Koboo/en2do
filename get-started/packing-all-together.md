# Packing all together

Now all important classes have been created/instantiated and you can bring things together.

_Example of creating instances:_

```java
public class Application {

    public static void main(String[] args) {
        MongoManager manager = new MongoManager();
        CustomerRepository repository = manager.create(CustomerRepository.class);
    }
}
```

Now you can start working with the created `CustomerRepository`!

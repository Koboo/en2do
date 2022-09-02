package eu.koboo.en2do;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.*;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class Result<T> {

    ExecutorService executorService;
    Callable<T> producer;

    public Result(ExecutorService executorService, Callable<T> producer) {
        this.executorService = executorService;
        this.producer = producer;
    }

    public void subscribe(Consumer<T> subscriber) {
        executorService.execute(() -> {
            try {
                T call = producer.call();
                subscriber.accept(call);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public T await(long timeout, TimeUnit unit) {
        CompletableFuture<T> future = new CompletableFuture<>();
        executorService.execute(() -> {
            try {
                T call = producer.call();
                future.complete(call);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        try {
            return future.get(timeout, unit);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public T await() {
        return await(10, TimeUnit.SECONDS);
    }
}
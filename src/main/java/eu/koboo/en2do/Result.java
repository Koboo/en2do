package eu.koboo.en2do;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class Result<T> {

    ExecutorService executorService;
    Callable<T> producer;
    Consumer<T> subscriber;

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

    public CompletableFuture<T> future() {
        CompletableFuture<T> future = new CompletableFuture<>();
        executorService.execute(() -> {
            try {
                T call = producer.call();
                future.complete(call);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return future;
    }

    public T await(long timeout, TimeUnit unit) {
        CompletableFuture<T> future = future();
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
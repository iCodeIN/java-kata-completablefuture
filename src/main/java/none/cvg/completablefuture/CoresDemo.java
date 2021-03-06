package none.cvg.completablefuture;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Slf4j
public class CoresDemo {

    public static void main(String... args) {

        //Decorator Pattern
        Consumer<Supplier> stopWatch = process -> {

            long startTime = System.currentTimeMillis();

            process.get();

            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;

            LOGGER.info("{}", elapsedTime);
        };

        Supplier<Integer> process = () -> {

            var number = 1;
            var cores = Runtime.getRuntime().availableProcessors();
            var clients = IntStream.rangeClosed(1, (cores - 1) * number).boxed()
                .map(Client::new)
                .collect(toList());

            var futureRequests = clients.stream()
                .map(Client::runAsync)
                .collect(toList());

            futureRequests.stream()
                .map(CompletableFuture::join)
                .collect(toList());

            return 0;
        };

        stopWatch.accept(process);
    }

    static class Client {

        private int x;

        Logger LOGGER = LoggerFactory.getLogger(Client.class);

        public Client(int x) {
            LOGGER.info("new Instance: {}", x);
            this.x = x;
        }

        CompletableFuture<Integer> runAsync() {

            return CompletableFuture
                .supplyAsync(() -> longProcess())
                .orTimeout(60, TimeUnit.SECONDS)
                .handle((response, ex) -> {
                    if (!Objects.isNull(ex)) {
                        LOGGER.error(ex.getLocalizedMessage(), ex);
                    }
                    return response;
                });
        }

        private Integer longProcess() {
            LOGGER.info("Running: {}", this.x);
            sleep(2);
            return 0;
        }

        @SneakyThrows
        private void sleep(int seconds) {
            Thread.sleep(seconds * 1000);
        }
    }
}

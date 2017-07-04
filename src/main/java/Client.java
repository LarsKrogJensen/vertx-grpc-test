import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.IntStream;

public class Client {
    private static final Logger log = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws IOException {
        Vertx vertx = Vertx.vertx();
       
        IntStream.range(0, 4).forEach(i -> vertx.deployVerticle(new ClientVerticle()));

    }

}


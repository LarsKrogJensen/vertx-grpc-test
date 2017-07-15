import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Client {
    private static final Logger log = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) throws IOException {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(ClientVerticle.class.getName(), new DeploymentOptions().setInstances(4));
    }
}


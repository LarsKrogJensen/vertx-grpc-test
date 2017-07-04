import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;

import java.io.IOException;

public class Server {
    public static void main(String[] args) throws IOException {
        Vertx vertx = Vertx.vertx(createVertxOptions());
        vertx.deployVerticle(ServerVerticle.class.getName(), new DeploymentOptions().setInstances(4));

        vertx.deployVerticle(new TimeVerticle());
    }

    private static VertxOptions createVertxOptions() {
        DropwizardMetricsOptions metricsOptions = new DropwizardMetricsOptions()
                .setEnabled(true)
                .setJmxEnabled(true)
                .setRegistryName("metrics");

        return new VertxOptions().setMetricsOptions(metricsOptions);
    }
}

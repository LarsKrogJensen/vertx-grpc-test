import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeVerticle extends AbstractVerticle {
    private EventBus bus;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        bus = vertx.eventBus();
        vertx.setPeriodic(1000, e -> publishTick());
        startFuture.succeeded();
    }

    private void publishTick() {
        bus.publish("TimeTick", sdf.format(new Date()));

    }
}

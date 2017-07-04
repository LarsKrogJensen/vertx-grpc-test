import examples.GreeterGrpc;
import examples.Time;
import examples.TimeRequest;
import io.grpc.stub.StreamObserver;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ServerVerticle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(ServerVerticle.class);

    private List<StreamObserver<Time>> timeStreams = new ArrayList<>();

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        GreeterGrpc.GreeterImplBase service = new Service();

        VertxServer rpcServer = VertxServerBuilder
                .forAddress(vertx, "localhost", 8080)
                .addService(service)
                .build();

        rpcServer.start(ar -> {
            if (ar.succeeded()) {
                log.info("GRPC service started");
                startFuture.succeeded();
            } else {
                log.error("Failed to start GRPC server");
                startFuture.fail(ar.cause());
            }
        });

        vertx.eventBus().consumer("TimeTick", this::timeConsumer);
    }

    private void timeConsumer(Message<String> tick) {
        log.info("Publishing time: " + tick.body());
        for (StreamObserver<Time> timeStream : timeStreams) {
            timeStream.onNext(Time.newBuilder().setCurrentTime(tick.body()).build());
        }
    }

    private class Service extends GreeterGrpc.GreeterImplBase {
        @Override
        public void currentTime(TimeRequest request, StreamObserver<Time> responseObserver) {
            log.info("Current time request");
            ServerVerticle.this.timeStreams.add(responseObserver);
        }
    }
}

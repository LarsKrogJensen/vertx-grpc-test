import examples.GreeterGrpc;
import examples.Time;
import examples.TimeRequest;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import io.vertx.core.AbstractVerticle;
import io.vertx.grpc.VertxChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientVerticle extends AbstractVerticle implements StreamObserver<Time> {
    private static final Logger log = LoggerFactory.getLogger(ClientVerticle.class);
    private GreeterGrpc.GreeterStub greeterStub;

    public ClientVerticle() {
    }

    @Override
    public void start() throws Exception {
        ManagedChannel channel = VertxChannelBuilder
                       .forAddress(vertx, "localhost", 8080)
                       .usePlaintext(true)
                       .build();

        greeterStub = GreeterGrpc.newStub(channel);
        startSubscribe();
    }

    private void startSubscribe() {
        greeterStub.currentTime(TimeRequest.newBuilder().build(), this);
    }

    @Override
    public void onNext(Time time) {
        log.info("Time " + time.getCurrentTime());
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Time error", throwable);
        vertx.setTimer(5000, event -> startSubscribe());
    }

    @Override
    public void onCompleted() {
        log.info("Time completed");
    }
}

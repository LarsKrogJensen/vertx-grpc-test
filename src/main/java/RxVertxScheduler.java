import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.plugins.RxJavaPlugins;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class RxVertxScheduler extends Scheduler {

    private static final Handler<AsyncResult<Object>> NOOP = result -> {
    };

    private final Vertx vertx;
    private final boolean blocking;
    private final boolean ordered;
    private final Context context;

    public RxVertxScheduler(Context context, boolean blocking) {
        this(context, blocking, true);
    }

    public RxVertxScheduler(Context context, boolean blocking, boolean ordered) {
        this.vertx = context.owner();
        this.context = context;
        this.blocking = blocking;
        this.ordered = ordered;
    }

    public RxVertxScheduler(Vertx vertx, boolean blocking) {
        this(vertx, blocking, true);
    }

    public RxVertxScheduler(Vertx vertx, boolean blocking, boolean ordered) {
        this.vertx = vertx;
        this.context = null;
        this.blocking = blocking;
        this.ordered = ordered;
    }

    @Override
    public Worker createWorker() {
        return new WorkerImpl();
    }

    private static final Object DUMB = new JsonObject();

    private class WorkerImpl extends Worker {

        private final ConcurrentHashMap<TimedAction, Object> actions = new ConcurrentHashMap<>();
        private final AtomicBoolean cancelled = new AtomicBoolean();

        @Override
        public Disposable schedule(Runnable action) {
            return schedule(action, 0, TimeUnit.MILLISECONDS);
        }

        @Override
        public Disposable schedule(Runnable action, long delayTime, TimeUnit unit) {
            action = RxJavaPlugins.onSchedule(action);
            TimedAction timed = new TimedAction(action, unit.toMillis(delayTime), 0);
            actions.put(timed, DUMB);
            return timed;
        }

        @Override
        public Disposable schedulePeriodically(Runnable action, long initialDelay, long period, TimeUnit unit) {
            action = RxJavaPlugins.onSchedule(action);
            TimedAction timed = new TimedAction(action, unit.toMillis(initialDelay), unit.toMillis(period));
            actions.put(timed, DUMB);
            return timed;
        }

        @Override
        public void dispose() {
            if (cancelled.compareAndSet(false, true)) {
                actions.keySet().forEach(TimedAction::dispose);
            }
        }

        @Override
        public boolean isDisposed() {
            return cancelled.get();
        }

        class TimedAction implements Disposable, Runnable {

            private final Context context;
            private long id;
            private final Runnable action;
            private final long periodMillis;
            private boolean cancelled;

            public TimedAction(Runnable action, long delayMillis, long periodMillis) {
                this.context = RxVertxScheduler.this.context != null ? RxVertxScheduler.this.context : vertx.getOrCreateContext();
                this.cancelled = false;
                this.action = action;
                this.periodMillis = periodMillis;
                if (delayMillis > 0) {
                    schedule(delayMillis);
                } else {
                    id = -1;
                    execute(null);
                }
            }

            private void schedule(long delay) {
                this.id = vertx.setTimer(delay, this::execute);
            }

            private void execute(Object o) {
                if (blocking) {
                    context.executeBlocking(this::run, ordered, NOOP);
                } else {
                    context.runOnContext(this::run);
                }
            }

            private void run(Object arg) {
                run();
            }

            @Override
            public void run() {
                synchronized (TimedAction.this) {
                    if (cancelled) {
                        return;
                    }
                }
                action.run();
                synchronized (TimedAction.this) {
                    if (periodMillis > 0) {
                        schedule(periodMillis);
                    }
                }
            }

            @Override
            public synchronized void dispose() {
                if (!cancelled) {
                    actions.remove(this);
                    if (id > 0) {
                        vertx.cancelTimer(id);
                    }
                    cancelled = true;
                }
            }

            @Override
            public synchronized boolean isDisposed() {
                return cancelled;
            }
        }
    }
}

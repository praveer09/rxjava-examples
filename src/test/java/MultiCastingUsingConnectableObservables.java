import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.observers.TestSubscriber;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static rx.Observable.defer;
import static rx.Observable.just;

public class MultiCastingUsingConnectableObservables {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Test
    public void doesNotStartEmittingEventsTillConnectIsCalled() throws Exception {
        TestSubscriber<String> testSubscriber = TestSubscriber.create();
        ConnectableObservable<String> observable = just("test")
                .doOnNext(logger::info)
                .publish();

        observable.subscribe(testSubscriber);

        testSubscriber.assertNoValues();

        observable.connect();
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertValue("test");
    }

    @Test
    public void multiCastsSameEventToMultipleSubscribers() throws Exception {
        TestSubscriber<Instant> subscriber1 = TestSubscriber.create();
        TestSubscriber<Instant> subscriber2 = TestSubscriber.create();

        ConnectableObservable<Instant> observable = defer(() -> just(Instant.now()))
                .doOnNext(i -> logger.info(i.toString()))
                .publish();

        observable.subscribe(subscriber1);
        observable.subscribe(subscriber2);

        observable.connect();

        subscriber1.awaitTerminalEvent();
        subscriber2.awaitTerminalEvent();

        assertThat(subscriber1.getOnNextEvents())
                .containsExactlyElementsOf(subscriber2.getOnNextEvents());
    }

    @Test
    public void connectHasToBeCalledAgainToRestartAfterCompleted() throws Exception {
        TestSubscriber<Instant> subscriber1 = TestSubscriber.create();
        TestSubscriber<Instant> subscriber2 = TestSubscriber.create();

        ConnectableObservable<Instant> observable = defer(() -> just(Instant.now()))
                .doOnNext(i -> logger.info(i.toString()))
                .publish();

        observable.subscribe(subscriber1);
        observable.connect();
        subscriber1.awaitTerminalEvent();
        subscriber1.assertValueCount(1);

        observable.subscribe(subscriber2);
        observable.connect();
        subscriber2.awaitTerminalEvent();
        subscriber2.assertValueCount(1);

        assertThat(subscriber1.getOnNextEvents().get(0))
                .isNotEqualTo(subscriber2.getOnNextEvents().get(0));
    }

    @Test
    public void autoConnectsToSourceAfterASpecifiedNumberOfConnections() throws Exception {
        TestSubscriber<Instant> subscriber1 = TestSubscriber.create();
        TestSubscriber<Instant> subscriber2 = TestSubscriber.create();
        Observable<Instant> observable = defer(() -> just(Instant.now()))
                .doOnNext(i -> logger.info(i.toString()))
                .publish().autoConnect(2);
        observable.subscribe(subscriber1);
        observable.subscribe(subscriber2);

        subscriber1.assertValueCount(1);
        subscriber1.assertCompleted();
        subscriber2.assertValueCount(1);
        subscriber2.assertCompleted();
        assertThat(subscriber1.getOnNextEvents().get(0))
                .isEqualTo(subscriber2.getOnNextEvents().get(0));
    }
}

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class CreatingObservables {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreatingObservables.class);

    @Test
    public void createUsingFromCallable() throws Exception {
        TestSubscriber<String> testSubscriber = TestSubscriber.create();

        Observable.fromCallable(() -> {
            Thread.sleep(2_000);
            LOGGER.info("Finished the callable");
            return "done";
        }).subscribeOn(Schedulers.newThread()).subscribe(testSubscriber);


        testSubscriber.awaitTerminalEvent(2_500, MILLISECONDS);
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);
        testSubscriber.assertValue("done");
    }

    @Test
    public void createAnObservableOfRangeOfNumbers() throws Exception {
        TestSubscriber<Integer> testSubscriber = TestSubscriber.create();

        Observable.range(1, 100).subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(100);
    }

    @Test
    public void createAnObservableThatEmitsInAnInterval() throws Exception {
        TestSubscriber<Long> testSubscriber = TestSubscriber.create();

        Observable.interval(1, 500, MILLISECONDS).take(2_750, MILLISECONDS).subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent(3_000, MILLISECONDS);
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(6);
    }

    @Test
    public void createAnObservableUsingCreate() throws Exception {
        TestSubscriber<Integer> testSubscriber = TestSubscriber.create();

        Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                for (int i = 0; i < 5; i++) {
                    if (subscriber.isUnsubscribed()) return;
                    int result = doSomeTimeTakingIoOperation(i);
                    LOGGER.info("Value received is {}", result);

                    if (subscriber.isUnsubscribed()) return;
                    subscriber.onNext(result);
                }

                if (subscriber.isUnsubscribed()) return;

                subscriber.onCompleted();
            }

            private int doSomeTimeTakingIoOperation(int i) {
                try { Thread.sleep(1_000); } catch (InterruptedException ignored) { }
                return i;
            }
        }).subscribeOn(Schedulers.io()).subscribe(testSubscriber);

        testSubscriber.awaitTerminalEventAndUnsubscribeOnTimeout(2_500, MILLISECONDS);
        testSubscriber.assertNotCompleted();
        testSubscriber.assertValueCount(2);

    }
}

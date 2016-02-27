import org.junit.Test;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observers.Observers;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import java.util.concurrent.Callable;

public class MultiThreading {
    @Test
    public void runsInBlockingManner() throws Exception {
        Observable.fromCallable(thatReturnsNumberOne())
                .map(numberToString())
                .subscribe(printResult());

    }

    @Test
    public void runsObservableOnADifferentThread() throws Exception {
        Observable.fromCallable(thatReturnsNumberOne())
                .subscribeOn(Schedulers.newThread())
                .map(numberToString())
                .subscribe(printResult());

    }

    @Test
    public void runsSubscriberOnADifferentThread() throws Exception {
        TestSubscriber<String> testSubscriber = TestSubscriber.create(Observers.create(printResult()));

        Observable.fromCallable(thatReturnsNumberOne())
                .map(numberToString())
                .observeOn(Schedulers.newThread())
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
    }

    @Test
    public void runsOperatorOnADifferentThread() throws Exception {
        TestSubscriber<String> testSubscriber = TestSubscriber.create(Observers.create(printResult()));

        Observable.fromCallable(thatReturnsNumberOne())
                .observeOn(Schedulers.newThread())
                .map(numberToString())
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
    }

    @Test
    public void runsObservableOperatorAndSubscriberOnDifferentThreads() throws Exception {
        TestSubscriber<String> testSubscriber = TestSubscriber.create(Observers.create(printResult()));

        Observable.fromCallable(thatReturnsNumberOne())
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .map(numberToString())
                .observeOn(Schedulers.newThread())
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
    }

    private Action1<String> printResult() {
        return result -> {
            System.out.println("Subscriber thread: " + Thread.currentThread().getName());
            System.out.println("Result: " + result);
        };
    }

    private Func1<Integer, String> numberToString() {
        return number -> {
            System.out.println("Operator thread: " + Thread.currentThread().getName());
            return String.valueOf(number);
        };
    }

    private Callable<Integer> thatReturnsNumberOne() {
        return () -> {
            System.out.println("Observable thread: " + Thread.currentThread().getName());
            return 1;
        };
    }
}

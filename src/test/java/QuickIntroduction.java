import org.junit.Test;
import rx.Observable;
import rx.Subscriber;

public class QuickIntroduction {
    @Test
    public void verboseVersion() throws Exception {
        Observable<Integer> source = Observable.range(1, 5);

        Subscriber<Integer> consumer = new Subscriber<Integer>() {
            @Override
            public void onNext(Integer number) { System.out.println(number); }

            @Override
            public void onError(Throwable e) { System.out.println("error"); }

            @Override
            public void onCompleted() { System.out.println("completed"); }
        };
        source.subscribe(consumer);
    }

    @Test
    public void conciseVersion() throws Exception {
        Observable.range(1, 5).subscribe(
            number -> System.out.println(number),
            error -> System.out.println("error"),
            () -> System.out.println("completed")
        );
    }
}

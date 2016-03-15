import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import rx.Observable;
import rx.observers.Subscribers;
import rx.observers.TestSubscriber;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResilienceTests {

    private Service mockService;
    private TestSubscriber<String> subscriber;

    @Before
    public void setUp() throws Exception {
        mockService = Mockito.mock(Service.class);
        subscriber = TestSubscriber.create(Subscribers.create(
                System.out::println,
                e -> System.out.println("Error: " + e.getMessage()),
                () -> System.out.println("Done")
        ));
    }

    @Test
    public void handlesErrorUsingOnError() throws Exception {
        when(mockService.processRequest()).thenThrow(new IOException("IO Error"));


        Observable.fromCallable(mockService::processRequest)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoValues();
        subscriber.assertNotCompleted();
        subscriber.assertError(IOException.class);
    }

    @Test
    public void handlesErrorByReturningSomeValue() throws Exception {
        when(mockService.processRequest()).thenThrow(new IOException("IO Error"));

        Observable.fromCallable(mockService::processRequest)
                .onErrorReturn(e -> "default")
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertValue("default");
        subscriber.assertCompleted();
        subscriber.assertNoErrors();
    }

    @Test
    public void handlesErrorBySubscribingToAnotherObservable() throws Exception {
        when(mockService.processRequest())
                .thenThrow(new IOException("IO Error"));

        Service anotherService = mock(Service.class);
        when(anotherService.processRequest()).thenReturn("result");

        Observable.fromCallable(mockService::processRequest)
                .onErrorResumeNext(Observable.fromCallable(anotherService::processRequest))
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertValue("result");
        subscriber.assertCompleted();
        subscriber.assertNoErrors();
    }

    private interface Service {
        String processRequest() throws IOException;
    }

}

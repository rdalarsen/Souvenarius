package me.worric.souvenarius.data.repository.location;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.location.Geocoder;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.location.FusedLocationProviderClient;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import me.worric.souvenarius.R;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class LocationRepositoryImplTest {

    private static final String ERROR_MESSAGE_1 = "sample error";
    private static final String ERROR_MESSAGE_2 = "missing permissions";
    private static final Map<Integer,String> sErrorMessages;

    static {
        sErrorMessages = new HashMap<>();
        sErrorMessages.put(R.string.error_message_location_repo_no_device_location, ERROR_MESSAGE_1);
        sErrorMessages.put(R.string.error_message_location_repo_missing_permission, ERROR_MESSAGE_2);
    }

    @Rule public MockitoRule mMockitoRule = MockitoJUnit.rule();
    @Rule public InstantTaskExecutorRule mInstantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock private FusedLocationProviderClient mClient;
    @Spy private LocationTaskRunnerImpl mLocationTaskRunner;

    private LocationRepository mRepository;
    private Geocoder mGeocoder = new Geocoder(InstrumentationRegistry.getTargetContext(),
            Locale.getDefault());

    @Before
    public void setUp() {
        mRepository = new LocationRepositoryImpl(mGeocoder, mClient, mLocationTaskRunner, sErrorMessages);
    }

    /**
     * We are able to execute the test on the current thread; that is what were doing with Runnable:run
     * See <a href="https://stackoverflow.com/a/6583868/8562738">this SO post</a> for more info.
     *
     * @throws InterruptedException
     */
    @Test
    public void locationAsyncTask_correctlyHandlesNullLocation() throws InterruptedException {
        final CountDownLatch signal = new CountDownLatch(1);
        final LocationRepositoryImpl.LocationResultListener listener = result -> {
            assertThat(result.message, is(ERROR_MESSAGE_1));
            signal.countDown();
        };

        mLocationTaskRunner.runGeocodingTask(null, mGeocoder, sErrorMessages, listener);
        signal.await();
    }

}
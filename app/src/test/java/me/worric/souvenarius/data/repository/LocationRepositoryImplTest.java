package me.worric.souvenarius.data.repository;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LiveData;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import me.worric.souvenarius.R;
import me.worric.souvenarius.data.Result;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocationRepositoryImplTest {

    private static final String ERROR_MESSAGE_1 = "no device location";
    private static final String ERROR_MESSAGE_2 = "missing permissions";
    private static final Map<Integer,String> sErrorMessages;

    static {
        sErrorMessages = new HashMap<>();
        sErrorMessages.put(R.string.error_message_location_repo_no_device_location, ERROR_MESSAGE_1);
        sErrorMessages.put(R.string.error_message_location_repo_missing_permission, ERROR_MESSAGE_2);
    }

    @Rule public InstantTaskExecutorRule mTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock private FusedLocationProviderClient mClient;
    @Mock private Task<Location> mLocationTask;
    @Mock private LifecycleOwner mLifecycleOwner;
    @Mock private Geocoder mGeocoder;

    @Captor private ArgumentCaptor<OnSuccessListener<Location>> mSuccessListenerCaptor;
    @Captor private ArgumentCaptor<OnFailureListener> mFailureListenerCaptor;

    private LocationRepository mRepository;

    @Before
    public void setUp() {
        mRepository = new LocationRepositoryImpl(mGeocoder, mClient, sErrorMessages);
    }

    @Test
    public void onFetchLocation_correctlyCallsFusedLocationProviderClient() {
        when(mClient.getLastLocation()).thenReturn(mLocationTask);
        when(mLocationTask.addOnSuccessListener(any(OnSuccessListener.class)))
                .thenReturn(mLocationTask);
        when(mLocationTask.addOnFailureListener(any(OnFailureListener.class)))
                .thenReturn(mLocationTask);

        LiveData<Result<Address>> result = mRepository.getLocation();

        verify(mLocationTask).addOnSuccessListener(mSuccessListenerCaptor.capture());
        verify(mLocationTask).addOnFailureListener(mFailureListenerCaptor.capture());
        assertThat(mSuccessListenerCaptor.getValue(), is(notNullValue()));
        assertThat(mFailureListenerCaptor.getValue(), is(notNullValue()));
        assertThat(result, is(notNullValue()));
    }

    @Test
    public void onFetchLocation_failureListenerExecutesCorrectly() {
        final Lifecycle lifecycle = getPreparedLifecycle(mLifecycleOwner);

        when(mClient.getLastLocation()).thenReturn(mLocationTask);
        when(mLifecycleOwner.getLifecycle()).thenReturn(lifecycle);
        when(mLocationTask.addOnSuccessListener(any(OnSuccessListener.class))).thenReturn(mLocationTask);
        when(mLocationTask.addOnFailureListener(any(OnFailureListener.class))).thenAnswer(invocation -> {
            Object[] arguments = invocation.getArguments();
            if (arguments != null && arguments.length > 0 && arguments[0] != null) {
                OnFailureListener failureListener = (OnFailureListener) arguments[0];
                failureListener.onFailure(mock(RuntimeException.class));
            }
            return mLocationTask;
        });

        mRepository.getLocation().observe(mLifecycleOwner, addressResult -> {
            assertThat(addressResult.response, is(nullValue()));
            assertThat(addressResult.message, is(equalTo(ERROR_MESSAGE_1)));
        });

        verify(mClient).getLastLocation();
        verify(mLocationTask).addOnFailureListener(any(OnFailureListener.class));
        verify(mLifecycleOwner, atLeastOnce()).getLifecycle();
    }

    @Test
    public void onFetchLocation_correctlyHandlesSecurityExceptionOnMissingPermissions() {
        final Lifecycle lifecycle = getPreparedLifecycle(mLifecycleOwner);

        when(mLifecycleOwner.getLifecycle()).thenReturn(lifecycle);
        when(mClient.getLastLocation()).thenThrow(mock(SecurityException.class));

        mRepository.getLocation().observe(mLifecycleOwner, addressResult -> {
            assertThat(addressResult.message, is(equalTo(ERROR_MESSAGE_2)));
            assertThat(addressResult.response, is(nullValue()));
        });

        verify(mClient).getLastLocation();
        verify(mLifecycleOwner, atLeastOnce()).getLifecycle();
    }

    private Lifecycle getPreparedLifecycle(LifecycleOwner owner) {
        LifecycleRegistry lifecycle = new LifecycleRegistry(owner);
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
        return lifecycle;
    }

}
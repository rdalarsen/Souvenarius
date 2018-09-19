package me.worric.souvenarius.data.repository;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.worric.souvenarius.R;
import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.model.SouvenirDb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class FirebaseHandlerImplTest {

    private static final String TEST_STRING_1 = "not signed in";
    private static final String TEST_STRING_2 = "already executing request";
    private static final Map<Integer,String> sErrorMessages;

    static {
        sErrorMessages = new HashMap<>();
        sErrorMessages.put(R.string.error_message_firebase_not_signed_in, TEST_STRING_1);
        sErrorMessages.put(R.string.error_message_firebase_already_executing, TEST_STRING_2);
    }

    @Rule public MockitoRule mMockitoRule = MockitoJUnit.rule();

    @Mock private FirebaseAuth mAuth;
    @Mock private FirebaseHandlerImpl.CustomEventListener mValueEventListener;
    @Mock private DatabaseReference mReference;
    @Mock private DatabaseReference mReference2;

    @Captor private ArgumentCaptor<String> mChildCaptor;
    @Captor private ArgumentCaptor<String> mChildCaptor2;
    @Captor private ArgumentCaptor<FirebaseHandlerImpl.CustomEventListener> mCustomValueEventListenerCaptor;
    @Captor private ArgumentCaptor<SouvenirDb> mSouvenirDbCaptor;
    @Captor private ArgumentCaptor<DatabaseReference.CompletionListener> mCompletionCaptor;

    private FirebaseHandlerImpl mHandler;

    @Before
    public void setUp() {
        mHandler = new FirebaseHandlerImpl(mAuth, mReference, sErrorMessages);
    }

    @Test
    public void fetchSouvenirs_correctlyHandlesNullUid() {
        final FirebaseHandler.OnResultListener listener = souvenirs -> {
            assertThat(souvenirs.message, is(equalTo(TEST_STRING_1)));
            assertThat(souvenirs.response, is(nullValue()));
            assertThat(souvenirs.status, is(equalTo(Result.Status.FAILURE)));
        };

        when(mAuth.getUid()).thenReturn(null);

        mHandler.fetchSouvenirsForCurrentUser(listener);

        verify(mAuth).getUid();
    }

    @Test
    public void fetchSouvenirs_correctlyHandlesExistingValueEventListener() {
        final String someId = "someId";
        final FirebaseHandler.OnResultListener listener = souvenirs -> {
            assertThat(souvenirs.message, is(equalTo(TEST_STRING_2)));
            assertThat(souvenirs.response, is(nullValue()));
            assertThat(souvenirs.status, is(equalTo(Result.Status.FAILURE)));
        };

        when(mAuth.getUid()).thenReturn(someId);
        when(mValueEventListener.isRunning()).thenReturn(true);

        mHandler.setValueEventListener(mValueEventListener);
        mHandler.fetchSouvenirsForCurrentUser(listener);

        verify(mAuth).getUid();
        verify(mValueEventListener).isRunning();
        verify(mReference, never()).child(anyString());
        verify(mReference, never()).addListenerForSingleValueEvent(any(ValueEventListener.class));
    }

    @Test
    public void fetchSouvenirs_correctlySetsValueEventListenerAndInvokesCallback() {
        final String authUid = "someId";
        final String testId = "testId";
        final SouvenirDb testSouvenir = new SouvenirDb();
        testSouvenir.setId(testId);
        final FirebaseHandler.OnResultListener listener = souvenirs -> {
            assertThat(souvenirs.message, is(nullValue()));
            assertThat(souvenirs.response, is(notNullValue()));
            assertThat(souvenirs.response.size(), is(equalTo(1)));
            assertThat(souvenirs.response.get(0).getId(), is(equalTo(testId)));
            assertThat(souvenirs.status, is(equalTo(Result.Status.SUCCESS)));
        };
        final Result<List<SouvenirDb>> resultList = Result.success(Arrays.asList(testSouvenir));
        final FirebaseHandlerImpl.CustomEventListener eventListener =
                getCustomEventListener(listener, resultList);

        when(mAuth.getUid()).thenReturn(authUid);
        when(mReference.child(anyString())).thenReturn(mReference);
        doAnswer(invocation -> {
            assertThat(eventListener.isRunning(), is(true));
            eventListener.onDataChange(mock(DataSnapshot.class));
            return null;
        }).when(mReference).addListenerForSingleValueEvent(any(ValueEventListener.class));

        mHandler.fetchSouvenirsForCurrentUser(listener, eventListener);

        verify(mReference).child(mChildCaptor.capture());
        verify(mReference).addListenerForSingleValueEvent(mCustomValueEventListenerCaptor.capture());
        assertThat(mChildCaptor.getValue(), is(equalTo(authUid)));
        assertThat(eventListener.isRunning(), is(false));
    }

    private FirebaseHandlerImpl.CustomEventListener getCustomEventListener(
            FirebaseHandler.OnResultListener listener,
            Result<List<SouvenirDb>> resultList) {
        return new FirebaseHandlerImpl.CustomEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                super.onDataChange(dataSnapshot);
                listener.onResult(resultList);
            }
        };
    }

    @Test
    public void storeSouvenir_correctlyCallsDatabaseReference() {
        final String testId = "testId";
        final String testUid = "testUid";
        final SouvenirDb testSouvenir = new SouvenirDb();
        testSouvenir.setId(testId);
        testSouvenir.setUid(testUid);
        final DatabaseReference.CompletionListener listener = (databaseError, databaseReference) ->
                assertThat(databaseReference, is(equalTo(mReference2)));

        when(mReference.child(anyString())).thenReturn(mReference2);
        when(mReference2.child(anyString())).thenReturn(mReference2);
        doAnswer(invocation -> {
            listener.onComplete(mock(DatabaseError.class), mReference2);
            return null;
        }).when(mReference2).setValue(
                any(SouvenirDb.class),
                any(DatabaseReference.CompletionListener.class));

        mHandler.storeSouvenir(testSouvenir, listener);

        verify(mReference).child(mChildCaptor.capture());
        verify(mReference2).child(mChildCaptor2.capture());
        verify(mReference2).setValue(mSouvenirDbCaptor.capture(), mCompletionCaptor.capture());
        assertThat(mChildCaptor.getValue(), is(equalTo(testUid)));
        assertThat(mChildCaptor2.getValue(), is(equalTo(testId)));
        assertThat(mSouvenirDbCaptor.getValue().getId(), is(equalTo(testSouvenir.getId())));
        assertThat(mCompletionCaptor.getValue(), is(equalTo(listener)));
    }

    @Test
    public void deleteSouvenir_correctlyCallsDatabaseReference() {
        final String testId = "testId";
        final String testUid = "testUid";
        final SouvenirDb testSouvenir = new SouvenirDb();
        testSouvenir.setId(testId);
        testSouvenir.setUid(testUid);
        final DatabaseReference.CompletionListener listener = (databaseError, databaseReference) ->
                assertThat(databaseReference, is(equalTo(mReference2)));

        when(mReference.child(anyString())).thenReturn(mReference2);
        when(mReference2.child(anyString())).thenReturn(mReference2);
        doAnswer(invocation -> {
            listener.onComplete(null, mReference2);
            return null;
        }).when(mReference2).removeValue(any(DatabaseReference.CompletionListener.class));

        mHandler.deleteSouvenir(testSouvenir, listener);

        verify(mReference).child(mChildCaptor.capture());
        verify(mReference2).child(mChildCaptor2.capture());
        verify(mReference2).removeValue(mCompletionCaptor.capture());
        assertThat(mChildCaptor.getValue(), is(equalTo(testUid)));
        assertThat(mChildCaptor2.getValue(), is(equalTo(testId)));
        assertThat(mCompletionCaptor.getValue(), is(equalTo(listener)));
    }

    @Test
    public void customEventListener_defaultRunningValueIsFalse() {
        FirebaseHandlerImpl.CustomEventListener eventListener =
                new FirebaseHandlerImpl.CustomEventListener();

        assertThat(eventListener.isRunning(), is(false));
    }

    @Test
    public void customEventListener_onDataChangeTogglesRunningValue() {
        FirebaseHandlerImpl.CustomEventListener eventListener = new FirebaseHandlerImpl.CustomEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                super.onDataChange(dataSnapshot);
            }
        };

        eventListener.setRunning(true);
        assertThat(eventListener.isRunning(), is(true));

        eventListener.onDataChange(mock(DataSnapshot.class));
        assertThat(eventListener.isRunning(), is(false));
    }

    @Test
    public void customEventListener_onCancelledTogglesRunningValue() {
        FirebaseHandlerImpl.CustomEventListener eventListener = new FirebaseHandlerImpl.CustomEventListener() {
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                super.onCancelled(databaseError);
            }
        };

        eventListener.setRunning(true);
        assertThat(eventListener.isRunning(), is(true));

        eventListener.onCancelled(mock(DatabaseError.class));
        assertThat(eventListener.isRunning(), is(false));
    }

}
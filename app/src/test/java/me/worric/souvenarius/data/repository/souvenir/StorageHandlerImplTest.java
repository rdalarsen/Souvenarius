package me.worric.souvenarius.data.repository.souvenir;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StorageHandlerImplTest {

    @Mock private StorageReference mReference;
    @Mock private Uri mUri;
    @Mock private UploadTask mUploadTask;
    @Mock private Task<Void> mVoidTask;

    @Captor private ArgumentCaptor<String> mChildCaptor;
    @Captor private ArgumentCaptor<Uri> mUriCaptor;

    private StorageHandler mHandler;

    @Before
    public void setUp() {
        mHandler = new StorageHandlerImpl(mReference);
    }

    @Test
    public void uploadPhoto_correctlyUsesStorageReference() {
        final String path = "path/name";
        final File file = new File(path);
        final String nameFromPath = file.getName();

        when(mReference.child(anyString())).thenReturn(mReference);
        when(mReference.putFile(any(Uri.class))).thenReturn(mUploadTask);

        mHandler.uploadPhoto(file, mUri);

        verify(mReference).child(mChildCaptor.capture());
        verify(mReference).putFile(mUriCaptor.capture());
        assertThat(mChildCaptor.getValue(), is(equalTo(nameFromPath)));
        assertThat(mUriCaptor.getValue(), is(equalTo(mUri)));
    }

    @Test
    public void removePhoto_correctlyUsesStorageReference() {
        final String photoName = "1234.jpg";

        when(mReference.child(anyString())).thenReturn(mReference);
        when(mReference.delete()).thenReturn(mVoidTask);

        mHandler.removePhoto(photoName);

        verify(mReference).delete();
        verify(mReference).child(mChildCaptor.capture());
        assertThat(mChildCaptor.getValue(), is(equalTo(photoName)));
    }

    @Test
    public void removePhotos_correctlyUsesStorageReference() {
        final List<String> fileNames = Arrays.asList("foo.jpg", "bar.jpg", "baz.jpg");
        final int numExpectedInvocations = fileNames.size();

        when(mReference.child(anyString())).thenReturn(mReference);
        when(mReference.delete()).thenReturn(mVoidTask);

        mHandler.removePhotos(fileNames);

        verify(mReference, times(numExpectedInvocations)).delete();
        verify(mReference, times(numExpectedInvocations)).child(mChildCaptor.capture());
        assertThat(mChildCaptor.getAllValues().get(0), is(equalTo(fileNames.get(0))));
        assertThat(mChildCaptor.getAllValues().get(1), is(equalTo(fileNames.get(1))));
        assertThat(mChildCaptor.getAllValues().get(2), is(equalTo(fileNames.get(2))));
    }

}
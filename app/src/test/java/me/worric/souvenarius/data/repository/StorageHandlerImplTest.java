package me.worric.souvenarius.data.repository;

import android.net.Uri;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class StorageHandlerImplTest {

    @Mock
    private StorageReference mReference;
    @Mock
    private Uri mUri;
    @Mock
    private UploadTask mUploadTask;
    @Mock
    private Task<Void> mVoidTask;
    private StorageHandler mHandler;

    @Before
    public void setUp() {
        mHandler = new StorageHandlerImpl(mReference);
    }

    @Test
    public void uploadPhoto_correctlyUsesStorageReference() {
        String path = "path/name";
        File file = new File(path);
        String nameFromPath = file.getName();

        ArgumentCaptor<String> childCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Uri> uriCaptor = ArgumentCaptor.forClass(Uri.class);
        when(mReference.child(childCaptor.capture())).thenReturn(mReference);
        when(mReference.putFile(uriCaptor.capture())).thenReturn(mUploadTask);

        mHandler.uploadPhoto(file, mUri);

        assertThat(childCaptor.getValue(), is(equalTo(nameFromPath)));
        assertThat(uriCaptor.getValue(), is(equalTo(mUri)));
        verify(mReference).child(childCaptor.getValue());
        verify(mReference).putFile(uriCaptor.getValue());
    }

    @Test
    public void removePhoto_correctlyUsesStorageReference() {
        String photoName = "1234.jpg";
        ArgumentCaptor<String> childCaptor = ArgumentCaptor.forClass(String.class);
        when(mReference.child(childCaptor.capture())).thenReturn(mReference);
        when(mReference.delete()).thenReturn(mVoidTask);

        mHandler.removePhoto(photoName);

        assertThat(childCaptor.getValue(), is(equalTo(photoName)));
        verify(mReference).child(childCaptor.getValue());
        verify(mReference).delete();
    }

    @Test
    public void removePhotos_correctlyUsesStorageReference() {
        List<String> fileNames = Arrays.asList(
                "foo.jpg",
                "bar.jpg",
                "baz.jpg"
        );
        ArgumentCaptor<String> childCaptor = ArgumentCaptor.forClass(String.class);
        when(mReference.child(childCaptor.capture())).thenReturn(mReference);
        when(mReference.delete()).thenReturn(mVoidTask);

        mHandler.removePhotos(fileNames);

        verify(mReference, times(fileNames.size())).delete();
        assertThat(childCaptor.getAllValues().get(0), is(equalTo(fileNames.get(0))));
        assertThat(childCaptor.getAllValues().get(1), is(equalTo(fileNames.get(1))));
        assertThat(childCaptor.getAllValues().get(2), is(equalTo(fileNames.get(2))));
    }

}
package me.worric.souvenarius.ui.detail;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.text.Editable;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.ArrayList;

import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.data.repository.souvenir.SouvenirRepository;
import me.worric.souvenarius.data.repository.souvenir.SouvenirRepositoryImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DetailViewModelTest {

    @Rule public InstantTaskExecutorRule mRule = new InstantTaskExecutorRule();

    @Mock private SouvenirRepositoryImpl mSouvenirRepository;
    @Mock private Editable mEditable;
    @Mock private File mFile;
    @Mock private Context mContext;

    @Spy private MutableLiveData<String> mSouvenirId;
    @Spy private MediatorLiveData<SouvenirDb> mCurrentSouvenir;
    @Spy private MutableLiveData<File> mPhotoFile;
    @Spy private ArrayList<String> mFileNamesList;

    @Captor private ArgumentCaptor<SouvenirDb> mSouvenirDbCaptor;
    @Captor private ArgumentCaptor<String> mStringCaptor;

    private DetailViewModel mViewModel;

    @Before
    public void setUp() {
        mViewModel = new DetailViewModel(mSouvenirRepository, mSouvenirId, mCurrentSouvenir, mPhotoFile);
    }

    @Test
    public void setSouvenirId_updatesLiveDataWhenValueNotPresent() {
        final String fakeSouvenirId = "fakeId";

        mViewModel.setSouvenirId(fakeSouvenirId);

        verify(mSouvenirId).setValue(anyString());
    }

    @Test
    public void setSouvenirId_doesNotUpdateLiveDataWhenValueAlreadyPresent() {
        final String newFakeSouvenirId = "newFakeId";

        final String fakeSouvenirId = "fakeId";
        mSouvenirId.setValue(fakeSouvenirId);

        mViewModel.setSouvenirId(newFakeSouvenirId);

        verify(mSouvenirId, times(1)).setValue(anyString());
        verify(mSouvenirId, times(1)).getValue();
    }

    @Test
    public void setPhotoFile_updatesLiveData() {
        final File fakeFile = new File("fake/path");

        mViewModel.setPhotoFile(fakeFile);

        verify(mPhotoFile).setValue(any(File.class));
    }

    @Test
    public void getCurrentSouvenir_returnsLiveDataWithNullValue() {
        LiveData<SouvenirDb> result = mViewModel.getCurrentSouvenir();

        assertThat(result, is(notNullValue()));
        assertThat(result.getValue(), is(nullValue()));
    }

    @Test
    public void updateSouvenirText_handlesNullSouvenir() {
        final DetailFragment.TextType titleTextType = DetailFragment.TextType.TITLE;
        mCurrentSouvenir.setValue(null);

        mViewModel.updateSouvenirText(mEditable, titleTextType);

        verifyZeroInteractions(mSouvenirRepository);
    }

    @Test
    public void updateSouvenirText_callsThroughToRepository() {
        final DetailFragment.TextType titleTextType = DetailFragment.TextType.TITLE;

        final String fakeTitle = "fakeTitle";
        when(mEditable.toString()).thenReturn(fakeTitle);

        final String fakeId = "fakeId";
        final SouvenirDb testSouvenir = new SouvenirDb();
        testSouvenir.setId(fakeId);
        mCurrentSouvenir.setValue(testSouvenir);

        mViewModel.updateSouvenirText(mEditable, titleTextType);

        verify(mSouvenirRepository).updateSouvenir(mSouvenirDbCaptor.capture(), isNull());
        assertThat(mSouvenirDbCaptor.getValue().getTitle(), is(equalTo(fakeTitle)));
        assertThat(mSouvenirDbCaptor.getValue().getId(), is(equalTo(fakeId)));
    }

    @Test
    public void deletePhoto_handlesNullSouvenir() {
        mCurrentSouvenir.setValue(null);

        boolean result = mViewModel.deletePhoto(mFile);

        assertThat(result, is(equalTo(false)));
    }

    @Test
    public void deletePhoto_handlesPhotoNotRemovedFromSouvenirList() {
        final SouvenirDb souvenir = new SouvenirDb();
        souvenir.setPhotos(mFileNamesList);
        mCurrentSouvenir.setValue(souvenir);

        final String fileName = "file.jpg";
        when(mFile.getName()).thenReturn(fileName);

        boolean result = mViewModel.deletePhoto(mFile);

        verify(mFile).getName();
        verify(mFileNamesList).remove(anyString());
        assertThat(result, is(equalTo(false)));
    }

    @Test
    public void deletePhoto_removesPhotoFromSouvenirAndDeletesFile() {
        final String fileName = "file.jpg";
        mFileNamesList.add(fileName);

        when(mFile.getName()).thenReturn(fileName);
        when(mFile.delete()).thenReturn(true);
        when(mFile.exists()).thenReturn(true);

        final SouvenirDb souvenir = new SouvenirDb();
        souvenir.setPhotos(mFileNamesList);
        mCurrentSouvenir.setValue(souvenir);

        boolean result = mViewModel.deletePhoto(mFile);

        verify(mFile).delete();
        verify(mFileNamesList).remove(anyString());
        verify(mSouvenirRepository).updateSouvenir(any(SouvenirDb.class), isNull());
        verify(mSouvenirRepository).deletePhotoFromStorage(mStringCaptor.capture());
        assertThat(mStringCaptor.getValue(), is(equalTo(fileName)));
        assertThat(result, is(equalTo(true)));
    }

    @Test
    public void deletePhoto_handlesFileNotPresent() {
        final String fileName = "file.jpg";
        mFileNamesList.add(fileName);

        when(mFile.getName()).thenReturn(fileName);
        when(mFile.exists()).thenReturn(false);

        final SouvenirDb souvenir = new SouvenirDb();
        souvenir.setPhotos(mFileNamesList);
        mCurrentSouvenir.setValue(souvenir);

        boolean result = mViewModel.deletePhoto(mFile);

        verify(mFile, never()).delete();
        verify(mFileNamesList).remove(anyString());
        assertThat(result, is(equalTo(true)));
    }

    @Test
    public void clearPhoto_handlesNullPhotoFile() {
        mPhotoFile.setValue(null);

        mViewModel.clearPhoto();

        verify(mPhotoFile).getValue();
    }

    @Test
    public void clearPhoto_updatesLiveDataWithNullValue() {
        mPhotoFile.setValue(mFile);

        mViewModel.clearPhoto();

        verify(mPhotoFile).setValue(isNull());
    }

    @Test
    public void clearPhoto_deletesFileIfExists() {
        when(mFile.exists()).thenReturn(true);
        mPhotoFile.setValue(mFile);

        mViewModel.clearPhoto();

        verify(mFile).delete();
    }

    @Test
    public void clearPhoto_doesNotDeleteFileIfNotExists() {
        when(mFile.exists()).thenReturn(false);
        mPhotoFile.setValue(mFile);

        mViewModel.clearPhoto();

        verify(mFile, never()).delete();
    }

    @Test
    public void clearPhoto_updatesLiveDataAndDeletesFileWhenPresent() {
        when(mFile.exists()).thenReturn(true);
        mPhotoFile.setValue(mFile);

        mViewModel.clearPhoto();

        verify(mFile).delete();
        verify(mPhotoFile).setValue(isNull());
    }

    @Test
    public void addPhoto_handlesNullPhotoFile() {
        mPhotoFile.setValue(null);
        mCurrentSouvenir.setValue(new SouvenirDb());

        boolean result = mViewModel.addPhoto();

        verify(mPhotoFile).getValue();
        assertThat(result, is(equalTo(false)));
    }

    @Test
    public void addPhoto_handlesNullSouvenir() {
        mPhotoFile.setValue(mFile);
        mCurrentSouvenir.setValue(null);

        boolean result = mViewModel.addPhoto();

        verify(mPhotoFile).getValue();
        assertThat(result, is(equalTo(false)));
    }

    @Test
    public void addPhoto_addsPhotoCorrectly() {
        final String fileName = "file.jpg";
        when(mFile.getName()).thenReturn(fileName);
        mPhotoFile.setValue(mFile);

        final String fakeId = "fakeId";
        final SouvenirDb souvenir = new SouvenirDb();
        souvenir.setId(fakeId);
        souvenir.setPhotos(mFileNamesList);
        mCurrentSouvenir.setValue(souvenir);

        boolean result = mViewModel.addPhoto();

        verify(mPhotoFile).getValue();
        verify(mCurrentSouvenir).getValue();
        verify(mFileNamesList).add(anyString());
        verify(mSouvenirRepository).updateSouvenir(mSouvenirDbCaptor.capture(), any(File.class));
        assertThat(mSouvenirDbCaptor.getValue().getId(), is(equalTo(fakeId)));
        assertThat(result, is(equalTo(true)));
    }

    @Test
    public void addPhoto_handlesFileNameNotAddedToSouvenirPhotoList() {
        final String fileName = "file.jpg";
        when(mFile.getName()).thenReturn(fileName);
        mPhotoFile.setValue(mFile);

        final SouvenirDb souvenir = new SouvenirDb();
        souvenir.setPhotos(mFileNamesList);
        mCurrentSouvenir.setValue(souvenir);

        when(mFileNamesList.add(anyString())).thenReturn(false);

        boolean result = mViewModel.addPhoto();

        verify(mFileNamesList).add(anyString());
        verify(mSouvenirRepository, never()).updateSouvenir(any(SouvenirDb.class), any(File.class));
        assertThat(result, is(equalTo(false)));
    }

    @Test
    public void deleteSouvenir_handlesNullCurrentSouvenir() {
        mCurrentSouvenir.setValue(null);

        mViewModel.deleteSouvenir(mContext);

        verify(mSouvenirRepository, never()).deleteSouvenir(any(SouvenirDb.class),
                any(SouvenirRepository.OnDeleteSuccessListener.class));
    }

    @Test
    public void deleteSouvenir_correctlyDeletesSouvenir() {
        final String fakeId = "fakeId";
        final SouvenirDb souvenir = new SouvenirDb();
        souvenir.setId(fakeId);
        mCurrentSouvenir.setValue(souvenir);

        mViewModel.deleteSouvenir(mContext);

        verify(mSouvenirRepository).deleteSouvenir(mSouvenirDbCaptor.capture(),
                any(SouvenirRepository.OnDeleteSuccessListener.class));
        assertThat(mSouvenirDbCaptor.getValue().getId(), is(equalTo(fakeId)));
    }

}
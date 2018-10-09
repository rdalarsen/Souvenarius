package me.worric.souvenarius.ui.add;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.location.Address;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.data.repository.location.LocationRepositoryImpl;
import me.worric.souvenarius.data.repository.souvenir.SouvenirRepository;
import me.worric.souvenarius.data.repository.souvenir.SouvenirRepositoryImpl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddViewModelTest {

    @Rule public InstantTaskExecutorRule mExecutorRule = new InstantTaskExecutorRule();

    @Mock private LocationRepositoryImpl mLocationRepository;
    @Mock private SouvenirRepositoryImpl mSouvenirRepository;
    @Mock private Context mContext;
    @Mock private File mFile;
    @Mock private LiveData<Result<Address>> mAddressResult;

    @Spy private MutableLiveData<File> mFileMutableLiveData;

    @Captor private ArgumentCaptor<SouvenirDb> mSouvenirDbCaptor;
    @Captor private ArgumentCaptor<File> mFileCaptor;

    @InjectMocks private AddViewModel mViewModel;

    private final SouvenirSaveInfo mSaveInfo =
            new SouvenirSaveInfo("fakeStory", "fakeTitle", "fakePlace");

    @Test
    public void addSouvenir_returnsFalseIfPhotoIsNull() {
        mFileMutableLiveData.setValue(null);

        assertThat(mViewModel.addSouvenir(mSaveInfo, mContext), is(equalTo(false)));
    }

    @Test
    public void addSouvenir_correctlyInvokesRepository() {
        mFileMutableLiveData.setValue(new File("fake/path"));

        assertThat(mViewModel.addSouvenir(mSaveInfo, mContext), is(equalTo(true)));

        verify(mSouvenirRepository).addSouvenir(any(SouvenirDb.class), any(File.class),
                any(SouvenirRepository.OnAddSuccessListener.class));
    }

    @Test
    public void addSouvenir_correctlyConstructsSouvenirDbFromSaveInfo() {
        mFileMutableLiveData.setValue(new File("fake/path"));

        mViewModel.addSouvenir(mSaveInfo, mContext);

        verify(mSouvenirRepository).addSouvenir(mSouvenirDbCaptor.capture(), any(File.class),
                any(SouvenirRepository.OnAddSuccessListener.class));
        assertThat(mSouvenirDbCaptor.getValue().getTitle(), is(equalTo(mSaveInfo.getTitle())));
        assertThat(mSouvenirDbCaptor.getValue().getPlace(), is(equalTo(mSaveInfo.getPlace())));
        assertThat(mSouvenirDbCaptor.getValue().getStory(), is(equalTo(mSaveInfo.getStory())));
    }

    @Test
    public void setPhotoFile_successfullyUpdatesLiveData() {
        final File testFile = new File("fake/path");

        mViewModel.setPhotoFile(testFile);

        verify(mFileMutableLiveData).setValue(testFile);
    }

    @Test
    public void setPhotoFile_updatesLiveDataWhenValueIsNull() {
        final String fakePath = "fake/path";
        mFileMutableLiveData.setValue(null);

        mViewModel.setPhotoFile(fakePath);

        verify(mFileMutableLiveData, atLeastOnce()).setValue(mFileCaptor.capture());
        assertThat(mFileCaptor.getValue().getPath(), is(equalTo(new File(fakePath).getPath())));
    }

    @Test
    public void setPhotoFile_doesNotUpdateLiveDataWhenValueIsNotNull() {
        final String newPath = "new/path";
        final File existingFile = new File("existing/path");
        mFileMutableLiveData.setValue(existingFile);

        mViewModel.setPhotoFile(newPath);

        verify(mFileMutableLiveData, times(1)).setValue(mFileCaptor.capture());
        assertThat(mFileCaptor.getValue().getPath(), is(not(equalTo(new File(newPath).getPath()))));
    }

    @Test
    public void getPhotoFile_returnsLiveDataWithNullValue() {
        LiveData<File> mFile = mViewModel.getPhotoFile();

        assertThat(mFile, is(notNullValue()));
        assertThat(mFile.getValue(), is(nullValue()));
    }

    @Test
    public void deleteTempPhoto_returnsFalseWhenCurrentValueIsNull() {
        mFileMutableLiveData.setValue(null);

        boolean result = mViewModel.deleteTempPhoto();

        assertThat(result, is(equalTo(false)));
    }

    @Test
    public void deleteTempPhoto_setsValueOfLiveDataToNullAndDeletesFile() {
        when(mFile.delete()).thenReturn(true);
        mFileMutableLiveData.setValue(mFile);

        boolean result = mViewModel.deleteTempPhoto();

        verify(mFile).delete();
        verify(mFileMutableLiveData, atLeastOnce()).setValue(mFileCaptor.capture());
        assertThat(mFileCaptor.getValue(), is(nullValue()));
        assertThat(result, is(equalTo(true)));
    }

    @Test
    public void getLocationInfo_callsRepository() {
        when(mLocationRepository.getLocation()).thenReturn(mAddressResult);

        mViewModel.getLocationInfo();

        verify(mLocationRepository).getLocation();
    }

    @Test
    public void onCleared_callsThroughToRepository() {
        mViewModel.onCleared();

        verify(mLocationRepository).clearResult();
    }

}
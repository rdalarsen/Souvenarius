package me.worric.souvenarius.ui.search;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.data.repository.souvenir.SouvenirRepository;
import timber.log.Timber;

public class SearchViewModel extends ViewModel {

    private final SouvenirRepository mRepository;
    private final MutableLiveData<String> mSouvenirTitleTrigger;
    private final LiveData<Result<List<SouvenirDb>>> mSouvenirSearchResults;

    @Inject
    public SearchViewModel(SouvenirRepository repository) {
        this(repository, new MutableLiveData<>());
    }

    public SearchViewModel(SouvenirRepository repository,
                           MutableLiveData<String> souvenirTitleTrigger) {
        mRepository = repository;
        mSouvenirTitleTrigger = souvenirTitleTrigger;
        mSouvenirSearchResults = Transformations.switchMap(mSouvenirTitleTrigger,
                mRepository::findSouvenirsByTitle);
    }

    public LiveData<Result<List<SouvenirDb>>> getSouvenirSearchResults() {
        return mSouvenirSearchResults;
    }

    public LiveData<Result.Status> getStatus() {
        return Transformations.map(mSouvenirSearchResults, r -> {
            Timber.d("Status=%s", r.status != null ? r.status : "NULL VALUE");
            return r.status;
        });
    }

    public void submitTitleSearchQuery(String titleSearchQuery) {
        String oldValue = mSouvenirTitleTrigger.getValue();
        if (oldValue != null && Objects.equals(oldValue, titleSearchQuery)) {
            return;
        }
        mSouvenirTitleTrigger.setValue(titleSearchQuery);
    }

}

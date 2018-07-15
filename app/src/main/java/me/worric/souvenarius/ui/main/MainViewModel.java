package me.worric.souvenarius.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import me.worric.souvenarius.data.model.Souvenir;
import me.worric.souvenarius.data.model.SouvenirResponse;
import me.worric.souvenarius.data.repository.SouvenirRepository;
import timber.log.Timber;

public class MainViewModel extends ViewModel {

    private final SouvenirRepository mSouvenirRepository;
    private final MutableLiveData<MainFragment.SortStyle> mSortStyle;

    @Inject
    public MainViewModel(SouvenirRepository souvenirRepository) {
        mSouvenirRepository = souvenirRepository;
        mSortStyle = new MutableLiveData<>();
    }

    public LiveData<List<Souvenir>> getSouvenirs() {
        return Transformations.map(mSouvenirRepository.getSouvenirs(), souvenirs -> {
            if (souvenirs != null && souvenirs.size() > 0) {
                List<Souvenir> resultList = new ArrayList<>(souvenirs.size());
                for (SouvenirResponse response : souvenirs) {
                    resultList.add(response.toSouvenir());
                }
                return resultList;
            }
            Timber.w("Souvenirs do not exist, returning null");
            return null;
        });
    }

    public LiveData<List<Souvenir>> getSortedSouvenirs() {
        return Transformations.switchMap(mSortStyle, sortStyle -> {
            if (sortStyle.equals(MainFragment.SortStyle.DATE_DESC)) {
                return sortSouvenirs(dateComparator, false);
            } else if (sortStyle.equals(MainFragment.SortStyle.DATE_ASC)) {
                return sortSouvenirs(dateComparator, true);
            }
            throw new IllegalArgumentException("Unknown sort style: " + sortStyle.name());
        });
    }

    private LiveData<List<Souvenir>> sortSouvenirs(final Comparator<Souvenir> comparator,
                                                   final boolean reversed) {
        return Transformations.map(mSouvenirRepository.getSouvenirs(), souvenirs -> {
            if (souvenirs != null && souvenirs.size() > 0) {
                List<Souvenir> resultList = new ArrayList<>(souvenirs.size());
                for (SouvenirResponse response : souvenirs) {
                    resultList.add(response.toSouvenir());
                }
                Collections.sort(resultList, comparator);
                if (reversed) Collections.reverse(resultList);
                return resultList;
            }
            return Collections.emptyList();
        });
    }

    public void toggleSortStyle() {
        MainFragment.SortStyle sortStyle = mSortStyle.getValue();
        if (sortStyle == null) throw new IllegalStateException("SortStyle is null! Shouldn't happen!");

        if (sortStyle.equals(MainFragment.SortStyle.DATE_DESC)) {
            mSortStyle.setValue(MainFragment.SortStyle.DATE_ASC);
        } else {
            mSortStyle.setValue(MainFragment.SortStyle.DATE_DESC);
        }
    }

    public void setSortStyle(MainFragment.SortStyle sortStyle) {
        mSortStyle.setValue(sortStyle);
    }

    public LiveData<MainFragment.SortStyle> getSortStyle() {
        return mSortStyle;
    }

    private Comparator<Souvenir> dateComparator = (s1, s2) ->
            Long.compare(s1.getTimestamp(), s2.getTimestamp());

    @Override
    protected void onCleared() {
        Timber.d("onCleared called");
    }
}

package me.worric.souvenarius.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import org.threeten.bp.Instant;

import java.util.Arrays;

import javax.inject.Inject;

import me.worric.souvenarius.data.model.Souvenir;
import me.worric.souvenarius.data.repository.SouvenirRepository;
import timber.log.Timber;

public class MainViewModel extends ViewModel {

    private final SouvenirRepository mSouvenirRepository;

    @Inject
    public MainViewModel(SouvenirRepository souvenirRepository) {
        mSouvenirRepository = souvenirRepository;
    }

    public LiveData<String> getPlaceOfFirstSouvenir() {
        return Transformations.map(mSouvenirRepository.getSouvenirs(), souvenirs -> {
            if (souvenirs != null && souvenirs.size() > 0) {
                Timber.i("Souvenirs exist, returning place of first souvenir");
                return souvenirs.get(0).getPlace();
            }
            Timber.w("Souvenirs do not exist, returning error message");
            return "Place not available";
        });
    }

    public void addSouvenir() {
        Souvenir souvenir = new Souvenir();
        souvenir.setId(4);
        souvenir.setPlace("the place");
        souvenir.setTimestamp(Instant.now().toEpochMilli());
        souvenir.setImages(Arrays.asList("fasf982h", "fha2eja0"));
        mSouvenirRepository.addSouvenir(souvenir);
    }

    public LiveData<String> getHelloWorldText() {
        return mSouvenirRepository.getHelloWorldText();
    }

}

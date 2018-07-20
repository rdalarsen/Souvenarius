package me.worric.souvenarius.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.db.model.SouvenirDb;
import me.worric.souvenarius.data.repository.SouvenirRepository;

public class MainViewModel extends ViewModel {

    private final SouvenirRepository mSouvenirRepository;

    @Inject
    public MainViewModel(SouvenirRepository souvenirRepository) {
        mSouvenirRepository = souvenirRepository;
    }

    public LiveData<Result<List<SouvenirDb>>> getSouvenirs() {
        return mSouvenirRepository.getSortedSouvenirs();
    }

    public void addNewSouvenir() {
        SouvenirDb db = new SouvenirDb();
        db.setPlace("The testPlace");
        db.setStory("The testStory");
        db.setTimestamp(1530381740282L);
        db.setTitle("The testTitle");
        db.setPhotos(Arrays.asList("fsdjfalæskdjf.jpg", "sfhsldfhgas.jpg"));
        mSouvenirRepository.addNewSouvenir(db, null);
    }

    public void nukeDb() {
        mSouvenirRepository.nukeDb();
    }

}

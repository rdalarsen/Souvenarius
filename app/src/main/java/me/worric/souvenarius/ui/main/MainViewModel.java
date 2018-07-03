package me.worric.souvenarius.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;
import javax.inject.Named;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<String> mHelloWorld;

    @Inject
    public MainViewModel(@Named("theMessage") String message) {
        mHelloWorld = new MutableLiveData<>();
        mHelloWorld.setValue(message);
    }

    public LiveData<String> getHelloWorldText() {
        return mHelloWorld;
    }

}

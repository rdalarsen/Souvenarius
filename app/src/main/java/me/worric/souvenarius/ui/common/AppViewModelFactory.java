package me.worric.souvenarius.ui.common;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * The code for this generified ViewmodelFactory is inspired by
 * <a href="https://github.com/googlesamples/android-architecture-components/blob/ea59732402708c8e7bca3ecc24a7c9ca85736b55/GithubBrowserSample/app/src/main/java/com/android/example/github/viewmodel/GithubViewModelFactory.java">this</a>
 * file from the GitHubBrowser Google sample repository.
 */
@Singleton
public class AppViewModelFactory implements ViewModelProvider.Factory {

    private final Map<Class<? extends ViewModel>,Provider<ViewModel>> mDaggerMultiBindingMap;

    @Inject
    public AppViewModelFactory(Map<Class<? extends ViewModel>, Provider<ViewModel>> daggerMultiBindingMap) {
        mDaggerMultiBindingMap = daggerMultiBindingMap;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        Provider<? extends ViewModel> viewmodelProvider = mDaggerMultiBindingMap.get(modelClass);

        if (viewmodelProvider == null) {
            for (Map.Entry<Class<? extends ViewModel>, Provider<ViewModel>> entry : mDaggerMultiBindingMap.entrySet()) {
                Class classKey = entry.getKey();

                if (modelClass.isAssignableFrom(classKey)) {
                    viewmodelProvider = entry.getValue();
                    break;
                }
            }
        }

        if (viewmodelProvider == null) throw new IllegalArgumentException("Unknown class: " + modelClass);

        return (T) viewmodelProvider.get();
    }

}

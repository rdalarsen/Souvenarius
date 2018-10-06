package me.worric.souvenarius.ui.common;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import me.worric.souvenarius.di.KeyForViewModel;
import me.worric.souvenarius.ui.add.AddViewModel;
import me.worric.souvenarius.ui.detail.DetailViewModel;
import me.worric.souvenarius.ui.main.MainViewModel;
import me.worric.souvenarius.ui.search.SearchViewModel;

@Module
public abstract class ViewModelModule {

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(AppViewModelFactory appViewModelFactory);

    @Binds
    @IntoMap
    @KeyForViewModel(MainViewModel.class)
    abstract ViewModel bindViewModel(MainViewModel mainViewModel);

    @Binds
    @IntoMap
    @KeyForViewModel(DetailViewModel.class)
    abstract ViewModel bindDetailViewModel(DetailViewModel detailViewModel);

    @Binds
    @IntoMap
    @KeyForViewModel(AddViewModel.class)
    abstract ViewModel bindAddViewModel(AddViewModel addViewModel);

    @Binds
    @IntoMap
    @KeyForViewModel(SearchViewModel.class)
    abstract ViewModel bindSearchViewModel(SearchViewModel searchViewModel);

}

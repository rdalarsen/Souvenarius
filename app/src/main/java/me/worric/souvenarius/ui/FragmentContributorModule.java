package me.worric.souvenarius.ui;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import me.worric.souvenarius.ui.add.AddFragment;
import me.worric.souvenarius.ui.detail.DetailFragment;
import me.worric.souvenarius.ui.detail.EditDialog;
import me.worric.souvenarius.ui.main.MainFragment;
import me.worric.souvenarius.ui.search.SearchFragment;

@Module
public abstract class FragmentContributorModule {

    @ContributesAndroidInjector
    abstract MainFragment contributeMainFragment();

    @ContributesAndroidInjector
    abstract AddFragment contributeAddFragment();

    @ContributesAndroidInjector
    abstract DetailFragment contributeDetailFragment();

    @ContributesAndroidInjector
    abstract SearchFragment contributeSearchFragment();

    @ContributesAndroidInjector
    abstract EditDialog contributeEditDialogFragment();

}

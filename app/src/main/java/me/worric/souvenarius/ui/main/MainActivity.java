package me.worric.souvenarius.ui.main;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import me.worric.souvenarius.R;

public class MainActivity extends AppCompatActivity implements HasSupportFragmentInjector {

    @Inject
    protected ViewModelProvider.Factory mFactory;
    private MainViewModel mMainViewModel;
    @Inject
    DispatchingAndroidInjector<Fragment> mInjector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainViewModel = ViewModelProviders.of(this, mFactory).get(MainViewModel.class);
    }


    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return mInjector;
    }

}

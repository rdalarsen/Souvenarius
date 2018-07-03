package me.worric.souvenarius.ui.main;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import me.worric.souvenarius.R;
import me.worric.souvenarius.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    @Inject
    protected ViewModelProvider.Factory mFactory;

    private MainViewModel mMainViewModel;
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        mMainViewModel = ViewModelProviders.of(this, mFactory).get(MainViewModel.class);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.setViewmodel(mMainViewModel);
        mBinding.setLifecycleOwner(this);
    }

}

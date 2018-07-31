package me.worric.souvenarius.di;

import android.arch.lifecycle.ViewModel;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import dagger.MapKey;

@MapKey
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface KeyForViewModel {
    Class<? extends ViewModel> value();
}

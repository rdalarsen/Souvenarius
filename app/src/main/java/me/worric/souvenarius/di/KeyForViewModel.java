package me.worric.souvenarius.di;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.lifecycle.ViewModel;
import dagger.MapKey;

@MapKey
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface KeyForViewModel {
    Class<? extends ViewModel> value();
}

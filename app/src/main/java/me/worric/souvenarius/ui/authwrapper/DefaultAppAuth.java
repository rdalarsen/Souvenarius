package me.worric.souvenarius.ui.authwrapper;

import com.google.firebase.auth.FirebaseAuth;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultAppAuth extends AbstractAppAuth {

    @Inject
    public DefaultAppAuth(FirebaseAuth firebaseAuth) {
        super(firebaseAuth);
    }

}

package me.worric.souvenarius.ui.authwrapper;

import com.google.firebase.auth.FirebaseAuth;

@Deprecated
public class DefaultAppAuth extends AbstractAppAuth {

    public DefaultAppAuth(FirebaseAuth firebaseAuth) {
        super(firebaseAuth);
    }

}

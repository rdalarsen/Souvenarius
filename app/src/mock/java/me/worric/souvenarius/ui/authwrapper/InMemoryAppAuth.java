package me.worric.souvenarius.ui.authwrapper;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

@Singleton
public class InMemoryAppAuth implements AppAuth {

    public static final class MockData {

        private MockData() {}

        private static final Map<AppUser,String> sTestData = new HashMap<>();

        // TODO: streamline the UIDs in both Room test data and here
        static {
            sTestData.put(new MockAppUser("HFnz2Pc627Qv06f5RVso6Y8QAcq1","user1@user1.com", "Mr. User1"),
                    "user1");
            sTestData.put(new MockAppUser("67890","user2@user2.com","Mr. User2"),
                    "user2");
        }

        public static Map<AppUser, String> get() {
            return sTestData;
        }

    }

    private final Map<AppUser,String> mTestData;
    private AppUser mCurrentUser;

    @Inject
    public InMemoryAppAuth(Map<AppUser,String> testData) {
        mTestData = testData;
        mCurrentUser = testData.keySet().iterator().next();
    }

    @Nullable
    @Override
    public AppUser getCurrentUser() {
        return mCurrentUser;
    }

    @Override
    public void signInWithEmailAndPassword(@NonNull String email, @NonNull String password, @NonNull AppAuthResult result) {
        Timber.d("SignIn called. email=%s,password=%s", email, password);
        for (Map.Entry<AppUser,String> entry : mTestData.entrySet()) {
            AppUser user = entry.getKey();
            if (user.getEmail().equals(email) && entry.getValue().equals(password)) {
                Timber.i("User sign-in successful; email/password combo is valid.");
                mCurrentUser = user;
                result.onSuccess(user);
                return;
            }
        }
        Timber.w("User sign-in failed; no valid email/password found");
        result.onFailure(new Exception("User not found, or password not matching"));
    }

    @Nullable
    @Override
    public String getUid() {
        Timber.i("getUid called. UID=%s", mCurrentUser == null ? null : mCurrentUser.getUid());
        return mCurrentUser == null ? null : mCurrentUser.getUid();
    }

    @Override
    public void signOut() {
        Timber.i("Signing out");
        mCurrentUser = null;
    }
    
}

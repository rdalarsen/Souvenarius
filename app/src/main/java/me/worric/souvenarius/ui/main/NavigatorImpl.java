package me.worric.souvenarius.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import me.worric.souvenarius.R;
import me.worric.souvenarius.di.ActivityScope;
import me.worric.souvenarius.ui.add.AddFragment;
import me.worric.souvenarius.ui.detail.DetailFragment;
import me.worric.souvenarius.ui.search.SearchFragment;
import me.worric.souvenarius.ui.createaccount.CreateAccountFragment;
import me.worric.souvenarius.ui.signin.SignInFragment;
import me.worric.souvenarius.ui.widget.SouvenirWidgetProvider;
import timber.log.Timber;

@ActivityScope
public class NavigatorImpl implements Navigator {

    private final FragmentManager mFragmentManager;

    @Inject
    public NavigatorImpl(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
        Timber.d("fragmentManager hashcode: %s, This HashCode: %s", fragmentManager.hashCode(), this.hashCode());
    }

    @Override
    public void navigateToDetail(String souvenirId) {
        mFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DetailFragment.newInstance(souvenirId))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void navigateToAdd() {
        mFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void navigateToSearch() {
        mFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SearchFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void navigateToSignIn() {
        mFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SignInFragment.newInstance())
                .commit();
    }

    @Override
    public void navigateToMain() {
        mFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MainFragment.newInstance())
                .commit();
    }

    @Override
    public void navigateToCreateAccount() {
        mFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateAccountFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void navigateBack() {
        mFragmentManager.popBackStack();
    }

    @Override
    public void initNavigation(@Nullable Bundle savedInstanceState, @Nullable FirebaseUser user,
                               @Nullable Intent launchIntent) {
        if (savedInstanceState != null || launchIntent == null) {
            return;
        } else if (user == null) {
            createReplaceTransaction(false, false, SignInFragment.newInstance())
                    .commit();
            return;
        }

        // First we handle the case that the app was closed via back button and then launched
        // from history at a later time. In this case, we want the app to show the main screen,
        // regardless of what action is in the Intent, e.g. if the app was launched from the
        // widget last time.
        // See this SO post on the matter: https://stackoverflow.com/a/41381757/8562738.
        // Also check this article: https://medium.com/@JakobUlbrich/flag-attributes-in-android-how-to-use-them-ac4ec8aee7d1
        int flags = launchIntent.getFlags();
        if ((flags | LAUNCHED_FROM_HISTORY_AND_IN_NEW_TASK) == flags) {
            Timber.i("Intent flags: detected launch from history + new task. Doing normal launch");
            createReplaceTransaction(false, false, MainFragment.newInstance())
                    .commit();
            return;
        }

        String action = launchIntent.getAction();
        if (TextUtils.isEmpty(action)) throw new IllegalArgumentException("Action cannot be null or empty");
        Timber.i("action of launch intent is: %s", action);

        switch (action) {
            case SouvenirWidgetProvider.ACTION_WIDGET_LAUNCH_ADD_SOUVENIR:
                // handle widget action of launching add souvenir
                createReplaceTransaction(false, true, MainFragment.newInstance())
                        .commit();
                createReplaceTransaction(true, true, AddFragment.newInstance())
                        .commit();
                break;
            case SouvenirWidgetProvider.ACTION_WIDGET_LAUNCH_SOUVENIR_DETAILS:
                // handle widget action of launching souvenir details
                createReplaceTransaction(false, true, MainFragment.newInstance())
                        .commit();
                createReplaceTransaction(true, true, DetailFragment.newInstance(launchIntent
                        .getStringExtra(SouvenirWidgetProvider.EXTRA_SOUVENIR_ID)))
                        .commit();
                break;
            case Intent.ACTION_MAIN:
                // handle normal launch of the app
                createReplaceTransaction(false, false, MainFragment.newInstance())
                        .commit();
                break;
            default:
                throw new IllegalArgumentException("Unknown action: " + action);
        }
    }

    private FragmentTransaction createReplaceTransaction(boolean addToBackStack,
                                                         boolean reorderingEnabled,
                                                         Fragment fragment) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();

        transaction.replace(R.id.fragment_container, fragment);
        if (addToBackStack) transaction.addToBackStack(null);
        if (reorderingEnabled) transaction.setReorderingAllowed(true);

        return transaction;
    }

}

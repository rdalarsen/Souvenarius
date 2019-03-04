package me.worric.souvenarius.ui.createaccount;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import me.worric.souvenarius.R;
import me.worric.souvenarius.databinding.FragmentCreateaccountBinding;
import timber.log.Timber;

public class CreateAccountFragment extends Fragment {

    private FragmentCreateaccountBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_createaccount, container, false);
        mBinding.setLifecycleOwner(this);
        mBinding.setClickListener(mClickListener);
        return mBinding.getRoot();
    }

    private ClickListener mClickListener = new ClickListener() {
        @Override
        public void onCreateAccountClicked(String email, String password) {
            Timber.d("create account clicked. (email=%s,password=%s)", email, password);
        }
    };

    public interface ClickListener {
        void onCreateAccountClicked(final String email, final String password);
    }

    public static CreateAccountFragment newInstance() {
        return new CreateAccountFragment();
    }

}

package me.worric.souvenarius.ui.detail;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import me.worric.souvenarius.R;
import me.worric.souvenarius.databinding.DialogEditBinding;
import me.worric.souvenarius.ui.common.NetUtils;

/**
 * Inspired by <a href="https://guides.codepath.com/android/using-dialogfragment">this article</a>.
 */
public class EditDialog extends DialogFragment {

    private static final String KEY_TEXT_TYPE = "key_text_type";
    private DetailViewModel mViewModel;
    private DetailFragment.TextType mTextType;
    private DialogEditBinding mBinding;
    @Inject
    protected ViewModelProvider.Factory mFactory;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_edit, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextType = (DetailFragment.TextType) getArguments().getSerializable(KEY_TEXT_TYPE);
        mViewModel = ViewModelProviders.of(getParentFragment(), mFactory).get(DetailViewModel.class);
        mBinding.setTextType(mTextType);
        mBinding.setViewmodel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.setClickListener(mClickListener);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void showErrorToast() {
        Toast.makeText(getContext(), "No internet connection. Cannot edit.", Toast.LENGTH_SHORT).show();
    }

    private ClickListener mClickListener = new ClickListener() {
        @Override
        public void onSaveClicked(View view) {
            if (!NetUtils.isConnected(getContext())) {
                showErrorToast();
            } else {
                mViewModel.updateSouvenirText(mBinding.etEditDetail.getText(), mTextType);
            }
            getDialog().dismiss();
        }

        @Override
        public void onCancelClicked(View view) {
            getDialog().dismiss();
        }
    };

    public interface ClickListener {
        void onSaveClicked(View view);
        void onCancelClicked(View view);
    }

    public static EditDialog newInstance(DetailFragment.TextType textType) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_TEXT_TYPE, textType);
        EditDialog fragment = new EditDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public EditDialog() {
    }

}

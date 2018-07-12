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

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import me.worric.souvenarius.R;
import me.worric.souvenarius.databinding.DialogEditBinding;

/**
 * Inspired by <a href="https://guides.codepath.com/android/using-dialogfragment">this article</a>.
 */
public class EditDialogFragment extends DialogFragment {

    private static final String KEY_TEXT_TYPE = "key_text_type";
    private static final String KEY_DIALOG_TITLE = "key_dialog_title";
    @Inject
    protected ViewModelProvider.Factory mFactory;
    private DetailViewModel mViewModel;
    private DetailFragment.TextType mTextType;
    private DialogEditBinding mBinding;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTextType = (DetailFragment.TextType) getArguments().getSerializable(KEY_TEXT_TYPE);
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
        mViewModel = ViewModelProviders.of(getParentFragment(), mFactory).get(DetailViewModel.class);
        mBinding.setTextType(mTextType);
        mBinding.setViewmodel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.setClickHandler(mTextClickHandler);

        /*String title = getArguments().getString(KEY_DIALOG_TITLE, "default title");
        getDialog().setTitle("Edit the " + title);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);*/
    }

    private EditTextClickHandler mTextClickHandler = new EditTextClickHandler() {
        @Override
        public void onTextEdited(View view) {
            mViewModel.updateSouvenirText(mBinding.etEditDetail.getText(), mTextType);
            getDialog().dismiss();
        }

        @Override
        public void onCancelled(View view) {
            getDialog().dismiss();
        }
    };

    public interface EditTextClickHandler {
        void onTextEdited(View view);
        void onCancelled(View view);
    }

    public static EditDialogFragment newInstance(String title, DetailFragment.TextType textType) {
        Bundle args = new Bundle();
        args.putString(KEY_DIALOG_TITLE, title);
        args.putSerializable(KEY_TEXT_TYPE, textType);

        EditDialogFragment fragment = new EditDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public EditDialogFragment() {}

}

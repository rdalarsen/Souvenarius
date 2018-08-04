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
    private static final String KEY_SCROLL_POSITION = "key_scroll_position";
    private DetailViewModel mViewModel;
    private DetailFragment.TextType mTextType;
    private DialogEditBinding mBinding;
    private int[] mScrollViewPosition;
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
        mViewModel.getCurrentSouvenir().observe(this, souvenirDb -> {
            mBinding.setCurrentSouvenir(souvenirDb);
            restoreScrollPosition(savedInstanceState);
        });
        mBinding.setTextType(mTextType);
        mBinding.setViewmodel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.setClickListener(mClickListener);
//        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void restoreScrollPosition(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_SCROLL_POSITION)) {
            int[] scrollPosition = savedInstanceState.getIntArray(KEY_SCROLL_POSITION);
            mBinding.svDetailEditDialogRoot.post(() ->
                    mBinding.svDetailEditDialogRoot.scrollTo(scrollPosition[0],scrollPosition[1]));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mScrollViewPosition = new int[]{mBinding.svDetailEditDialogRoot.getScrollX(),
                mBinding.svDetailEditDialogRoot.getScrollY()};
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mScrollViewPosition != null) {
            outState.putIntArray(KEY_SCROLL_POSITION, mScrollViewPosition);
        }
    }

    private void showErrorToast() {
        Toast.makeText(getContext(), R.string.error_message_detail_edit_dialog_no_connection, Toast.LENGTH_SHORT).show();
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

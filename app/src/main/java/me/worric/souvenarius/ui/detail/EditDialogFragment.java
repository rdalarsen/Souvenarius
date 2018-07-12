package me.worric.souvenarius.ui.detail;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import me.worric.souvenarius.R;

/**
 * Inspired by <a href="https://guides.codepath.com/android/using-dialogfragment">this article</a>.
 */
public class EditDialogFragment extends DialogFragment {

    @Inject
    protected ViewModelProvider.Factory mFactory;
    private DetailViewModel mViewModel;
    private EditText mEditText;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_edit_title, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = ViewModelProviders.of(getParentFragment(), mFactory).get(DetailViewModel.class);

        mEditText = view.findViewById(R.id.et_edit_title);
        (view.findViewById(R.id.btn_edit_save)).setOnClickListener(v ->
                mViewModel.setTitle(mEditText.getText().toString()));
        (view.findViewById(R.id.btn_edit_cancel)).setOnClickListener(v ->
                getDialog().dismiss());

        String title = getArguments().getString("title", "default title");
        getDialog().setTitle("Edit the " + title);

        mEditText.setText(mViewModel.getTitle());
        mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public static EditDialogFragment newInstance(String title) {
        Bundle args = new Bundle();
        args.putString("title", title);

        EditDialogFragment fragment = new EditDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public EditDialogFragment() {}
}

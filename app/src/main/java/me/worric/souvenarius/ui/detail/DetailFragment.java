package me.worric.souvenarius.ui.detail;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import me.worric.souvenarius.BR;
import me.worric.souvenarius.R;
import me.worric.souvenarius.data.model.Souvenir;
import me.worric.souvenarius.databinding.FragmentDetailBinding;

public class DetailFragment extends Fragment {

    @Inject
    protected ViewModelProvider.Factory mFactory;
    private DetailViewModel mViewModel;
    private FragmentDetailBinding mBinding;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Souvenir souvenir = getSouvenirFromBundle(getArguments());
        mViewModel = ViewModelProviders.of(this, mFactory).get(DetailViewModel.class);
        mViewModel.setSouvenir(souvenir);

        mBinding.setLifecycleOwner(this);
        mBinding.setVariable(BR.viewmodel, mViewModel);
        mBinding.setVariable(BR.clickHandler, (OnClickEdit) v ->
                EditDialogFragment.newInstance("Le titel")
                        .show(getChildFragmentManager(), "edit_title"));
    }

    private Souvenir getSouvenirFromBundle(Bundle arguments) {
        Souvenir souvenir = new Souvenir();
        souvenir.setTitle("The title");
        souvenir.setTimestamp(1531221883679L);
        souvenir.setPlace("The place");
        souvenir.setPhotos(Arrays.asList("JPEG_20180711_072642_8927165028337173907.jpg"));
        souvenir.setStory("The story");
        return souvenir;
    }

    public static DetailFragment newInstance() {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnClickEdit {
        void onClickEdit(View view);
    }

}

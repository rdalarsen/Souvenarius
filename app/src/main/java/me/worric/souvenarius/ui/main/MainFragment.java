package me.worric.souvenarius.ui.main;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import me.worric.souvenarius.R;
import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.db.model.SouvenirDb;
import me.worric.souvenarius.databinding.FragmentMainBinding;
import timber.log.Timber;

public class MainFragment extends Fragment {

    @Inject
    protected ViewModelProvider.Factory mFactory;
    @Inject
    protected SharedPreferences mSharedPreferences;
    private FragmentMainBinding mBinding;
    private MainViewModel mViewModel;
    private SouvenirAdapter mAdapter;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity(), mFactory).get(MainViewModel.class);
        mViewModel.getSouvenirs().observe(this, souvenirs -> {
            if (souvenirs.status.equals(Result.Status.SUCCESS)) mAdapter.swapLists(souvenirs.response);
        });
        mBinding.setViewmodel(mViewModel);
        mBinding.setLifecycleOwner(this);
        mBinding.setClickHandler(mClickHandler);
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        mAdapter = new SouvenirAdapter(mItemClickListener);
        mBinding.rvSouvenirList.setAdapter(mAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewModel.getSouvenirs().removeObservers(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_main_nuke_db:
                mViewModel.nukeDb();
                return true;
            case R.id.action_main_sync:
                mViewModel.sync();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private final ItemClickListener mItemClickListener = souvenir -> {
        try {
            ((MainActivity)getActivity()).handleItemClicked(souvenir);
        } catch (ClassCastException e) {
            Toast.makeText(getContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    };

    public interface ItemClickListener {
        void onItemClicked(SouvenirDb souvenir);
    }

    public final ClickHandler mClickHandler = new ClickHandler() {

        @Override
        public void onToggleSortClicked(View view) {
            SortStyle sortStyle = getSortStyleFromPrefs();
            Timber.i("SortStyle is: %s", sortStyle.toString());
            if (sortStyle.equals(SortStyle.DATE_DESC)) {
                mSharedPreferences.edit()
                        .putString("sortStyle", SortStyle.DATE_ASC.toString())
                        .apply();
            } else {
                mSharedPreferences.edit()
                        .putString("sortStyle", SortStyle.DATE_DESC.toString())
                        .apply();
            }
        }

        @Override
        public void onAddDataClicked(View view) {
            mViewModel.addNewSouvenir();
        }
    };

    @NonNull
    private SortStyle getSortStyleFromPrefs() {
        String value = mSharedPreferences.getString("sortStyle", SortStyle.DATE_DESC.toString());
        return SortStyle.valueOf(value);
    }

    public interface ClickHandler {
        void onToggleSortClicked(View view);
        void onAddDataClicked(View view);
    }

    public static MainFragment newInstance() {

        Bundle args = new Bundle();

        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public enum ListStyle {
        LIST,
        STAGGERED
    }

}

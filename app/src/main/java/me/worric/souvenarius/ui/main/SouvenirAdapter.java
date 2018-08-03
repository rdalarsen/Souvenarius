package me.worric.souvenarius.ui.main;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import me.worric.souvenarius.R;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.databinding.ItemMainSouvenirBinding;

public class SouvenirAdapter extends RecyclerView.Adapter<SouvenirAdapter.SouvenirViewholder> {

    private final MainFragment.ItemClickListener mListener;
    private List<SouvenirDb> mSouvenirs;

    SouvenirAdapter(MainFragment.ItemClickListener listener) {
        mListener = listener;
    }

    public void swapLists(List<SouvenirDb> souvenirs) {
        mSouvenirs = souvenirs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SouvenirViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemMainSouvenirBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_main_souvenir, parent, false);
        binding.setItemClickListener(mListener);
        return new SouvenirViewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SouvenirViewholder holder, int position) {
        holder.bind(mSouvenirs.get(position));
    }

    @Override
    public int getItemCount() {
        return mSouvenirs == null ? 0 : mSouvenirs.size();
    }

    static class SouvenirViewholder extends RecyclerView.ViewHolder {

        private final ItemMainSouvenirBinding mBinding;

        SouvenirViewholder(ItemMainSouvenirBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        void bind(SouvenirDb souvenir) {
            mBinding.setSouvenir(souvenir);
            mBinding.executePendingBindings();
        }
    }

}

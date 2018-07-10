package me.worric.souvenarius.ui.main;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import me.worric.souvenarius.R;
import me.worric.souvenarius.data.model.Souvenir;
import me.worric.souvenarius.databinding.SouvenirItemBinding;

public class SouvenirAdapter extends RecyclerView.Adapter<SouvenirAdapter.SouvenirViewholder> {

    private List<Souvenir> mSouvenirs;

    public void swapLists(List<Souvenir> souvenirs) {
        this.mSouvenirs = souvenirs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SouvenirViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SouvenirItemBinding binding =
                DataBindingUtil.inflate(inflater, R.layout.souvenir_item, parent, false);
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

        private SouvenirItemBinding mBinding;

        SouvenirViewholder(SouvenirItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        void bind(Souvenir souvenir) {
            mBinding.setSouvenir(souvenir);
            mBinding.executePendingBindings();
        }
    }

}

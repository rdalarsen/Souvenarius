package me.worric.souvenarius.ui.detail;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import me.worric.souvenarius.R;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.databinding.ItemDetailPhotoBinding;

public class SouvenirPhotoAdapter extends RecyclerView.Adapter<SouvenirPhotoAdapter.SouvenirPhotoViewholder> {

    private final DetailFragment.DeletePhotoClickListener mListener;
    private SouvenirDb mSouvenir;

    SouvenirPhotoAdapter(DetailFragment.DeletePhotoClickListener listener) {
        mListener = listener;
    }

    public void swapPhotos(final SouvenirDb newSouvenir) {
        mSouvenir = newSouvenir;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SouvenirPhotoViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemDetailPhotoBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_detail_photo, parent, false);
        binding.setClickListener(mListener);
        return new SouvenirPhotoViewholder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SouvenirPhotoViewholder holder, int position) {
        String photoName = (mSouvenir != null && mSouvenir.getPhotos().size() > 0)
                ? mSouvenir.getPhotos().get(position)
                : null;
        holder.bind(photoName);
    }

    @Override
    public int getItemCount() {
        return (mSouvenir != null) ? mSouvenir.getPhotos().size() : 0;
    }

    static class SouvenirPhotoViewholder extends RecyclerView.ViewHolder {

        private final ItemDetailPhotoBinding mBinding;

        SouvenirPhotoViewholder(ItemDetailPhotoBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        void bind(String photoName) {
            mBinding.setPlaceInArray(getAdapterPosition());
            mBinding.setPhotoName(photoName);
            mBinding.executePendingBindings();
        }
    }

}

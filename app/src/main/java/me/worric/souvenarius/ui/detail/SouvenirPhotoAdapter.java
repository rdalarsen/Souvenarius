package me.worric.souvenarius.ui.detail;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import me.worric.souvenarius.R;
import me.worric.souvenarius.data.db.model.SouvenirDb;
import me.worric.souvenarius.databinding.ItemDetailPhotoBinding;

public class SouvenirPhotoAdapter extends RecyclerView.Adapter<SouvenirPhotoAdapter.SouvenirPhotoViewholder> {

    private final DetailFragment.PhotoClickListener mListener;
    private SouvenirDb mSouvenir;

    SouvenirPhotoAdapter(DetailFragment.PhotoClickListener listener) {
        mListener = listener;
    }

    public void swapPhotos(final SouvenirDb newSouvenir, RecyclerView rvSouvenirPhotoList) {
        // TODO: disabled
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return (mSouvenir != null) ? mSouvenir.getPhotos().size() : 0;
            }

            @Override
            public int getNewListSize() {
                return (newSouvenir != null) ? newSouvenir.getPhotos().size() : 0;
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                if (mSouvenir != null && newSouvenir != null) {
                    return mSouvenir.getPhotos().get(oldItemPosition).equals(newSouvenir.getPhotos().get(newItemPosition));
                }
                return false;
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                if (mSouvenir != null && newSouvenir != null) {
                    return mSouvenir.getPhotos().get(oldItemPosition).equals(newSouvenir.getPhotos().get(newItemPosition));
                }
                return false;
            }
        });

        mSouvenir = newSouvenir;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SouvenirPhotoViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemDetailPhotoBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_detail_photo, parent, false);
        binding.setPhotoClickListener(mListener);
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
            mBinding.setPhotoName(photoName);
            mBinding.executePendingBindings();
        }
    }

}

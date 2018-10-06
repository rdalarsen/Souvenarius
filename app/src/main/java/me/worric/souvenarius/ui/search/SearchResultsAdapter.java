package me.worric.souvenarius.ui.search;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import me.worric.souvenarius.R;
import me.worric.souvenarius.data.Result;
import me.worric.souvenarius.data.model.SouvenirDb;
import me.worric.souvenarius.databinding.ItemSearchResultBinding;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.SearchResultsViewHolder> {

    public interface SearchResultClickListener {
        void onSearchResultClicked(SouvenirDb souvenir);
    }

    private final SearchResultClickListener mClickListener;
    private List<SouvenirDb> mSouvenirs;

    public SearchResultsAdapter(SearchResultClickListener clickListener) {
        mClickListener = clickListener;
    }

    public void swapItems(@Nullable Result<List<SouvenirDb>> result) {
        if (result == null || result.status.equals(Result.Status.FAILURE)) return;

        int newListLength = result.response.size();
        int oldListLength = mSouvenirs == null ? 0 : mSouvenirs.size();
        int listDiff = Math.abs(newListLength - oldListLength);

        mSouvenirs = result.response;

        if (oldListLength == 0) {
            notifyItemRangeInserted(0, newListLength);
        } else if (newListLength > oldListLength) {
            notifyItemRangeInserted(oldListLength, listDiff);
        } else if (newListLength < oldListLength) {
            notifyItemRangeRemoved(oldListLength, listDiff);
        }

        notifyItemRangeChanged(0, listDiff);
    }

    @NonNull
    @Override
    public SearchResultsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        ItemSearchResultBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_search_result,
                viewGroup, false);
        binding.setClickListener(mClickListener);
        return new SearchResultsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultsViewHolder holder, int position) {
        SouvenirDb souvenir = mSouvenirs.get(position);
        holder.bindSouvenirItem(souvenir);
    }

    @Override
    public int getItemCount() {
        return mSouvenirs != null ? mSouvenirs.size() : 0;
    }

    static class SearchResultsViewHolder extends RecyclerView.ViewHolder {

        private final ItemSearchResultBinding mBinding;

        SearchResultsViewHolder(ItemSearchResultBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        void bindSouvenirItem(SouvenirDb souvenir) {
            mBinding.setSouvenir(souvenir);
            mBinding.executePendingBindings();
        }

    }

}

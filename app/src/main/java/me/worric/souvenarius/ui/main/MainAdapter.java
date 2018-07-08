package me.worric.souvenarius.ui.main;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import me.worric.souvenarius.R;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewholder> {

    private List<String> ids;

    public void swapLists(List<String> ids) {
        this.ids = ids;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MainViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new MainViewholder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewholder holder, int position) {
        holder.text1.setText("FirebaseID:");
        holder.text2.setText(ids.get(position));
    }

    @Override
    public int getItemCount() {
        return ids == null ? 0 : ids.size();
    }

    public static class MainViewholder extends RecyclerView.ViewHolder {

        TextView text1;
        TextView text2;

        public MainViewholder(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(R.id.tv_item_1);
            text2 = itemView.findViewById(R.id.tv_item_2);
        }
    }

}

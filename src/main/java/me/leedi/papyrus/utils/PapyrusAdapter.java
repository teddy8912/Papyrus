package me.leedi.papyrus.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import me.leedi.papyrus.R;

import java.util.List;

public class PapyrusAdapter extends RecyclerView.Adapter<PapyrusAdapter.ViewHolder> {
    Context context;
    private List<Papyrus> items;
    private int itemLayout;

    public PapyrusAdapter(Context context, List<Papyrus> items, int itemLayout) {
        this.context = context;
        this.items = items;
        this.itemLayout = itemLayout;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Papyrus item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.date.setText(item.getDate());
        holder.description.setText(item.getDescription());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView date;
        public TextView description;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.papyrus_item_title);
            date = (TextView) itemView.findViewById(R.id.papyrus_item_date);
            description = (TextView) itemView.findViewById(R.id.papyrus_item_description);
        }
    }


    @Override
    public int getItemCount() {
        // TODO Auto-generated method stub
        return items.size();
    }
}

package me.leedi.papyrus.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import me.leedi.papyrus.R;

import java.util.List;

public class PapyrusAdapter extends ArrayAdapter<Papyrus> {
    TextView title, description, date;
    Context context;
    int layout;
    List<Papyrus> items;

    public PapyrusAdapter(Context context, int layout, List<Papyrus> items) {
        super(context, layout, items);
        this.context = context;
        this.items = items;
        this.layout = layout;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        //View 객체 연결
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(layout, null);
        }

        title = (TextView) view.findViewById(R.id.papyrus_item_title);
        description = (TextView) view.findViewById(R.id.papyrus_item_description);
        date = (TextView) view.findViewById(R.id.papyrus_item_date);
        
        Papyrus papyrus = items.get(position);
        
        title.setText(papyrus.getTitle());
        description.setText(papyrus.getDescription());
        date.setText(papyrus.getDate());
        return view;
    }
}
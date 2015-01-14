package me.leedi.papyrus.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import me.leedi.papyrus.R;
import me.leedi.papyrus.utils.Papyrus;
import me.leedi.papyrus.utils.PapyrusAdapter;

import java.util.ArrayList;
import java.util.List;

public class PapyrusFragment extends Fragment {
    RecyclerView mRecyclerView; // RecyclerView 초기화
    Context mContext; // Context 초기회
    public PapyrusFragment(Context context) {
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 레이아웃 초기화
        View view = inflater.inflate(R.layout.fragment_papyrus, container, false);
        init(view, mContext);
        return view;
    }

    private void init(View v, Context context){
        mRecyclerView = (RecyclerView) v.findViewById(R.id.list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(new PapyrusAdapter(context, getPapyrus() , R.layout.papyrus_list_item));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
    }
    
    private List<Papyrus> getPapyrus() {
        List<Papyrus> items = new ArrayList<Papyrus>();
        return items;
    }
}

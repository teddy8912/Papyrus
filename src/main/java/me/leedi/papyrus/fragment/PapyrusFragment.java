package me.leedi.papyrus.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
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
import me.leedi.papyrus.utils.ServerUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class PapyrusFragment extends Fragment {
    int start = 0;
    RecyclerView mRecyclerView; // RecyclerView 초기화
    Context mContext; // Context 초기화
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
        final List<Papyrus> items = new ArrayList<>();
        final SharedPreferences pref = getActivity().getSharedPreferences("common", Context.MODE_PRIVATE);
        // TODO : Thread 생성 (동기 방식) 에서 AsyncTask (비동기 방식) 으로 교체를 하자!
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONArray json = ServerUtils.papyrusGet(pref.getString("userId", null), Integer.toString(start) , getActivity());
                for (int i=0; i<json.length(); i++) {
                    Papyrus papyrus = new Papyrus();
                    try {
                        // TODO : Description 은 substring 으로 20자 이내로만 나오게 설계해야하며, 
                        // TODO : Date 는 UNIX 시간이기에 Java 에서 계산된 시계 방식으로 표기하도록 설계해야한다.
                        papyrus.setTitle(json.getJSONObject(i).getString("title"));
                        papyrus.setDescription(json.getJSONObject(i).getString("content"));
                        papyrus.setDate(json.getJSONObject(i).getString("date"));
                        items.add(papyrus);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        return items;
    }
}

package me.leedi.papyrus.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PapyrusFragment extends Fragment {
    int start = 0;
    Context mContext; // Context 초기화
    public PapyrusFragment(Context context) {
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 레이아웃 초기화
        View view = inflater.inflate(R.layout.fragment_papyrus, container, false);
        String userId = mContext.getSharedPreferences("common", Context.MODE_PRIVATE).getString("userId", null);
        loadTask task = new loadTask(view, mContext);
        task.execute(userId);
        return view;
    }

    public class loadTask extends AsyncTask<String, Void, List<Papyrus>> {
        Context context;
        View v;
        RecyclerView mRecyclerView;
        
        public loadTask(View v, Context context) {
            this.v = v;
            this.context = context;            
        }
        
        @Override
        protected void onPreExecute() {
            mRecyclerView = (RecyclerView) v.findViewById(R.id.list);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        }
        
        @Override
        protected List<Papyrus> doInBackground(String... userId) {
            List<Papyrus> items = new ArrayList<>();
            JSONArray json = ServerUtils.papyrusGet(userId[0], Integer.toString(start) , context);
            for (int i=0; i<json.length(); i++) {
                Papyrus papyrus = new Papyrus();
                try {
                    // TODO : 방금, 몇 분전, 몇 시간전, 몇 일전, 일자 이런식으로 표현되도록 설계해야 한다!
                    papyrus.setTitle(json.getJSONObject(i).getString("title"));
                    String description = json.getJSONObject(i).getString("content");
                    int end = description.length();
                    if (end < 20) {
                        description = json.getJSONObject(i).getString("content").substring(0, end);
                    }
                    else {
                        description = json.getJSONObject(i).getString("content").substring(0, 20);
                    }
                    long unixTime = Long.parseLong(json.getJSONObject(i).getString("date"));
                    Date date = new Date(unixTime * 1000);
                    SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.KOREAN);
                    papyrus.setDescription(description);
                    papyrus.setDate(DateFormat.format(date));
                    items.add(papyrus);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return items;
        }

        @Override
        protected void onPostExecute(List<Papyrus> items) {
            mRecyclerView.setAdapter(new PapyrusAdapter(context, items , R.layout.papyrus_list_item));
        }
    }
}

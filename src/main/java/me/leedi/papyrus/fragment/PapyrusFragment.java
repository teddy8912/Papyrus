package me.leedi.papyrus.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
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

public class PapyrusFragment extends Fragment implements AbsListView.OnScrollListener {
    String userId; // 이름처럼 유저 ID
    int start = 0; // Database Offset
    boolean ListViewLock; // 중복 요청 방지 Boolean
    ListView mListView; // ListView 초기화
    Context mContext; // Context 초기화
    
    List<Papyrus> items = new ArrayList<>(); // 아이템 목록 생성
    PapyrusAdapter mAdapter; // 어댑터 생성
    
    View footer;
    
    public PapyrusFragment(Context context) {
        mContext = context;
        userId = mContext.getSharedPreferences("common", Context.MODE_PRIVATE).getString("userId", null); // 유저 ID 가져오기
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 레이아웃 초기화
        View view = inflater.inflate(R.layout.fragment_papyrus, container, false);
        mListView = (ListView) view.findViewById(R.id.list);
        
        // 푸터(최하단)를 이용해서 다음 목록 가져오도록 설계
        footer = inflater.inflate(R.layout.papyrus_footer, null);
        footer.setVisibility(View.INVISIBLE);
        mListView.addFooterView(footer);
        
        // 어댑터로 목록을 설정
        mAdapter = new PapyrusAdapter(mContext, R.layout.papyrus_list_item, items);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(this);
        
        // 불러오는 AsyncTask 실행
        new loadTask(mContext).execute(userId);
        
        // 뷰 반환
        return view;
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int state) {
        // 스크롤 상태가 변동이 없다면
        if (state == SCROLL_STATE_IDLE)
        {
            // 최하단이고 중복요청이 아니라면
            if (mListView.getLastVisiblePosition() >= mListView.getCount()-1 && !ListViewLock)
            {
                footer.setVisibility(View.VISIBLE);
                start = start + 1; // Offset 카운트를 올리고
                new loadTask(mContext).execute(userId); // 불러오는 AsyncTask 실행
            }
        }
    }


    public class loadTask extends AsyncTask<String, Void, Boolean> {
        Context context;
        
        public loadTask(Context context) {
            this.context = context;            
        }
        
        @Override
        protected void onPreExecute() {
            ListViewLock = true; // 중복 요청 방지
            footer.setVisibility(View.VISIBLE);
        }
        
        @Override
        protected Boolean doInBackground(String... userId) {
            JSONArray json = ServerUtils.papyrusGet(userId[0], Integer.toString(start) , context); // JSON 배열 가져오기
            for (int i=0; i<json.length(); i++) { // JSON 배열 가공
                Papyrus papyrus = new Papyrus();
                try {
                    // TODO : 방금, 몇 분전, 몇 시간전, 몇 일전, 일자 이런식으로 표현되도록 설계해야 한다!
                    papyrus.setTitle(json.getJSONObject(i).getString("title")); // 제목 설정
                    // 내용 미리보기 설정
                    String description = json.getJSONObject(i).getString("content");
                    int end = description.length();
                    if (end < 20) {
                        description = json.getJSONObject(i).getString("content").substring(0, end);
                    }
                    else {
                        description = json.getJSONObject(i).getString("content").substring(0, 20);
                    }
                    // POSIX 시간 (UNIX 시간)으로 저장된 데이터를 변환
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
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mAdapter.notifyDataSetChanged(); // 어댑터에 데이터 변경알림
            ListViewLock = false; // 중복 요청 방지
            footer.setVisibility(View.INVISIBLE);
        }
    }
}

package me.leedi.papyrus.fragment;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.widget.DatePicker;
import android.widget.ListView;
import com.melnykov.fab.FloatingActionButton;
import me.leedi.papyrus.R;
import me.leedi.papyrus.activity.ComposeActivity;
import me.leedi.papyrus.utils.ComplexUtils;
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
    String[] params = new String[2];
    ListView mListView; // ListView 초기화
    Context mContext; // Context 초기화
    SimpleDateFormat DateFormat; // DateFormat 초기화
    
    public PapyrusFragment(Context context) {
        mContext = context;
        params[0] = mContext.getSharedPreferences("common", Context.MODE_PRIVATE).getString("userId", null); // 유저 ID 가져오기
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 레이아웃 초기화
        View view = inflater.inflate(R.layout.fragment_papyrus, container, false);
        mListView = (ListView) view.findViewById(R.id.list);
        setHasOptionsMenu(true); // 메뉴가 존재함을 보고합니다!

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.attachToListView(mListView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, ComposeActivity.class));
            }
        });
        
        // 불러오는 AsyncTask 실행
        Date date = new Date();
        DateFormat = new SimpleDateFormat("yyyy/M/dd", Locale.KOREAN);
        params[1] = DateFormat.format(date);
        new loadTask(mContext).execute(params);
        
        // 뷰 반환
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (ComplexUtils.isNeedRefresh()) {
            Date date = new Date();
            DateFormat = new SimpleDateFormat("yyyy/M/dd", Locale.KOREAN);
            params[1] = DateFormat.format(date);
            new loadTask(mContext).execute(params);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_papyrus, menu); // Menu 목록을 가져옵니다.
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_datepick:
                Date date = new Date();
                int year = Integer.parseInt(new SimpleDateFormat("yyyy", Locale.KOREAN).format(date));
                int month = Integer.parseInt(new SimpleDateFormat("M", Locale.KOREAN).format(date));
                int day = Integer.parseInt(new SimpleDateFormat("dd", Locale.KOREAN).format(date));
                new DatePickerDialog(mContext, listener, year, month - 1, day).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            month = month + 1;
            params[1] = Integer.toString(year) + "/" + Integer.toString(month) + "/" + Integer.toString(day);
            new loadTask(mContext).execute(params);
        }
    };

    public class loadTask extends AsyncTask<String, Void, List<Papyrus>> {
        Context context;
        
        public loadTask(Context context) {
            this.context = context;
        }
        
        @Override
        protected List<Papyrus> doInBackground(String... params) {
            JSONArray json = ServerUtils.papyrusGet(params[0], params[1] , context); // JSON 배열 가져오기
            List<Papyrus> items = new ArrayList<>(); // 아이템 목록 생성
            for (int i=0; i<json.length(); i++) { // JSON 배열 가공
                Papyrus papyrus = new Papyrus();
                try {
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
                    DateFormat = new SimpleDateFormat("a h:m", Locale.KOREAN);
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
            mListView.setAdapter(new PapyrusAdapter(context, R.layout.papyrus_list_item, items));
        }
    }
}
package me.leedi.papyrus.fragment;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ListView;
import com.melnykov.fab.FloatingActionButton;
import me.leedi.papyrus.R;
import me.leedi.papyrus.activity.ComposeActivity;
import me.leedi.papyrus.activity.DetailActivity;
import me.leedi.papyrus.utils.*;
import org.json.JSONArray;
import org.json.JSONException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
    
    List<Papyrus> items = new ArrayList<>();
    PapyrusAdapter mAdapter;
    
    public PapyrusFragment(Context context) {
        mContext = context;
        params[0] = mContext.getSharedPreferences("common", Context.MODE_PRIVATE).getString("userId", null); // 유저 ID 가져오기
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 레이아웃 초기화
        View view = inflater.inflate(R.layout.fragment_papyrus, container, false);
        mListView = (ListView) view.findViewById(R.id.list);
        mAdapter = new PapyrusAdapter(mContext, R.layout.papyrus_list_item, items); // 어댑터 설정
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(onItemClickListener); // 아이템 선택 시의 설정
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
    
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Papyrus papyrus = mAdapter.getItem(i);
            String contentId = papyrus.getContentId();
            Intent detail = new Intent(mContext, DetailActivity.class);
            detail.putExtra("contentId", contentId);
            startActivity(detail);
        }
    };

    private class loadTask extends AsyncTask<String, Void, Void> {
        Context context;
        
        public loadTask(Context context) {
            this.context = context;
        }
        
        @Override
        protected void onPreExecute() {
            items.clear();
        }
        
        @Override
        protected Void doInBackground(String... params) {
            JSONArray json = ServerUtils.papyrusGet(params[0], params[1] , context); // JSON 배열 가져오기
            for (int i=0; i<json.length(); i++) { // JSON 배열 가공
                Papyrus papyrus = new Papyrus();
                try {
                    papyrus.setContentId(json.getJSONObject(i).getString("contentId")); // 컨텐츠 ID 설정
                    papyrus.setTitle(SecurityUtils.AESDecode(json.getJSONObject(i).getString("title"), context)); // 제목 설정
                    // 내용 미리보기 설정
                    papyrus.setDescription(SecurityUtils.AESDecode(json.getJSONObject(i).getString("content"), context));
                    // POSIX 시간 (UNIX 시간)으로 저장된 데이터를 변환
                    long unixTime = Long.parseLong(json.getJSONObject(i).getString("date"));
                    Date date = new Date(unixTime * 1000);
                    DateFormat = new SimpleDateFormat("a h:mm", Locale.KOREAN);
                    papyrus.setDate(DateFormat.format(date));
                    items.add(papyrus);
                } catch (JSONException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException | InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
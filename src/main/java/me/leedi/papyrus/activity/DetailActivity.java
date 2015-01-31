package me.leedi.papyrus.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.widget.TextView;
import me.leedi.papyrus.R;
import me.leedi.papyrus.utils.SecurityUtils;
import me.leedi.papyrus.utils.ServerUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class DetailActivity extends ActionBarActivity {
    TextView content; // 내용 TextView
    String[] params = new String[2]; // 파라미터

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Toolbar 초기화
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        content = (TextView) findViewById(R.id.contentBody);
        
        params[0] = getSharedPreferences("common", MODE_PRIVATE).getString("userId", null); // 유저 ID 가져오기
        params[1] = getIntent().getStringExtra("contentId"); // 컨텐츠 ID 가져오기
        new loadTask(actionBar).execute(params);
    }

    private class loadTask extends AsyncTask<String, Void, String[]> {
        ActionBar actionBar;
        
        public loadTask(ActionBar actionBar) {
            this.actionBar = actionBar;
        }
        
        @Override
        protected String[] doInBackground(String... params) {
            String[] data = new String[2];
            JSONObject json = ServerUtils.papyrusDetail(params[0], params[1], DetailActivity.this);
            try {
                data[0] = json.getString("title"); // 제목 가져오기
                data[1] = json.getString("content"); // 내용 가져오기
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
            return data;
        }

        @Override
        protected void onPostExecute(String[] data) {
            try {
                actionBar.setTitle(SecurityUtils.AESDecode(data[0], DetailActivity.this)); // 액션바에 타이틀 변경
                content.setText(SecurityUtils.AESDecode(data[1], DetailActivity.this)); // 내용 설정
            } catch (UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp()
    { // 액션바 이전 버튼을 눌렀을 때
        finish(); // 그냥 Activity 만 종료해도 상관없다.
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: // 디바이스의 물리(소프트) 이전키를 눌렀을 때
                finish(); // 그냥 Activity 만 종료한다.
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }
}

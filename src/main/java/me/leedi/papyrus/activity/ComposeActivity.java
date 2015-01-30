package me.leedi.papyrus.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import me.leedi.papyrus.R;
import me.leedi.papyrus.utils.ComplexUtils;
import me.leedi.papyrus.utils.SecurityUtils;
import me.leedi.papyrus.utils.ServerUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ComposeActivity extends ActionBarActivity {
    EditText title, content;
    String[] params = new String[3];

    // TODO : 편집 기능 구현
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        // Toolbar 초기화
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        
        title = (EditText) findViewById(R.id.input_title);
        content = (EditText) findViewById(R.id.input_content);
        
        ComplexUtils.setNeedRefresh(false);
        params[0] = getSharedPreferences("common", MODE_PRIVATE).getString("userId", null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_compose, menu);
        return super.onCreateOptionsMenu(menu);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_save:
                try {
                    params[1] = SecurityUtils.AESEncode(title.getText().toString(), ComposeActivity.this);
                    params[2] = SecurityUtils.AESEncode(content.getText().toString(), ComposeActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new composeTask(ComposeActivity.this).execute(params);
                break;
        }
        return true;
    }

    public class composeTask extends AsyncTask<String, Void, Boolean> {
        Context context;

        public composeTask(Context context) {
            this.context = context;
        }

        // 차후 반환유형을 SQLite 연동 시 Boolean 에서 String 형태로 바꿔야한다.
        @Override
        protected Boolean doInBackground(String... params) {
            Date date = new Date();
            String strdate = new SimpleDateFormat("yyyy/M/dd", Locale.KOREAN).format(date);
            String contentId = ServerUtils.papyrusNew(params[0], params[1], params[2], strdate, context);
            return contentId != null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                ComplexUtils.setNeedRefresh(true);
                finish();
            }
            else {
                new AlertDialog.Builder(ComposeActivity.this)
                        .setMessage(R.string.alert_composeerr)
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
            }
        }
    }
}
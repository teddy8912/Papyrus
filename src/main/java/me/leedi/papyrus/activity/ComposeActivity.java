package me.leedi.papyrus.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import me.leedi.papyrus.R;

public class ComposeActivity extends ActionBarActivity {

    // TODO : 작성/편집 기능 구현
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        // Toolbar 초기화
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }
}

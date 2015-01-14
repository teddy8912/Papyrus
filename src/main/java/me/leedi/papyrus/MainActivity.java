package me.leedi.papyrus;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import me.leedi.papyrus.fragment.PapyrusFragment;


public class MainActivity extends ActionBarActivity {
    Fragment fragment = null; // Fragment 초기화(?)
    FragmentManager fragmentManager = getFragmentManager(); // FragmentManager 를 가져온다.
    private ActionBarDrawerToggle mDrawerToggle; // 액션바 토글!
    private DrawerLayout mDrawerLayout; // DrawerLayout 변수 생성
    private ListView mDrawerList; // DrawerLayout에 들어갈 ListView 변수 생성 (DrawerList 라고 통칭하겠다)

    // Activity(Thread)를 생성해봅시다!
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] mListName = getResources().getStringArray(R.array.listname);

        // DrawerLayout 와 DrawerList 초기화
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mDrawerList = (ListView) findViewById(R.id.drawer_content);

        // Toolbar 초기화
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // DrawerList 에 Adapter 설정
        mDrawerList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mListName));
        // DrawerList 에서 item 클릭 시 설정
        mDrawerList.setOnItemClickListener(new DrawerListItemClickListener());

        // 액션바 토글을 설정한다.
        // 지정된 String 들은 비장애인 뿐만 아니라 장애인들을 배려하는 접근성 기능을 위해서 사용된다.
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close) {

            /** DrawerLayout 이 닫힐 때 */
            public void onDrawerClosed(View v) {
                super.onDrawerClosed(v);
            }

            /** DrawerLayout 이 열릴 때 */
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };

        // 액션바 토글 클릭 시 DrawerLayout 을 제어할 수 있도록 설정한다.
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        // 메인화면인 Papyrus 목록을 띄운다.
        fragment = new PapyrusFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit();
    }

    /** 선택된 item 의 Fragement 로 이동시켜주자! */
    private void selectItem(int position) {
        // 선택된 item 에 따라 Fragment 를 가져온다.
        if (position == 0) {
            fragment = new PapyrusFragment();
        }
        // 가져온 Fragment 를 Content (FrameLayout)영역에 띄운다.
        fragmentManager.beginTransaction()
                .replace(R.id.content, fragment)
                .commit();

        // DrawerLayout 은 닫는다.
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // 앱을 종료하지 않고 있는 상태에서 귀환 시.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    // DrawerList 의 item 클릭 리스너를 짜봅시다!
    private class DrawerListItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }
}

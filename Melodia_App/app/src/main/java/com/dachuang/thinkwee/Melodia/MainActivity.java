package com.dachuang.thinkwee.Melodia;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private FragmentManager manager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle("Melodia");

        Window window = MainActivity.this.getWindow();
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色

        RecordFragment recordfragment = new RecordFragment();
        manager = this.getFragmentManager();
        transaction = manager.beginTransaction();
        transaction.replace(R.id.frame, recordfragment);
        transaction.commit();
        this.setTitle("录制");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            builder.setTitle("团队介绍");
            builder.setMessage(R.string.about);
            builder.setNegativeButton("牛逼", null);
            builder.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main) {
            this.setTitle("上传");
            UploadFragment homepagefragment = new UploadFragment();
            manager = this.getFragmentManager();
            transaction = manager.beginTransaction();
            transaction.replace(R.id.frame, homepagefragment);
            transaction.commit();

        } else if (id == R.id.nav_record) {
            this.setTitle("录制");
            RecordFragment recordfragment = new RecordFragment();
            manager = this.getFragmentManager();
            transaction = manager.beginTransaction();
            transaction.replace(R.id.frame, recordfragment);
            transaction.commit();


        } else if (id == R.id.nav_play) {
            this.setTitle("播放");
            PlayFragment playfragment = new PlayFragment();
            manager = this.getFragmentManager();
            transaction = manager.beginTransaction();
            transaction.replace(R.id.frame, playfragment);
            transaction.commit();

        } else if (id == R.id.nav_piano) {
            this.setTitle("跟弹");
            PianoFragment pianofragment = new PianoFragment();
            manager = this.getFragmentManager();
            transaction = manager.beginTransaction();
            transaction.replace(R.id.frame, pianofragment);
            transaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

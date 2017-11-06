package com.example.tmappliction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
//我用appcompatactivity类替代了fragmentActivity
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TodayFragment today_fg;
    private StatisticsFragment statistics_fg;
    private AgendaFragment agenda_fg;
    private SettingFragment setting_fg;

    private RelativeLayout firstBottomLayout;
    private RelativeLayout secondBottomLayout;
    private RelativeLayout thirdBottomLayout;
    private RelativeLayout fourthBottomLayout;
    private ImageView firstBottomImage;
    private ImageView secondBottomImage;
    private ImageView thirdBottomImage;
    private ImageView fourthBottomImage;
    private TextView firstBottomText;
    private TextView secondBottomText;
    private TextView thirdBottomText;
    private TextView fourthBottomText;
    private int white = 0xFFFFFFFF;
    private int gray = 0xFF7597B3;
    private int dark = 0xff000000;

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        initView();
        setChoiceItem(0);
        serviceSwitch();//开启程序是否自动开启服务

    }
    private void serviceSwitch(){
        SharedPreferences pref = getSharedPreferences("data",MODE_PRIVATE);
        boolean judgeImg = pref.getBoolean("nof",false);
        if(judgeImg){
            startService(new Intent(this,DataSaveService.class));
        }
    }

    /**
     * 初始化主页面
     */
    private void initView(){

        firstBottomImage = (ImageView) findViewById(R.id.first_bottom_image);
        secondBottomImage = (ImageView) findViewById(R.id.second_bottom_image);
        thirdBottomImage = (ImageView) findViewById(R.id.third_bottom_image);
        fourthBottomImage = (ImageView) findViewById(R.id.fourth_bottom_image);

        firstBottomText = (TextView) findViewById(R.id.first_bottom_text);
        secondBottomText = (TextView) findViewById(R.id.second_bottom_text);
        thirdBottomText = (TextView) findViewById(R.id.third_bottom_text);
        fourthBottomText = (TextView) findViewById(R.id.fourth_bottom_text);

        firstBottomLayout = (RelativeLayout) findViewById(R.id.first_bottom_layout);
        secondBottomLayout = (RelativeLayout) findViewById(R.id.second_bottom_layout);
        thirdBottomLayout = (RelativeLayout) findViewById(R.id.third_bottom_layout);
        fourthBottomLayout = (RelativeLayout) findViewById(R.id.fourth_bottom_layout);
        firstBottomLayout.setOnClickListener(MainActivity.this);
        secondBottomLayout.setOnClickListener(MainActivity.this);
        thirdBottomLayout.setOnClickListener(MainActivity.this);
        fourthBottomLayout.setOnClickListener(MainActivity.this);

    }
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.first_bottom_layout:
                setChoiceItem(0);
                break;
            case R.id.second_bottom_layout:
                setChoiceItem(1);
                break;
            case R.id.third_bottom_layout:
                setChoiceItem(2);
                break;
            case R.id.fourth_bottom_layout:
                setChoiceItem(3);
                break;
            default:
                break;
        }
    }
    /**
     * 设置点击选项卡事件处理
     *
     */
    private void setChoiceItem(int index){
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        clearChoice();
        hideFragments(fragmentTransaction);
        switch(index){
            case 0:
                firstBottomText.setTextColor(dark);
                firstBottomLayout.setBackgroundColor(gray);
                if(today_fg == null){
                    today_fg = new TodayFragment();
                    fragmentTransaction.add(R.id.centerFrag_layout,today_fg);
                }else{
                    fragmentTransaction.show(today_fg);
                }
                break;
            case 1:
                secondBottomText.setTextColor(dark);
                secondBottomLayout.setBackgroundColor(gray);
                if(statistics_fg == null){
                    statistics_fg = new StatisticsFragment();
                    fragmentTransaction.add(R.id.centerFrag_layout,statistics_fg);
                }else{
                    fragmentTransaction.show(statistics_fg);
                }
                break;
            case 2:
                thirdBottomText.setTextColor(dark);
                thirdBottomLayout.setBackgroundColor(gray);
                if(agenda_fg == null){
                    agenda_fg = new AgendaFragment();
                    fragmentTransaction.add(R.id.centerFrag_layout,agenda_fg);
                }else{
                    fragmentTransaction.show(agenda_fg);
                }
                break;
            case 3:
                fourthBottomText.setTextColor(dark);
                fourthBottomLayout.setBackgroundColor(gray);
                if(setting_fg == null){
                    setting_fg = new SettingFragment();
                    fragmentTransaction.add(R.id.centerFrag_layout,setting_fg);
                }else{
                    fragmentTransaction.show(setting_fg);
                }
                break;
        }
        fragmentTransaction.commit();
    }

    private void clearChoice(){
        firstBottomText.setTextColor(gray);
        firstBottomLayout.setBackgroundColor(white);
        secondBottomText.setTextColor(gray);
        secondBottomLayout.setBackgroundColor(white);
        thirdBottomText.setTextColor(gray);
        thirdBottomLayout.setBackgroundColor(white);
        fourthBottomText.setTextColor(gray);
        fourthBottomLayout.setBackgroundColor(white);

    }
    private void hideFragments(FragmentTransaction fragmentTransaction){
        if(today_fg != null){
            fragmentTransaction.hide(today_fg);
        }
        if(statistics_fg != null){
            fragmentTransaction.hide(statistics_fg);
        }
        if(agenda_fg != null){
            fragmentTransaction.hide(agenda_fg);
        }
        if(setting_fg != null){
            fragmentTransaction.hide(setting_fg);
        }
    }
}

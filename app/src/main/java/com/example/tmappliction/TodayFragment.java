package com.example.tmappliction;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by dell on 2017/8/10.
 */

public class TodayFragment extends Fragment {

    private static final String TAG = "TodayFragment";
    private DateFormat mDateFormat = new SimpleDateFormat();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LitePal.initialize(getContext());
        View view = inflater.inflate(R.layout.today, container, false);
        initAll(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initAll(getView());
    }

    private void initAll(View view) {
        int param_time;//当天所用手机总时间
        TextView totalTime = (TextView) view.findViewById(R.id.total_today);
        RecyclerView recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView .setLayoutManager(layoutManager);
        List<UsageStats> mUsageStatistics = getUsageStatistics(UsageStatsManager.INTERVAL_DAILY);
        UsageListAdapter adapter = new UsageListAdapter(mUsageStatistics,getActivity());
        recyclerView.setAdapter(null);
        recyclerView.setAdapter(adapter);
        param_time = drawBarChart(mUsageStatistics,view);
        totalTime.setText(String.valueOf(param_time)+" minutes");
        saveInDB(mUsageStatistics,param_time);

        SharedPreferences pref = getContext().getSharedPreferences("data",MODE_PRIVATE);
        boolean judgeImg = pref.getBoolean("nof",false);
        SharedPreferences.Editor editor1 = pref.edit();
        if(judgeImg) {
            editor1.putBoolean("nof",true);
            getActivity().startService(new Intent(getActivity(),DataSaveService.class));
        }else{
            editor1.putBoolean("nof",false);
            Intent startIntent = new Intent(getActivity(),DataSaveService.class);
            getActivity().stopService(startIntent);
        }
        editor1.apply();
    }

    private long getStartTimeOfDay(long now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private long getEndTimeOfDay(long now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    public List<UsageStats> getUsageStatistics(int intervalType) {
        Calendar cal = Calendar.getInstance();

        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getActivity().
                getSystemService(Context.USAGE_STATS_SERVICE);
        List<UsageStats> queryUsageStats = null;

        long endTime = cal.getTimeInMillis();
        //cal.add(Calendar.DAY_OF_WEEK,-1);//DAY_OF_MONTH  和 DAY_OF_WEEK 都一样
        long startTime = getStartTimeOfDay(endTime);

        switch(intervalType){
            case  UsageStatsManager.INTERVAL_DAILY:


                queryUsageStats = mUsageStatsManager.queryUsageStats(intervalType,startTime,endTime);
                if (queryUsageStats.size() == 0) {
                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
                    {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());//获取当前碎片所处的上下文？？？？
                        dialog.setTitle("Error");
                        dialog.setMessage("请您在使用前先设置权限");
                        dialog.setNegativeButton("设置", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try{
                                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                                }catch (Exception e){
                                    Toast.makeText(getActivity(),"无法开启查看使用情况的应用程序的面板",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        dialog.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                android.os.Process.killProcess(android.os.Process.myPid());
                            }
                        });
                        dialog.show();
                    }
                    else{
                        Toast.makeText(getActivity(),"您的安卓版本过低，无法使用此应用",Toast.LENGTH_SHORT);
                    }
                }
                break;
            default:
                break;
        }

        for (int i=queryUsageStats.size()-1; i>=0; i--) {
            if(queryUsageStats.get(i).getLastTimeUsed() < startTime)
                queryUsageStats.remove(i);
        }
        //排除使用时间小于1分钟的
        for (int i = queryUsageStats.size()-1;i>=0;i--){
            if(queryUsageStats.get(i).getTotalTimeInForeground()<90000)
                queryUsageStats.remove(i);
        }


        //排序
        for(int i=0;i<queryUsageStats.size();i++){
            for(int j=i;j<queryUsageStats.size();j++){
                if(queryUsageStats.get(i).getTotalTimeInForeground()<
                        queryUsageStats.get(j).getTotalTimeInForeground()){
                    Collections.swap(queryUsageStats,i,j);
                }
            }
        }
        return queryUsageStats;
    }



    //画表格的同时返回总时间
    public int drawBarChart(List<UsageStats> usageStatsList,View view){
        int totalTime = 0;
        BarChart barChart = (BarChart)view.findViewById(R.id.today_barchart);
        if(usageStatsList.size()==0) barChart.setVisibility(BarChart.INVISIBLE);//如果无数据则不显示图表
        ArrayList<BarEntry> vals = new ArrayList<>();
        ArrayList<String>xVals = new ArrayList<>();
        barChart.setNoDataText("您今日未使用App");
        List<UsageStats> mUsageStatsList = usageStatsList;
        Context mcontext = getActivity();
        PackageManager packageManager = mcontext.getPackageManager();
        ApplicationInfo applicationInfo ;
        int index = 0;
        for(UsageStats usageStats: mUsageStatsList){
            totalTime += usageStats.getTotalTimeInForeground()/60000;
            String aaa=mDateFormat.format(new Date(usageStats.getFirstTimeStamp()));

            try{
                applicationInfo = packageManager.getApplicationInfo(usageStats.getPackageName(),0);
                xVals.add(packageManager.getApplicationLabel(applicationInfo)+"");
                BarEntry barEntry = new BarEntry(usageStats.getTotalTimeInForeground()/60000,index);
                vals.add(barEntry);
            }catch (PackageManager.NameNotFoundException e){
                e.getStackTrace();
            }
            index++;
        }
        barChart.setEnabled(false);//不生成左下角的标签
        BarDataSet a = new BarDataSet(vals,"Time Of Usage");
        a.setAxisDependency(YAxis.AxisDependency.LEFT);
        a.setBarSpacePercent(50f); //设置柱形间距，值越大，柱形越小
        BarData data = new BarData(xVals,a);
        barChart.setData(data);
        barChart.setVisibleXRangeMaximum(6);//设置x轴最大标签数，需要在设置数据源后生效
        barChart.setVisibleXRangeMinimum(6);//设置x轴最小标签数
        barChart.setHighlightPerTapEnabled(false);//取消高亮
        barChart.setHighlightPerDragEnabled(false);//取消高亮
        barChart.setScaleEnabled(false);//取消x y轴缩放
        barChart.getAxisRight().setEnabled(false);//隐藏y轴右边边线
        barChart.setDescription("");
        barChart.animateY(3000);//动画
        XAxis xl = barChart.getXAxis();
        xl.setLabelRotationAngle(-45);//设置x轴字体显示角度
        xl.setLabelsToSkip(0);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);//设置X轴的位置
        barChart.invalidate();
        return totalTime;
    }

    public static void saveInDB(List<UsageStats> mUsageStatistics, int totalTime){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month_today = calendar.get(Calendar.MONTH);
        int day_today = calendar.get(Calendar.DAY_OF_WEEK);

        LitePal.getDatabase();
        boolean flag = false;//用来标识数据库中是否已经储存了当天数据

        //查找数据库中所有数据，放入list中
        List<DayUsageStats> dayUsageStatsList = DataSupport.findAll(DayUsageStats.class);
        int count = dayUsageStatsList.size()-7;

        Log.d("数据库中数据量",dayUsageStatsList.size()+"");
//        DataSupport.deleteAll(DayUsageStats.class);//不能先全部删除数据库中数据再重新添加。并不知道为什么

        //查找列表中如果有当天的数据，则更改totalTime和usagetaslistL字段
        for(DayUsageStats u : dayUsageStatsList){
            if(u.getDay() == day_today && u.getMonth() == month_today && u.getYear() == year){
                u.setUsageStatsList(mUsageStatistics);
                u.setTotalTime(totalTime);
                u.updateAll("day = ? and month = ? and year = ?",
                        String.valueOf(day_today),String.valueOf(month_today),String.valueOf(year));
                flag = true;
                break;
            }
        }
        //如果列表中没有当天数据，则新增一条数据
        if(flag == false){
            DayUsageStats tem = new DayUsageStats();
            tem.setMonth(month_today);
            tem.setDay(day_today);
            tem.setYear(year);
            tem.setUsageStatsList(mUsageStatistics);
            tem.setTotalTime(totalTime);
            tem.save();
        }
        //确保七条数据
        if(count > 0){
            DataSupport.delete(DayUsageStats.class,0);
        }
    }

}

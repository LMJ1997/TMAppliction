package com.example.tmappliction;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.telephony.TelephonyManager;
import android.content.Context;
import android.widget.TextView;
import java.text.DecimalFormat;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import org.litepal.crud.DataSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by dell on 2017/8/10.
 */

public class StatisticsFragment extends Fragment {

    private static final int GET_RANK_DONE = 1;
    private static final int GET_RANK_FAILED = 0;

    private int totalTimeUpLoad;
    private TextView textView;
    private String ranking;
    private double rank;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.statistics, container, false);
        drawLineChart(view);
        textView =(TextView) view.findViewById(R.id.rankingText);
        TextView timetrans = (TextView)view.findViewById(R.id.timetrans);
        timeTrans(timetrans,totalTimeUpLoad);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    PHPMySQL phpMySQL = new PHPMySQL();
                    SharedPreferences pref = getActivity().getSharedPreferences("userinfo", MODE_PRIVATE);
                    phpMySQL.inertUsetime(totalTimeUpLoad,pref.getString("phoneNumber",""));
                    ranking = phpMySQL.getRanking(totalTimeUpLoad);

                    rank = Double.parseDouble(ranking);
                    handler.sendEmptyMessage(GET_RANK_DONE);
                }catch (Exception e){
                    handler.sendEmptyMessage(GET_RANK_FAILED);
                    e.printStackTrace();
                }
            }
        }).start();
        return view;
}

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @SuppressLint("SetTextI18n")
        public void handleMessage(Message msg){
            switch (msg.what) {
                case GET_RANK_DONE:
                    textView.setText("您今日使用手机时间少于"+rank+"%的用户");
                    break;
                case GET_RANK_FAILED:
                    break;

                default:
                    break;
            }

        }
    };

    public void drawLineChart(View v){
        LineChart lineChart = (LineChart)v.findViewById(R.id.stats_linechart);
        List<DayUsageStats> dayUsageStatsList = DataSupport.findAll(DayUsageStats.class);
        Log.d("这是数据数量:",dayUsageStatsList.size()+"");
        ArrayList<Entry> vals = new ArrayList<>();
        ArrayList<String>xVals = new ArrayList<>();
        int i = 0;
        for(DayUsageStats tem : dayUsageStatsList){
            Entry entry = new Entry(Float.valueOf(tem.getTotalTime()),i);
            vals.add(entry);
            xVals.add("");
            i++;
        }
        //获取要上传的时间
        totalTimeUpLoad=dayUsageStatsList.get(dayUsageStatsList.size()-1).getTotalTime();

        LineDataSet a = new LineDataSet(vals,"七天使用情况");
        a.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineData lineData = new LineData(xVals,a);
        lineChart.setData(lineData);
        lineChart.setVisibleXRangeMaximum(7);//设置x轴最大标签数，需要在设置数据源后生效
        lineChart.setVisibleXRangeMinimum(7);//设置x轴最小标签数
        lineChart.setHighlightPerTapEnabled(false);//取消高亮
        lineChart.setHighlightPerDragEnabled(false);//取消高亮
        lineChart.setScaleEnabled(false);//取消x y轴缩放
        lineChart.getAxisRight().setEnabled(false);//隐藏y轴右边边线
        XAxis xl = lineChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);//设置X轴的位置
        lineChart.setDescription("七天使用情况");
        lineChart.animateY(4000);//动画
        lineChart.invalidate();
    }

     private void timeTrans(TextView textView,int totalTime){
        String suggestion = "您今日使用手机"+totalTime+"分钟("
                +new DecimalFormat("0.00").format(totalTime/60.0)+"小时),在这段时间里,您可以: 看书"
                +totalTime*700+"字/背单词"+totalTime*2+"个/跑步"+new DecimalFormat("0.00").format(totalTime*0.12)+
                "公里/消耗脂肪"+new DecimalFormat("0.00").format(totalTime/1200.0)+"斤。";
        textView.setText(suggestion);
    }

}
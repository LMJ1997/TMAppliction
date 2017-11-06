package com.example.tmappliction;


import android.app.usage.UsageStats;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by dell on 2017/8/11.
 */

public class UsageListAdapter extends RecyclerView.Adapter<UsageListAdapter.ViewHolder> {
    private List<UsageStats> mUsageStatsList;
    private Context mcontext;

    public UsageListAdapter(List<UsageStats> usageStats,Context context){
        mUsageStatsList = usageStats;
        mcontext = context;
    }
     static class ViewHolder extends RecyclerView.ViewHolder {
         private final TextView mAppName;
         private final TextView mAppTime;
         private final ImageView mAppIcon;
         public ViewHolder(View view){
             super(view);
             mAppIcon = (ImageView)view.findViewById(R.id.app_icon);
             mAppName = (TextView) view.findViewById(R.id.app_name);
             mAppTime = (TextView) view.findViewById(R.id.app_today_time);
         }
     }
     @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
         View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item,parent,false);
         ViewHolder holder = new ViewHolder(view);
         return holder;
     }
     @Override
    public void onBindViewHolder(ViewHolder holder, int position){
         UsageStats usageStats = mUsageStatsList.get(position);
         PackageManager packageManager = mcontext.getPackageManager();
         ApplicationInfo applicationInfo = new ApplicationInfo();//
         try{
                applicationInfo = packageManager.getApplicationInfo(usageStats.getPackageName(),0);//
         }catch (PackageManager.NameNotFoundException e){
             e.getStackTrace();
         }

         holder.mAppIcon.setImageDrawable(packageManager.getApplicationIcon(applicationInfo));
        //旧版： holder.mAppName.setText(usageStats.getPackageName());
         holder.mAppName.setText(packageManager.getApplicationLabel(applicationInfo));
         holder.mAppTime.setText(String.valueOf(usageStats.getTotalTimeInForeground()/60000)+" minutes");
     }
     @Override
    public int getItemCount(){
         return mUsageStatsList.size();
     }

}


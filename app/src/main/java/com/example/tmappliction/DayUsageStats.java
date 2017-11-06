package com.example.tmappliction;

import android.app.usage.UsageStats;

import org.litepal.crud.DataSupport;

import java.util.Date;
import java.util.List;

/**
 * Created by dell on 2017/8/19.
 */

public class DayUsageStats extends DataSupport{
    private int year;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    private int id;
    private List<UsageStats> UsageStatsList;
    private int totalTime;
    private int month;
    private int day;

    public int getMonth() {
        return month;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public List<UsageStats> getUsageStatsList() {
        return UsageStatsList;
    }

    public void setUsageStatsList(List<UsageStats> usageStatsList) {
        UsageStatsList = usageStatsList;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }
}

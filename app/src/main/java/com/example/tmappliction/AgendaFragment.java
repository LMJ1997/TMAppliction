package com.example.tmappliction;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.dsw.calendar.component.MonthView;
import com.dsw.calendar.views.GridCalendarView;

/**
 * Created by dell on 2017/8/10.
 */

public class AgendaFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.agenda, container, false);
        //Toolbar toolbar = (Toolbar)view.findViewById(R.id.toolbar);
        //((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        final GridCalendarView calendarView = (GridCalendarView)view.findViewById(R.id.calendarView);
        calendarView.setDateClick(new MonthView.IDateClick(){
            @Override
            public void onClickOnDate(int year, int month, int day){
                Intent intent = new Intent(getActivity(),AgendaSetAct.class);
                startActivity(intent);
            }
        });

        return view;
    }
}


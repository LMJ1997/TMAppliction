package com.example.tmappliction;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;

public class MessageAndRemind extends AppCompatActivity implements View.OnClickListener {

    private ImageView messageRemindBack;
    private Switch messageRemind_switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_and_remind);

        messageRemindBack = (ImageView)findViewById(R.id.message_remind_back);
        messageRemindBack.setOnClickListener(this);

        messageRemind_switch = (Switch)findViewById(R.id.message_remind_switch);
        messageRemind_switch.setOnClickListener(this);
        SharedPreferences messageRemind = this.getSharedPreferences("messageRemind", MODE_PRIVATE);
        messageRemind_switch.setChecked(messageRemind.getBoolean("is", true));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.message_remind_back:
                finish();
                break;
            case R.id.message_remind_switch:
                SharedPreferences.Editor editor = getSharedPreferences("messageRemind", MODE_PRIVATE).edit();
                editor.putBoolean("is", messageRemind_switch.isChecked());
                editor.apply();
                break;
            default:
                break;
        }
    }
}

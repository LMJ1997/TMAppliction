package com.example.tmappliction;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;

public class Privacy extends AppCompatActivity implements View.OnClickListener {

    private ImageView privacyBack;
    private Switch privacySwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        privacyBack = (ImageView)findViewById(R.id.privacy_back);
        privacyBack.setOnClickListener(this);

        privacySwitch = (Switch)findViewById(R.id.privacy_switch);
        privacySwitch.setOnClickListener(this);
        SharedPreferences privacy = this.getSharedPreferences("privacy", MODE_PRIVATE);
        privacySwitch.setChecked(privacy.getBoolean("is", true));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.privacy_back:
                finish();
                break;
            case R.id.privacy_switch:
                SharedPreferences.Editor editor = getSharedPreferences("privacy", MODE_PRIVATE).edit();
                editor.putBoolean("is", privacySwitch.isChecked());
                editor.apply();
                break;
            default:
                break;
        }
    }
}

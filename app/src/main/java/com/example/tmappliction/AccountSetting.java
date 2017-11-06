package com.example.tmappliction;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class AccountSetting extends AppCompatActivity implements View.OnClickListener {

    private ConstraintLayout constraintLayout1;
    private ConstraintLayout constraintLayout2;
    private ConstraintLayout constraintLayout3;

    private ImageView goBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);
        constraintLayout1 = (ConstraintLayout)findViewById(R.id.constraintLayout2);
        constraintLayout2 = (ConstraintLayout)findViewById(R.id.constraintLayout3);
        constraintLayout3 = (ConstraintLayout)findViewById(R.id.constraintLayout4);
        constraintLayout1.setOnClickListener(this);
        constraintLayout2.setOnClickListener(this);
        constraintLayout3.setOnClickListener(this);

        goBack = (ImageView)findViewById(R.id.account_setting_back);
        goBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.constraintLayout2:
                Intent intent = new Intent(this, ChangeNickName.class);
                startActivity(intent);
                break;
            case R.id.constraintLayout3:
                Intent intent1 = new Intent(this, ChangePassword.class);
                startActivity(intent1);
                break;
            case R.id.constraintLayout4:
                Intent intent2 = new Intent(this, ChangePhoneNum.class);
                startActivity(intent2);
                break;
            case R.id.account_setting_back:
                finish();
                break;
            default:
                break;
        }
    }
}

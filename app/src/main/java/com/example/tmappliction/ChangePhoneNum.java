package com.example.tmappliction;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mob.MobSDK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class ChangePhoneNum extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ChangePhoneNum";

    private EventHandler eventHandler;
    private int i = 60;

    private EditText inputOldPhoneNum;
    private EditText inputYanZhengMa;
    private EditText inputNewPhoneNum;
    private Button sendYanZhengMaButton;

    private static final int IS_OLDPHONENUM_EXIST = 1;
    private static final int NOT_NEWPHONENUM_EXIST = 2;
    private static final int IS_NEWPHONENUM_EXIST = 3;
    private static final int OTHER_ERROR = 4;
    private static final int VERIFICATION_SUCCESS = 5;
    private static final int VERIFICATIONCODE_NOT_CORRECT = 6;
    private static final int CHANGE_DONE = 7;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(eventHandler);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_phone_num);
        inputOldPhoneNum = (EditText)findViewById(R.id.inputOldPhoneNum);
        inputNewPhoneNum = (EditText)findViewById(R.id.inputNewPhoneNum);
        inputYanZhengMa = (EditText)findViewById(R.id.inputyanZhengMa2);
        ConstraintLayout updatePhoneNumButton = (ConstraintLayout) findViewById(R.id.updatePhoneNumButton);
        sendYanZhengMaButton = (Button)findViewById(R.id.sendYanZhengMaButton);

        ImageView changePhoneBack = (ImageView) findViewById(R.id.change_phone_back);
        changePhoneBack.setOnClickListener(this);
        updatePhoneNumButton.setOnClickListener(this);
        sendYanZhengMaButton.setOnClickListener(this);

        MobSDK.init(this);

        eventHandler = new EventHandler() {
            public void afterEvent(int event, int result, Object data) {
                if(result == SMSSDK.RESULT_COMPLETE) {
                    if(event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        handler.sendEmptyMessage(VERIFICATION_SUCCESS);
                    }
                }
                else if(result == SMSSDK.RESULT_ERROR) {
                    if(event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        handler.sendEmptyMessage(VERIFICATIONCODE_NOT_CORRECT);
                    }
                }
            }
        };

        SMSSDK.registerEventHandler(eventHandler);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.change_phone_back:
                finish();
                break;
            case R.id.sendYanZhengMaButton:
                if (inputOldPhoneNum.getText().toString().length() != 11){
                    Toast.makeText(this,"请正确输入原手机号",Toast.LENGTH_SHORT).show();
                    break;
                }
                SharedPreferences pref = getSharedPreferences("userinfo",MODE_PRIVATE);
                if (!((pref.getString("phoneNumber","")).equals(inputOldPhoneNum.getText().toString()))){
                    Toast.makeText(this,"输入与当前登陆手机号不匹配",Toast.LENGTH_SHORT).show();
                    break;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PHPMySQL connector = new PHPMySQL();
                        JSONArray jsonArray = connector.searchData("userinfo", "phonenumber", "phonenumber=" + inputNewPhoneNum.getText().toString());
                        try {
                            if (jsonArray == null) {
                                handler.sendEmptyMessage(NOT_NEWPHONENUM_EXIST);//该情况下可以更换号码,则发送验证码给新号码
                            }else {
                                JSONObject jsonObject = jsonArray.getJSONObject(0);
                                String result = jsonObject.optString("network", "");
                                if (result.equals("")) {
                                    handler.sendEmptyMessage(IS_NEWPHONENUM_EXIST);
                                }
                                else if (result.equals("error")) {
                                    handler.sendEmptyMessage(OTHER_ERROR);
                                }
                            }
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            case R.id.updatePhoneNumButton:
                if (inputYanZhengMa.getText().toString().length()!=4){
                    Toast.makeText(ChangePhoneNum.this,"请输入4位验证码",Toast.LENGTH_SHORT).show();
                    break;
                }
                SMSSDK.submitVerificationCode("86", inputNewPhoneNum.getText().toString(), inputYanZhengMa.getText().toString());

            default:
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case -9:
                    sendYanZhengMaButton.setText("(" + i + "s)");
                    break;
                case -8:
                    sendYanZhengMaButton.setText("获取验证码");
                    sendYanZhengMaButton.setClickable(true);
                    i = 60;
                    break;
                case IS_OLDPHONENUM_EXIST:
                    SMSSDK.getVerificationCode("86", inputOldPhoneNum.getText().toString());
                    Toast.makeText(ChangePhoneNum.this,"验证码发送成功，请等待大约一分钟",Toast.LENGTH_SHORT).show();
                    sendYanZhengMaButton.setClickable(false);
                    sendYanZhengMaButton.setText("(" + i + "s)");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (; i > 0; i--) {
                                handler.sendEmptyMessage(-9);
                                if (i <= 0) {
                                    break;
                                }
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            handler.sendEmptyMessage(-8);
                        }
                    }).start();
                    break;
                case IS_NEWPHONENUM_EXIST:
                    Toast.makeText(ChangePhoneNum.this,"新输入的手机号已被绑定，请更换号码",Toast.LENGTH_SHORT).show();
                    break;
                case NOT_NEWPHONENUM_EXIST:
                    SMSSDK.getVerificationCode("86", inputNewPhoneNum.getText().toString());
                    Toast.makeText(ChangePhoneNum.this, "获取验证码成功，接收可能需要一分钟~", Toast.LENGTH_SHORT).show();
                    sendYanZhengMaButton.setClickable(false);
                    sendYanZhengMaButton.setText("(" + i + "s)");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (; i > 0; i--) {
                                handler.sendEmptyMessage(-9);
                                if (i <= 0) {
                                    break;
                                }
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            handler.sendEmptyMessage(-8);
                        }
                    }).start();
                    break;

                case OTHER_ERROR:
                    Toast.makeText(ChangePhoneNum.this,"出错了，请稍后再试",Toast.LENGTH_SHORT).show();
                    break;

                case VERIFICATION_SUCCESS:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            PHPMySQL connector = new PHPMySQL();
                            SharedPreferences pref = getSharedPreferences("userinfo",MODE_PRIVATE);
                            //取出本地数据
                            String nickName = pref.getString("nickname","");
                            //修改本地数据
                            SharedPreferences.Editor editor = getSharedPreferences("userinfo",MODE_PRIVATE).edit();
                            editor.putString("nickname", nickName);
                            editor.putString("phoneNumber",inputNewPhoneNum.getText().toString());
                            editor.putString("password",inputNewPhoneNum.getText().toString());
                            editor.apply();

                            String result = connector.updateData("userinfo", "phonenumber='" + inputNewPhoneNum.getText().toString() + "'", "phonenumber='" + inputOldPhoneNum.getText().toString() + "'");
                            if(result.equalsIgnoreCase("done")) {
                                handler.sendEmptyMessage(CHANGE_DONE);
                            }
                            else {
                                Log.e(TAG, "run: " + result);
                            }

                        }
                    }).start();
                    break;
                case CHANGE_DONE:
                    Toast.makeText(ChangePhoneNum.this,"修改电话号码成功",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case VERIFICATIONCODE_NOT_CORRECT:
                    Toast.makeText(ChangePhoneNum.this,"验证码不正确",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}

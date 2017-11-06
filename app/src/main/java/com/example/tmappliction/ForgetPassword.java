package com.example.tmappliction;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mob.MobSDK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class ForgetPassword extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ForgetPassword";

    private int i = 60;
    private EventHandler eventHandler;

    private static final int VERIFICATIONCODE_NOT_CORRECT = -5;
    private static final int VERIFICATION_SUCCESS = 9;
    private static final int CHANGE_DONE = 10;
    private static final int NOT_REGISTERED = 11;
    private static final int REGISTERED = 12;
    private static final int OTHER_ERROR = 13;

    private EditText phoneNumForgetPassword;
    private EditText verificationCodeForgetPassword;
    private EditText newPasswordForgetPassword;
    private EditText confirmNewPasswordForgetPassword;
    private Button getVerificationCodeButton;
    private Button confirmForgetPassword;
    private Button goBackForgetPassword;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(eventHandler);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

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

        phoneNumForgetPassword = (EditText)findViewById(R.id.phoneNum_forgetPassword);
        verificationCodeForgetPassword = (EditText)findViewById(R.id.verificationCode_forgetPassword);
        newPasswordForgetPassword = (EditText)findViewById(R.id.newPassword_forgetPassword);
        confirmNewPasswordForgetPassword = (EditText)findViewById(R.id.confirmNewPassword_forgetPassword);
        getVerificationCodeButton = (Button)findViewById(R.id.get_VerifivationCode_forgetPassword);
        confirmForgetPassword = (Button)findViewById(R.id.confirm_ForgetPassword);
        goBackForgetPassword = (Button)findViewById(R.id.go_back_forgetPassword);


        goBackForgetPassword.setOnClickListener(this);
        getVerificationCodeButton.setOnClickListener(this);
        confirmForgetPassword.setOnClickListener(this);

        SMSSDK.registerEventHandler(eventHandler);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.get_VerifivationCode_forgetPassword:
                if(phoneNumForgetPassword.getText().toString().length() != 11) {
                    Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                    break;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PHPMySQL connector = new PHPMySQL();
                        JSONArray jsonArray = connector.searchData("userinfo", "phonenumber", "phonenumber=" + phoneNumForgetPassword.getText().toString());
                        try {
                            if(jsonArray == null) {
                                handler.sendEmptyMessage(NOT_REGISTERED);
                            }
                            else {
                                JSONObject jsonObject = jsonArray.getJSONObject(0);
                                String result = jsonObject.optString("network", "");
                                if (result.equals("")) {
                                    handler.sendEmptyMessage(REGISTERED);
                                }
                                else if (result.equals("error")) {
                                    handler.sendEmptyMessage(OTHER_ERROR);
                                }
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            case R.id.confirm_ForgetPassword:
                if(phoneNumForgetPassword.getText().toString().length() != 11) {
                    Toast.makeText(this, "请检查您的手机号", Toast.LENGTH_SHORT).show();
                    break;
                }
                else if(verificationCodeForgetPassword.getText().toString().length() != 4) {
                    Toast.makeText(this, "验证码应该为4位", Toast.LENGTH_SHORT).show();
                    break;
                }
                else if(newPasswordForgetPassword.getText().toString().length() < 8) {
                    Toast.makeText(this, "密码为8~16位", Toast.LENGTH_SHORT).show();
                    break;
                }
                else if(!(confirmNewPasswordForgetPassword.getText().toString().equals(newPasswordForgetPassword.getText().toString()))) {
                    Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                    break;
                }
                SMSSDK.submitVerificationCode("86", phoneNumForgetPassword.getText().toString(), verificationCodeForgetPassword.getText().toString());
                break;
            case R.id.go_back_forgetPassword:
                finish();
                break;
            default:
                break;
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            switch (msg.what) {
                case -9:
                    getVerificationCodeButton.setText("(" + i + "s)");
                    break;
                case -8:
                    getVerificationCodeButton.setText("获取验证码");
                    getVerificationCodeButton.setClickable(true);
                    i = 60;
                    break;
                case VERIFICATION_SUCCESS:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            PHPMySQL connector = new PHPMySQL();
                            String result = connector.updateData("userinfo", "password='" + newPasswordForgetPassword.getText().toString() + "'", "phonenumber='" + phoneNumForgetPassword.getText().toString() + "'");
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
                    Toast.makeText(ForgetPassword.this, "修改密码成功", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case VERIFICATIONCODE_NOT_CORRECT:
                    Toast.makeText(ForgetPassword.this, "验证码不正确", Toast.LENGTH_SHORT).show();
                    break;
                case REGISTERED:
                    SMSSDK.getVerificationCode("86", phoneNumForgetPassword.getText().toString());
                    Toast.makeText(ForgetPassword.this, "获取验证码成功，接收可能需要一分钟~", Toast.LENGTH_SHORT).show();
                    getVerificationCodeButton.setClickable(false);
                    getVerificationCodeButton.setText("(" + i + "s)");
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
                case NOT_REGISTERED:
                    Toast.makeText(ForgetPassword.this, "该手机号还未注册，请先注册", Toast.LENGTH_SHORT).show();
                    break;
                case OTHER_ERROR:
                    Toast.makeText(ForgetPassword.this, "啊哦，出错了，请稍候再试吧", Toast.LENGTH_SHORT).show();
                default:
                    break;
            }
        }
    };

}

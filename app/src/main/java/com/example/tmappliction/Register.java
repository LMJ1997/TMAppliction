package com.example.tmappliction;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mob.MobSDK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class Register extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Register";
    private static final int HAVE_BEEN_REGISTERED = 10;
    private static final int REGISTER_DONE = 11;
    private static final int VERIFICATION_SUCCESS = 9;
    private static final int VERIFICATIONCODE_NOT_CORRECT = -5;
    private static final int OTHER_ERROR = 12;
    private static final int NOT_BEEN_REGISTERED = 13;

    private boolean haveBeenRegistered = false;

    int i = 60;



    private EventHandler eventHandler;
    private Button registerButton;
    private Button getVerificationCodeButton;
    private TextView alreadyHaveAccount;
    private EditText phoneNumRegister;
    private EditText nicknameRegister;
    private EditText passwordRegister;
    private EditText confirmPasswordRegister;
    private EditText verificationCodeRegister;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(eventHandler);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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

        registerButton = (Button)findViewById(R.id.button_register);
        getVerificationCodeButton = (Button)findViewById(R.id.get_verificationCode_register);
        alreadyHaveAccount = (TextView)findViewById(R.id.already_have_account);
        phoneNumRegister = (EditText)findViewById(R.id.phoneNum_register);
        verificationCodeRegister = (EditText)findViewById(R.id.verification_register);
        nicknameRegister = (EditText)findViewById(R.id.nickname_register);
        passwordRegister = (EditText)findViewById(R.id.password_register);
        confirmPasswordRegister = (EditText)findViewById(R.id.confirm_password_register);

        alreadyHaveAccount.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        getVerificationCodeButton.setOnClickListener(this);

        SMSSDK.registerEventHandler(eventHandler);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.get_verificationCode_register:
                if(phoneNumRegister.getText().toString().length() != 11) {
                    Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                    break;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PHPMySQL connector = new PHPMySQL();
                        JSONArray jsonArray = null;
                        try {
                            jsonArray = connector.searchData("userinfo", "phonenumber", "phonenumber=" + phoneNumRegister.getText().toString());

                            if(jsonArray != null) {
                                JSONObject jsonObject = jsonArray.getJSONObject(0);
                                String result = jsonObject.optString("network", "");
                                if (result.equals("error")){
                                    handler.sendEmptyMessage(OTHER_ERROR);
                                }
                                else if (result.equals("")) {
                                    handler.sendEmptyMessage(HAVE_BEEN_REGISTERED);
                                }
                            }
                            else  {
                                handler.sendEmptyMessage(NOT_BEEN_REGISTERED);
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            case R.id.button_register:
                if(phoneNumRegister.getText().toString().length() != 11) {
                    Toast.makeText(this, "请检查您的手机号", Toast.LENGTH_SHORT).show();
                    break;
                }
                else if (verificationCodeRegister.getText().toString().length() != 4) {
                    Toast.makeText(this, "验证码应该为4位", Toast.LENGTH_SHORT).show();
                    break;
                }
                else if (nicknameRegister.getText().toString().length() == 0) {
                    Toast.makeText(this, "请输入昵称", Toast.LENGTH_SHORT).show();
                    break;
                }
                else if (passwordRegister.getText().toString().length() < 8) {
                    Toast.makeText(this, "密码为8~16位", Toast.LENGTH_SHORT).show();
                    break;
                }
                else if (!(confirmPasswordRegister.getText().toString().equals(passwordRegister.getText().toString()))) {
                    Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                    break;
                }
                SMSSDK.submitVerificationCode("86", phoneNumRegister.getText().toString(), verificationCodeRegister.getText().toString());
                break;
            case R.id.already_have_account:
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
                            String result = connector.insertData("userinfo", "nickname, password, phonenumber", "'"+nicknameRegister.getText().toString()+"', '"+passwordRegister.getText().toString()+"', '" + phoneNumRegister.getText().toString()+"'");
                            if(result.equals("Done")) {
                                handler.sendEmptyMessage(REGISTER_DONE);
                            }
                            else {
                                Log.e(TAG, "handleMessage: " + result);
                            }
                        }
                    }).start();

                    break;
                case NOT_BEEN_REGISTERED:
                    SMSSDK.getVerificationCode("86", phoneNumRegister.getText().toString());
                    Toast.makeText(Register.this, "获取验证码成功，接收可能需要一分钟~", Toast.LENGTH_SHORT).show();
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
                case HAVE_BEEN_REGISTERED:
                    Toast.makeText(Register.this, "该手机号已经被注册,请直接登录或找回密码", Toast.LENGTH_SHORT).show();
                    break;
                case REGISTER_DONE:
                    Toast.makeText(Register.this, "恭喜您，注册成功！", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case VERIFICATIONCODE_NOT_CORRECT:
                    Toast.makeText(Register.this, "验证码不正确", Toast.LENGTH_SHORT).show();
                    break;
                case OTHER_ERROR:
                    Toast.makeText(Register.this, "啊哦，出错了，请稍候再试吧", Toast.LENGTH_SHORT).show();
                default:
                    break;
            }
        }
    };
}

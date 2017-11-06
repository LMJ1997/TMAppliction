package com.example.tmappliction;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mob.MobSDK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;


public class ChangePassword extends AppCompatActivity implements View.OnClickListener {

    private EventHandler eventHandler;
    private int i = 60;

    private static final int CHANGE_PASSWORD_SUCCESS = 1;
    private static final int INVALID_INFOMATION = 0;
    private static final int OTHER_ERROR = 2;
    private static final int SEND_YANZHENGMA_SUCCESS = 3;
    private static final int VERIFICATION_SUCCESS = 9;
    private static final int VERIFICATIONCODE_NOT_CORRECT = -5;
    private static final int NOT_CURR_PHONE = 11;
    private static final int IS_CURR_PHONE=6;//查到了该记录

    private EditText inputYanZhengMa;
    private EditText inputNewPassword;
    private EditText inputPhoneNum;
    private Button yanZhengMaFaSongButton;
    private ConstraintLayout updatePasswordButton;
    private ImageView changePasswordBack;

    private PHPMySQL phpMySQL;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(eventHandler);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        inputPhoneNum =(EditText)findViewById(R.id.inputPhoneNum);
        inputYanZhengMa = (EditText)findViewById(R.id.inputYanzhengMa);
        inputNewPassword = (EditText)findViewById(R.id.inputNewPassword);
        updatePasswordButton = (ConstraintLayout)findViewById(R.id.UpdatePasswordButton);
        yanZhengMaFaSongButton = (Button)findViewById(R.id.yanZhengMabutton);
        changePasswordBack = (ImageView)findViewById(R.id.change_password_back);

        updatePasswordButton.setOnClickListener(this);
        yanZhengMaFaSongButton.setOnClickListener(this);
        changePasswordBack.setOnClickListener(this);

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


    //handler用于将新线程中的信息传给主线程
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case -9:
                    yanZhengMaFaSongButton.setText("(" + i + "s)");
                    break;
                case -8:
                    yanZhengMaFaSongButton.setText("获取验证码");
                    yanZhengMaFaSongButton.setClickable(true);
                    i = 60;
                    break;
                case CHANGE_PASSWORD_SUCCESS:
                    Toast.makeText(ChangePassword.this,"修改密码成功",Toast.LENGTH_SHORT).show();
                    break;
                case OTHER_ERROR:
                    Toast.makeText(ChangePassword.this,"好像哪里出问题了，请重试",Toast.LENGTH_SHORT).show();
                    break;
                case SEND_YANZHENGMA_SUCCESS:
                    Toast.makeText(ChangePassword.this,"验证码已发送",Toast.LENGTH_SHORT).show();
                    break;
                case VERIFICATIONCODE_NOT_CORRECT:
                    Toast.makeText(ChangePassword.this, "验证码不正确", Toast.LENGTH_SHORT).show();
                    break;
                case VERIFICATION_SUCCESS:
                    Toast.makeText(ChangePassword.this, "修改密码成功", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case IS_CURR_PHONE:
                    SMSSDK.getVerificationCode("86", inputPhoneNum.getText().toString());
                    Toast.makeText(ChangePassword.this, "获取验证码成功，接收可能需要一分钟~", Toast.LENGTH_SHORT).show();
                    yanZhengMaFaSongButton.setClickable(false);
                    yanZhengMaFaSongButton.setText("(" + i + "s)");
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
                case NOT_CURR_PHONE:
                    Toast.makeText(ChangePassword.this,"请输入当前登录的手机号",Toast.LENGTH_SHORT).show();
                default:
                    break;
            }

        }
    };



    private void change_password(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                phpMySQL = new PHPMySQL();
                String result;
                inputNewPassword = (EditText)findViewById(R.id.inputNewPassword);
                String newPassword = inputNewPassword.getText().toString();

                //使用SharedPreferences,将事先在Login中储存的电话号码（即账号）取出，将它作为username传入数据库中寻找该用户的信息
                SharedPreferences pref = getSharedPreferences("userinfo",MODE_PRIVATE);
                String userName_phoneNum = pref.getString("phoneNumber","");

                //由于后续要更新userinfo文件，使用SharedPerference修改时会清空整个文件，所以要提前将其中内容取出来
                String nickName = pref.getString("nickName","");
                String phoneNum = pref.getString("phoneNum","");
                String password = pref.getString("password","");

                //使用PHPMySQL类中的updateData方法，传入三个参数,修改服务器数据库中的信息
                result = phpMySQL.updateData("userinfo","password='"+newPassword+"'","phonenumber='"+userName_phoneNum+"'" );
                //使用handle将返回的信息传出，进行下一步UI操作
                if (result.equals("Done")){
                    handler.sendEmptyMessage(CHANGE_PASSWORD_SUCCESS);
                }else {
                    handler.sendEmptyMessage(OTHER_ERROR);
                }


                //更新SharedPreferences储存的userinfo文件中的信息
                SharedPreferences.Editor editor = getSharedPreferences("userinfo",MODE_PRIVATE).edit();
                editor.putString("nickname", nickName);
                editor.putString("phoneNum",phoneNum);
                editor.putString("password",phoneNum);
                editor.apply();
            }
        }).start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.change_password_back:
                finish();
                break;
            case R.id.yanZhengMabutton:
                if(inputPhoneNum.getText().toString().length() != 11) {
                    Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                    break;
                }
                SharedPreferences pref = getSharedPreferences("userinfo",MODE_PRIVATE);
                if ((pref.getString("phoneNumber", "")).equals(inputPhoneNum.getText().toString()))
                    handler.sendEmptyMessage(IS_CURR_PHONE);
                else
                    handler.sendEmptyMessage(NOT_CURR_PHONE);
                break;
            case R.id.UpdatePasswordButton:
                if(inputPhoneNum.getText().toString().length() != 11) {
                    Toast.makeText(this, "请检查您的手机号", Toast.LENGTH_SHORT).show();
                    break;
                }
                else if(inputYanZhengMa.getText().toString().length() != 4) {
                    Toast.makeText(this, "验证码应该为4位", Toast.LENGTH_SHORT).show();
                    break;
                }
                else if(inputNewPassword.getText().toString().length() < 8) {
                    Toast.makeText(this, "密码为8~16位", Toast.LENGTH_SHORT).show();
                    break;
                }
                change_password();
                SMSSDK.submitVerificationCode("86", inputPhoneNum.getText().toString(), inputYanZhengMa.getText().toString());
                break;

            default:
                break;
        }
    }
}

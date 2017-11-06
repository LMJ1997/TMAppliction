package com.example.tmappliction;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import cz.msebera.android.httpclient.Header;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Login";
    private static final int LOGIN_SUCCESS = 1;
    private static final int INVALID_INFOMATION = 0;
    private static final int OTHER_ERROR = 2;
    private static final int GET_IMG_SUCCESS = 3;
    private static final int WRITE_TO_SP_SUCCESS = 4;

    private static String nickname = "";
    private static String img = "";

    private static ProgressDialog pd;
    private Context mContext;

    private EditText phoneNumLogin;
    private EditText passwordLogin;
    private TextView newUserRegister;
    private TextView forgetPassword;
    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //SharedPreferences.Editor editor = getSharedPreferences("userinfo", MODE_PRIVATE).edit();
        //editor.clear();
        //editor.apply();
        mContext = this;

        SharedPreferences pref = getSharedPreferences("userinfo", MODE_PRIVATE);
        if(!(pref.getString("password", "hahaha").equals("hahaha"))) {
            autoLogin();
        }

        login = (Button)findViewById(R.id.login_button);
        newUserRegister = (TextView) findViewById(R.id.new_user_register);
        forgetPassword = (TextView)findViewById(R.id.forget_password);
        passwordLogin = (EditText)findViewById(R.id.password_login);
        phoneNumLogin = (EditText)findViewById(R.id.phoneNum_login);
        phoneNumLogin.setText(pref.getString("phoneNumber", ""));

        newUserRegister.setClickable(true);
        forgetPassword.setClickable(true);

        login.setOnClickListener(this);
        newUserRegister.setOnClickListener(this);
        forgetPassword.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_button:
                try {
                    if(phoneNumLogin.getText().toString().equals("") || phoneNumLogin.getText().toString().length() < 11) {
                        Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    else if(passwordLogin.getText().toString().equals("")) {
                        Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    else if(passwordLogin.getText().toString().length() < 8) {
                        Toast.makeText(this, "帐号或密码错误", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String result;
                            PHPMySQL connector = new PHPMySQL();
                            result = connector.login(phoneNumLogin.getText().toString(), passwordLogin.getText().toString());
                            if (result.equals("Done")) {
                                handler.sendEmptyMessage(LOGIN_SUCCESS);
                            }
                            else if (result.equalsIgnoreCase("invalid username or password")){
                                handler.sendEmptyMessage(INVALID_INFOMATION);
                            }
                            else {
                                handler.sendEmptyMessage(OTHER_ERROR);
                                Log.e(TAG, "run: " + result);
                            }
                        }
                    }).start();
                }
                catch (Exception e) {
                    Log.e(TAG, "onClick: login_Button: " + e.toString());
                }
                break;
            case R.id.new_user_register:
                Intent intentRegister = new Intent(Login.this, Register.class);
                startActivity(intentRegister);
                break;
            case R.id.forget_password:
                Intent intentForgetPassword = new Intent(Login.this, ForgetPassword.class);
                startActivity(intentForgetPassword);
                break;
            default:
                break;
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //super.handleMessage(msg);
            
            if (msg.what == LOGIN_SUCCESS) {
                pd = ProgressDialog.show(mContext, null, "正在同步数据，请稍候...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        JSONArray jsonArray;
                        PHPMySQL connector = new PHPMySQL();
                        jsonArray = connector.searchData("userinfo", "nickname, img", "phonenumber='" + phoneNumLogin.getText().toString() +"'");
                        try {
                            JSONObject json = jsonArray.getJSONObject(0);
                            nickname = json.getString("nickname");
                            img = json.getString("img");
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        SharedPreferences.Editor editor = getSharedPreferences("userinfo", MODE_PRIVATE).edit();
                        editor.putString("phoneNumber", phoneNumLogin.getText().toString());
                        editor.putString("password", phoneNumLogin.getText().toString());
                        editor.putString("nickname", nickname);
                        editor.apply();
                        handler.sendEmptyMessage(WRITE_TO_SP_SUCCESS);
                    }
                }).start();
            }
            else if (msg.what == WRITE_TO_SP_SUCCESS) {
                File dir = new File(getExternalCacheDir() + "/" + phoneNumLogin.getText().toString());
                if (!(dir).exists()) {
                    dir.mkdir();
                }
                if(!(img.equals("null"))) {
                    AsyncHttpClient client = new AsyncHttpClient();
                    client.get("http://120.78.64.178/img/" + img, new BinaryHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
                            String tempPath = getExternalCacheDir() + "/" + phoneNumLogin.getText().toString() + "/" + img;
                            Bitmap bmp = BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length);

                            File file = new File(tempPath);
                            Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
                            // 压缩比例
                            int quality = 100;
                            try {
                                // 若存在则删除
                                if (file.exists())
                                    file.delete();
                                // 创建文件
                                file.createNewFile();
                                //
                                OutputStream stream = new FileOutputStream(file);
                                // 压缩输出
                                bmp.compress(format, quality, stream);
                                // 关闭
                                stream.close();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {

                        }
                    });
                }
                handler.sendEmptyMessage(GET_IMG_SUCCESS);
                cleanExternalCache(mContext, img);
            }
            else if(msg.what == GET_IMG_SUCCESS) {
                cleanExternalCache(mContext, img);
                Intent intent = new Intent(Login.this, MainActivity.class);
                Toast.makeText(Login.this, "登陆成功", Toast.LENGTH_SHORT).show();
                pd.dismiss();
                startActivity(intent);
                finish();
            }
            else if(msg.what == OTHER_ERROR) {
                Toast.makeText(Login.this, "啊哦，出错了，请稍候再试吧", Toast.LENGTH_SHORT).show();
            }
            else if(msg.what == INVALID_INFOMATION) {
                Toast.makeText(Login.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void cleanExternalCache(Context context, String imageName) {
        SharedPreferences s = getSharedPreferences("userinfo", MODE_PRIVATE);
        for (File child: new File(context.getExternalCacheDir() + "/" + s.getString("phoneNumber", "")).listFiles()) {
            if (!(child.getName().equals(imageName))) {
                child.delete();
            }
        }
    }

    private void autoLogin() {
        Intent intent = new Intent(Login.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

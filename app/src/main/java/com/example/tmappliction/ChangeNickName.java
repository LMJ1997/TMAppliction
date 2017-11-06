package com.example.tmappliction;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class ChangeNickName extends AppCompatActivity implements View.OnClickListener {

    private static final int CHANGE_NICKNAME_SUCCESS = 1;
    private static final int INVALID_INFOMATION = 0;
    private static final int OTHER_ERROR = 2;
    private static final int SEND_YANZHENGMA_SUCCESS = 3;

    private ImageView changeNicknameBack;

    private String newNickname = "";

    EditText inputNewNickname;
    ConstraintLayout updateNicknameButton;
    PHPMySQL phpMySQL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_nick_name);
        inputNewNickname = (EditText) findViewById(R.id.inputNewNickname);
        updateNicknameButton = (ConstraintLayout) findViewById(R.id.update_nickname_button);
        updateNicknameButton.setOnClickListener(this);
        changeNicknameBack = (ImageView)findViewById(R.id.change_nickname_back);
        changeNicknameBack.setOnClickListener(this);

        SharedPreferences userinfo = this.getSharedPreferences("userinfo", MODE_PRIVATE);
        inputNewNickname.setText(userinfo.getString("nickname", ""));
    }


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == CHANGE_NICKNAME_SUCCESS){
                Toast.makeText(ChangeNickName.this,"修改昵称成功",Toast.LENGTH_SHORT).show();
            }else if (msg.what == OTHER_ERROR){
                Toast.makeText(ChangeNickName.this,"好像哪里出错了，请重试",Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void change_nickname(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                phpMySQL = new PHPMySQL();
                String result;

                //使用SharedPreferences,将事先在Login中储存的电话号码（即账号）取出，将它作为username传入数据库中寻找该用户的信息
                SharedPreferences pref = getSharedPreferences("userinfo",MODE_PRIVATE);
                String userName_phoneNum = pref.getString("phoneNumber","");

                //由于后续要更新userinfo文件，使用SharedPerference修改时会清空整个文件，所以要提前将其中内容取出来
                String nickName = pref.getString("nickName","");
                String phoneNum = pref.getString("phoneNum","");
                String password = pref.getString("password","");

                //使用PHPMySQL类中的updateData方法，传入三个参数,修改服务器数据库中的信息
                result = phpMySQL.updateData("userinfo","nickname='"+newNickname+"'","phonenumber='"+userName_phoneNum+"'" );
                //使用handle将返回的信息传出，进行下一步UI操作
                if (result.equals("Done")){
                    handler.sendEmptyMessage(CHANGE_NICKNAME_SUCCESS);
                }else {
                    handler.sendEmptyMessage(OTHER_ERROR);
                }


                //更新SharedPreferences储存的userinfo文件中的信息
                SharedPreferences.Editor editor = getSharedPreferences("userinfo",MODE_PRIVATE).edit();
                editor.putString("nickname", newNickname);
                editor.putString("phoneNum",phoneNum);
                editor.putString("password",phoneNum);
                editor.apply();
            }
        }).start();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.change_nickname_back:
                finish();
                break;
            case R.id.update_nickname_button:
                SharedPreferences userinfo = this.getSharedPreferences("userinfo", MODE_PRIVATE);
                inputNewNickname = (EditText)findViewById(R.id.inputNewNickname);
                newNickname = inputNewNickname.getText().toString();
                if (newNickname.equals(""))
                    Toast.makeText(this, "请输入要修改的昵称", Toast.LENGTH_SHORT).show();
                else if (newNickname.equals(userinfo.getString("nickname", "")))
                    Toast.makeText(this, "您的昵称没有更改", Toast.LENGTH_SHORT).show();
                else
                    change_nickname();
                break;
            default:
                break;
        }
    }
}


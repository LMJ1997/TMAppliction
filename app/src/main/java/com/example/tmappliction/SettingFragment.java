package com.example.tmappliction;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.yalantis.ucrop.UCrop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class SettingFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "SettingFragment";
    private static final int UPLOAD_SUCCESS = 11;
    private static final int UPLOAD_FAILED = 12;

    private Switch saveData;
    private ImageView avatarImg;

    private Context mContext;
    private SelectPicPopupWindow menuWindow;
    private static final int REQUESTCODE_PICK = 8;
    private static ProgressDialog pd;
    private String imageName = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.setting,container, false);
        initAll(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initAll(getView());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.logout_my :
                SharedPreferences.Editor editor = this.getActivity().getSharedPreferences("userinfo", MODE_PRIVATE).edit();
                editor.remove("password");
                editor.apply();
                imageName = "";
                cleanExternalCache(getContext());
                Intent intent = new Intent(this.getActivity(), Login.class);
                startActivity(intent);
                this.getActivity().finish();
                break;
            case R.id.switch_saveData:
                SharedPreferences pref = getContext().getSharedPreferences("data",MODE_PRIVATE);
                boolean judgeImg = pref.getBoolean("nof",false);
                SharedPreferences.Editor editor1 = pref.edit();
                if(!judgeImg) {
                    saveData.setChecked(true);
                    editor1.putBoolean("nof",true);
                    getActivity().startService(new Intent(getActivity(),DataSaveService.class));
                }else{
                    saveData.setChecked(false);
                    editor1.putBoolean("nof",false);
                    Intent startIntent = new Intent(getActivity(),DataSaveService.class);
                    getActivity().stopService(startIntent);
                }
                editor1.apply();
                break;
            case R.id.account_setting_my:
                Intent intent1 = new Intent(this.getActivity(), AccountSetting.class);
                startActivity(intent1);
                break;
            case R.id.message_and_remind_my:
                Intent intent2 = new Intent(this.getActivity(), MessageAndRemind.class);
                startActivity(intent2);
                break;
            case R.id.privacy_my:
                Intent intent3 = new Intent(this.getActivity(), Privacy.class);
                startActivity(intent3);
                break;
            case R.id.about_my:
                Intent intent4 = new Intent(this.getActivity(), AboutUs.class);
                startActivity(intent4);
                break;
            case R.id.update_my:
                break;
            case R.id.avatarImg:// 更换头像点击事件
                menuWindow = new SelectPicPopupWindow(mContext, itemsOnClick);
                menuWindow.showAtLocation(getView(),
                        Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);

                break;
            default:
                break;
        }
    }

    private View.OnClickListener itemsOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            menuWindow.dismiss();
            switch (v.getId()) {
                case R.id.pickPhotoBtn:
                    Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
                    pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/jpg");
                    startActivityForResult(pickIntent, REQUESTCODE_PICK);
                    break;
                default:
                    break;
            }
        }
    };

    private void cleanExternalCache(Context context) {
        SharedPreferences s = getContext().getSharedPreferences("userinfo", MODE_PRIVATE);
        for (File child: new File(context.getExternalCacheDir() + "/" + s.getString("phoneNumber", "")).listFiles()) {
            if (!child.getName().equals(imageName)) {
                //noinspection ResultOfMethodCallIgnored
                child.delete();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUESTCODE_PICK:
                    Log.i(TAG, "onActivityResult: " + data.getData());
                    startPhotoZoom(data.getData());
                    break;
                case UCrop.REQUEST_CROP:
                    Log.i(TAG, "onActivityResult: " + UCrop.getOutput(data));
                    pd = ProgressDialog.show(mContext, null, "正在上传图片，请稍候...");
                    uploadFile();
                    if (NetWorkUtils.isNetworkConnected(getContext())) {
                        avatarImg.setImageURI(UCrop.getOutput(data));
                    }
                    break;
                case UCrop.RESULT_ERROR:
                    break;
            }
        }
    }


    public void startPhotoZoom(Uri uri) {
        SharedPreferences s = getContext().getSharedPreferences("userinfo", MODE_PRIVATE);
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getContext().getExternalCacheDir() + "/" + s.getString("phoneNumber", ""), System.currentTimeMillis() + ".jpg")));
        UCrop.Options options = new UCrop.Options();
        // 修改标题栏颜色
        options.setToolbarColor(getResources().getColor(R.color.colorPrimary));
        // 修改状态栏颜色
        options.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        // 隐藏底部工具
        options.setHideBottomControls(true);
        // 图片格式
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        // 设置图片压缩质量
        options.setCompressionQuality(100);
        // 是否让用户调整范围(默认false)，如果开启，可能会造成剪切的图片的长宽比不是设定的
        // 如果不开启，用户不能拖动选框，只能缩放图片
        options.setFreeStyleCropEnabled(true);
        options.withMaxResultSize(200,200);

        uCrop.withOptions(options);
        //设置裁剪图片的宽高比，比如16：9
        uCrop.withAspectRatio(1, 1);
        //uCrop.useSourceImageAspectRatio();
        //跳转裁剪页面
        uCrop.start(getContext(), this);
    }


    private void uploadFile() {
        if(NetWorkUtils.isNetworkConnected(getContext())) {
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            try {
                nearestPic();
                cleanExternalCache(getContext());
                SharedPreferences s = getContext().getSharedPreferences("userinfo", MODE_PRIVATE);
                File f = new File(getContext().getExternalCacheDir()+ "/" + s.getString("phoneNumber", "") + "/" + imageName) ;
                if(f.exists()){
                    Log.i("AsyncHttp", "Yes") ;
                    params.put("file", f);
                }else{
                    Log.i("AsyncHttp", "No") ;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            client.post("http://120.78.64.178/uploadImg.php", params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    try {
                        String resp = new String(responseBody, "utf-8");
                        Log.e(TAG, "onSuccess: " + resp);
                        handler.sendEmptyMessage(UPLOAD_SUCCESS);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.e(TAG, "onFailure: " + statusCode, error);
                    handler.sendEmptyMessage(UPLOAD_FAILED);
                }
            });
        }
        else {
            pd.dismiss();
            Toast.makeText(mContext, "网络连接失败", Toast.LENGTH_SHORT).show();
        }

    }

    private void nearestPic() {
        SharedPreferences s = getContext().getSharedPreferences("userinfo", MODE_PRIVATE);
        List<File> files = FileUtil.listFileSortByModifyTime(getContext().getExternalCacheDir()+ "/" + s.getString("phoneNumber", ""));
        if (files.size() == 0)
            imageName = "";
        else
            imageName = files.get(files.size() - 1).getName();
    }

    private void setAvatar() {
        nearestPic();
        if(!imageName.equals("")) {
            SharedPreferences s = getContext().getSharedPreferences("userinfo", MODE_PRIVATE);
            avatarImg.setImageURI(Uri.fromFile(new File(getContext().getExternalCacheDir() + "/" + s.getString("phoneNumber", "")+ "/" + imageName)));
        }
    }
    private void initAll(View view) {

        nearestPic();

        mContext = getContext();

        TextView nicknameMy = (TextView) view.findViewById(R.id.nickname_my);
        TextView phoneNumMy = (TextView) view.findViewById(R.id.phoneNum_my);
        saveData = (Switch)view.findViewById(R.id.switch_saveData);
        avatarImg = (ImageView)view.findViewById(R.id.avatarImg);
        avatarImg.setOnClickListener(this);
        setAvatar();

        SharedPreferences info = getContext().getSharedPreferences("userinfo", MODE_PRIVATE);
        nicknameMy.setText(info.getString("nickname", "+1H"));
        String phoneNum = info.getString("phoneNumber", "13333333333");
        phoneNum = phoneNum.substring(0, 3) + "******" + phoneNum.substring(9);
        phoneNumMy.setText(phoneNum);

        ConstraintLayout privacy = (ConstraintLayout) view.findViewById(R.id.privacy_my);
        ConstraintLayout messageAndRemind = (ConstraintLayout) view.findViewById(R.id.message_and_remind_my);
        ConstraintLayout accountSetting = (ConstraintLayout) view.findViewById(R.id.account_setting_my);
        Button logoutMy = (Button) view.findViewById(R.id.logout_my);
        ConstraintLayout aboutUs = (ConstraintLayout) view.findViewById(R.id.about_my);
        ConstraintLayout update = (ConstraintLayout) view.findViewById(R.id.update_my);
        update.setOnClickListener(this);
        aboutUs.setOnClickListener(this);
        logoutMy.setOnClickListener(this);
        saveData.setOnClickListener(this);
        accountSetting.setOnClickListener(this);
        messageAndRemind.setOnClickListener(this);
        privacy.setOnClickListener(this);

        SharedPreferences pref = getContext().getSharedPreferences("data",MODE_PRIVATE);
        boolean judgeImg = pref.getBoolean("nof",false);
        SharedPreferences.Editor editor1 = pref.edit();
        boolean isWorking = isWorked();
        if(judgeImg) {
            saveData.setChecked(true);
            editor1.putBoolean("nof",true);
            if(!isWorking){
                getActivity().startService(new Intent(getActivity(),DataSaveService.class));
            }
        }else{
            saveData.setChecked(false);
            editor1.putBoolean("nof",false);
            if(isWorking){
                Intent startIntent = new Intent(getActivity(),DataSaveService.class);
                getActivity().stopService(startIntent);
            }
        }
        editor1.apply();
    }

    private boolean isWorked() {
        ActivityManager myManager = (ActivityManager) getContext().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        assert myManager != null;
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager.getRunningServices(100);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().equals("com.example.tmappliction.DataSaveService")) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPLOAD_SUCCESS:
                    nearestPic();
                    cleanExternalCache(getContext());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            PHPMySQL conn = new PHPMySQL();
                            SharedPreferences pref = getContext().getSharedPreferences("userinfo", MODE_PRIVATE);
                            conn.updateData("userinfo", "img='" + imageName + "'", "phonenumber='" + pref.getString("phoneNumber", "") + "'");
                        }
                    }).start();
                    SharedPreferences s = getContext().getSharedPreferences("userinfo", MODE_PRIVATE);
                    avatarImg.setImageURI(Uri.fromFile(new File(getContext().getExternalCacheDir() + "/" + s.getString("phoneNumber", "") + imageName)));
                    initAll(getView());
                    pd.dismiss();
                    Toast.makeText(mContext, "更换头像成功", Toast.LENGTH_SHORT).show();
                    break;

                case UPLOAD_FAILED:
                    pd.dismiss();
                    Toast.makeText(getContext(), "啊哦，出错了，请等会儿再试试吧", Toast.LENGTH_SHORT).show();
                default:
                    break;
            }
        }
    };
}

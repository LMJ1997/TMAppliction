package com.example.tmappliction;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by derek on 2017/9/13.
 */

public class PHPMySQL {

    //以下所有方法都可以灵活使用，理论上可以实现所有对数据库的操作，灵活使用的方法详细直接QQ问我，不懂的地方直接QQ问我。

    private static final String TAG = "PHPMySQL";

    URL url;
    HttpURLConnection conn;

    public String insertData(String tableName, String columnName, String values) {  //insert into table (column1, column2……) values (value1, value2……)
        try {                                                                       //例如往userinfo表中插入用户名(aneon)密码(abcd123)传进来的参数就应该是tableName: userinfo, columnName: username, password, values: 'aneon', 'abcd123'
            url = new URL("http://120.78.64.178/insert.php");
        }
        catch(MalformedURLException e) {
            Log.e(TAG, "insertData: " + e.toString() );
            return "Exception 1";
        }

        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("tableName", tableName)
                .appendQueryParameter("columnName", columnName)
                .appendQueryParameter("values", values);
        String query = builder.build().getEncodedQuery();

        return getResult(url, query);
    }

    public String updateData(String tableName, String columnNewValues, String columnOldValues) {    //update table set column1=value1, column2=value2 where column=value
        try {                                                                                       //例如更新userinfo表中的username为aneon的用户的password这一列的值为aaaa123传入的参数应该是: tableName: userinfo, columnNewValues: password='aaaa123', columnOldValues: username='aneon'
            url = new URL("http://120.78.64.178/update.php");
        }
        catch(MalformedURLException e) {
            Log.e(TAG, "updateData: " + e.toString() );
            return "Exception 1";
        }

        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("tableName", tableName)
                .appendQueryParameter("columnNewValues", columnNewValues)
                .appendQueryParameter("columnOldValues", columnOldValues);
        String query = builder.build().getEncodedQuery();

        return getResult(url, query);
    }

    public String deleteData(String tableName, String columnName, String value) {  //delete from table where column=value
        try {                                                                       //例如删除userinfo表中username这一列为aneon的用户的一整行数据传入的参数应该是: tableName: userinfo, columnName: username, value: aneon
            url = new URL("http://120.78.64.178/delete.php");
        }
        catch(MalformedURLException e) {
            Log.e(TAG, "deleteData: " + e.toString() );
            return "Exception 1";
        }

        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("tableName", tableName)
                .appendQueryParameter("columnName", columnName)
                .appendQueryParameter("values", value);
        String query = builder.build().getEncodedQuery();

        return getResult(url, query);
    }

    public String login(String username, String password) { //select username, password form userinfo where username=value1 and password=value2
        try {                                               //这个方法用在登录界面，直接传入用户名和密码服务器端从数据库中选择账号密码为输入的账号密码，如果服务器返回值为空则表明查询不到，则登录失败，传入的参数不多解释。
            url = new URL("http://120.78.64.178/login.php");
        }
        catch(MalformedURLException e) {
            Log.e(TAG, "login: " + e.toString() );
        }

        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("username", "'" + username + "'")
                .appendQueryParameter("password", "'" + password + "'");
        String query = builder.build().getEncodedQuery();

        return getResult(url, query);
    }

    public JSONArray searchData(String tableName, String columnName, String condition) {    //select column1, column2 …… from userinfo where column=value(或者使用模糊查找column like value)
        try {                                                                                //使用精确查找，例如查找用户名为aneon的用户的密码: tableName: userinfo, columnName: password, condition: username='aneon'
            url = new URL("http://120.78.64.178/search.php");
        }
        catch(MalformedURLException e) {
            Log.e(TAG, "searchData: " + e.toString() );
        }

        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("tableName", tableName)
                .appendQueryParameter("columnName", columnName)
                .appendQueryParameter("condition", condition);
        String query = builder.build().getEncodedQuery();

        return getJSONResult(url, query);
    }

    public String getRanking(int usetime) {
        try {
            url = new URL("http://120.78.64.178/getRanking.php");
        }
        catch(MalformedURLException e) {
            Log.e(TAG, "getRanking: " + e.toString() );
        }

        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("usetime", Integer.toString(usetime));
        String query = builder.build().getEncodedQuery();

        if(connectToServer(url, query)) {
            try {
                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = br.readLine()) != null)
                    sb.append(line);
                is.close();

                return sb.toString();
            }
            catch(IOException e) {
                Log.e(TAG, "getResult: " + e.toString());
                return "Get return value failed";
            }
            finally {
                conn.disconnect();
            }
        }
        else {
            return "Connection error";
        }
    }

    public String inertUsetime(int usetime, String phonenumber) {
        try {
            url = new URL("http://120.78.64.178/insertUsetime.php");
        }
        catch(MalformedURLException e) {
            Log.e(TAG, "insertUsetime: " + e.toString() );
        }

        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("usetime", Integer.toString(usetime))
                .appendQueryParameter("phonenumber", phonenumber);
        String query = builder.build().getEncodedQuery();

        return getResult(url, query);
    }

    public JSONArray doSQL(String sql) {    //执行标准SQL语句，所有标准SQL语句都可以执行，对数据库可进行任何操作，谨慎使用
        try {
            url = new URL("http://120.78.64.178/dosql.php");
        }
        catch(MalformedURLException e) {
            Log.e(TAG, "doSQL: " + e.toString() );
        }

        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("sql", sql);
        String query = builder.build().getEncodedQuery();

        return getJSONResult(url, query);
    }

    @NonNull
    private String getResult(URL url, String query){
        if(connectToServer(url, query)) {
            try {
                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = br.readLine()) != null)
                    sb.append(line);
                is.close();
                if(sb.toString().equalsIgnoreCase("true"))
                    return "Done";
                else
                    return sb.toString();
            }
            catch(IOException e) {
                Log.e(TAG, "getResult: " + e.toString());
                return "Get return value failed";
            }
            finally {
                conn.disconnect();
            }
        }
        else {
            return "Connection error";
        }
    }

    @Nullable
    private JSONArray getJSONResult(URL url, String query) {
        JSONArray jArray = null;
        if(connectToServer(url, query)) {
            String result = "";
            try {
                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = br.readLine()) != null)
                    sb.append(line);
                is.close();
                result = sb.toString();
            }
            catch(IOException e) {
                Log.e(TAG, "getJSONResult: " + e.toString());
                return null;
            }
            finally {
                conn.disconnect();
            }

            try {
                jArray = new JSONArray(result);
            }
            catch (JSONException e) {
                Log.e(TAG, "getJSONResult: " + e.toString());
                return null;
            }
        }
        else {
            try {
                JSONObject json = new JSONObject("{\"network\":\"error\"}");
                jArray = new JSONArray();
                jArray.put(0, json);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jArray;
    }

    private boolean connectToServer(URL url, String query) {
        try {
            conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(600);
            conn.setConnectTimeout(600);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();

            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK)
                throw new IOException();
        }
        catch(IOException e) {
            Log.e(TAG, "connectToServer: " + e.toString() );
            return false;
        }
        return true;
    }
}

package com.safety.android.http;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.safety.android.LunchActivity;
import com.safety.android.MainActivity;
import com.safety.android.SQLite3.PermissionInfo;
import com.safety.android.SQLite3.PermissionLab;
import com.safety.android.SQLite3.UserInfo;
import com.safety.android.SQLite3.UserLab;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class login extends AppCompatActivity {

    final String URL=OKHttpFetch.URL;

    public static String token="";

    private EditText mUsername;
    private EditText mPassWord;
    private CheckBox mCheckbox;

    public static String username=null;
    String password=null;

    private Integer type=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
     //   Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
     //   setSupportActionBar(toolbar);

        Button mButton=(Button)findViewById(R.id.mButton);
        mUsername=(EditText)findViewById(R.id.mUserName);
        mPassWord=(EditText)findViewById(R.id.mPassWord);
        mCheckbox =(CheckBox)findViewById(R.id.mCheckBox);

        mCheckbox.setChecked(true);

        readfrominfo();     //调用读内部存储函数

        mPassWord.setTransformationMethod(PasswordTransformationMethod.getInstance());

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                    username=mUsername.getText().toString();
                    password=mPassWord.getText().toString();

                   new FetchItemsTask().execute();

            }
        });

    }

    //读内部存储函数
    private void readfrominfo(){

            List<UserInfo> list= UserLab.get(getApplication()).getUserInfo();

            try {
                UserInfo userInfo = list.get(0);

                mUsername.setText(userInfo.getName());       //填写username与password
                mPassWord.setText(userInfo.getPassword());

            }catch (Exception e) {
                e.printStackTrace();
            }

    }

    private class FetchItemsTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String  doInBackground(Void... params) {

            OkHttpClient client=OKHttpFetch.getOkHttpClient();

            final MediaType FORM_CONTENT_TYPE
                    = MediaType.parse("application/json;charset=utf-8");

            JSONObject object =new JSONObject();
            try {
                object.put("username",username);
                object.put("password",password);
            } catch (JSONException e) {
                e.printStackTrace();
            }


            System.out.println(object.toString());
            RequestBody body=RequestBody.create(FORM_CONTENT_TYPE,object.toString());

            final Request loginRequest = new Request.Builder()
                    .url(URL + "/jeecg-boot/sys/login")
                    .post(body)
                    .build();


            Call loginCall = client.newCall(loginRequest);

            String s = null;

            try {
                //非异步执行
                Response loginResponse = loginCall.execute();
                //获取返回数据的头部
                Headers headers = loginResponse.headers();
                HttpUrl loginUrl = loginRequest.url();
                //获取头部的Cookie,注意：可以通过Cooke.parseAll()来获取
                List<Cookie> cookies = Cookie.parseAll(loginUrl, headers);
                System.out.print("cookies=" + cookies);
                //防止header没有Cookie的情况
                if (cookies != null) {
                    //存储到Cookie管理器中
                    client.cookieJar().saveFromResponse(loginUrl, cookies);//这样就将Cookie存储到缓存中了
                    s=loginResponse.body().string();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return s;

        }

        @Override
        protected void onPostExecute(String items) {
            System.out.println("items="+items);
            try {
                JSONObject jsonObject=new JSONObject(items);

                if(jsonObject!=null){

                    System.out.println("jsonObject="+jsonObject);

                    String message=jsonObject.getString("message");

                    System.out.println("message="+message);

                    String result=jsonObject.getString("result");

                    if(message.equals("登录成功")) {

                        JSONObject jsonObject2=new JSONObject(result);

                        token=jsonObject2.getString("token");

                        if (mCheckbox.isChecked()) {

                            UserInfo userInfo = new UserInfo();

                            userInfo.setName(username);
                            userInfo.setPassword(password);
                            userInfo.setDate(new Date());
                            userInfo.setToken(token);
                            MainActivity.token=token;

                            try {
                                int i = UserLab.get(getApplication()).updateUserInfo(userInfo);
                                if (i < 1) {
                                    UserLab.get(getApplicationContext()).addUserInfo(userInfo);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        Integer multiDepart=jsonObject2.getInt("multi_depart");

                        if(multiDepart>1){

                            LayoutInflater inflater = getLayoutInflater();
                            View validateView = inflater.inflate(
                                    R.layout.dialog_validate, null);
                            final LinearLayout layout_validate = (LinearLayout) validateView.findViewById(R.id.layout_validate);
                            layout_validate.removeAllViews();

                            Map<String, Object> sMap = new HashMap();

                            List list = new ArrayList();

                            int i = 0;
                            Iterator iterator = list.iterator();
                            while (iterator.hasNext()) {
                                Map<String, Object> cMap = (Map) iterator.next();
                                for (Map.Entry<String, Object> map : cMap.entrySet()) {

                                    View validateItem = inflater.inflate(R.layout.item_validate_enter, null);
                                    validateItem.setTag(i);
                                    layout_validate.addView(validateItem);
                                    TextView tv_validateName = (TextView) validateItem.findViewById(R.id.tv_validateName);
                                    EditText et_validate = (EditText) validateItem.findViewById(R.id.et_validate);
                                    TextView et_validateText = validateItem.findViewById(R.id.et_validate_text);
                                    et_validate.setVisibility(View.GONE);

                                    tv_validateName.setText(map.getKey());
                                    et_validateText.setText(map.getValue().toString());

                                    i++;
                                }
                            }

                            View spinnerView = inflater.inflate(R.layout.spinner_accept, null);

                            TextView spinnerText=spinnerView.findViewById(R.id.spinner_text);

                            spinnerText.setText("");

                            Drawable drawable = getApplication().getResources().getDrawable(R.drawable.departs);
                            drawable.setBounds(0,0,5,5);
                            spinnerText.setBackground(drawable);
                            spinnerText.setHeight(5);
                            layout_validate.addView(spinnerView);

                            Spinner spinner;
                            //private Spinner spinner2;
                            spinner = validateView.findViewById(R.id.Spinner01);

                            String departs=jsonObject2.getString("departs");

                            final JSONArray jsonArray=new JSONArray(departs);

                            String[] listDepart=new String[jsonArray.length()];

                            for(int j=0;j<jsonArray.length();j++){

                                JSONObject jsonObject1= (JSONObject) jsonArray.get(j);
                                String departName=jsonObject1.getString("departName");
                                listDepart[j]=departName;
                            }

                            final String[] m = listDepart;

                            ArrayAdapter<String> adapter;

                            //将可选内容与ArrayAdapter连接起来
                            adapter = new ArrayAdapter<String>(login.this, android.R.layout.simple_spinner_item, m);

                            //设置下拉列表的风格
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                            //将adapter 添加到spinner中
                            spinner.setAdapter(adapter);

                            //添加事件Spinner事件监听
                            spinner.setOnItemSelectedListener(new SpinnerSelectedListener());

                            //设置默认值
                            spinner.setVisibility(View.VISIBLE);

                            AlertDialog dialog = new AlertDialog.Builder(login.this)
                                    .setTitle("选择登陆部门")
                                    .setView(validateView)
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {


                                            new FetchItemsTaskSelectDepart().execute(jsonArray);
                                            dialog.dismiss();
                                        }

                                    }).create();

                            dialog.show();

                        }else {
                            new FetchItemsTaskPermission().execute();
                        }
                    }else{

                        Toast.makeText(login.this,message,Toast.LENGTH_SHORT).show();

                    }
                }else{

                    Toast.makeText(login.this,"登陆失败",Toast.LENGTH_SHORT).show();        //吐司界面，参数依次为提示发出Activity,提示内容,提示时长

                }

            } catch (JSONException e) {

                Toast.makeText(login.this,"登陆失败",Toast.LENGTH_SHORT).show();

                e.printStackTrace();
            }catch (NullPointerException e){

                Toast.makeText(login.this,"登陆失败",Toast.LENGTH_SHORT).show();

                e.printStackTrace();
            }

        }

    }

    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {

            System.out.println("arg2="+arg2);

            type=arg2;

        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }


    private class FetchItemsTaskSelectDepart extends AsyncTask<JSONArray,Void,String> {

        @Override
        protected String doInBackground(JSONArray... jsonArrays) {

            JSONArray jsonArray=jsonArrays[0];

            JSONObject jsonObject=null;

            try {

                jsonObject= (JSONObject) jsonArray.get(type);
                jsonObject.put("username",username);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return new OKHttpFetch(getApplication()).post(FlickrFetch.base+"/sys/selectDepart",jsonObject,"put");
        }

        @Override
        protected void onPostExecute(String json) {

            new FetchItemsTaskPermission().execute();

        }
    }

    private class FetchItemsTaskPermission extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            //String json = new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/sys/permission/getUserPermissionByToken?token="+token);
            String json = new OKHttpFetch(getApplicationContext()).get(FlickrFetch.base + "/sys/permission/getUserPermissionByName?username="+username);
            return json;
        }


        @Override
        protected void onPostExecute(String json) {

            JSONObject jsonObject = null;

            List<UserInfo> list= UserLab.get(MainActivity.getContext()).getUserInfo();

            Iterator iterator=list.iterator();

            while (iterator.hasNext()){
                UserInfo userInfo= (UserInfo) iterator.next();
                if(userInfo.getName().equals(username)){
                    token=userInfo.getToken();
                    System.out.println("username==="+username+"token=="+token);
                    continue;
                }
            }


            try {
                jsonObject = new JSONObject(json);
                String result=jsonObject.getString("result");

                JSONObject jsonObject2=new JSONObject(result);

                String auth=jsonObject2.getString("auth");
                String menu=jsonObject2.getString("menu");

                JSONArray authArray=new JSONArray(auth);
                JSONArray menuArray=new JSONArray(menu);

                PermissionLab.get(getApplicationContext()).delPermission();

                for(int i=0;i<authArray.length();i++){
                    JSONObject jsonObject1=authArray.getJSONObject(i);
                    PermissionInfo permissionInfo=new PermissionInfo();
                    permissionInfo.setAction(jsonObject1.getString("action"));
                    permissionInfo.setDescribe(jsonObject1.getString("describe"));
                    permissionInfo.setType(jsonObject1.getInt("type"));
                    PermissionLab.get(getApplicationContext()).addPermission(permissionInfo);
                }

                for(int i=0;i<menuArray.length();i++){
                    //System.out.println(menuArray.getString(i));
                    JSONObject jsonObject1=menuArray.getJSONObject(i);
                    try{
                        JSONArray menuArray1=jsonObject1.getJSONArray("children");
                        for(int j=0;j<menuArray1.length();j++) {
                            JSONObject jsonObject3=menuArray1.getJSONObject(j);
                            PermissionInfo permissionInfo = new PermissionInfo();
                            permissionInfo.setPath(jsonObject3.getString("path"));
                            permissionInfo.setComponent(jsonObject3.getString("component"));
                            permissionInfo.setName(jsonObject3.getString("name"));
                            permissionInfo.setId(jsonObject3.getString("id"));
                            PermissionLab.get(getApplicationContext()).addPermission(permissionInfo);
                        }
                    }catch (Exception e){
                        PermissionInfo permissionInfo = new PermissionInfo();
                        permissionInfo.setPath(jsonObject1.getString("path"));
                        permissionInfo.setComponent(jsonObject1.getString("component"));
                        permissionInfo.setName(jsonObject1.getString("name"));
                        permissionInfo.setId(jsonObject1.getString("id"));
                        PermissionLab.get(getApplicationContext()).addPermission(permissionInfo);
                    }

                }

                Toast.makeText(login.this, "登录成功", Toast.LENGTH_SHORT).show();        //吐司界面，参数依次为提示发出Activity,提示内容,提示时长

                Intent intent = new Intent(getApplication(), LunchActivity.class);
                startActivity(intent);

            } catch (JSONException e) {

                Toast.makeText(login.this,"登陆失败",Toast.LENGTH_SHORT).show();

                e.printStackTrace();

            }


        }

    }


}

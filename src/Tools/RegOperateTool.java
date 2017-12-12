package Tools;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import yangTalkback.Act.CommonProgressDialog;
import yangTalkback.Act.DialogAdapter;
import yangTalkback.Act.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * 注册码操作工具
 * Created by Administrator on 2017/3/30.
 */

public class RegOperateTool {
    private RequestQueue myRequestQueue;
    private SharedPreferences sp;
    public static boolean istoolTip = false;//注册码状态改变是否提醒，例如：注册码到期，禁用等
    public static boolean isNumberLimit = false;//是否限制次数
    public static boolean isForbidden = false;//是否禁用
    public static boolean isAllowedToMinus = false;//隐蔽对讲中的变量，是否允许减次
    public static int REGSIZE = 0;
    public static String URL_Reg_Center = "http://zc.xun365.net";//注册码中心系统
    public static String APP_MARK = "YBDJ";//软件标识
    private CommonProgressDialog mProgressDialog;
    private String nearestVersion;
    private Context context;
    public static String strreg;
    private AMapLocationClient locationClient = null;
    private String Lat;
    private String Lng;
    private String Addr ;
    private Dialog dialog_Reg;
    private ProgressDialog progressDialog;
    private CancelCallBack cancelCallBack;

    public RegOperateTool(Context context) {
        this.context = context;
        sp = context.getSharedPreferences("REG", MODE_PRIVATE);
        strreg = sp.getString("OBJREG", "");
    }
    public RegOperateTool(Context context, String str) {
        this.context = context;
        sp = context.getSharedPreferences("REG", MODE_PRIVATE);
        strreg = sp.getString("OBJREG", "");
        initLocation();
        if (strreg == null || TextUtils.isEmpty(strreg)) {
            showRegDialog();
        }else {
            checkRegStatus();
        }
    }
    /**
     * 调用减次接口
     *
     * @param size 需要减的次数
     */
    public void SetRegisCodeNumber(final int size) {
        getRequestQueue();
        String url = URL_Reg_Center + "/WebService/RegisCode.asmx/SetRegisCodeNumber";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String str) {
                if (str == null) {
                    Toast.makeText(context, "服务器返回异常，请联系管理员", Toast.LENGTH_SHORT).show();
                    return;
                }
                sp.edit().putInt("MINUSTIMES", 0).commit();
                String json = getStr(str);
                try {
                    JSONObject obj = new JSONObject(json);
                    String model = obj.getString("Model");
                    //  "注册码已经禁用"
                    if (model.equals("注册码已经禁用")) {
                        isForbidden = true;
                        SaveRegStatus("注册码已禁用");
                        if (istoolTip) {
                            Toast.makeText(context, "注册码无效，请联系管理员", Toast.LENGTH_SHORT).show();
                        }

                        return;
                    } else if (model.equals("注册码次数已用完")) {
                        SaveRegStatus("注册码次数已用完");
                        REGSIZE++;
                        if (istoolTip) {
                            Toast.makeText(context, "注册码次数已用完，请联系管理员", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    } else if (model.equals("注册码使用时间过期")) {
                        SaveRegStatus("注册码已过期");
                        if (istoolTip) {
                            Toast.makeText(context, "注册码使用时间过期，请联系管理员", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    } else if (model.equals("注册码不正确")) {
                        SaveRegStatus("注册码不正确");
                        if (istoolTip) {
                            Toast.makeText(context, "注册码不存在，请联系管理员", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    } else {
                        SaveRegStatus("注册码正常");
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> map = new HashMap<String, String>();
                map.put("softType", "mb");
                map.put("regisCode", strreg);
                map.put("number", size + "");

                return map;
            }
        };
        myRequestQueue.add(stringRequest);

    }

    private RequestQueue getRequestQueue() {
        if (myRequestQueue == null) {
            myRequestQueue = Volley
                    .newRequestQueue(context.getApplicationContext());
        }
        return myRequestQueue;
    }

    private String getStr(String str) {
        int ii = 0;
        int j = 0;
        ii = str.indexOf("{");
        j = str.lastIndexOf("}");
        return str.substring(ii, j + 1);
    }

    /**
     * 保存注册码状态
     *
     * @param str 状态描述
     */
    private void SaveRegStatus(String str) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("REGSTATUS", str);
        editor.commit();
    }

    /**
     * 检查有没有未减掉的注册码次数
     */
    public void CheckUnMinusedRegSizeToMinus() {
        if (isTheRegStatusOkNoToast(context)) {
            if (sp.getInt("UNMINUSEDSIZE", 0) > 0) {
                if (isNumberLimit) {
                    SetRegisCodeNumber(sp.getInt("UNMINUSEDSIZE", 0));
                    sp.edit().putInt("UNMINUSEDSIZE", 0).commit();
                }

            }
        }

    }

    //查看注册码未减的次数
    public void UnMinusedRegSizeToCommit() {
        if (REGSIZE > 1) {
            SharedPreferences.Editor et = sp.edit();
            et.putInt("UNMINUSEDSIZE", REGSIZE - 1);
            et.commit();
        }
    }

    /**
     * 判定注册码状态是否正常
     *
     * @return
     */
    public  boolean isTheRegStatusOk(Context context) {
        SharedPreferences sp = context.getSharedPreferences("REG", MODE_PRIVATE);
        String reg_status = sp.getString("REGSTATUS", "注册码正常");
        //  "注册码已经禁用"
        if (reg_status.equals("注册码已禁用")) {
                Toast.makeText(context, "注册码无效，请联系管理员", Toast.LENGTH_SHORT).show();
            return false;
        } else if (reg_status.equals("注册码次数已用完")) {
                Toast.makeText(context, "注册码次数已用完，请联系管理员", Toast.LENGTH_SHORT).show();

            return false;
        } else if (reg_status.equals("注册码已过期")) {
                Toast.makeText(context, "注册码使用时间过期，请联系管理员", Toast.LENGTH_SHORT).show();
            return false;
        } else if (reg_status.equals("注册码不正确")) {
                Toast.makeText(context, "注册码不存在，请联系管理员", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }


    /**
     * 判定注册码状态是否正常
     *
     * @return
     */
    public  boolean isTheRegStatusOkNoToast(Context context) {
        SharedPreferences sp = context.getSharedPreferences("REG", MODE_PRIVATE);
        String reg_status = sp.getString("REGSTATUS", "注册码正常");
        //  "注册码已经禁用"
        if (reg_status.equals("注册码已禁用")) {
            return false;
        } else if (reg_status.equals("注册码次数已用完")) {
            return false;
        } else if (reg_status.equals("注册码已过期")) {
            return false;
        } else if (reg_status.equals("注册码不正确")) {
            return false;
        } else {
            return true;
        }
    }
// 判断网络是否正常

    public static boolean isConnected(Context context) {
        boolean isOk = true;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mobNetInfo = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiNetInfo = connectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifiNetInfo != null && !wifiNetInfo.isConnectedOrConnecting()) {
                if (mobNetInfo != null && !mobNetInfo.isConnectedOrConnecting()) {
                    NetworkInfo info = connectivityManager
                            .getActiveNetworkInfo();
                    if (info == null) {
                        isOk = false;
                    }
                }
            }
            mobNetInfo = null;
            wifiNetInfo = null;
            connectivityManager = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isOk;
    }



    /**
     * 检查注册码的状态
     */
    public void checkRegStatus() {
        if (!isConnected(context.getApplicationContext())) {
            Toast.makeText(context, "网络异常，请检查手机网络", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        myRequestQueue = getRequestQueue();

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,URL_Reg_Center + "/WebService/SoftWare.asmx/GetRegisCodeInfo_NoPhoneMessage", new Response.Listener<String>() {


            @Override
            public void onResponse(String response) {
                //如果注册码已经注册
                if (!TextUtils.isEmpty(response)) {
                    String json = Getstr(response);
                    try {
                        JSONObject obj = new JSONObject(json);
                        String result = obj.getString("Result");
                        JSONArray mArray = obj.getJSONArray("Model");
                        if (!TextUtils.isEmpty(result) && result.equals("ok")) {
                            if (mArray.length() == 0) {
                                SaveRegStatus("注册码不正确");
                                Toast.makeText(context, "注册码不存在",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                JSONObject obj_ = (JSONObject) mArray.get(0);
                                String Imei = obj_.getString("Imei");
                                String isImei = obj_.getString("isImei");
                                String isValid = obj_.getString("isValid");
                                String isNumber = obj_.getString("isNumber");
                                String isDisabled = obj_.getString("isDisabled");
                                String isAutoUpdate = obj_.getString("isAutoUpdate");
                                String isToolTip = obj_.getString("isToolTip").trim();
//保存注册码状态信息
                                String RegisCodeState = obj_.getString("RegisCodeState");
                                if (RegisCodeState != null) {
                                    if (RegisCodeState.equals("已过期")) {
                                        SaveRegStatus("注册码已过期");
                                    } else if (RegisCodeState.equals("已禁用")) {
                                        SaveRegStatus("注册码已禁用");
                                    } else if (RegisCodeState.equals("次数用尽")) {
                                        SaveRegStatus("注册码次数已用完");
                                    } else {
                                        SaveRegStatus("注册码正常");
                                    }
                                }

                                SharedPreferences.Editor editor = sp.edit();
                                if (isToolTip.equals("0")) {
                                    istoolTip = false;
                                    editor.putBoolean("ISTOOLTIP", false);
                                } else {
                                    istoolTip = true;
                                    editor.putBoolean("ISTOOLTIP", true);
                                }
                                if (isNumber.equals("0")) {//0代表有次数限制
                                    isNumberLimit = true;
                                    editor.putBoolean("ISNUMBER", true);
                                } else {
                                    isNumberLimit = false;
                                    editor.putBoolean("ISNUMBER", false);
                                }
                                editor.commit();
                                if (istoolTip) {
                                    if (isValid != null && !TextUtils.isEmpty(isValid)) {
                                        if (isValid.equals("0")) {//注册码限制时间
                                            String ValidEnd = obj_.getString("ValidEnd");
                                            String time = ValidEnd.split(" ")[0];
                                            if (TheDayToNextDay(time) > 0 && TheDayToNextDay(time) < 8) {

                                                if (IsTheRegStatusTime("isValid")) {
                                                    WarnRegStatus("注册码有效期还剩" + TheDayToNextDay(time) + "天，请联系管理员", "isValid");
                                                }

                                            }
                                        }
                                    }

                                    if (isNumber != null && !TextUtils.isEmpty(isNumber)) {
                                        if (isNumber.equals("0")) {//注册码有次数限制
                                            String NumberTotal = obj_.getString("Number");
                                            String NumberUsed = obj_.getString("NumberNow");
                                            int NumberNow = Integer.parseInt(NumberTotal) - Integer.parseInt(NumberUsed);
                                            if (NumberNow < 100) {
                                                if (IsTheRegStatusTime("isNumber")) {
                                                    WarnRegStatus("注册码次数还剩" + NumberNow + "次，请联系管理员", "isNumber");
                                                }

                                            }
                                        }
                                    }

                                }
                                if (isDisabled != null && !TextUtils.isEmpty(isDisabled)) {
                                    if (isDisabled.equals("0")) {//注册码已禁用
                                        isForbidden = true;
                                        WarnRegStatus("注册码无效，请联系管理员", "disable");
                                        return;
                                    }else{
                                        isForbidden = false;
                                    }
                                }

                                if (isAutoUpdate != null && !TextUtils.isEmpty(isAutoUpdate)) {
                                    if (isAutoUpdate.equals("1")) {//允许自动升级
                                        GetNearestVersionFromService();
                                    }
                                }
                            }
                        } else {
                            if (IsTheRegStatusTime("isWrong")) {
                                WarnRegStatus("服务器连接异常", "isWrong");
                            }

                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }


                }


            }


        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(
                    VolleyError error) {
                Toast.makeText(context, "服务器异常，请联系管理员", Toast.LENGTH_SHORT).show();


            }
        }) {
            @Override
            protected Map<String, String> getParams()
                    throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("regisCode", strreg);
                map.put("softwareId", APP_MARK);
                map.put("softwareType", "mb");
                return map;
            }


        };

        myRequestQueue.add(stringRequest);


    }

    public static String Getstr(String response) {

        int i = 0;
        int y = 0;
        i = response.indexOf("{");
        y = response.lastIndexOf("}");
        String str = response.substring(i, y + 1);
        return str;
    }

    /**
     * 判定是否提醒注册吗状态
     */
    private boolean IsTheRegStatusTime(String status) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("NEXTWARNTIME", MODE_PRIVATE);
        final String time = sharedPreferences.getString("nextRegStatusTime" + status, "");
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String time2 = new SimpleDateFormat("yyyy-MM-dd").format(date);
        if (TextUtils.isEmpty(time)) {
            return true;
        } else {
            if (time.equals(time2)) {
                return true;
            } else {
                return false;
            }
        }

    }


    private void WarnRegStatus(final String text, final String status) {

        View v = LayoutInflater.from(context).inflate(R.layout.warn_reg_layout
                , null);
        final Dialog dialog_toWarn = new Dialog(context, R.style.DialogStyle);
        dialog_toWarn.setCanceledOnTouchOutside(false);
        dialog_toWarn.setCancelable(false);
        dialog_toWarn.show();
        Window window = dialog_toWarn.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
//        lp.width = dip2px(this, 300); // 宽度
//        lp.height = dip2px(this, 160); // 高度
        // lp.alpha = 0.7f; // 透明度
        window.setAttributes(lp);
        window.setContentView(v);
//        dialog_toSet.setOnKeyListener(new DialogInterface.OnKeyListener() {
//            @Override
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//
//                if (keyCode == event.KEYCODE_BACK) {
//                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                        finish();
//                    }
//                }
//                return false;
//            }
//        });
        final TextView nfs_set_no_tv = (TextView) v.findViewById(R.id.warn_reg_tv);
        final TextView warn_reg_textViewreg = (TextView) v.findViewById(R.id.warn_reg_textViewreg);
        nfs_set_no_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (text!=null&&!TextUtils.isEmpty(text)) {
                    if (text.equals("注册码无效，请联系管理员")) {
                         if (cancelCallBack!=null) {
                            cancelCallBack.toFinishActivity();
                        }
                    }else{
                        String nextTime = GetNextWarnTime(1);
                        SharedPreferences sharedPreferences = context.getSharedPreferences("NEXTWARNTIME", MODE_PRIVATE);
                        SharedPreferences.Editor et = sharedPreferences.edit();
                        et.putString("nextRegStatusTime" + status, nextTime);
                        et.commit();
                        dialog_toWarn.dismiss();
                    }

                }

            }
        });
        warn_reg_textViewreg.setText(text);
    }


    /**
     * 获取下次提醒的时间,day天后
     */
    private String GetNextWarnTime(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, day);
        Date date = calendar.getTime();
        String time = new SimpleDateFormat("yyyy-MM-dd").format(date);
        return time;
    }

    /**
     * 今天距离某天还剩多少天
     *
     * @return
     */
    private int TheDayToNextDay(String time) {
        int day = 0;
        try {
            Calendar mCalendar = Calendar.getInstance();
            Date nowDate = mCalendar.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            String time2 = sdf.format(nowDate);
            Date nowDate_ = sdf.parse(time2);
            Date nextDate = sdf.parse(time);
            day = (int) ((nextDate.getTime() - nowDate_.getTime()) / (24 * 60 * 60 * 1000));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return day;
    }

    /**
     * 从服务器获取最新的版本
     */
    private void GetNearestVersionFromService() {

        if (isConnected(context)) {
            getRequestQueue();
            StringRequest mStringRequest = new StringRequest(Request.Method.POST, URL_Reg_Center + "/WebService/SoftWare.asmx/GetAllSoftWareInfo", new Response.Listener<String>() {
                @Override
                public void onResponse(String str) {
                    if (str != null && !TextUtils.isEmpty(str)) {

                        try {
                            JSONObject obj = new JSONObject(str);
                            JSONArray infos = obj.getJSONArray("Model");
                            if (infos.length() > 0) {
                                JSONObject obj_ = (JSONObject) infos.get(0);
                                nearestVersion = obj_.getString("SoftwareVersion").trim();
                                String down_url = obj_.getString("softDownloadUrl");
                                String appDescription = obj_.getString("softDescription");
                                if (updateableSoftVersion(getAPPVersion(),nearestVersion)) {
                                    if (IsTheTime()) {
                                        WarnUpgradeDialog(down_url, appDescription);
                                    }

                                } else {//将
                                    SharedPreferences sharedPreferences = context.getSharedPreferences("NEXTWARNTIME", MODE_PRIVATE);
                                    SharedPreferences.Editor et = sharedPreferences.edit();
                                    et.putString("nextTime", "");
                                    et.commit();
                                }
                            } else {
                                Toast.makeText(context, "服务器上查不到该软件", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {

                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("softwareId",APP_MARK);
                    map.put("softwareType", "mb");
                    return map;
                }
            };
            myRequestQueue.add(mStringRequest);
        }

    }

    /**
     * 通过软件的版本名称判定是否升级
     * @param localVersionName 本地软件的版本名称
     * @param serverVersionName 服务端软件的版本名称
     * @return
     */
    private boolean updateableSoftVersion(String localVersionName, String serverVersionName) {
        if (TextUtils.isEmpty(localVersionName) || TextUtils.isEmpty(serverVersionName)) {
            return false;
        }
        String  local3 = "0";
        String  server3 = "0";
        String[] localVersion = localVersionName.split("\\.");
        String[] serverVersion = serverVersionName.split("\\.");
        String local1 = localVersion[0];
        String local2 = localVersion[1];
        if (localVersion.length==3) {
            local3 =localVersion[2];
        }
        String server1 = serverVersion[0];
        String server2 = serverVersion[1];
        if (serverVersion.length==3) {
            server3 =serverVersion[2];
        }
        if (Integer.parseInt(server1) > Integer.parseInt(local1)) {
            return true;
        }
        if (Integer.parseInt(server2) > Integer.parseInt(local2)) {
            return true;
        }
        if (Integer.parseInt(server3) > Integer.parseInt(local3)) {
            return true;
        }
        return false;
    }
    /**
     * 判定是否提醒升级
     */
    private boolean IsTheTime() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("NEXTWARNTIME", MODE_PRIVATE);
        final String time = sharedPreferences.getString("nextTime", "");
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String time2 = new SimpleDateFormat("yyyy-MM-dd").format(date);
        if (TextUtils.isEmpty(time)) {
            return true;
        } else {
            if (time.equals(time2)) {
                return true;
            } else {
                return false;
            }
        }

    }
    private void WarnUpgradeDialog(final String url, String description) {

        View v = LayoutInflater.from(context).inflate(R.layout.warn_layout, null);
        final Dialog dialog_toSet = new Dialog(context, R.style.DialogStyle);
        dialog_toSet.setCanceledOnTouchOutside(false);
        dialog_toSet.setCancelable(false);
        dialog_toSet.show();
        Window window = dialog_toSet.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        lp.width = dip2px(context, 300); // 宽度
        lp.height = dip2px(context, 260); // 高度
        // lp.alpha = 0.7f; // 透明度
        window.setAttributes(lp);
        window.setContentView(v);
        ListView feature_lv = (ListView) v.findViewById(R.id.feature_lv);
        feature_lv.setDivider(null);
        feature_lv.setAdapter(new DialogAdapter(context, description));
        dialog_toSet.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                if (keyCode == event.KEYCODE_BACK) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        dialog_toSet.dismiss();
                    }
                }
                return false;
            }
        });
        final TextView nfs_set_sure_tv = (TextView) v.findViewById(R.id.nfs_set_sure_tv);
        final TextView nfs_set_no_tv = (TextView) v.findViewById(R.id.nfs_set_no_tv);
        nfs_set_sure_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_toSet.dismiss();
                DownAPKfromService(url);
            }
        });
        nfs_set_no_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog_toSet.dismiss();
                String nextTime = GetNextWarnTime(7);
                SharedPreferences sharedPreferences = context.getSharedPreferences("NEXTWARNTIME", MODE_PRIVATE);
                SharedPreferences.Editor et = sharedPreferences.edit();
                et.putString("nextTime", nextTime);
                et.commit();
            }
        });
    }


    private void DownAPKfromService(String url) {

        if (isConnected(context)) {
            mProgressDialog = new CommonProgressDialog(context);
            mProgressDialog.setMessage("正在下载");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(true);
            final DownloadTask downloadTask = new DownloadTask(context);
            downloadTask.execute(url);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    downloadTask.cancel(true);
                }
            });
            //downFile(url);
        }

    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;

        DownloadTask(Context context) {
            this.context = context;
        }

        //执行异步任务（doInBackground）之前执行，并且在ui线程中执行
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //开始下载 对话框进度条显示
            mProgressDialog.show();
            mProgressDialog.setProgress(0);
        }

        @Override
        protected String doInBackground(String... params) {
            int i = 0;
            String uri = URL_Reg_Center + params[0];
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(uri);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file


                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }
                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();
                mProgressDialog.setMax(fileLength);
                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(GetAPKPath());
                byte data[] = new byte[4096];
                int total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
//					if (fileLength > 0) // only if total length is known
//					{
//						i = (int) (total * 100 / fileLength);
//					}
                    mProgressDialog.setProgress(total);
//					publishProgress(i);
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }
                if (connection != null)
                    connection.disconnect();
            }
            return "right";
        }

        //在ui线程中执行 可以操作ui
        @Override
        protected void onPostExecute(String string) {
            // TODO Auto-generated method stub
            super.onPostExecute(string);
            //下载完成 对话框进度条隐藏
            if (string.equals("right")) {
                mProgressDialog.cancel();
                Toast.makeText(context, "下载完成", Toast.LENGTH_SHORT).show();
                installApk();
            } else {
                mProgressDialog.cancel();
                Toast.makeText(context, "下载失败", Toast.LENGTH_LONG).show();
            }


        }

        /*
         * 在doInBackground方法中已经调用publishProgress方法 更新任务的执行进度后
         * 调用这个方法 实现进度条的更新
         * */
        @Override
        protected void onProgressUpdate(Integer... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);

        }
    }

    /**
     * 安装APK
     */
    private void installApk() {
        File file = new File(GetAPKPath());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 定义下载包存储路径
     */
    private String GetAPKPath() {
        File file = new File("/mnt/sdcard/.toInstallPG");
        if (!file.exists()) {
            file.mkdir();
        }
        String path = "/mnt/sdcard/.toInstallPG" + "/" + getAPPName() + nearestVersion + ".apk";
        return path;
    }

    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    /**
     * 获取软件名称
     */
    public String getAPPName() {
        String appName = "";
        PackageManager pm = context.getPackageManager();//得到PackageManager对象
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(context.getPackageName(), 0);
            appName = (String) pm.getApplicationLabel(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }

    // 将时间戳转成字符串
    public static String getDateToString(long time) {
        Date d = new Date(time);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return sf.format(d);
    }

    /**
     *弹出注册窗口
     */
    public void showRegDialog() {

        View v = LayoutInflater.from(context).inflate(R.layout.reg_dialog, null);
        dialog_Reg = new Dialog(context, R.style.DialogStyle);
        dialog_Reg.setCanceledOnTouchOutside(false);
        dialog_Reg.show();
        dialog_Reg.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dialog_Reg.dismiss();
                //退出程序
                 if (cancelCallBack!=null) {
                            cancelCallBack.toFinishActivity();
                        }
            }
        });
        Window window = dialog_Reg.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        lp.width = dip2px(context, 290); // 宽度
        lp.height = dip2px(context, 200); // 高度
        lp.alpha = 0.7f; // 透明度
        window.setAttributes(lp);
        window.setContentView(v);
        final TextView reg = (TextView) v.findViewById(R.id.editTextReg);
        ImageButton ib = (ImageButton) v.findViewById(R.id.imageButtonReg);
        ImageButton.OnClickListener listener = new ImageButton.OnClickListener() {

            public void onClick(View v) {
                if (!RegOperateTool.isConnected(context.getApplicationContext())) {
                    Toast.makeText(context, "网络异常，请检查手机网络", Toast.LENGTH_LONG)
                            .show();
                    return;
                }

                final String input = reg.getText().toString();
                if (input == null || TextUtils.isEmpty(input)) {
                    Toast.makeText(context, "请输入注册码",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                // 网络验证中
                progressDialog = ProgressDialog.show(context, "请稍候",
                        "注册码验证中请不要进行其他操作", true);
                progressDialog.setCancelable(true);

                getRequestQueue();

                StringRequest stringRequest = new StringRequest(
                        Request.Method.POST, RegOperateTool.URL_Reg_Center + "/WebService/SoftWare.asmx/GetRegisCodeInfo", new Response.Listener<String>() {


                    @Override
                    public void onResponse(String response) {
                        //如果注册码已经注册
                        if (!TextUtils.isEmpty(response)) {
                            String json = Getstr(response);
                            try {
                                JSONObject obj = new JSONObject(json);
                                String result = obj.getString("Result");
                                JSONArray mArray = obj.getJSONArray("Model");
                                if (!TextUtils.isEmpty(result) && result.equals("ok")) {
                                    if (mArray.length() == 0) {
                                        progressDialog.dismiss();
                                        Toast.makeText(context, "注册码不存在",
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        JSONObject obj_ = (JSONObject) mArray.get(0);
                                        String regStatus = obj_.getString("RegisCodeState").trim();
                                        String guestName = obj_.getString("Customer").trim();
                                        String Imei = obj_.getString("Imei").trim();
                                        String MAC = obj_.getString("MAC").trim();
                                        String isToolTip = obj_.getString("isToolTip").trim();
                                        String isNumber = obj_.getString("isNumber").trim();
                                        String Version = obj_.getString("Version").trim();
                                        if (!getAPPVersion().equals(Version)) {
                                            progressDialog.dismiss();
                                            Toast.makeText(context, "此注册码已绑定软件版本，请联系管理员",
                                                    Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                        if (regStatus.equals("正常")) {
                                            if (TextUtils.isEmpty(Imei)) {//说明该注册码没有绑定IMEI
                                                if (TextUtils.isEmpty(MAC)) {//也没有绑定MAC
                                                    RegSuccess(input, guestName, isToolTip,isNumber);
                                                } else {
                                                    //TODO 检测Mac是否一致
                                                    if (macAddress().equals(MAC)) {
                                                        RegSuccess(input, guestName, isToolTip,isNumber);
                                                    } else {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(context, "请确定注册码绑定的手机(MAC)是否正确",
                                                                Toast.LENGTH_LONG).show();
                                                    }

                                                }

                                            } else {//验证注册码是否匹配
                                                if (GetImei().equals(Imei)) {
                                                    if (TextUtils.isEmpty(MAC)) {
                                                        RegSuccess(input, guestName, isToolTip,isNumber);
                                                    } else {
                                                        //TODO 检测Mac是否一致
                                                        if (macAddress().equals(MAC)) {
                                                            RegSuccess(input, guestName, isToolTip,isNumber);
                                                        } else {
                                                            progressDialog.dismiss();
                                                            Toast.makeText(context, "请确定注册码绑定的手机(MAC)是否正确",
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    }

                                                } else {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(context, "请确定注册码绑定的手机(IMEI)是否正确",
                                                            Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        } else if (regStatus.equals("已过期")) {
                                            progressDialog.dismiss();
                                            Toast.makeText(context, "注册码已过期，请联系管理员", Toast.LENGTH_LONG).show();
                                        } else if (regStatus.equals("次数用尽")) {
                                            progressDialog.dismiss();
                                            Toast.makeText(context, "注册码调用次数已用尽，请联系管理员", Toast.LENGTH_LONG).show();
                                        } else if (regStatus.equals("已禁用")) {
                                            progressDialog.dismiss();
                                            Toast.makeText(context, "注册码无效，请联系管理员", Toast.LENGTH_LONG).show();
                                        }
                                    }


                                } else {
                                    Toast.makeText(context, "服务器连接异常", Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }


                        }


                    }


                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(
                            VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "服务器异常，请联系管理员", Toast.LENGTH_SHORT).show();


                    }
                }) {
                    @Override
                    protected Map<String, String> getParams()
                            throws AuthFailureError {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("regisCode", input);
                        map.put("softwareId", RegOperateTool.APP_MARK);
                        map.put("softwareType", "mb");
                        map.put("phoneMessage", getPhoneMessage());
                        return map;
                    }


                };

                myRequestQueue.add(stringRequest);


            }
        };

        ib.setOnClickListener(listener);

    }








    /**
     * 初始化定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void initLocation() {
        //初始化client
        locationClient = new AMapLocationClient(context.getApplicationContext());
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
        startLocation();
    }

    /**
     * 默认的定位参数
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(true);//可选，设置是否单次定位。默认是false
//		mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
//		AMapLocationClientOption.setLocationProtocol(AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
//		mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        return mOption;
    }
    /**
     * 开始定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void startLocation() {
//		//根据控件的选择，重新设置定位参数
//		resetOption();
        // 设置定位参数
        locationClient.setLocationOption(getDefaultOption());
        // 启动定位
        locationClient.startLocation();
    }

    /**
     * 停止定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void stopLocation() {
        // 停止定位
        locationClient.stopLocation();
    }

    /**
     * 销毁定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void destroyLocation() {
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            stopLocation();
            locationClient.onDestroy();
            locationClient = null;
        }
    }
    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation loc) {
            if (null != loc) {
                //解析定位结果
                Lat = loc.getLatitude() + "";
                Lng = loc.getLongitude() + "";
                Addr = loc.getAddress();
                CheckSavedVersion();
            }
        }
    };

    private String getPhoneMessage() {
        String lac = "";
        String cid = "";
        String nid = "";
        String imei = "";
        String phoneNo = "";
        String imsi = "";
        String mac = "";
        StringBuffer phoneInfo_sb = new StringBuffer();
        TelephonyManager mTManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (mTManager != null) {
            imei = mTManager.getDeviceId();
            phoneNo = mTManager.getLine1Number();
            imsi = mTManager.getSubscriberId();
        }
        mac = macAddress();
        phoneInfo_sb.append("PhoneNo:" + phoneNo + "," + "Imei:" + imei + "," + "Imsi:" + imsi + "," + "Mac:" + mac + ",");

        if (mTManager != null) {
            int phonetype = mTManager.getPhoneType();
            if (phonetype == TelephonyManager.PHONE_TYPE_GSM) {
                GsmCellLocation gcl = (GsmCellLocation) mTManager.getCellLocation();
                cid = gcl.getCid() + "";
                lac = gcl.getLac() + "";
            } else if (phonetype == TelephonyManager.PHONE_TYPE_CDMA) {
                CdmaCellLocation gcl = (CdmaCellLocation) mTManager
                        .getCellLocation();
                if (gcl != null) {
                    nid = gcl.getNetworkId() + "";// nid
                    cid = gcl.getBaseStationId() + "";// cellid
                    lac = gcl.getSystemId() + ""; // sid
                }

            }
            phoneInfo_sb.append("Lat:" + Lat + "," + "Log:" + Lng + "," + "Lac:" + lac + "," + "Cid:" + cid + "," + "Nid:" + nid + "," + "Addr:" + Addr);
        }
        return phoneInfo_sb.toString();
    }
    /**
     * 获取软件版本号
     */
    private String getAPPVersion() {
        PackageManager pm = context.getPackageManager();//得到PackageManager对象
        String version_app = "";
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);//得到PackageInfo对象，封装了一些软件包的信息在里面
            version_app = pi.versionName;//获取清单文件中versionCode节点的值
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version_app;
    }


    public String macAddress() {
        String address = null;
        try {
            // 把当前机器上的访问网络接口的存入 Enumeration集合中
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface netWork = interfaces.nextElement();
                // 如果存在硬件地址并可以使用给定的当前权限访问，则返回该硬件地址（通常是 MAC）。
                byte[] by = netWork.getHardwareAddress();
                if (by == null || by.length == 0) {
                    continue;
                }
                StringBuilder builder = new StringBuilder();
                for (byte b : by) {
                    builder.append(String.format("%02X:", b));
                }
                if (builder.length() > 0) {
                    builder.deleteCharAt(builder.length() - 1);
                }
                String mac = builder.toString();
                // 从路由器上在线设备的MAC地址列表，可以印证设备Wifi的 name 是 wlan0
                if (netWork.getName().equals("wlan0")) {
                    address = mac;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return address;
    }


    /**
     * 注册码验证成功后
     */
    private void RegSuccess(String input, String guestName, String isToolTip,String isNumber) {
        Toast.makeText(context, "注册码验证成功",
                Toast.LENGTH_LONG).show();
        strreg = input;
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("OBJREG", input);
        editor.putString("GUESTNAME", guestName);
        if (isToolTip.equals("0")) {//0代表不提示
            istoolTip = false;
            editor.putBoolean("ISTOOLTIP", false);
        } else {
            istoolTip = true;
            editor.putBoolean("ISTOOLTIP", true);
        }
        if (isNumber.equals("0")) {//0代表有次数限制
            isNumberLimit =true;
            editor.putBoolean("ISNUMBER", true);
        } else {
            isNumberLimit =false;
            editor.putBoolean("ISNUMBER", false);
        }
        editor.commit();
        dialog_Reg.dismiss();
        progressDialog.dismiss();

    }
    private String GetImei() {

        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        return imei;
    }

    private void CheckSavedVersion() {
        String savedVersion = sp.getString("SavedVersion", "");
//        String savedVersion = "1.0";
        String nowVersion = getAPPVersion();
        if (savedVersion.equals("")) {
            SharedPreferences.Editor et = sp.edit();
            et.putString("SavedVersion", nowVersion);
            et.commit();
        } else {
            if (!savedVersion.equals(nowVersion) && Double.parseDouble(nowVersion) > Double.parseDouble(savedVersion)) {
                //上传版本信息
                String info = GetInfoWhenVersionChanged(savedVersion, nowVersion);
                UploadVersionInfo(info);
            }

        }

    }

    private String GetInfoWhenVersionChanged(String originalVersion, String newestVersion) {
        String PhoneNo = "";
        String Imei = "";
        String time = "";
        time = RegOperateTool.getDateToString(System.currentTimeMillis());
        StringBuffer Info_sb = new StringBuffer();
        TelephonyManager mTManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (mTManager != null) {
            Imei = mTManager.getDeviceId();
            PhoneNo = mTManager.getLine1Number();
        }
        Info_sb.append("SoftName:" + getAPPName() + "," + "GuestName:" + sp.getString("GUESTNAME", "") + "," + "RegCode:" + strreg + "," + "PhoneNo:" + PhoneNo + "," + "Imei:" + Imei + "," + "Mac:" + macAddress() + "," + "Lat:" + Lat + "," + "Lng:" + Lng + "," + "Addr:" + Addr + "," + "OriginalVersion:" + originalVersion + "," + "NewestVersion:" + newestVersion + "," + "Time:" + time);

        return Info_sb.toString();
    }


    private void UploadVersionInfo(final String info) {
        getRequestQueue();
        String url = RegOperateTool.URL_Reg_Center + "/WebService/SoftWare.asmx/SetVersionInfo";
        StringRequest mStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (s != null && !TextUtils.isEmpty(s)) {
                    SharedPreferences.Editor et = sp.edit();
                    et.putString("SavedVersion", getAPPVersion());
                    et.commit();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("regisCode", strreg);
                map.put("versionMsg", info);
                return map;
            }
        };
        myRequestQueue.add(mStringRequest);
    }
    public void SetCancelCallBack(CancelCallBack callBack){
        this.cancelCallBack = callBack;
    }
    public interface CancelCallBack{
        void toFinishActivity();
    }
}

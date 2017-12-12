package yangTalkback.Act;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.example.volumekey.AExecuteAsRoot;
import com.example.volumekey.VolumeService;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import AXLib.Model.RefObject;
import AXLib.Utility.EventArg;
import AXLib.Utility.Ex.StringEx;
import AXLib.Utility.ICallback;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.StreamSocket;
import AXLib.Utility.ThreadEx;
import Tools.RegOperateTool;
import yangTalkback.App.App;
import yangTalkback.App.AppConfig;
import yangTalkback.Base.ActCLBase;
import yangTalkback.Base.AutoRefView;
import yangTalkback.Cpt.ImageButtonEx;
import yangTalkback.Net.ClientConnection;

@AutoRefView(id = R.layout.act_login, layout = 0x03)
public class actLogin extends ActCLBase {
    private static boolean _D = AppConfig._D;
    private static boolean _D1 = _D && true;// 本地测试
    private boolean _tryConnecting = false;// 是否正在尝试连接
    private int _tryConnectionCount = 0;// 尝试连接次数
    private Thread _connectThread = null;// 连接服务器线程
    private Class<?> _loginGoPage = null;
    private boolean _isAutorunMode = false;
    @AutoRefView(id = R.act_login.tbID)
    public EditText tbID;// 输入号码
    @AutoRefView(id = R.act_login.tbPwd)
    public EditText tbPwd;
    @AutoRefView(id = R.act_login.cbRPwd)
    public CheckBox cbRPwd;
    @AutoRefView(id = R.act_login.ibRPwd, click = "ibRPwd_Click")
    public ImageButtonEx ibRPwd;
    @AutoRefView(id = R.act_login.btSetting, click = "btSetting_Click")
    public Button btSetting;// 设置按钮
    @AutoRefView(id = R.act_login.btLogin, click = "btLogin_Click")
    public Button btLogin;// 登录按钮
    private boolean _isAutoBootRuned = false;
    private int _maxTryLoginCount = 50;
    private Object _lock = new Object();
    private Object _loginLock = new Object();

    private RegOperateTool regOperateTool;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        acquireWakeLock();
    }

    /**
     * 获取root权限
     */
    private void initRoot() {
        final List<String> cmds = new ArrayList<String>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < VolumeService.THREAD_NUM; i++) {
                    cmds.add("chmod 664 /dev/input/event" + i);
                }
            }
        }).start();

        boolean ret = AExecuteAsRoot.execute(cmds);


    }

    public void onScreenReady() {
        initRoot();
        _ac.LeaveExitApp = false;
        tbID.setText(_ac.LoginID);
        if (_ac.IsRememberPWD) {
            tbPwd.setText(_ac.LoginPWD);
            cbRPwd.setChecked(true);
            ibRPwd.setImageResource(R.drawable.ico_login_sel_active);
        }
        regOperateTool = new RegOperateTool(this, "");
        regOperateTool.SetCancelCallBack(new RegOperateTool.CancelCallBack() {
            @Override
            public void toFinishActivity() {
                finish();
            }
        });
        _loginGoPage = actMain.class;
        // _loginGoPage = actMonitor.class;
        String autorun = GetActivityDefaultExtraValue(false);

        if (_ac.IsAutorun && _ac.IsRememberPWD && App.GetConnection() != null && App.GetConnection().getIsConnected()) {
            startActivity(actMain.class);
        } else if (_ac.IsAutorun && _ac.IsRememberPWD && StringEx.equalsIgnoreCase("Autorun", autorun)) {
            _isAutorunMode = true;
            CallByNewThread("AutoBootRun");
        } else if (_D1) {
            tbPwd.setText("000000-00");
            btLogin_Click(null);
        }
    }

    @Override
    public boolean OnKeyDown_Back() {
        App.exit();
        return super.OnKeyDown_Back();
    }

    public void btLogin_Click(EventArg<View> arg) {
        final String ip = _ac.ServerIP;
        String idStr = this.tbID.getText().toString();
        String pwdStr = this.tbPwd.getText().toString();
        if (StringEx.isEmpty(ip)) {
            Alert("未设置登陆服务器,请先设置");
            return;
        }
        if (StringEx.isEmpty(idStr) || StringEx.isEmpty(pwdStr)) {
            Alert("编号或密码输入不正确");
            return;
        }
        _tryConnecting = true;
        ConnectServer();
    }

    public void btSetting_Click(EventArg<View> arg) {
        startActivity(actSetting.class);
    }

    public void ibRPwd_Click(EventArg<View> arg) {
        cbRPwd.setChecked(!cbRPwd.isChecked());
        ibRPwd.setImageResource(cbRPwd.isChecked() ? R.drawable.ico_login_sel_active : R.drawable.ico_login_sel_bg);
    }

    public void AlertAndExit() {
        ThreadEx.stop(_connectThread);
        ThreadEx.ThreadCall(new ICallback() {

            @Override
            public void invoke() {
                ThreadEx.sleep(200);
                CloseLoading();
                // actLogin.this.AlertAndExit("取消连接服务器，程序将退出");
            }
        });

    }

    public void AutoBootRun() {
        try {
            OpenLoading("正在执行自动启动登录，请稍候", false, null);
            Thread.sleep(30 * 1000);
            post(new ICallback() {
                @Override
                public void invoke() {

                    btLogin_Click(null);
                }
            });
        } catch (Exception e) {

        } finally {

        }
    }

    private void TryConnectServer(String ip, int port) {

        while (true) {
            StreamSocket ss = null;
            try {
                ss = new StreamSocket();
                ss.connect(ip, port);
                ss.close();
                return;
            } catch (Exception e) {
                try {
                    ss.close();
                } catch (Exception e1) {

                }
                ThreadEx.sleep(1000);
            }
        }
    }

    public void ConnectServer() {
        if (!_tryConnecting)
            return;
        // loading时取消后回调处理
        final ICallback cancelCallBack = new ICallback() {
            @Override
            public void invoke() {
                _tryConnecting = false;
                AlertAndExit();
            }
        };
        OpenLoading("正在连接服务器...", true, cancelCallBack);

        final String ip = _ac.ServerIP;
        final String idStr = this.tbID.getText().toString();
        final short id = (short) Integer.parseInt(idStr);
        final String pwd = tbPwd.getText().toString();

        final StreamSocket ss = new StreamSocket();
        try {
            ss.setReceiveBufferSize(8 * 1024);
            ss.setSendBufferSize(8 * 1024);
        } catch (SocketException e) {
        }
        synchronized (_lock) {
            ThreadEx.stop(_connectThread);
            _connectThread = ThreadEx.GetThreadHandle(new ICallback() {
                @Override
                public void invoke() {

                    if (_isAutorunMode) {
                        TryConnectServer(ip, AppConfig.Instance.ServerPort);
                        ThreadEx.sleep(5000);
                    }

                    ClientConnection cc = null;
                    try {
                        ss.connect(ip, AppConfig.Instance.ServerPort);// 连接服务器
                    } catch (Exception ex) {
                        String stack = RuntimeExceptionEx.GetStackTraceString(ex);
                        if (_tryConnectionCount <= _maxTryLoginCount) {// 连接失败，尝试重连
                            for (int i = 5; i >= 0; i--) {
                                if (!_tryConnecting)
                                    return;
                                OpenLoading(String.format("连接服务器失败，%d秒后自动重连。", i), true, cancelCallBack);
                                ThreadEx.sleep(1000);
                            }
                            _tryConnectionCount++;
                            OpenLoading("正在重新连接服务器...", true, cancelCallBack);
                            ThreadEx.sleep(1000);
                            if (_tryConnecting) {
                                post(new ICallback() {// 同步到UI重新连接服务器
                                    public void invoke() {
                                        ConnectServer();
                                    }
                                });
                            }
                            return;
                        } else {
                            AlertAndExit("未能连接服务器，请稍后再试。");
                        }
                    }
                    _tryConnectionCount = 0;

                    cc = new ClientConnection(ss);
                    ThreadEx.sleep(1000);
                    try {
                        OpenLoading("正在登录...", true, cancelCallBack);
                        RefObject<String> refObj = new RefObject<String>(null);
                        boolean loginResult = false;
                        try {

                            synchronized (_loginLock) {
                                ClientConnection lastCC = App.GetConnection();
                                if (lastCC != null && lastCC.getIsLogined() && lastCC.getIsConnected()) {
                                    if (App.LastAct instanceof actLogin) {
                                        startActivity(actMain.class);
                                        return;
                                    }
                                } else {
                                    loginResult = cc.Login(id, pwd, "", true, refObj);
                                    if (loginResult) {
                                        App.SetConnection(cc);// 设置当前连接
                                    }
                                }
                            }
                        } catch (Exception e) {

                        } finally {
                            CloseLoading();
                        }
                        if (loginResult) {
                            SaveLoginInfo();
                            OpenLoading("登录成功，正在进入...", true, cancelCallBack);
                            ThreadEx.sleep(1000);
                            CloseLoading();
                            startActivity(_loginGoPage);// 跳转页面
                            return;
                        } else {
                            cc.Disconnect();
                            cc = null;
                            Alert(String.format("登录失败，%s", refObj.Value), true);
                            return;
                        }
                    } catch (Exception ex) {
                        String stack = RuntimeExceptionEx.GetStackTraceString(ex);
                        if (_tryConnectionCount <= _maxTryLoginCount) {
                            for (int i = 5; i >= 0; i--) {
                                if (!_tryConnecting)
                                    return;
                                OpenLoading(String.format("网络连接出现错误,%d秒后自动重连。", i), true, cancelCallBack);
                                ThreadEx.sleep(1000);
                            }
                            _tryConnectionCount++;
                            OpenLoading("正在重新连接服务器...", true, cancelCallBack);
                            ThreadEx.sleep(1000);
                            if (_tryConnecting) {
                                post(new ICallback() {
                                    public void invoke() {
                                        ConnectServer();
                                    }
                                });
                            }
                            return;
                        } else {
                            AlertAndExit("网络错误，请稍后再试。");
                        }
                    }
                }

            });
            _connectThread.start();
        }
    }

    // 通信超时处理
    public void TimeoutReconnect() {
        _tryConnecting = true;
        final ICallback cancelCallBack = new ICallback() {
            @Override
            public void invoke() {
                _tryConnecting = false;
                AlertAndExit();
            }
        };
        OpenLoading("与服务器连接超时，正在重连...", true, cancelCallBack);
        ThreadEx.ThreadCall(new ICallback() {
            @Override
            public void invoke() {
                ThreadEx.sleep(1000);
                post(new ICallback() {
                    @Override
                    public void invoke() {
                        if (_tryConnecting)
                            ConnectServer();
                    }
                });

            }
        });
    }

    public void SaveLoginInfo() {
        _ac.LoginID = this.tbID.getText().toString();
        _ac.LoginPWD = this.tbPwd.getText().toString();
        _ac.IsRememberPWD = this.cbRPwd.isChecked();
        _ac.Save();
    }

    // 引发通信超时
    @Override
    public void OnClientConnectionTimeout() {
        if (_ac.TimeoutReconnect) {
            TimeoutReconnect();
        } else {
            AlertAndExit("与服务器通信超时，程序将退出！");
        }
    }

    // 界面返回，如果是超时则进行超时处理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (resultCode == TimeoutReconnect) {
            OnClientConnectionTimeout();
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }

    }

    // 连接断开处理，如果使用自动重连则不作任何操作
    @Override
    public void OnClientConnectionDisconnected(Exception e) {
        if (!_ac.TimeoutReconnect) {
            super.OnClientConnectionDisconnected(e);
        }
    }

    // 连接断开处理，如果使用自动重连则不作任何操作
    @Override
    public void OnClientConnectionDisconnected() {
        if (!_ac.TimeoutReconnect) {
            super.OnClientConnectionDisconnected();
        }
    }

    public static boolean hasInternet(Activity activity, Context c) {
        ConnectivityManager manager = (ConnectivityManager) activity.getSystemService(c.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return false;
        }
        if (info.isRoaming()) {
            return true;
        }
        return true;

    }

    public static String getIMEI(Context context) {
        String IMEI;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        IMEI = telephonyManager.getDeviceId();

        return IMEI;
    }

    private void CheckCDKey() {

        String cdkey = getResources().getString(R.string.cd_key);
        if (!StringEx.equalsIgnoreCase(_ac.CDKey, cdkey)) {
            if (!hasInternet(this, this.getApplicationContext())) {
                AlertAndExit("您的设备当前无可用网络连接，请设置网络连接后重试！");
                return;
            }
        }
    }

    WakeLock wakeLock = null;

    // 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
    private void acquireWakeLock() {
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) this.getSystemService(POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "PostLocationService");
            if (null != wakeLock) {
                wakeLock.acquire();
            }
        }
    }

    // 释放设备电源锁
    private void releaseWakeLock() {
        if (null != wakeLock) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    @Override
    public void finish() {
        acquireWakeLock();

        super.finish();
    }


}

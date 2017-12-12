package yangTalkback.Act;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.example.volumekey.CallBackInBG;
import com.example.volumekey.VolumeService;
import com.example.volumekey.VolumeService.MyBinder;

import java.util.Timer;
import java.util.TimerTask;

import AXLib.Utility.EventArg;
import AXLib.Utility.Ex.StringEx;
import AXLib.Utility.IAction;
import AXLib.Utility.ICallback;
import AXLib.Utility.ISelect;
import AXLib.Utility.JSONHelper;
import AXLib.Utility.ListEx;
import AXLib.Utility.Predicate;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.ThreadEx;
import Tools.RegOperateTool;
import yangTalkback.App.App;
import yangTalkback.Base.AutoRefView;
import yangTalkback.Base.Prompt;
import yangTalkback.Base.Prompt.PromptButton;
import yangTalkback.Comm.CLLog;
import yangTalkback.Comm.IDModel;
import yangTalkback.Comm.TalkbackStatus;
import yangTalkback.Cpt.GenGridView.ActGenDataViewActivity1;
import yangTalkback.Cpt.cptMenu;
import yangTalkback.Cpt.itemTalkbackInfo;
import yangTalkback.Net.Model.TalkbackChannelInfo;
import yangTalkback.Net.Model.TalkbackStatusInfo;
import yangTalkback.Protocol.PBCmdC;
import yangTalkback.Protocol.PBCmdR;
import yangTalkback.Protocol.PBMedia;

@AutoRefView(id = R.layout.act_talkback, layout = 0x03)
public class actTalkback extends ActGenDataViewActivity1<TalkbackStatusInfo> {

    @AutoRefView(id = R.act_talkback.cptMenu)
    public cptMenu cptMenu = new cptMenu(this);
    @AutoRefView(id = R.act_talkback.btTalk, touch = "btTalk_Touch")
    public Button btTalk;// 退出按钮
    @AutoRefView(id = R.act_talkback.btQuit, click = "btQuit_Click")
    public Button btQuit;// 退出按钮
    @AutoRefView(id = R.act_talkback.gvGrid)
    public GridView gvGrid;// 显示号码列表
    private Intent mIntent;

    /**
     * 当前音量
     */
    private int currentVolume;
    /**
     * 控制音量的对象
     */
    public AudioManager mAudioManager;
    /**
     * 系统最大音量
     */
    private int maxVolume;
    /**
     * 确保关闭程序后，停止线程
     */
    private boolean isDestroy;
    private Timer timer;
    private BlueToothReceiver receiver;
    private ListEx<IDModel> _SysIDList = new ListEx<IDModel>();// 保存系统的号码列表
    private ListEx<Short> _selIDList = new ListEx<Short>();
    private String _key = null;

    private Thread _threadRefresh = null;
    private TalkbackChannelInfo _info = null;
    private ListEx<TalkbackStatusInfo> _statusList;
    private TalkbackPlayCtrl _playCtrl = null;
    private TalkbackCaptureCtrl _capCtrl = null;
    private boolean _talkBtnCanDown = true;// 讲话按钮是否可以被按下
    private boolean _micBtnIsDown = false;// 耳机麦克风按扭是否被按下
    private boolean _enableMicphoneBtnCtrl = false;// 使用耳机麦克风按钮控制
    private BluetoothAdapter adapter;
    private RegOperateTool regOperateTool;

    // private EarphoneInsertReceiver receiver;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = BluetoothAdapter.getDefaultAdapter();
        mAudioManager = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        receiver = new BlueToothReceiver();
        IntentFilter intentFilter1 = new IntentFilter(
                "android.bluetooth.device.action.ACL_CONNECTED");
        registerReceiver(receiver, intentFilter1);
        IntentFilter intentFilter2 = new IntentFilter(
                "android.bluetooth.device.action.ACL_DISCONNECTED");
        registerReceiver(receiver, intentFilter2);
        handleHeadsetStateChange();

        mConn = new MyConn();
        mIntent = new Intent(actTalkback.this, VolumeService.class);
        bindService(mIntent, mConn, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        unbindService(mConn);
        unregisterReceiver(receiver);
        stopService(mIntent);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        handleHeadsetStateChange();
        super.onResume();
    }

    private void handleHeadsetStateChange() {
        if (BluetoothProfile.STATE_CONNECTED == adapter
                .getProfileConnectionState(BluetoothProfile.HEADSET)) {
            System.out.println("on");// 手机连接上蓝牙耳机
            if (!mAudioManager.isBluetoothScoOn()) {
                mAudioManager.setMode(AudioManager.MODE_IN_CALL);
                mAudioManager.setBluetoothScoOn(true);
                mAudioManager.startBluetoothSco();// 蓝牙录音的关键，启动SCO连接，耳机话筒才起作用
            }
            if (timer == null) {
                TimerTask task = new TimerTask() {
                    public void run() {
                        Message message = new Message();
                        message.what = 11;
                        handler.sendMessage(message);
                    }
                };

                timer = new Timer(true);
                timer.schedule(task, 1000, 1000); // 延时1000ms后执行，1000ms执行一次
            }
            // timer.cancel(); //退出计时器
        }/*
         * else if (BluetoothProfile.STATE_DISCONNECTED == adapter
		 * .getProfileConnectionState(BluetoothProfile.HEADSET)) {
		 * System.out.println("off");// 未连接
		 * mAudioManager.setBluetoothScoOn(false);
		 * mAudioManager.stopBluetoothSco(); }
		 */
    }

    public class BlueToothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            // Toast.makeText(context, "蓝牙状态改变广播 !", Toast.LENGTH_LONG).show();

            Log.i("TAG---BlueTooth", "接收到蓝牙状态改变广播！！");
            /*
			 * if(BluetoothDevice.ACTION_FOUND.equals(action)) {
			 * Toast.makeText(context, device.getName() + " 设备已发现！！",
			 * Toast.LENGTH_LONG).show(); } else
			 */
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Toast.makeText(context, device.getName() + "已连接",
                        Toast.LENGTH_LONG).show();
                handler.sendEmptyMessage(1);
            }

            // else if
            // (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action))
            // {
            // Toast.makeText(context, device.getName() + "正在断开蓝牙连接。。。",
            // Toast.LENGTH_LONG).show();
            // }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Toast.makeText(context, device.getName() + "蓝牙连接已断开！！！",
                        Toast.LENGTH_LONG).show();
                handler.sendEmptyMessage(2);
            }

            //
            // intent.putExtra("Bluetooth", btMessage);
            // intent.setClass(context, MainActivity.class);
            // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // context.startActivity(intent);

        }

    }

    public void onScreenReady() {
        regOperateTool = new RegOperateTool(this);
        if (RegOperateTool.isAllowedToMinus) {
            if (RegOperateTool.isNumberLimit) {
                RegOperateTool.isAllowedToMinus = false;
                regOperateTool.SetRegisCodeNumber(1);
            }
        }

        if (_connection == null) {
            AlertAndExit("网络连接异常！");
            return;
        }
        _key = GetActivityDefaultExtraValue(false);
        if (_key == null)
            _key = (String) GetActivityExtraValue("_notification_param");
        if (_key == null) {
            AlertAndOut("参数错误");
            return;
        }
        InitControls();
    }

    public void InitControls() {
        if (_enableMicphoneBtnCtrl) {
            // dioManager.registerMediaButtonEventReceiver(name);
        }

        cptMenu.ExecutionEvent.add(this, "cptMenu_ExecutionEvent");
        cptMenu.SetActiveMenu("Talkback");

        if (_ac.IsTwowayMode) {
            btTalk.setBackgroundResource(R.drawable.c_radius_button20_2);
            btTalk.setEnabled(false);
        }

        _playCtrl = new TalkbackPlayCtrl(this, _connection.ID,
                _ac.IsTwowayMode, _ac.SpeakMode);
        _capCtrl = new TalkbackCaptureCtrl(this, _connection.ID,
                _ac.IsTwowayMode, _ac.SpeakMode);

        this.CallByNewThread("Enter");// 启用新线程调用方法

    }

    public void UninitControl() {
        if (_enableMicphoneBtnCtrl) {
            // AudioManager audioManager = (AudioManager) this
            // .getSystemService(Context.AUDIO_SERVICE);
            // ComponentName name = new ComponentName(this.getPackageName(),
            // HeadSetReceiver.class.getName());
            // audioManager.unregisterMediaButtonEventReceiver(name);
        }
        _capCtrl.Stop();
        _playCtrl.Stop();
        ThreadEx.stop(_threadRefresh);
        _threadRefresh = null;
    }

    // 进入对讲
    public void DoEnterTalkback(String key) {
        finish();
        super.DoEnterTalkback(key);

    }

    public void Quit() {

        ThreadEx.ThreadCall(new ICallback() {
            public void invoke() {
                if (_connection != null && _connection.getIsLogined()) {
                    App.SetTalkbackStatus(TalkbackStatus.Leaveing);
                    PBCmdC pbc = new PBCmdC(_connection.ID,
                            PBCmdC.CMD_Type_TALK_Leave, JSONHelper.toJSON(_key));
                    PBCmdR pbr = _connection.CmdC(pbc);
                    App.SetTalkbackStatus(TalkbackStatus.Idle);
                }
                finish();
            }
        });

    }

    /**
     * 进入对讲
     */
    public void Enter() {
        try {
            OpenLoading("正在进入对讲", false, null);
            if (_connection != null && _connection.getIsLogined()) {
                App.SetTalkbackStatus(TalkbackStatus.Leaveing);
                PBCmdC pbc = new PBCmdC(_connection.ID,
                        PBCmdC.CMD_Type_TALK_Enter, JSONHelper.toJSON(_key));
                PBCmdR pbr = _connection.CmdC(pbc);
                if (pbr == null)
                    throw RuntimeExceptionEx
                            .Create("_connection.CmdC(pbc)==null");
                if (!pbr.Result) {
                    AlertAndOut(pbr.Message);
                    return;
                }
                _SysIDList = _connection.GetAllIDByCache();// 获取所有号码
                // 获取对讲信息
                pbc = new PBCmdC(_connection.ID, PBCmdC.CMD_Type_TALK_Info,
                        JSONHelper.toJSON(_key));
                pbr = _connection.CmdC(pbc);
                _info = JSONHelper.forJSON(pbr.JSON, TalkbackChannelInfo.class);
                // 过滤出在对讲通道的ID
                _SysIDList = _SysIDList.Where(new Predicate<IDModel>() {
                    public boolean Test(IDModel obj) {
                        return _info.OriginalIDList.contains((Object) obj.ID);
                    }
                });

                _statusList = _SysIDList
                        .Select(new ISelect<IDModel, TalkbackStatusInfo>() {
                            public TalkbackStatusInfo Select(IDModel t) {
                                TalkbackStatusInfo status = new TalkbackStatusInfo();
                                status.IDModel = t;
                                if (_info.ActiveIDList.contains(t.ID))
                                    status.JoinStatus = 1;
                                else if (_info.AvailableIDList.contains(t.ID))
                                    status.JoinStatus = 0;
                                else
                                    status.JoinStatus = -1;

                                return status;
                            }
                        });
                _playCtrl.SetTalkbackChannelInfo(_info);

                // 设置通话模式
                pbc = new PBCmdC(_connection.ID, PBCmdC.CMD_Type_TALK_SetMode,
                        JSONHelper.toJSON(new String[]{_key,
                                _ac.IsTwowayMode ? "1" : "0"}));
                pbr = _connection.CmdC(pbc);

                _playCtrl.Start();
                _capCtrl.Start();
                InitGridViewActivity(gvGrid, 2, R.layout.item_talkback_info);
                _threadRefresh = CallByNewThread("RefreshThread");
                App.SetTalkbackStatus(TalkbackStatus.Talkbacking);
                App.LastTalkChannelKey = _key;
            }
        } catch (Exception e) {
            String stack = RuntimeExceptionEx.GetStackTraceString(e);
            CLLog.Error(e);
            AlertAndOut("进入对讲失败");
        } finally {
            CloseLoading();
        }
    }

    public void RefreshThread() {
        ThreadEx.sleep(3000);
        while (!this.IsFinished && !this.isFinishing()
                && _connection.getIsConnected()) {
            try {
                PBCmdC pbc = new PBCmdC(_connection.ID,
                        PBCmdC.CMD_Type_TALK_Info, JSONHelper.toJSON(_key));
                PBCmdR pbr = _connection.CmdC(pbc);
                if (pbr == null)
                    continue;

                _info = JSONHelper.forJSON(pbr.JSON, TalkbackChannelInfo.class);
                if (_info == null)
                    continue;

                // 过滤出在对讲通道的ID
                _SysIDList = _SysIDList.Where(new Predicate<IDModel>() {
                    public boolean Test(IDModel obj) {
                        return _info.OriginalIDList.contains((Object) obj.ID);
                    }
                });
                _statusList = _SysIDList
                        .Select(new ISelect<IDModel, TalkbackStatusInfo>() {
                            public TalkbackStatusInfo Select(IDModel t) {
                                TalkbackStatusInfo status = new TalkbackStatusInfo();
                                status.IDModel = t;
                                if (_info.ActiveIDList.contains(t.ID))
                                    status.JoinStatus = 1;
                                else if (_info.AvailableIDList.contains(t.ID))
                                    status.JoinStatus = 0;
                                else
                                    status.JoinStatus = -1;
                                return status;
                            }
                        });
                Reflash();
                _playCtrl.SetTalkbackChannelInfo(_info);

            } catch (Exception e) {
                throw RuntimeExceptionEx.Create(e);
            } finally {
                ThreadEx.sleep(3000);
            }
        }
    }

    /**
     * 退出对讲
     */
    public void Leave() {
        try {
            OpenLoading("正在退出对讲", false, null);
            if (_connection != null && _connection.getIsLogined()) {
                App.SetTalkbackStatus(TalkbackStatus.Leaveing);
                PBCmdC pbc = new PBCmdC(_connection.ID,
                        PBCmdC.CMD_Type_TALK_Leave, JSONHelper.toJSON(_key));
                PBCmdR pbr = _connection.CmdC(pbc);
                App.SetTalkbackStatus(TalkbackStatus.Idle);
            }
        } catch (Exception e) {

        } finally {
            CloseLoading();
        }
    }

    // @Override
    // public boolean OnKeyDown_Back() {
    // return false;
    // }

    @Override
    public void finish() {
        UninitControl();
        // unregisterReceiver(receiver);
        super.finish();
    }

    public void cptMenu_ExecutionEvent(final EventArg<Object> arg) {
        if (!StringEx.equalsIgnoreCase(arg.e.toString(), "Talkback")) {
            Prompt("是否退出对讲？", Prompt.PromptButton.NO,
                    new IAction<Prompt.PromptButton>() {
                        @Override
                        public void invoke(PromptButton obj) {
                            if (obj == Prompt.PromptButton.YES) {
                                ThreadEx.ThreadCall(new ICallback() {
                                    public void invoke() {
                                        App.LastTalkChannelKey = null;
                                        Leave();
                                        finish();
                                        if (StringEx.equalsIgnoreCase(
                                                arg.e.toString(), "Record")) {
                                            startActivity(actRecord.class);
                                        }
                                    }
                                });
                            }
                        }
                    });

        }

    }

    // 获取数据项
    public ListEx<TalkbackStatusInfo> getData(int index) {
        if (index == 1)
            return _statusList;
        else
            return new ListEx<TalkbackStatusInfo>();
    }

    public void btTalk_Touch(EventArg<MotionEvent> arg) {
        if (!_talkBtnCanDown)
            return;
        if (arg.e.getAction() == MotionEvent.ACTION_DOWN) {
            _capCtrl.Capture(true);
            _playCtrl.SetSinwayCanPlay(false);
            // _playCtrl.SetTalkButtonDownStatus(true);
        } else if (arg.e.getAction() == MotionEvent.ACTION_UP) {
            _capCtrl.Capture(false);
            _playCtrl.SetSinwayCanPlay(true);
            // _playCtrl.SetTalkButtonDownStatus(false);
        }
    }


    public void btQuit_Click(EventArg<View> arg) {
        Prompt("是否退出对讲？", Prompt.PromptButton.NO,
                new IAction<Prompt.PromptButton>() {
                    @Override
                    public void invoke(PromptButton obj) {
                        if (obj == Prompt.PromptButton.YES) {
                            App.LastTalkChannelKey = null;
                            Leave();
                            finish();
                        }
                    }
                });
    }

    @Override
    protected ActGenDataViewActivity1.IGridViewItemViewCPT<TalkbackStatusInfo> CreateItem(
            TalkbackStatusInfo model) {
        return new itemTalkbackInfo(_act, model);
    }

    // 列表中按钮点击事件
    public void ItemClickEvent(EventArg<TalkbackStatusInfo> arg) {

    }

    public void MediaPushIn(PBMedia pb) {
        _playCtrl.PushIn(pb);
    }

    public void PushOut(PBMedia pb) {
        if (!this.IsFinished && !this.isFinishing()
                && _connection.getIsConnected()) {
            _connection.PushMedia(pb);
        }
    }

    public void SetTalkButtonEnabled(final boolean isEnabled) {
        _talkBtnCanDown = isEnabled;
        post(new ICallback() {
            public void invoke() {
                // 当前配置必须为单工模式，才能设置按钮是否可用
                if (!_ac.IsTwowayMode) {
                    btTalk.setBackgroundResource(isEnabled ? R.drawable.c_radius_button20
                            : R.drawable.c_radius_button20_2);
                    btTalk.setEnabled(isEnabled);
                }
            }
        });

    }

    public boolean GetTalkButtonEnabled() {
        return _talkBtnCanDown;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case 100:
                    Bundle data = msg.getData();
                    // 按键值
                    int keyCode = data.getInt("key_code");
                    // 按键时长
                    long eventTime = data.getLong("event_time");
                    // 设置超过1000毫秒，就触发长按事件 //谷歌把超过1000s定义为长按。
                    boolean isLongPress = (eventTime > 1000);
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:// 播放或暂停
                            break;
                        // 短按=播放下一首音乐，长按=音量加
                        case KeyEvent.KEYCODE_MEDIA_NEXT:
                            if (isLongPress) {
                            } else {
                            }
                            break;
                        // 短按=播放上一首音乐，长按=音量减
                        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                            if (isLongPress) {
                            } else {
                            }
                            break;
                    }
                    break;
                case 1:// 检测到蓝牙耳机已连接设备
                    handleHeadsetStateChange();
                    break;
                case 2:// 检测到蓝牙耳机已断开连接设备
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                    mAudioManager.setBluetoothScoOn(false);
                    mAudioManager.stopBluetoothSco();
                    break;
                case 11:
                    if (!mAudioManager.isBluetoothScoOn()) {
                        mAudioManager.setMode(AudioManager.MODE_IN_CALL);
                        mAudioManager.setBluetoothScoOn(true);
                        mAudioManager.startBluetoothSco();// 蓝牙录音的关键，启动SCO连接，耳机话筒才起作用
                    }
                    break;
                case 15:
                    _capCtrl.Capture(false);
                    _playCtrl.SetSinwayCanPlay(true);
                    //减一次

                    break;

                case 16:
                    _capCtrl.Capture(true);
                    _playCtrl.SetSinwayCanPlay(false);
                    break;

                default:// 其他消息-则扔回上层处理
                    super.handleMessage(msg);
            }
        }
    };
    private MyConn mConn;


    private class MyConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName arg0, IBinder service) {

            MyBinder mBinder = (MyBinder) service;
            VolumeService mVolumeService = mBinder.getService();
            mVolumeService.SetCallBackInBG(new CallBackInBG() {

                @Override
                public void up() {
                    //单工模式时生效
                    if (!_ac.IsTwowayMode) {
                        handler.sendEmptyMessage(15);


                    }


                }

                @Override
                public void down() {
                    //单工模式生效
                    if (!_ac.IsTwowayMode) {
                        handler.sendEmptyMessage(16);

                    }


                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            // TODO Auto-generated method stub

        }

    }


    @Override
    public void onBackPressed() {
        Prompt("是否退出对讲？", Prompt.PromptButton.NO,
                new IAction<Prompt.PromptButton>() {
                    @Override
                    public void invoke(PromptButton obj) {
                        if (obj == Prompt.PromptButton.YES) {
                            App.LastTalkChannelKey = null;
                            Leave();
                            finish();
                        }
                    }
                });
    }
}

package yangTalkback.Act;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;

import AXLib.Model.KeyValue;
import AXLib.Utility.Console;
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
import yangTalkback.Cpt.GenGridView.ActGenDataViewActivity1;
import yangTalkback.Cpt.cptDDLEx;
import yangTalkback.Cpt.cptIDItem;
import yangTalkback.Cpt.cptMenu;
import yangTalkback.Net.Model.TalkbackChannelInfo;
import yangTalkback.Protocol.PBCmdC;
import yangTalkback.Protocol.PBCmdR;

@AutoRefView(id = R.layout.act_main, layout = 0x03)
public class actMain extends ActGenDataViewActivity1<IDModel> {

    @AutoRefView(id = R.act_main.cptMenu)
    public cptMenu cptMenu = new cptMenu(this);
    @AutoRefView(id = R.act_main.ddlChannel)
    public Spinner ddlChannel;
    public cptDDLEx<String> cptDDL = null;
    @AutoRefView(id = R.act_main.btJoin, click = "btJoin_Click")
    public Button btJoin;// 退出按钮
    @AutoRefView(id = R.act_main.btJoins, click = "btquit_Click")
    public Button btJoins;// 退出按钮
    @AutoRefView(id = R.act_main.gvGrid)
    public GridView gvGrid;// 显示号码列表

    private ListEx<IDModel> _SysIDList = new ListEx<IDModel>();// 保存系统的号码列表
    public ListEx<Short> SelIDList = new ListEx<Short>();
    private ListEx<IDModel> IDListOnline = new ListEx<IDModel>();
    public ListEx<Short> sysIDList_s = new ListEx<Short>();
    private boolean _isTalkbackReqing = false;
    private boolean _isFirstDDLSel = false;// 弹出下拉第一次不知为什么会触发选择事件，这里用一个字段来标识是否第一次
    private BluetoothAdapter adapter;
    private RegOperateTool regOperateTool;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    public void onScreenReady() {
      regOperateTool = new RegOperateTool(this);
        if (_connection == null) {
            AlertAndExit("网络连接异常！");
        }
        InitControls();

    }

    public void InitControls() {
        cptMenu.ExecutionEvent.add(this, "cptMenu_ExecutionEvent");
        cptMenu.SetActiveMenu("Main");
        cptDDL = new cptDDLEx<String>(this, ddlChannel);
        cptDDL.Selected.add(this, "cptDDl_Selected");
        this.CallByNewThread("LoadAllID");// 启用新线程调用方法
    }

    // 加载所有号码
    public void LoadAllID() {
        if (_connection != null && _connection.getIsLogined()) {
            _SysIDList = _connection.GetAllID();// 获取所有号码
            if (_SysIDList != null) {
                _SysIDList = _SysIDList.Where(new Predicate<IDModel>() {
                    public boolean Test(IDModel obj) {
                        return obj.ID != _connection.ID;
                    }
                });
            } else {
                _SysIDList = new ListEx<IDModel>();
            }
            InitGridViewActivity(gvGrid, 3, R.layout.item_idinfo, -1,
                    Tools.DensityUtil.dip2px(this, 50));
            RestoreTalkback();
            RefreshThread();

        }
    }

    public ListEx<Short> getListOnline() {
        IDListOnline.clear();
        sysIDList_s = new ListEx<Short>();
        ListEx<IDModel> SysIDListNow = _connection.GetAllID();// 获取所有号码

        for (int i = 0; i < SysIDListNow.size(); i++) {
            IDModel bean = SysIDListNow.get(i);
            if (bean.IsOnLine) {
                if (_connection.ID != bean.ID) {
                    sysIDList_s.add(bean.ID);
                    IDListOnline.add(bean);
                }

                // PubDate.ib.setVisibility(View.VISIBLE );
            }
        }
        return sysIDList_s;
    }

    public void RefreshThread() {

        ThreadEx.sleep(1000 * 5);
        while (!this.IsFinished && !this.isFinishing()) {

            if (_connection != null && _connection.getIsLogined()
                    && App.LastAct == this) {
                ListEx<IDModel> list = _connection.GetAllID();// 获取所有号码
                if (list != null) {
                    list = list.Where(new Predicate<IDModel>() {
                        public boolean Test(IDModel obj) {
                            return obj.ID != _connection.ID;
                        }
                    });
                    _SysIDList = list;
                    Reflash();
                }
            }
            if (App.LastAct == this)
                ThreadEx.sleep(1000 * 5);
            else
                ThreadEx.sleep(1000 * 30);
        }
    }

    public void RestoreTalkback() {
        if (App.LastTalkChannelKey != null) {
            String lastKey = App.LastTalkChannelKey;
            App.LastTalkChannelKey = null;
            try {
                if (_connection != null && _connection.getIsLogined()) {
                    PBCmdC pbc = new PBCmdC(_connection.ID,
                            PBCmdC.CMD_Type_TALK_MyChannel, "");
                    PBCmdR pbr = _connection.CmdC(pbc);
                    if (pbr != null && pbr.Result) {
                        java.lang.reflect.Type token = (new TypeToken<ListEx<TalkbackChannelInfo>>() {
                        }).getType();
                        ListEx<TalkbackChannelInfo> list = JSONHelper.forJSON(
                                pbr.JSON, token);
                        for (TalkbackChannelInfo info : list) {
                            if (StringEx.equals(lastKey, info.Key)) {
                                startActivity(actTalkback.class, lastKey);
                                return;
                            }
                        }
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    @Override
    public boolean OnKeyDown_Back() {
        Prompt("是否退出隐蔽对讲手机客户端？", PromptButton.NO,
                new IAction<Prompt.PromptButton>() {
                    @Override
                    public void invoke(PromptButton obj) {
                        if (obj == PromptButton.YES) {
                            App.exit();
                        }
                    }
                });
        return false;
    }

    // 获取数据项
    public ListEx<IDModel> getData(int index) {
        if (index == 1)
            return _SysIDList;
        else
            return new ListEx<IDModel>();
    }

    public void cptDDl_Selected(EventArg<String> arg) {
        if (_isFirstDDLSel) {
            _isFirstDDLSel = false;
            return;
        }
        startActivity(actTalkback.class, arg.e);
    }

    /**
     * 我的对讲 的点击事件
     */
    public void LoadMyChannel() {
        if (RegOperateTool.istoolTip) {
            SharedPreferences sp = getSharedPreferences("REG", MODE_PRIVATE);
            String reg_status = sp.getString("REGSTATUS", "注册码正常");
            //  "注册码已经禁用"
            if (reg_status.equals("注册码已禁用")) {
                mHandler.sendEmptyMessage(1);
                return;
            } else if (reg_status.equals("注册码次数已用完")) {
                mHandler.sendEmptyMessage(2);
                return;
            } else if (reg_status.equals("注册码已过期")) {
                mHandler.sendEmptyMessage(3);
                return;
            } else if (reg_status.equals("注册码不正确")) {
                mHandler.sendEmptyMessage(4);
                return;
            }

        }else{
            if (RegOperateTool.isForbidden) {
                Toast.makeText(actMain.this, "注册码无效，请联系管理员", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        RegOperateTool.isAllowedToMinus = true;

        try {
            OpenLoading("正在获取对讲信息", false, null);
            if (_connection != null && _connection.getIsLogined()) {
                PBCmdC pbc = new PBCmdC(_connection.ID,
                        PBCmdC.CMD_Type_TALK_MyChannel, "");
                PBCmdR pbr = _connection.CmdC(pbc);
                if (pbr != null && pbr.Result) {
                    java.lang.reflect.Type token = (new TypeToken<ListEx<TalkbackChannelInfo>>() {
                    }).getType();
                    ListEx<TalkbackChannelInfo> list = JSONHelper.forJSON(
                            pbr.JSON, token);
                    if (list.size() == 0) {
                        Alert("当前没有您参与的对讲");
                        return;
                    }
                    if (list.size() == 1) {
                        startActivity(actTalkback.class, list.get(0).Key);
                        return;
                    }
                    final ListEx<KeyValue<String, String>> tab = list
                            .Select(new ISelect<TalkbackChannelInfo, KeyValue<String, String>>() {
                                public KeyValue<String, String> Select(
                                        TalkbackChannelInfo t) {
                                    return new KeyValue<String, String>(t.Key,
                                            GetNameByID(t.OriginalIDList));
                                }
                            });
                    post(new ICallback() {
                        @Override
                        public void invoke() {
                            // 这里要做弹出选择的，先使用默认最后一个的方式
                            // startActivity(actTalkback.class,
                            // tab.get(tab.size() - 1).Key);
                            // return;
                            _isFirstDDLSel = true;
                            tab.insertElementAt(new KeyValue<String, String>(
                                    "0", "请选择加入对讲或取消"), 0);
                            cptDDL.setSource(tab);
                            cptDDL.Open();
                        }
                    });

                }
            }
        } catch (Exception e) {
            CLLog.Error(e);
            OpenLoading("获取对讲信息失败", false, null);
        } finally {
            CloseLoading();
        }
    }

    /**
     * 全民对讲的点击事件
     *
     * @param arg
     */
    public void btquit_Click(EventArg<View> arg) {
        if (RegOperateTool.istoolTip) {
            if (!regOperateTool.isTheRegStatusOk(actMain.this)) {
                return;
            }
        }else{
            if (RegOperateTool.isForbidden) {
                Toast.makeText(actMain.this, "注册码无效，请联系管理员", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        RegOperateTool.isAllowedToMinus = true;
        getListOnline();
        if (sysIDList_s != null && sysIDList_s.size() != 0) {
            if (_isTalkbackReqing)
                return;
            _isTalkbackReqing = true;
            OpenLoading("正在发起对讲");
            ThreadEx.ThreadCall(new ICallback() {
                @Override
                public void invoke() {
                    try {
                        ListEx<Short> list = sysIDList_s.ToList();
                        list.add(_connection.ID);
                        String json = JSONHelper.toJSON(list);
                        PBCmdC pbc = new PBCmdC(_connection.ID,
                                PBCmdC.CMD_Type_TALK_Req, json);

                        PBCmdR pbr = _connection.CmdC(pbc);
                        if (pbr.Result) {//请求对讲成功
                            String key = JSONHelper.forJSON(pbr.JSON,
                                    String.class);
                            startActivity(actTalkback.class, key);
                            return;
                        } else {
                            // 对讲通道已经存在
                            if (pbr.JSON != null && pbr.Message != null
                                    && pbr.Message.contains("通道已经存在")) {
                                String key = JSONHelper.forJSON(pbr.JSON,
                                        String.class);
                                startActivity(actTalkback.class, key);
                                return;
                            } else {
                                Alert("发起对讲失败", true);
                            }

                        }
                    } catch (Exception e) {
                        String stack = RuntimeExceptionEx
                                .GetStackTraceString(e);
                        Alert("发起对讲发生异常", true);
                    } finally {
                        CloseLoading();
                        _isTalkbackReqing = false;
                    }

                }
            });
            return;
        } else {
            Alert("当前无在线成员参与对讲");
            return;
        }

    }

    /**
     * 发起对讲的点击事件
     *
     * @param arg
     */
    public void btJoin_Click(EventArg<View> arg) {
        if (RegOperateTool.istoolTip) {
            if (!regOperateTool.isTheRegStatusOk(actMain.this)) {
                return;
            }
        }else{
            if (RegOperateTool.isForbidden) {
                Toast.makeText(actMain.this, "注册码无效，请联系管理员", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        RegOperateTool.isAllowedToMinus = true;
        getListOnline();
        if (sysIDList_s != null && sysIDList_s.size() != 0) {
            if (SelIDList.size() == 0) {
                Alert("请选择需要参与对讲的成员");
                return;
            }
//            for (IDModel idModel : IDListOnline) {
//                if (idModel.TalkStatus == 3 && SelIDList.contains(idModel.ID)) {
//                    Toast.makeText(_act, idModel.Name + "正在对讲，请稍后再试", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//            }

            if (_isTalkbackReqing)
                return;
            _isTalkbackReqing = true;
            OpenLoading("正在发起对讲");
            ThreadEx.ThreadCall(new ICallback() {
                @Override
                public void invoke() {
                    try {
                        ListEx<Short> list = SelIDList.ToList();
                        list.add(_connection.ID);
                        String json = JSONHelper.toJSON(list);
                        PBCmdC pbc = new PBCmdC(_connection.ID,
                                PBCmdC.CMD_Type_TALK_Req, json);

                        PBCmdR pbr = _connection.CmdC(pbc);
                        if (pbr.Result) {
                            String key = JSONHelper.forJSON(pbr.JSON, String.class);
                            startActivity(actTalkback.class, key);
                            return;
                        } else {
                            // 对讲通道已经存在
                            if (pbr.JSON != null && pbr.Message != null
                                    && pbr.Message.contains("通道已经存在")) {
                                String key = JSONHelper.forJSON(pbr.JSON,
                                        String.class);
                                startActivity(actTalkback.class, key);
                                return;
                            } else {
                                Alert("发起对讲失败", true);
                            }

                        }
                    } catch (Exception e) {
                        String stack = RuntimeExceptionEx.GetStackTraceString(e);
                        Alert("发起对讲发生异常", true);
                    } finally {
                        CloseLoading();
                        _isTalkbackReqing = false;
                    }

                }
            });
        } else {
            Alert("当前无在线成员参与对讲");
        }
    }

    public void cptMenu_ExecutionEvent(EventArg<Object> arg) {
        if (StringEx.equalsIgnoreCase(arg.e.toString(), "Talkback")) {
            CallByNewThread("LoadMyChannel");

        }
        if (StringEx.equalsIgnoreCase(arg.e.toString(), "Record")) {
            startActivity(actRecord.class);

        }
    }

    @Override
    protected ActGenDataViewActivity1.IGridViewItemViewCPT<IDModel> CreateItem(
            IDModel model) {
        return new cptIDItem(_act, model);
    }

    // 点击成员 将选中状态添加或移除到selIDList
    public boolean OnIDSelectChanged(boolean sel, short id) {
        if (sel) {
            if (SelIDList.size() >= 5 && false) {
                Notice("超过最大成员数5");
                return false;
            } else {
                if (!SelIDList.contains((Object) id))
                    SelIDList.add(id);
                return true;
            }
        } else {
            SelIDList.remove((Object) id);
            return true;
        }

    }

    @Override
    public void ItemExecutionEvent(EventArg<Object> arg) {
        Console.d("ItemExecutionEvent", arg.e);

    }

    // 列表中按钮点击事件
    public void ItemClickEvent(EventArg<IDModel> arg) {
        IDModel model = ((cptIDItem) arg.sender).getModel();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (resultCode == TimeoutReconnect) {
            Intent intent1 = new Intent();
            setResult(TimeoutReconnect, intent1);
            finish(3000);
        }
        super.onActivityResult(requestCode, resultCode, intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "设置").setIcon(
                R.drawable.ico_setting);
        menu.add(Menu.NONE, Menu.FIRST + 2, 2, "退出").setIcon(
                R.drawable.ico_exit);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case Menu.FIRST + 1:
                startActivity(actSetting.class);
                break;
            case Menu.FIRST + 2:
                App.exit();
                break;
            case Menu.FIRST + 3:
                // App.exit();
                break;
            default:
                break;
        }
        return true;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 1:
                    Toast.makeText(actMain.this, "注册码已禁用，请联系管理员", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(actMain.this, "注册码次数已用完，请联系管理员", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(actMain.this, "注册码已过期，请联系管理员", Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    Toast.makeText(actMain.this, "注册码不正确，请联系管理员", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }


        }
    };
}

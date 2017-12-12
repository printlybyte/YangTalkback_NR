package yangTalkback.Act;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import AXLib.Model.KeyValue;
import AXLib.Utility.EventArg;
import AXLib.Utility.Ex.Config.GeneralConfig;
import AXLib.Utility.Ex.StringEx;
import AXLib.Utility.ListEx;
import yangTalkback.App.AppConfig;
import yangTalkback.Base.ActCLBase;
import yangTalkback.Base.AutoRefView;
import yangTalkback.Codec.CamVideoCoderProfile.CameraType;
import yangTalkback.Codec.CamVideoCoderProfile.ResolutionType;
import yangTalkback.Cpt.cptDDLEx;

//参数设置界面
@AutoRefView(id = R.layout.act_setting, layout = 0x03)
public class actSetting extends ActCLBase {
	private static boolean _D = AppConfig._D;
	private static boolean _D1 = _D && false;// 本地测试

	@AutoRefView(id = R.act_setting.tbVideoCaptureRestartSpan)
	public EditText tbVideoCaptureRestartSpan;// 视频重置时间
	@AutoRefView(id = R.act_setting.rbFrontCamera)
	public RadioButton rbFrontCamera;// 前置摄像头选择
	@AutoRefView(id = R.act_setting.rbBackCamera)
	public RadioButton rbBackCamera;// 后置摄像头选择

	@AutoRefView(id = R.act_setting.cbVideoCaptureStopMode1)
	public RadioButton rbVideoCaptureStopMode1;// 视频采集模式
	@AutoRefView(id = R.act_setting.cbVideoCaptureStopMode2)
	public RadioButton rbVideoCaptureStopMode2;// 视频采集模式
	@AutoRefView(id = R.act_setting.cbLeaveExitApp)
	public CheckBox cbLeaveExitApp;// 切换后台处理方式
	@AutoRefView(id = R.act_setting.cbTimeoutReconnect)
	public CheckBox cbTimeoutReconnect;// 超时处理方式

	@AutoRefView(id = R.act_setting.tbServerIP)
	public EditText tbServerIP;

	@AutoRefView(id = R.act_setting.rbSpeaker0)
	public RadioButton rbSpeaker0;
	@AutoRefView(id = R.act_setting.rbSpeaker1)
	public RadioButton rbSpeaker1;
	@AutoRefView(id = R.act_setting.rbSinway)
	public RadioButton rbSinway;
	@AutoRefView(id = R.act_setting.rbTwoway, click = "rbTwoway_Click")
	public RadioButton rbTwoway;
	@AutoRefView(id = R.act_setting.rbAutorun0)
	public RadioButton rbAutorun0;
	@AutoRefView(id = R.act_setting.rbAutorun1)
	public RadioButton rbAutorun1;

	@AutoRefView(id = R.act_setting.cbVideoCapMode)
	public CheckBox cbVideoEncodeMode;// 视频模式

	@AutoRefView(id = R.act_setting.tbPicModeQuality)
	public EditText tbPicModeQuality;// 图片质量

	@AutoRefView(id = R.act_setting.btSave, click = "btSave_Click")
	public Button btLogin;// 保存按钮
	@AutoRefView(id = R.act_setting.btCancel, click = "btCancel_Click")
	public Button btCancel;// 保存按钮

	@AutoRefView(id = R.act_setting.ddlVideoSize)
	public Spinner ddlVideoSize;// 视频分辨率

	@AutoRefView(id = R.act_setting.rbTCP)
	public RadioButton rbTCP;// 传输模式
	@AutoRefView(id = R.act_setting.rbUDP)
	public RadioButton rbUDP;// 传输模式

	public cptDDLEx<Integer> cptVideoSize = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void onScreenReady() {
		super.onScreenReady();
		rbFrontCamera.setChecked(_ac.CameraSelected == CameraType.FrontCamera);
		rbBackCamera.setChecked(_ac.CameraSelected != CameraType.FrontCamera);

		cbLeaveExitApp.setChecked(_ac.LeaveExitApp);
		cbTimeoutReconnect.setChecked(_ac.TimeoutReconnect);

		cbVideoEncodeMode.setChecked(_ac.VideoEncodeMode == 1);
		rbVideoCaptureStopMode1.setChecked(_ac.VideoCaptrueStopMode == 0);
		rbVideoCaptureStopMode2.setChecked(_ac.VideoCaptrueStopMode == 1);
		tbVideoCaptureRestartSpan.setText(String.valueOf(_ac.VideoCaptrueRestartMinutes));
		tbPicModeQuality.setText(String.valueOf(_ac.PicModeQuality));

		tbServerIP.setText(_ac.ServerIP);
		rbSpeaker0.setChecked(_ac.SpeakMode == 0);
		rbSpeaker1.setChecked(_ac.SpeakMode != 0);
		rbSinway.setChecked(!_ac.IsTwowayMode);
		rbTwoway.setChecked(_ac.IsTwowayMode);
		rbAutorun1.setChecked(_ac.IsAutorun);
		rbAutorun0.setChecked(!_ac.IsAutorun);

		cptVideoSize = new cptDDLEx<Integer>(this, ddlVideoSize);
		ListEx<KeyValue<Integer, String>> videoSizeSource = new ListEx<KeyValue<Integer, String>>();
		videoSizeSource.add(new KeyValue<Integer, String>(0, "默认"));
		videoSizeSource.add(new KeyValue<Integer, String>(ResolutionType.QVGA.getId(), "320*240"));
		videoSizeSource.add(new KeyValue<Integer, String>(ResolutionType.VGA.getId(), "640*480"));
		videoSizeSource.add(new KeyValue<Integer, String>(ResolutionType._480P.getId(), "780*480"));
		videoSizeSource.add(new KeyValue<Integer, String>(ResolutionType._720P.getId(), "1280*720"));
		cptVideoSize.setSource(videoSizeSource);
		cptVideoSize.setSelectedKey(_ac.VideoSizeModel);

		rbTCP.setChecked(!_ac.UDPMode);
		rbUDP.setChecked(_ac.UDPMode);
	}

	public void btSave_Click(EventArg<View> arg) {
		if (StringEx.equals(this.tbVideoCaptureRestartSpan.getText().toString(), "")) {
			Alert("输入不正确");
			return;
		}
		_ac.CameraSelected = rbFrontCamera.isChecked() ? CameraType.FrontCamera : CameraType.BackCamera;
		_ac.VideoCaptrueStopMode = rbVideoCaptureStopMode1.isChecked() ? 0 : 1;

		_ac.LeaveExitApp = this.cbLeaveExitApp.isChecked();
		_ac.TimeoutReconnect = this.cbTimeoutReconnect.isChecked();
		_ac.VideoCaptrueRestartMinutes = Integer.parseInt(this.tbVideoCaptureRestartSpan.getText().toString());
		_ac.VideoEncodeMode = this.cbVideoEncodeMode.isChecked() ? 1 : 0;
		_ac.PicModeQuality = Integer.parseInt(this.tbPicModeQuality.getText().toString());
		_ac.VideoSizeModel = cptVideoSize.getSelectedKey();
		_ac.UDPMode = rbUDP.isChecked();

		_ac.ServerIP = tbServerIP.getText().toString();
		_ac.SpeakMode = rbSpeaker0.isChecked() ? 0 : 1;
		_ac.IsTwowayMode = !rbSinway.isChecked();
		_ac.IsAutorun = rbAutorun1.isChecked();
		GeneralConfig.Instance.Save("Autorun", _ac.IsAutorun ? "1" : "0");
		_ac.Save();

		AlertAndOut("保存成功");
	}

	public void btCancel_Click(EventArg<View> arg) {
		finish();
	}

	public void rbTwoway_Click(EventArg<View> arg) {
		Alert("为了达到良好的对讲效果，选择双工模式下建议使用听筒模式并配带耳机进行对讲");
	}

}

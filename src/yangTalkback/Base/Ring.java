//package YangTalkback.Base;
//
//import java.io.IOException;
//import java.util.Date;
//
//import AXLib.Utility.TimeUtil;
//import YangTalkback.Act.R;
//import YangTalkback.App.App;
//import android.content.Context;
//import android.content.res.AssetFileDescriptor;
//import android.media.AudioManager;
//import android.media.MediaPlayer;
//import android.os.Vibrator;
////铃声震动封装
//public class Ring {
//	private static MediaPlayer _mPlayer = null;
//	private static AudioManager _audioService = null;
//	private static Vibrator _vibrator = null;
//	public static Date _lastVibratorTime = TimeUtil.GetCurrentUtilDate();
//	public final static int GRing = 0;
//
//	public static void Play() {
//		if (_mPlayer == null) {
//			_mPlayer = new MediaPlayer();
//			_mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//			_mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//				@Override
//				public void onCompletion(MediaPlayer player) {
//					player.seekTo(0);
//				}
//			});
//			AssetFileDescriptor file = null;
//
//			file = App.Application.getResources().openRawResourceFd(R.raw.duanxin);
//			try {
//				_mPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
//				file.close();
//				_mPlayer.setVolume(100, 100);
//				_mPlayer.prepare();
//			} catch (IOException ioe) {
//				_mPlayer = null;
//			}
//			_audioService = (AudioManager) App.Application.getSystemService(Context.AUDIO_SERVICE);
//			_vibrator = (Vibrator) App.Application.getSystemService(Context.VIBRATOR_SERVICE);
//		}
//		App.LastAct.setVolumeControlStream(AudioManager.STREAM_MUSIC);
//		boolean shouldPlayBeep = true;
//		if (_audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL)
//			shouldPlayBeep = false;
//
//		if (shouldPlayBeep && _mPlayer != null)
//			_mPlayer.start();
//
//		if (TimeUtil.XYNow(TimeUtil.AddSeconds(_lastVibratorTime, 2))) {
//			_vibrator.vibrate(1000);
//			_lastVibratorTime = TimeUtil.GetCurrentUtilDate();
//		}
//
//
//		// 第一个参数，指代一个震动的频率数组。每两个为一组，每组的第一个为等待时间，第二个为震动时间。
//		// 比如 [2000,500,100,400],会先等待2000毫秒，震动500，再等待100，震动400
//		// 第二个参数，repest指代从 第几个索引（第一个数组参数） 的位置开始循环震动。
//		// 会一直保持循环，我们需要用 vibrator.cancel()主动终止
//		// vibrator.vibrate(new long[]{300,500},0);
//
//	}
//
//	public static void Play(int sound) {
//		Play();
//	}
//}

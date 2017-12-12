package yangTalkback.Module;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;

import java.io.IOException;

import yangTalkback.App.App;
import yangTalkback.Act.R;

public class Ring {
	private static MediaPlayer _mPlayer = null;
	private static AudioManager _audioService = null;
	private static Vibrator _vibrator = null;

	public final static int GRing = 0;

	public static void Play() {
		Play(R.raw.duanxin);
	}

	public static void Play(int resId) {
		if (_mPlayer == null) {
			_mPlayer = new MediaPlayer();
			_mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			_mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer player) {
					player.seekTo(0);
				}
			});
			AssetFileDescriptor file = App.Application.getResources().openRawResourceFd(R.raw.duanxin);
			try {
				_mPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
				file.close();
				_mPlayer.setVolume(100, 100);
				_mPlayer.prepare();
			} catch (IOException ioe) {
				_mPlayer = null;
			}
			_audioService = (AudioManager) App.Application.getSystemService(Context.AUDIO_SERVICE);
			_vibrator = (Vibrator) App.Application.getSystemService(Context.VIBRATOR_SERVICE);
		}
		App.LastAct.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		boolean shouldPlayBeep = true;
		if (_audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL)
			shouldPlayBeep = false;

		if (shouldPlayBeep && _mPlayer != null)
			_mPlayer.start();

		// 震动一次
		_vibrator.vibrate(500);
		// 第一个参数，指代一个震动的频率数组。每两个为一组，每组的第一个为等待时间，第二个为震动时间。
		// 比如 [2000,500,100,400],会先等待2000毫秒，震动500，再等待100，震动400
		// 第二个参数，repest指代从 第几个索引（第一个数组参数） 的位置开始循环震动。
		// 会一直保持循环，我们需要用 vibrator.cancel()主动终止
		// vibrator.vibrate(new long[]{300,500},0);

	}

	// public static void Play(int sound) {
	// Play();
	// }
}

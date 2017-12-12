package yangTalkback.Codec;

import yangTalkback.Comm.*;
import yangTalkback.Media.MediaFrame;

import com.ryong21.encode.Speex;

import AXLib.Utility.IDisposable;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.Ex.FunEx;

//SPEEX½âÂëÆ÷
public class SpeexDecode implements IDisposable {

	private Speex speex = null;
	private int samples = -1;

	public SpeexDecode() {
		if (speex == null) {
			speex = new Speex(4);
			samples = 160;
		}
	}

	public short[] Deocde(MediaFrame mf) {

		short[] buf = new short[samples];
		try {
			int decSize = speex.decode(speex.pSpx, mf.Data, buf, mf.nSize);
			buf = FunEx.ArrayResize(buf, 0, decSize);
			// speex.decode(mf.Data, buf, mf.nSize);
		} catch (Throwable e) {
			CLLog.Error(e);
			throw RuntimeExceptionEx.Create("½âÂëÉùÒô³ö´í");
			// speex.decode(mf.Data, buf, mf.nSize);
		}
		return buf;
	}

	@Override
	public void Dispose() {
		if (speex != null)
			speex.close(speex.pSpx);
	}

}

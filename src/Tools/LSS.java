package Tools;

import java.io.DataInputStream;
import java.io.FileDescriptor;
import java.io.IOException;

import AXLib.Utility.IDisposable;
import AXLib.Utility.RuntimeExceptionEx;
import AXLib.Utility.TH;
import android.R.string;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;

public class LSS implements IDisposable {
	private String name;
	private LocalSocket receiver, sender;
	
	private DataInputStream dataInputStream;
	public LocalServerSocket lss;
	public LSS(String name) throws IOException {
		this.name = name;
		initLocalSocket();
	}

	private void initLocalSocket() throws IOException {
		 
		lss = new LocalServerSocket(name);
		receiver = new LocalSocket();
		receiver.connect(new LocalSocketAddress(name));
		int buf_size = 500 * 1024;
		receiver.setReceiveBufferSize(buf_size);
		receiver.setSendBufferSize(buf_size);
		sender = lss.accept();
		sender.setReceiveBufferSize(buf_size);
		sender.setSendBufferSize(buf_size);
		dataInputStream = new DataInputStream(receiver.getInputStream());

	}

	public FileDescriptor getFileDescriptor() {
		return sender.getFileDescriptor();
	}

	public DataInputStream getReceiverStream() {
		return dataInputStream;
	}

	@Override
	public void Dispose() {
		try {
			 
			lss.close();
			receiver.close();
			sender.close();
			dataInputStream.close();
		} catch (IOException e) {
			String string = RuntimeExceptionEx.GetStackTraceString(e);
			RuntimeExceptionEx.PrintException(e);
		}

	}
}

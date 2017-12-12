package com.example.volumekey;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public abstract class AExecuteAsRoot {
	private static final String TAG = "com.example.suapplication.AExecuteAsRoot";


    public static boolean execute(List<String> commands) {
        boolean retval = false;

        try {
            if (null != commands && commands.size() > 0) {
                Process process = Runtime.getRuntime().exec("su");

                DataOutputStream os = new DataOutputStream(process.getOutputStream());

                for (String currCommand : commands) {
                    os.writeBytes(currCommand + "\n");
                    os.flush();
                }

                os.writeBytes("exit\n");
                os.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        process.getInputStream()));
                int read;
                char[] buffer = new char[4096];
                StringBuffer output = new StringBuffer();
                while ((read = reader.read(buffer)) > 0) {
                    output.append(buffer, 0, read);
                }
                reader.close();

                try {
                    int suProcessRetval = process.waitFor();
                    if (0 == suProcessRetval) {
                        retval = true;
                    } else {
                        retval = false;
                    }
                    Log.d(TAG, "output: "+output.toString()) ;
                    dump_output(output.toString());
                } catch (Exception ex) {
                    Log.e(TAG, "Error executing root action", ex);
                }
            }
        } catch (IOException ex) {
            Log.w(TAG, "Can't get root access", ex);
        } catch (SecurityException ex) {
            Log.w(TAG, "Can't get root access", ex);
        } catch (Exception ex) {
            Log.w(TAG, "Error executing internal operation", ex);
        }

        return retval;
    }

    private static void dump_output(String info) {
		// TODO Auto-generated method stub
		File log_file = new File("/sdcard/log_geng.txt");
		try {
			if (!log_file.exists()) {
					log_file.createNewFile();
			}
			
			FileWriter fw = new FileWriter(log_file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(info);
			bw.close();
		} catch(Exception e) {
			Log.v("gengj", "log file write failed\n");
		}
    }

}

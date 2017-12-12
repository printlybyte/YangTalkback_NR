package com.example.volumekey;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class LogFile {
	
	public static void writeFile(String str) {
		try
        {
        String path="/sdcard/volume_debug.txt";
        File file=new File(path);
//        if(!file.exists())
//            file.createNewFile();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        FileOutputStream out=new FileOutputStream(file,true); //濡傛灉杩藉姞鏂瑰紡鐢╰rue   
        
        StringBuffer sb=new StringBuffer();
        
        sb.append(sdf.format(new Date()) + "===");
        sb.append(str + "\n\n");
        Log.v("gengj==========", sb.toString());
        out.write(sb.toString().getBytes("utf-8"));//娉ㄦ剰闇�杞崲�?瑰簲鐨勫瓧绗﹂泦
        out.close();
        }
        catch(IOException ex)
        {
            System.out.println(ex.getStackTrace());
        }
	}

}

package com.dteviot.epubviewer;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by hyejin on 2017-04-16.
 */

public class FileHelper {
    private static final int BUFFER_SIZE = 8192;//2048;
    private static String TAG = FileHelper.class.getName().toString();
   // private static String parentPath = "";



    public static Boolean unzip(String sourceFile, String destinationFolder) {
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(sourceFile)));
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[BUFFER_SIZE];
            String sourceTemp = sourceFile;
            int pos = sourceTemp.indexOf("/Download/")+ 10;
            String posString = sourceTemp.substring(pos);
            String completeString = posString.substring(0,posString.lastIndexOf("."));
            File file_TEMP = new File(destinationFolder,completeString);
            file_TEMP.mkdirs();
            while ((ze = zis.getNextEntry()) != null) {
                String fileName = ze.getName();
                fileName =  completeString+ "/" + fileName;
                File file = new File(destinationFolder, fileName);
                File dir = ze.isDirectory() ? file : file.getParentFile();
                Log.d("인식된 파일경로이름: ",fileName);

                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Invalid path: " + dir.getAbsolutePath());
                if (ze.isDirectory()){
                    if(!file.isDirectory()){
                        file.mkdirs();
                    }
                }else {
                    File parentDir = file.getParentFile();
                    if(null != parentDir){
                        if(!parentDir.isDirectory()){
                            parentDir.mkdirs();
                        }
                    }
                }
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1){
                        fout.write(buffer, 0, count);
                    }
                } finally {
                    fout.close();
                }
            }
        } catch (IOException ioe) {
            Log.d(TAG, ioe.getMessage());
            return false;
        } finally {
            if (zis != null)
                try {
                    zis.close();
                } catch (IOException e) {

                }
        }
        return true;

    }
}

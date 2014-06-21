package com.happysanta.crazytyping.Core;

import android.os.Environment;
import android.util.Log;


import com.happysanta.crazytyping.Helper.Time;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;


public class Logs {
    public static final String SEND =
            "Logs.SEND";

    public static String getLog() {
        String logString = "";
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log = new StringBuilder();
            log.append("File created at " +  new Date());
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append("\n" +line);
            }

            logString = log.toString();
        } catch (IOException e) {
            logString = "can't read logs: " + e.getMessage();
        }
        return logString;
    }

    public static File getFile() {
        String logString = getLog();
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File (sdCard.getAbsolutePath() + "/vkspy/logs");
        dir.mkdirs();
        File file = new File(dir, "lastSaved.txt");

        try {
            //to write logcat in text file
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            // Write the string to the file
            osw.write(logString);
            osw.flush();
            osw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
    public static void removeLogs() {
        try {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + "/vkspy/logs/auto");
            //dir.mkdirs();
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
            if (dir.delete())

                Log.i("AGCY SPY", "Logs have been deleted");
            else

                Log.i("AGCY SPY", "Logs have not been deleted");
        } catch (Exception exp) {
            Log.i("AGCY SPY", "Logs have not been deleted");
        }
    }
    public static void saveNewFile() {

        String logString = getLog();
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File (sdCard.getAbsolutePath() + "/vkspy/logs/auto");
        dir.mkdirs();
        File file = new File(dir, "logcat"+ Time.getUnixNow()+".txt");

        try {
            //to write logcat in text file
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            // Write the string to the file
            osw.write(logString);
            osw.flush();
            osw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getFile();
    }
}


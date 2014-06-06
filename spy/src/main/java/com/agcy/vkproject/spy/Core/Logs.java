package com.agcy.vkproject.spy.Core;

import android.os.Environment;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


public class Logs {
    public static final String SEND =
            "com.agcy.vkproject.spy.Core.Logs.SEND";

    public static String getLog() {
        String logString = "";
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append("\n"+line);
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
        File dir = new File (sdCard.getAbsolutePath() + "/myLogcat");
        dir.mkdirs();
        File file = new File(dir, "logcat.txt");

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
}


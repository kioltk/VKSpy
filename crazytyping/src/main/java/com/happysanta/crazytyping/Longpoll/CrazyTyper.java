package com.happysanta.crazytyping.Longpoll;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.happysanta.crazytyping.Core.Memory;
import com.happysanta.crazytyping.Models.Dialog;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class CrazyTyper extends AsyncTask<Void, String, String> {


    private static final String LOG_TAG = "CRAZYTYPING TYPER";

    @Override
    protected String doInBackground(Void... params) {
        ArrayList<Dialog> targetedDialogs = Memory.getTargetedDialogs();
        if(targetedDialogs.isEmpty())
            return null;
        Boolean first = true;
        try {
            // цикл 10 секунд. если с последнего цикла прошло меньше, то слипим, до 10 секунд
            while (true) {
                if(isCancelled())
                    return null;
                long lastUpdate = System.currentTimeMillis();
                long timeLast = System.currentTimeMillis() - lastUpdate;
                if (!first && timeLast < 8 * 1000L) {
                    Thread.sleep(8 * 1000L - timeLast);
                }
                first = false;
                if(Memory.targetChanged){
                    targetedDialogs = Memory.getTargetedDialogs();
                    Memory.targetChanged = false;
                }


                // собираем диалоги пачками по 25, отправляем на сервер, пока не закончатся пачки
                ArrayList<String> codesPack = new ArrayList<String>();
                ArrayList<String> idsPacks = new ArrayList<String>();
                int i = 0;
                String codePack = "";
                String idsPack = "";
                for (Dialog targetedDialog : targetedDialogs) {
                    if(i == 25) {
                        i = 0;
                        codePack += "return i;";
                    }
                    if(i == 0){
                        if(!codePack.equals("")) {
                            codesPack.add(codePack);
                            idsPacks.add(idsPack);
                        }
                        codePack = "var i = 0;";
                        idsPack = "";

                    }
                    long id =  targetedDialog.getId();
                    String target = "\""+(id>2000000000? "chat_id\":"+(id-2000000000):"user_id\":"+id);
                    idsPack += id+",";
                    codePack += "i = i + API.messages.setActivity({\"type\":\"typing\","+target+ "});";
                    i++;
                }
                if(!codePack.equals("")){
                    codePack += "return i;";
                    codesPack.add(codePack);
                    idsPacks.add(idsPack);
                }
                Log.i(LOG_TAG,"We have "+codesPack.size()+" packs.");
                for ( i = 0; i < codesPack.size(); i++) {
                    codePack = codesPack.get(i);
                    idsPack = idsPacks.get(i);
                    VKParameters typingParams = new VKParameters();
                    typingParams.put("code", codePack);

                    new VKRequest("execute", typingParams).executeWithListener(new VKRequest.VKRequestListener() {

                        @Override
                        public void onComplete(VKResponse response) {
                            Log.i(LOG_TAG,"Pack typing sended");
                        }

                        @Override
                        public void onError(VKError error) {
                            Log.e(LOG_TAG,"Pack typing error");
                        }

                    });
                    onProgressUpdate(idsPack);
                }



            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        Memory.notifyTypingUpdate(values[0].split(","));

    }

    public void start(){
        execute();
    }
    public void cancel(){
        cancel(true);
    }


}

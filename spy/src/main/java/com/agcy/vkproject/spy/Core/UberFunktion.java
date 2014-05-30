package com.agcy.vkproject.spy.Core;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.agcy.vkproject.spy.Models.DurovOnline;
import com.agcy.vkproject.spy.R;
import com.agcy.vkproject.spy.UserActivity;
import com.bugsense.trace.BugSenseHandler;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.UnknownHostException;
import java.util.ArrayList;



public class UberFunktion {
    private static Context context;
    private static ProgressDialog dialog;
    private static int lastId;
    private static boolean updateOnly;

    /**
     * Прежде чем смотреть на этот ужас, возьмите попкорн - это надолго.
     * @param uberfunctionDialog
     */
    public static void initialize(final ProgressDialog uberfunctionDialog) {

        dialog = uberfunctionDialog;


        context = uberfunctionDialog.getContext();
        SharedPreferences durovPreferences = context.getSharedPreferences("durov", Context.MODE_MULTI_PROCESS);
        lastId =  durovPreferences.getInt("lastId",0);
        updateOnly = durovPreferences.getBoolean("loaded", false);

        if(updateOnly){
            dialog.setMessage(context.getString(R.string.durov_function_updating));
        }

        new Loader() {
            @Override
            public void onComplete(String finalResponse) {
                Log.i("AGCY SPY FEATURE",finalResponse);
                final Handler handler = new Handler();
                uberfunctionDialog.setMessage(context.getString(R.string.durov_ready));
                new Saver(finalResponse) {
                    @Override
                    protected void onSuccess() {
                        VKParameters parameters = new VKParameters();
                        parameters.put(VKApiConst.USER_IDS, 1);
                        parameters.put("fields", "sex,photo_200,photo_200_orig,photo_50,photo_100");

                        VKRequest donwloadUserRequest = VKApi.users().get(parameters);
                        donwloadUserRequest.executeWithListener(
                                new VKRequest.VKRequestListener() {
                                    @Override
                                    public void onComplete(final VKResponse response) {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {

                                                if(!updateOnly) {
                                                    VKApiUserFull durov = ((VKList<VKApiUserFull>) response.parsedModel).get(0);


                                                    durov.isFriend = true;
                                                    durov.tracked = true;
                                                    Memory.saveUser(durov);
                                                    SharedPreferences.Editor durovPreferences = context.getSharedPreferences("durov", Context.MODE_MULTI_PROCESS).edit();
                                                    durovPreferences.putBoolean("loaded", true).commit();
                                                }
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        AlertDialog.Builder successDialog = new AlertDialog.Builder(uberfunctionDialog.getContext());
                                                        successDialog.setCancelable(true);

                                                        successDialog.setTitle((R.string.durov_function_activated_title));
                                                        successDialog.setMessage(updateOnly ? (R.string.durov_function_updated) : (R.string.durov_function_activated));
                                                        successDialog.setPositiveButton("Хочу посмотреть!", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Intent showDurov = new Intent(context, UserActivity.class);
                                                                Bundle bundle = new Bundle();
                                                                bundle.putInt("id", 1);
                                                                showDurov.putExtras(bundle);
                                                                context.startActivity(showDurov);
                                                            }
                                                        });
                                                        successDialog.setNegativeButton("Закрыть", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {

                                                            }
                                                        });
                                                        Memory.reloadFriends();
                                                        successDialog.show();
                                                        uberfunctionDialog.dismiss();
                                                    }
                                                });

                                            }
                                        }).start();
                                    }

                                    @Override
                                    public void onError(VKError error) {
                                        uberfunctionDialog.setTitle("Error");
                                        uberfunctionDialog.setMessage("Нет соединения с вк");
                                        uberfunctionDialog.setIndeterminate(false);
                                        uberfunctionDialog.setCancelable(true);

                                    }
                                }
                        );

                    }

                    @Override
                    protected void onError(Exception exp) {
                        uberfunctionDialog.setTitle("Error");
                        uberfunctionDialog.setMessage("Не получилось сохранить");
                    }
                }.execute();
            }

            @Override
            public void onError(Exception exp) {

                uberfunctionDialog.setTitle(R.string.error);
                if(exp==null){
                    uberfunctionDialog.setMessage(context.getString(R.string.server_did_not_respond));
                }else{
                    if(exp instanceof UnknownHostException){
                        uberfunctionDialog.setMessage(context.getString(R.string.check_connection));
                    }else{

                        uberfunctionDialog.setMessage(context.getString(R.string.unknown_error));
                    }
                }
                uberfunctionDialog.setIndeterminate(false);
                uberfunctionDialog.setCancelable(true);

            }
        }.execute();

    }

    private abstract static class Loader{
        private final int count;
        private final int offset;
        private boolean executed = false;
        public Loader(){
            this(100,0);
        }
        public Loader(int count, int offset){
            this.count = count;
            this.offset = offset;
        }
        public void execute(){
            if(executed)
                return;
            executed = true;
            final Handler handler = new Handler();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String response = "";
                    try{
                        String url = Helper.getUberFunctionUrl();
                        String finalUrl = String.format(url, count, offset);
                        DefaultHttpClient httpClient = new DefaultHttpClient();
                        HttpGet get = new HttpGet(finalUrl);
                        HttpResponse httpResponse = httpClient.execute(get);
                        HttpEntity httpEntity = httpResponse.getEntity();
                        response = EntityUtils.toString(httpEntity);
                        Thread.sleep(500);
                        Log.i("AGCY SPY", "UberFunction responses");

                    }catch (final Exception exp){

                        Log.e("AGCY SPY FEATURE","", exp);
                        BugSenseHandler.sendException(exp);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onError(exp);
                            }
                        });
                        return;
                    }
                    if(response.equals("")) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onError(null);
                            }
                        });
                        return;
                    }

                    final String finalResponse = response;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onComplete(finalResponse);
                        }
                    });
                }
            }).start();
        }
        public abstract void onComplete(String finalResponse);
        public abstract void onError(Exception exp);
    }
    private abstract static class Saver{

        private final String response;

        public Saver(String response){
            this.response = response;
        }
        public void execute(){
            final Handler handler = new Handler();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        Thread.sleep(500);
                        ArrayList<DurovOnline> onlines = new ArrayList<DurovOnline>();
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray jsonArray = jsonResponse.getJSONArray("response");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = (JSONObject) jsonArray.get(i);
                            DurovOnline online = new DurovOnline(object.getInt("id"),object.getInt("from"),object.getInt("to"));
                            if(i==0){

                                int lastIdTemp = online.to == 0 ? online.id - 1 : online.id;
                                SharedPreferences.Editor durovPreferences = context.getSharedPreferences("durov", Context.MODE_MULTI_PROCESS).edit();
                                durovPreferences.putInt("lastId", lastIdTemp).commit();
                            }
                            if(online.id<=lastId)
                                break;
                            onlines.add(0,online);

                        }
                        Memory.saveDurovOnlines(onlines);
                    }catch (final Exception exp){
                        Log.e("AGCY SPY FEATURE", "", exp);
                        BugSenseHandler.sendException(exp);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                onError(exp);
                            }
                        });
                        return;
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            onSuccess();
                        }
                    });
                }
            }).start();

        }

        protected abstract void onSuccess();

        protected abstract void onError(Exception exp);
    }
}

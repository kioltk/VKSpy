package com.happysanta.vkspy.Core;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.happysanta.vkspy.Helper.Time;
import com.happysanta.vkspy.Models.DurovOnline;
import com.happysanta.vkspy.R;
import com.bugsense.trace.BugSenseHandler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;



public class UberFunktion {
    private static Context context;
    private static ProgressDialog dialog;
    private static int lastId;
    private static boolean updateOnly;
    private static boolean online;
    public static boolean loading = false;

    /**
     *
     * Прежде чем смотреть на этот ужас, возьмите попкорн - это надолго.
     *
     */
    public static boolean putNewDialogWindow(final ProgressDialog uberfunctionDialog){

        dialog = uberfunctionDialog;

        if(context==null ) {
            if (uberfunctionDialog != null)
                context = uberfunctionDialog.getContext();
            else
                return false;
        }
        return true;
    }
    public static void initializeBackground(Context context){
        UberFunktion.context = context;
        initialize(null);
    }

    public static void initialize(ProgressDialog uberfunctionDialog) {
        return;
        /*
        Log.i("AGCY SPY FEATURE","New feature request");

        if(!putNewDialogWindow(uberfunctionDialog))
            return;

        if(loading)
            return;
        loading = true;

        online = false;
        Log.i("AGCY SPY FEATURE","Start");
        SharedPreferences durovPreferences = context.getSharedPreferences("durov", Context.MODE_MULTI_PROCESS);
        lastId =  durovPreferences.getInt("lastId",0);
        updateOnly = durovPreferences.getBoolean("loaded", false) || Memory.users.getById(1)!=null;

        if(updateOnly){
            if(UberFunktion.dialog !=null)
                UberFunktion.dialog.setMessage(context.getString(R.string.durov_function_updating));
        }

        new Loader() {




            @Override
            public void onComplete(String finalResponse) {
                Log.i("AGCY SPY FEATURE",finalResponse);
                final Handler handler = new Handler();
                if(dialog!=null)
                    dialog.setMessage(context.getString(R.string.durov_function_almost));
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
                                                        loading = false;
                                                        if(dialog!=null) {
                                                            AlertDialog.Builder successDialogBuilder = new AlertDialog.Builder(dialog.getContext());
                                                            successDialogBuilder.setCancelable(true);

                                                            successDialogBuilder.setTitle((R.string.durov_function_activated_title));
                                                            TextView resultView = new TextView(context);
                                                            resultView.setTextColor(0xff333333);
                                                            resultView.setTextSize(16);
                                                            resultView.setPadding(0, Helper.convertToDp(20), 0, Helper.convertToDp(20));
                                                            resultView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                                                            resultView.setGravity(Gravity.CENTER);
                                                            resultView.setText(updateOnly ? (R.string.durov_function_updated) : (R.string.durov_function_activated));
                                                            successDialogBuilder.setView(resultView);
                                                            successDialogBuilder.setPositiveButton(context.getString(R.string.durov_wanna_see), new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    Intent showDurov = new Intent(context, UserActivity.class);
                                                                    Bundle bundle = new Bundle();
                                                                    bundle.putInt("id", 1);
                                                                    showDurov.putExtras(bundle);
                                                                    showDurov.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                    context.startActivity(showDurov);
                                                                }
                                                            });
                                                            successDialogBuilder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                }
                                                            });
                                                            AlertDialog successDialog = successDialogBuilder.create();
                                                            successDialog.setCanceledOnTouchOutside(true);
                                                            successDialog.show();
                                                            try {
                                                                dialog.dismiss();
                                                            }catch (Exception exp){

                                                            }
                                                        }

                                                        if(online){
                                                            Notificator.notifyDurov();
                                                        }
                                                        if(!updateOnly) {
                                                            Memory.reloadFriends();
                                                            Helper.trackedUpdated();
                                                        }
                                                    }
                                                });

                                            }
                                        }).start();
                                    }

                                    @Override
                                    public void onError(VKError error) {
                                        loading = false;
                                        if(dialog!=null) {
                                            try {
                                                showError(dialog, context.getString(R.string.error), context.getString(R.string.vk_down))
                                                        .show();
                                                dialog.dismiss();
                                            }catch (Exception exp){

                                            }
                                        }

                                    }
                                }
                        );

                    }

                    @Override
                    protected void onError(Exception exp) {
                        loading = false;
                        if(dialog!=null) {
                            try {
                                showError(dialog, context.getString(R.string.error), context.getString(R.string.unknown_error));
                            } catch (Exception exception) {

                            }
                        }
                    }
                }.execute();
            }

            @Override
            public void onError(Exception exp) {
                loading = false;
                if(dialog!=null) {
                    try {
                        AlertDialog errorDialog = showError(dialog, context.getString(R.string.error), context.getString(R.string.vk_down));

                        dialog.dismiss();
                        if (exp == null) {
                            errorDialog.setMessage(context.getString(R.string.server_did_not_respond));
                        } else {
                            if (exp instanceof UnknownHostException) {
                                errorDialog.setMessage(context.getString(R.string.check_connection));
                            } else {
                                errorDialog.setMessage(context.getString(R.string.unknown_error));
                            }
                        }
                        errorDialog.setCancelable(true);
                        errorDialog.setCanceledOnTouchOutside(true);
                        errorDialog.show();
                    }catch(Exception exception){

                    }
                }

            }
        }.execute();
        */
    }
    private static AlertDialog showError(Dialog dialog,String title, String message) {
        try {
            Activity activity = dialog.getOwnerActivity();
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(title);
            builder.setMessage(message);
            return builder.create();
        } catch(Exception exp){
            BugSenseHandler.sendException(exp);
        }
        return null;
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
        public void execute() {
            if (executed)
                return;
            executed = true;
            final Handler handler = new Handler();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String response = "";
                    try {
                        String url = Helper.getUberFunctionUrl();
                        String finalUrl = String.format(url, count, offset);
                        DefaultHttpClient httpClient = new DefaultHttpClient();
                        HttpGet get = new HttpGet(finalUrl);
                        HttpResponse httpResponse = httpClient.execute(get);
                        HttpEntity httpEntity = httpResponse.getEntity();
                        response = EntityUtils.toString(httpEntity);
                        Thread.sleep(500);
                        Log.i("AGCY SPY", "UberFunction responses");

                    } catch (final Exception exp) {

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

                    final String finalResponse = response;
                    try {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onComplete(finalResponse);
                            }
                        });

                        return;
                    } catch (Exception exp) {

                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onError(null);
                        }
                    });

                }
            }).start();
        }
        public void reexecute(){
            if(executed)
                executed = false;
            execute();
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
                                durovPreferences.putInt("lastId", lastIdTemp);
                                durovPreferences.putInt("lastUpdate", Time.getUnixNow());
                                durovPreferences.commit();
                                if(online.to==0){
                                    UberFunktion.online = true;
                                }
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

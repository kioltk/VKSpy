package com.agcy.vkproject.spy.Longpoll;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by kiolt_000 on 26-Apr-14.
 */
public abstract class LongPollConnection extends AsyncTask<Void, Void, String> {
    private final String server;
    private final String key;
    private final String ts;
    String url = "http://%s?act=a_check&key=%s&ts=%s&wait=25&mode=64";
    private Exception exception;
    private boolean finished = false;

    public LongPollConnection(String server, String key, String ts){
        this.server = server;
        this.key = key;
        this.ts = ts;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            String response = "";
            String finalUrl = String.format(url,server,key,ts);
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet(finalUrl);

            HttpResponse httpResponse = httpClient.execute(get);
            HttpEntity httpEntity = httpResponse.getEntity();
            response = EntityUtils.toString(httpEntity);
            Log.i("AGCY SPY","longoll responses");
            if(!isCancelled())
                return response;
        }catch (Exception exp){
            this.exception = exp;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String jsonResponse) {
        finished= true;
        if(jsonResponse!=null)
            try {
                JSONObject response = new JSONObject(jsonResponse);
                if (response.has("failed")) {
                    throw new ServerException();
                }
                String ts = response.getString("ts");
                JSONArray updates = response.getJSONArray("updates");
                onSuccess(updates, ts);
                return;
            } catch (Exception e) {
                this.exception = e;
            }
        else
            this.exception = new ConnectionLostException();
        onError(exception);
    }


    public abstract void onSuccess(JSONArray updates, String ts);
    public abstract void onError(Exception exp);

    public boolean isFinished() {
        return finished;
    }

    public static class ServerException extends Exception {
    }

    private class ConnectionLostException extends Exception {
    }
}

package com.happysanta.vkspy.Core;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.happysanta.vkspy.Helper.Type;
import com.happysanta.vkspy.Longpoll.LongPollService;
import com.happysanta.vkspy.MainActivity;
import com.happysanta.vkspy.R;
import com.happysanta.vkspy.WelcomeActivity;
import com.bugsense.trace.BugSenseHandler;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCaptchaDialog;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.api.VKError;

/**
 * Created by kiolt_000 on 27-Apr-14.
 */
public class VKSdk {



    private static final String VK_SDK_ACCESS_TOKEN_PREF_KEY = "VK_SDK_ACCESS_TOKEN_PLEASE_DONT_TOUCH";
    private static final String[] sMyScope = new String[] {
            VKScope.FRIENDS,
            VKScope.MESSAGES
    };
    private static Context context;

    public static void DESTROY() {
        com.vk.sdk.VKSdk.logout();
        context = null;
    }
    public static void initialize(Context context){
        VKSdk.context = context;
        com.vk.sdk.VKSdk.initialize(sdkBackgroundListener, "4328079",
                VKAccessToken.tokenFromSharedPreferences(context, VK_SDK_ACCESS_TOKEN_PREF_KEY));
    }
    public static void authorize(){
        com.vk.sdk.VKSdk.authorize(sMyScope, true, true);
    }
    public static void authorizeFirst(final Activity firstActivity) {
        final com.vk.sdk.VKSdk vksdk = com.vk.sdk.VKSdk.instance();
                vksdk.setSdkListener(new VKSdkListener() {
                    @Override
                    public void onCaptchaError(VKError captchaError) {

                    }

                    @Override
                    public void onTokenExpired(VKAccessToken expiredToken) {

                    }

                    @Override
                    public void onAccessDenied(VKError authorizationError) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        switch (authorizationError.errorCode){
                            case VKError.VK_API_ERROR:
                                builder.setMessage(R.string.need_access);
                                break;
                        }
                                //builder.show();
                        //com.vk.sdk.VKSdk.authorize(sMyScope);
                        BugSenseHandler.sendEvent("Access denied");
                    }

                    @Override
                    public void onRenewAccessToken(VKAccessToken token) {
                        super.onRenewAccessToken(token);

                    }

                    @Override
                    public void onReceiveNewToken(VKAccessToken newToken) {

                        SharedPreferences prefs = context.getSharedPreferences("user", context.MODE_MULTI_PROCESS);
                        int userid  = prefs.getInt("id", 0);
                        if(!newToken.userId.equals(String.valueOf(userid))){
                            Memory.clearAll();
                        }

                        startMainActivity();
                        firstActivity.finish();

                        vksdk.setSdkListener(sdkBackgroundListener);
                    }

                });
        com.vk.sdk.VKSdk.authorize(sMyScope, true, true);

    }
    private static final VKSdkListener sdkBackgroundListener = new VKSdkListener() {
        @Override
        public void onCaptchaError(VKError captchaError) {
            new VKCaptchaDialog(captchaError).show();
            BugSenseHandler.sendEvent("Captcha error");
        }

        @Override
        public void onTokenExpired(VKAccessToken expiredToken) {
            com.vk.sdk.VKSdk.authorize(sMyScope);
            BugSenseHandler.sendEvent("Token expired");
        }

        @Override
        public void onAccessDenied(VKError authorizationError) {
            new AlertDialog.Builder(context)
                    .setMessage(authorizationError.errorMessage)
                    .show();

            BugSenseHandler.sendEvent("Access denied");
            if(!authorizationError.errorReason.equals("user_denied")) {
                context.startActivity(new Intent(context, WelcomeActivity.class));
                Helper.stopLongpoll();
            }
        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
            startMainActivity();

        }

        @Override
        public void onAcceptUserToken(VKAccessToken token) {
        }

        @Override
        public void onLogout() {

            BugSenseHandler.sendEvent("Logout");

            Intent intent = new Intent(context, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            Notificator.clearNotifications();

            Memory.clearAll();
            context.deleteDatabase("webview.db");
            context.deleteDatabase("webviewCache.db");
            context.deleteDatabase("webviewCookiesChromium.db");
            context.deleteDatabase("webviewCookiesChromiumPrivate.db");

            Log.i("AGCY SPY", "Databases deleted");

            Intent stopLongpoll = new Intent(context, LongPollService.class);
            Bundle bundle = new Bundle();
            bundle.putInt(LongPollService.ACTION, LongPollService.ACTION_LOGOUT);
            stopLongpoll.putExtras(bundle);
            context.startService(stopLongpoll);

            Type.stopTimers();
            Helper.stopMainActivity();
            Helper.clearAllPreferences();

        }
    };
    private static void startMainActivity() {
        Intent startMain = new Intent(context, MainActivity.class);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startMain);
    }


    public static void logout() {
        com.vk.sdk.VKSdk.logout();
    }
}

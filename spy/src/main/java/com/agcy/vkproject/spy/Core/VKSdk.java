package com.agcy.vkproject.spy.Core;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import com.agcy.vkproject.spy.MainActivity;
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
        com.vk.sdk.VKSdk.initialize(sdkListener, "4328079");
    }
    public static void initializeBackground(Context context){
        VKSdk.context = context;
        com.vk.sdk.VKSdk.initialize(sdkBackgroundListener, "4328079",
                VKAccessToken.tokenFromSharedPreferences(context, VK_SDK_ACCESS_TOKEN_PREF_KEY));
    }
    public static void authorize(){
        com.vk.sdk.VKSdk.authorize(sMyScope, true, true);
    }
    private static final VKSdkListener sdkBackgroundListener = new VKSdkListener() {
        @Override
        public void onCaptchaError(VKError captchaError) {
            new VKCaptchaDialog(captchaError).show();
        }

        @Override
        public void onTokenExpired(VKAccessToken expiredToken) {
            com.vk.sdk.VKSdk.authorize(sMyScope);
        }

        @Override
        public void onAccessDenied(VKError authorizationError) {
            new AlertDialog.Builder(context)
                    .setMessage(authorizationError.errorMessage)
                    .show();
        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
        }

        @Override
        public void onAcceptUserToken(VKAccessToken token) {
        }
    };
    private static final VKSdkListener sdkListener = new VKSdkListener() {
        @Override
        public void onCaptchaError(VKError captchaError) {
            new VKCaptchaDialog(captchaError).show();
        }

        @Override
        public void onTokenExpired(VKAccessToken expiredToken) {
            com.vk.sdk.VKSdk.authorize(sMyScope);
        }

        @Override
        public void onAccessDenied(VKError authorizationError) {
            new AlertDialog.Builder(context)
                    .setMessage(authorizationError.errorMessage)
                    .show();
        }

        @Override
        public void onReceiveNewToken(VKAccessToken newToken) {
            startMainActivity();
        }

        @Override
        public void onAcceptUserToken(VKAccessToken token) {

            startMainActivity();
        }
    };
    private static void startMainActivity() {
        context.startActivity(new Intent(context, MainActivity.class));
    }

}

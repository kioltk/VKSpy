package com.agcy.vkproject.spy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.Core.Notificator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCaptchaDialog;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKSdkListener;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKError;
import com.vk.sdk.util.VKUtil;

public class StartActivity extends Activity {


    private static final String[] sMyScope = new String[] {
            VKScope.FRIENDS,
            VKScope.MESSAGES
    };


    private final VKSdkListener sdkListener = new VKSdkListener() {
        @Override
        public void onCaptchaError(VKError captchaError) {
            new VKCaptchaDialog(captchaError).show();
        }

        @Override
        public void onTokenExpired(VKAccessToken expiredToken) {
            VKSdk.authorize(sMyScope);
        }

        @Override
        public void onAccessDenied(VKError authorizationError) {
            new AlertDialog.Builder(StartActivity.this)
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Memory.initialize(getApplicationContext());
        Notificator.initialize(getApplicationContext());

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
        ImageLoader.getInstance().init(config);

        VKUIHelper.onCreate(this);
        VKSdk.initialize(sdkListener, "4328079");
        if (VKSdk.wakeUpSession()) {
            startMainActivity();
            finish();
            return;
        }
        //
        String[] fingerprint = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        Log.d("Fingerprint", fingerprint[0]);


        //todo: show start!
    }




    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKUIHelper.onActivityResult(requestCode, resultCode, data);
    }




    private void showStart() {

        SharedPreferences preferences = getSharedPreferences("start", MODE_MULTI_PROCESS);
        preferences.edit().putBoolean("firstStart",false).commit();
    }

    public void login(View view) {
        VKSdk.authorize(sMyScope,true,true);
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }


}

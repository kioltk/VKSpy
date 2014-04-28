package com.agcy.vkproject.spy;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.Core.Notificator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.util.VKUtil;

public class StartActivity extends Activity {





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Memory.initialize(getApplicationContext());
        Notificator.initialize(getApplicationContext());

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
        ImageLoader.getInstance().init(config);

        VKUIHelper.onCreate(this);
        com.agcy.vkproject.spy.Core.VKSdk.initialize(this);
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
        com.agcy.vkproject.spy.Core.VKSdk.authorize();
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }


}

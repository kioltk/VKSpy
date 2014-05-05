package com.agcy.vkproject.spy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.agcy.vkproject.spy.Core.Helper;
import com.vk.sdk.VKUIHelper;

public class StartActivity extends Activity {





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Helper.initialize(getApplicationContext());

        VKUIHelper.onCreate(this);
        com.agcy.vkproject.spy.Core.VKSdk.initialize(this);



        com.agcy.vkproject.spy.Core.VKSdk.authorize();

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





    public void login(View view) {
        com.agcy.vkproject.spy.Core.VKSdk.authorize();
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }


}

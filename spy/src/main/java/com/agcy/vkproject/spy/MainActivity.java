package com.agcy.vkproject.spy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.agcy.vkproject.spy.Core.Memory;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKUsersArray;

public class MainActivity extends Activity {


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        VKUIHelper.onCreate(this);
        {
            VKRequest request = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "photo_100,online,last_seen"));
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    showFriends((VKUsersArray) response.parsedModel);
                }
            });
        }
        {

        }
    }

    public void showFriends(VKUsersArray friends) {
        Memory.friends = friends;
    }

    public void testPopup(View view) {
        Toast toast = new Toast(getBaseContext());

        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.popup, null);

        ImageView photo = (ImageView) rootView.findViewById(R.id.photo);
        TextView name = (TextView) rootView.findViewById(R.id.name);
        TextView message = (TextView) rootView.findViewById(R.id.message);
        //VKApiUser user = (VKApiUser) getItem(position);
        name.setText("Парень с тележкой");
        message.setText("Привёз товар");
        ImageLoader.getInstance().displayImage("http://cs614918.vk.me/v614918442/719a/wiF2wt1ssEc.jpg",photo);

        toast.setView(rootView);
        toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM,0,100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
    public void showFriends(View view) {
        showFriends();
    }
    public void showFriends() {
        startActivity(new Intent(this, FriendsActivity.class));
    }

}

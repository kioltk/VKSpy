package com.agcy.vkproject.spy;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.Fragments.FriendsListFragment;
import com.agcy.vkproject.spy.Fragments.UserFragment;
import com.vk.sdk.api.model.VKApiUser;


public class FriendsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        findViewById(android.R.id.content).setBackgroundColor(Color.rgb(240, 242, 245));

        if (savedInstanceState == null) {
            FriendsListFragment friendsList = new FriendsListFragment(Memory.friends, getBaseContext());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, friendsList)
                    .commit();
            friendsList.setOnUserSelected(new FriendsListFragment.OnSelectedListener() {

                @Override
                public void onSelect(VKApiUser user) {
                    showUser(user);
                }
            });
        }
    }
    public void showUser(VKApiUser user){
        UserFragment userFragment = new UserFragment(user);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, userFragment)
                .commit();
    }


}

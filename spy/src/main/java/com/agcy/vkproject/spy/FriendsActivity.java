package com.agcy.vkproject.spy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.Fragments.FriendsListFragment;
import com.vk.sdk.api.model.VKApiUser;


public class FriendsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        findViewById(android.R.id.content).setBackgroundColor(Color.rgb(240, 242, 245));

        if (savedInstanceState == null) {
            FriendsListFragment friendsList = new FriendsListFragment(Memory.users, getBaseContext());
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
    public void showUser(VKApiUser user) {
        Intent intent = new Intent(getBaseContext(), UserActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("id", user.id);
        intent.putExtras(bundle);
        startActivity(intent);
    }


}

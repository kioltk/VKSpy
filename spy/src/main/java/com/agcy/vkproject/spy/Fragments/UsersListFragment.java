package com.agcy.vkproject.spy.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.agcy.vkproject.spy.Adapters.CustomItems.UserItem;
import com.agcy.vkproject.spy.Adapters.UpdatesAdapter;
import com.agcy.vkproject.spy.Adapters.UserListAdapter;
import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.Models.Update;
import com.agcy.vkproject.spy.R;
import com.agcy.vkproject.spy.UserActivity;
import com.vk.sdk.api.model.VKApiUserFull;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class UsersListFragment extends Fragment {

    private ArrayList<VKApiUserFull> users;
    private Context context;
    private Helper.InitializationListener initializationListener = new Helper.InitializationListener() {
        @Override
        public void onLoadingEnded() {

            users = Memory.getFriends();
            create();
        }

        @Override
        public void onDownloadingEnded() {
            users = Memory.getFriends();
            create();
        }
    };
    private UserListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Memory.addOnlineListener(new UpdatesAdapter.NewItemListener() {
            @Override
            public void newItem(Update Item) {
                if(adapter!=null)
                    adapter.notifyDataSetChanged();
            }
        });
        this.context = getActivity();
        if(Memory.users.isEmpty()) {
            users = new ArrayList<VKApiUserFull>();
            Helper.addInitializationListener(initializationListener);
        }
        else{
            users = Memory.getFriends();
        }
    }


    View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_list, null);
        create();
        return rootView;
    }

    private void create() {
        if(!users.isEmpty()) {

            ListView listView = (ListView) rootView.findViewById(R.id.list);
            adapter = new UserListAdapter(users, context);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    UserItem item = (UserItem) parent.getItemAtPosition(position);

                    VKApiUserFull user = item.getContent();

                    Intent intent = new Intent(context, UserActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", user.id);
                    intent.putExtras(bundle);
                    startActivity(intent);

                }
            });
            listView.setAdapter(adapter);
            rootView.findViewById(R.id.loading).setVisibility(View.GONE);
        }else{
            rootView.findViewById(R.id.loading).setVisibility(View.VISIBLE);
        }
    }

}

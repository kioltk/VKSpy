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
import android.widget.TextView;

import com.agcy.vkproject.spy.Adapters.UserListAdapter;
import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.Fragments.Interfaces.LoadImagesOnScrollListener;
import com.agcy.vkproject.spy.Listeners.NewUpdateListener;
import com.agcy.vkproject.spy.Models.Update;
import com.agcy.vkproject.spy.R;
import com.agcy.vkproject.spy.UserActivity;
import com.vk.sdk.api.model.VKApiUserFull;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class UsersListFragment extends Fragment {

    public ArrayList<VKApiUserFull> users;
    protected Context context;
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
    private UsersListener usersListener = new UsersListener(){
        @Override
        public void reload(){
            users = Memory.getFriends();
            create();
        }
    };
    public UsersListener getListener(){
        return usersListener;
    }
    protected UserListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
        Memory.addOnlineListener(new NewUpdateListener() {
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


    protected View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_list, null);
        create();
        return rootView;
    }

    protected void create() {
        if(!users.isEmpty()) {

            final ListView listView = (ListView) rootView.findViewById(R.id.list);
            adapter = new UserListAdapter(users, context);
            listView.setAdapter(adapter);
            listView.setOnScrollListener(new LoadImagesOnScrollListener(listView));

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Intent intent = new Intent(context, UserActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", (int) id);
                    intent.putExtras(bundle);
                    startActivity(intent);

                }
            });
            rootView.findViewById(R.id.loading).setVisibility(View.GONE);
        }else{
            if(Helper.isInitialized()){
                ((TextView)rootView.findViewById(R.id.status)).setText(R.string.no_friends);
            }else
                rootView.findViewById(R.id.loading).setVisibility(View.VISIBLE);
        }
    }

    public abstract class UsersListener {
        public abstract void reload();
    }
}

package com.happysanta.spy.Fragments;

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

import com.happysanta.spy.Adapters.UserListAdapter;
import com.happysanta.spy.Core.Helper;
import com.happysanta.spy.Core.Memory;
import com.happysanta.spy.Fragments.Interfaces.LoadImagesOnScrollListener;
import com.happysanta.spy.Listeners.NewUpdateListener;
import com.happysanta.spy.Models.Update;
import com.happysanta.spy.R;
import com.happysanta.spy.UserActivity;
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

            recreate();
        }

        @Override
        public void onDownloadingEnded() {
            recreate();
        }
    };
    private UsersListener usersListener = new UsersListener(){
        @Override
        public void reload(){
            recreate();
        }
    };

    public void recreate() {
        loadUsers();
        create();
    }

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
            loadUsers();
        }
    }

    public void loadUsers() {
        users = Memory.getFriends();
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
            listView.setAdapter(null);

            adapter = new UserListAdapter(users, context);

            LoadImagesOnScrollListener listener = new LoadImagesOnScrollListener(listView);
            listView.setOnScrollListener(listener);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    if(id==0)
                        return;

                    Intent intent = new Intent(context, UserActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt("id", (int) id);
                    intent.putExtras(bundle);
                    startActivity(intent);

                }
            });
            rootView.findViewById(R.id.loading).setVisibility(View.GONE);
        }else {
            showNoUsers();
        }
    }
    public int getListEmpty(){
        return R.string.no_friends;
    }
    public void showNoUsers() {
        if (Helper.isDownloaded()) {
            ((TextView) rootView.findViewById(R.id.status)).setText(getListEmpty());
            rootView.findViewById(R.id.status).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.loading).setVisibility(View.GONE);
        } else {
            rootView.findViewById(R.id.loading).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.status).setVisibility(View.GONE);
        }
    }

    public abstract class UsersListener {
        public abstract void reload();
    }
}

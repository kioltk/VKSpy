package com.agcy.vkproject.spy.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.agcy.vkproject.spy.Adapters.CustomItems.UserItem;
import com.agcy.vkproject.spy.Adapters.UserListAdapter;
import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.R;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKApiUserFull;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class UsersListFragment extends Fragment {

    private ArrayList<VKApiUserFull> users;
    private final Context context;
    private OnSelectedListener selectedListener;
    private Helper.OnInitializationEndListener onInitializationEndListener = new Helper.OnInitializationEndListener() {
        @Override
        public void onEnd() {
            users = Memory.getUsers();
            create();
        }
    };

    public UsersListFragment(Context context, OnSelectedListener userSelectedListener){
        this.context = context;
        if(Memory.users.isEmpty()) {
            users = new ArrayList<VKApiUserFull>();
            Helper.addOnInitializationEndListener(onInitializationEndListener);
        }
        else{
            users = Memory.getUsers();
        }
        setOnUserSelectedListener(userSelectedListener);
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
            listView.setAdapter(new UserListAdapter(users, context));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(selectedListener !=null)
                        selectedListener.onSelect(((UserItem) parent.getItemAtPosition(position)).getContent());
                }
            });
            rootView.findViewById(R.id.loading).setVisibility(View.GONE);
        }else{
            rootView.findViewById(R.id.loading).setVisibility(View.VISIBLE);
        }
    }

    public void setOnUserSelectedListener(OnSelectedListener listener){
        this.selectedListener = listener;
    }
    public static abstract class OnSelectedListener {
        public abstract void onSelect(VKApiUser user);
    }
}

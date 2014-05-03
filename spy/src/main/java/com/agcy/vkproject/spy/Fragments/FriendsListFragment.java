package com.agcy.vkproject.spy.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.agcy.vkproject.spy.Adapters.UserListAdapter;
import com.agcy.vkproject.spy.R;
import com.vk.sdk.api.model.VKApiUser;
import com.vk.sdk.api.model.VKUsersArray;

/**
 * A placeholder fragment containing a simple view.
 */
public class FriendsListFragment extends Fragment {

    private final VKUsersArray friends;
    private final Context context;
    private OnSelectedListener onSelectedListener;


    public FriendsListFragment(VKUsersArray friends,Context context) {
        this.friends = friends;
        this.context = context;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        ListView rootView = (ListView) inflater.inflate(R.layout.fragment_friends,null);

        rootView.setAdapter(new UserListAdapter(friends,context));
        rootView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                onSelectedListener.onSelect((VKApiUser) parent.getItemAtPosition(position));
            }
        });
        return rootView;
    }
    public void setOnUserSelected(OnSelectedListener listener){
        this.onSelectedListener = listener;
    }
    public static abstract class OnSelectedListener {
        public abstract void onSelect(VKApiUser user);
    }
}

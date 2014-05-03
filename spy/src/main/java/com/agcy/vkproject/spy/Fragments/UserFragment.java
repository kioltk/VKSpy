package com.agcy.vkproject.spy.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.agcy.vkproject.spy.Adapters.UpdatesAdapter;
import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.R;
import com.vk.sdk.api.model.VKApiUser;

/**
 * Created by kiolt_000 on 26-Apr-14.
 */
public class UserFragment extends android.support.v4.app.Fragment {
    private final VKApiUser user;
    private final Context context;
    private boolean tracked;

    public UserFragment(VKApiUser user, Context context) {
        this.user = user;
        this.context = context;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_temp, null);



        final ListView list = (ListView) rootView.findViewById(R.id.list);
        list.setAdapter(new UpdatesAdapter(Memory.getOnlines(user.id), context));

        Button showOnlines = (Button) rootView.findViewById(R.id.showOnlines);
        showOnlines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.setAdapter(new UpdatesAdapter(Memory.getOnlines(user.id), context));
            }
        });


        Button showTypings = (Button) rootView.findViewById(R.id.showTypings);
        showTypings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.setAdapter(new UpdatesAdapter(Memory.getTyping(user.id), context));
            }
        });
        return rootView;
    }
}

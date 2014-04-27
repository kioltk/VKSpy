package com.agcy.vkproject.spy.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.agcy.vkproject.spy.Adapters.OnlineAdapter;
import com.agcy.vkproject.spy.Adapters.TypingAdapter;
import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vk.sdk.api.model.VKApiUser;

/**
 * Created by kiolt_000 on 26-Apr-14.
 */
public class UserFragment extends android.support.v4.app.Fragment {
    private final VKApiUser user;
    private final Context context;

    public UserFragment(VKApiUser user, Context context) {
        this.user = user;
        this.context = context;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_user, null);

        ImageView photo = (ImageView) rootView.findViewById(R.id.photo);
        TextView name = (TextView) rootView.findViewById(R.id.name);
        ImageView status = (ImageView) rootView.findViewById(R.id.status);
        name.setText(user.first_name+" "+user.last_name);
        if(user.online) {
            status.setVisibility(View.VISIBLE);
            //if (user.online_mobile) status.setText("В сети с мобильного");
        }
        ImageLoader.getInstance().displayImage(user.photo_100,photo);

        //todo: track him!

        final ListView list = (ListView) rootView.findViewById(R.id.list);
        list.setAdapter(new OnlineAdapter(Memory.getOnlines(user.id), context));

        Button showOnlines = (Button) rootView.findViewById(R.id.showOnlines);
        showOnlines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.setAdapter(new OnlineAdapter(Memory.getOnlines(user.id), context));
            }
        });
        Button track = (Button) rootView.findViewById(R.id.track);
        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "not implemented =(", Toast.LENGTH_SHORT).show();
            }
        });

        Button showTypings = (Button) rootView.findViewById(R.id.showTypings);
        showTypings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.setAdapter(new TypingAdapter(Memory.getTyping(user.id), context));
            }
        });
        return rootView;
    }
}

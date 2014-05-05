package com.agcy.vkproject.spy.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.agcy.vkproject.spy.Adapters.CustomItems.PreferenceItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.ToggleablePreferenceItem;
import com.agcy.vkproject.spy.Adapters.PreferenceAdapter;
import com.agcy.vkproject.spy.Longpoll.LongPollService;
import com.agcy.vkproject.spy.R;
import com.agcy.vkproject.spy.SettingsActivity;

import java.util.ArrayList;

/**
 * Created by kiolt_000 on 04-May-14.
 */
public class MainFragment extends android.support.v4.app.Fragment {

    private final Context context;

    public MainFragment(Context context){
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main,null);

        SharedPreferences popupPreferences = context.getSharedPreferences("popup", Context.MODE_MULTI_PROCESS);
        boolean popupStatus = popupPreferences.getBoolean("status", false);

        SharedPreferences longpollPreferences = context.getSharedPreferences("longpoll", Context.MODE_MULTI_PROCESS);
        boolean longpollStatus = longpollPreferences.getBoolean("status", false);


        TextView happySantaText = (TextView) rootView.findViewById(R.id.happySantaText);
        happySantaText.setText(Html.fromHtml("<b>VK Spy</b> invisibly monitors your friends and VK users activity."));
        TextView happySantaLink = (TextView) rootView.findViewById(R.id.happySantaLink);
        happySantaLink.setText(Html.fromHtml("<a href=\"http://vk.com/happysanta\">http://vk.com/<b>happysanta</b>"));
        happySantaLink.setMovementMethod(LinkMovementMethod.getInstance());


        ListView preferencesView = (ListView) rootView.findViewById(R.id.preference);
        ArrayList<PreferenceItem> preferences = new ArrayList<PreferenceItem>();
        preferences.add(new ToggleablePreferenceItem("Enable spy",null,longpollStatus) {
            @Override
            public void onClick() {

            }

            @Override
            public void onToggle(Boolean isChecked) {
                longpollToggle(isChecked);
            }
        });
        preferences.add(new ToggleablePreferenceItem("Enable notification", null, popupStatus) {
            @Override
            public void onToggle(Boolean isChecked) {

                SharedPreferences preferences = context.getSharedPreferences("popup", Context.MODE_MULTI_PROCESS);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("status",isChecked);
                editor.commit();
            }

            @Override
            public void onClick() {

            }
        });
        preferences.add(new PreferenceItem("Advanced settings",null) {
            @Override
            public void onClick() {
                startActivity(new Intent(context,SettingsActivity.class));
                Toast.makeText(context, "Ещё не сделано..", Toast.LENGTH_SHORT).show();
            }
        });
        preferencesView.setAdapter(new PreferenceAdapter(context,preferences));

        return rootView;


    }
    public void longpollToggle(Boolean active) {

        SharedPreferences preferences = context.getSharedPreferences("longpoll", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("status",active);
        editor.commit();


        Intent longPollService = new Intent(context, LongPollService.class);
        Bundle bundle  = new Bundle();
        bundle.putInt(LongPollService.ACTION,(active? LongPollService.ACTION_START:LongPollService.ACTION_STOP));
        longPollService.putExtras(bundle);
        context.startService(longPollService);

    }
}

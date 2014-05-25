package com.agcy.vkproject.spy.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.agcy.vkproject.spy.Adapters.CustomItems.PreferenceItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.ToggleablePreferenceItem;
import com.agcy.vkproject.spy.Adapters.PreferenceAdapter;
import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.Longpoll.LongPollService;
import com.agcy.vkproject.spy.R;
import com.agcy.vkproject.spy.SettingsActivity;

import java.util.ArrayList;

/**
 * Created by kiolt_000 on 04-May-14.
 */
public class MainFragment extends android.support.v4.app.Fragment {

    private Context context;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_main,null);


        SharedPreferences longpollPreferences = context.getSharedPreferences("longpoll", Context.MODE_MULTI_PROCESS);
        boolean longpollStatus = longpollPreferences.getBoolean("status", true);

        View happySanta = inflater.inflate(R.layout.main_santa, null);

        TextView happySantaText = (TextView) happySanta.findViewById(R.id.happySantaText);
        happySantaText.setText(Html.fromHtml(context.getString(R.string.desc)));
        TextView happySantaLink = (TextView) happySanta.findViewById(R.id.happySantaLink);
        happySantaLink.setText(Html.fromHtml("vk.com/<b>happysanta</b>"));
        happySantaLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("http://vk.com/happysanta")
                );
                startActivity(browserIntent);
            }
        });


        /*Bitmap tile = BitmapFactory.decodeResource(context.getResources(), R.drawable.underline);
        BitmapDrawable tiledBitmapDrawable = new BitmapDrawable(context.getResources(), tile);
        tiledBitmapDrawable.setTileModeX(Shader.TileMode.REPEAT);
        //tiledBitmapDrawable.setTileModeY(Shader.TileMode.REPEAT);

        santaGroundView.setBackgroundDrawable(tiledBitmapDrawable);

        BitmapDrawable bitmap = new BitmapDrawable(BitmapFactory.decodeResource(
                getResources(), R.drawable.underline2));
        bitmap.setTileModeX(Shader.TileMode.REPEAT);
        */
        LinearLayout layout = (LinearLayout)happySanta.findViewById(R.id.line);
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();  // deprecated

        for(int i = 0; width%(341*i + 1) < width;i++ ){
            layout.addView(new ImageView(context){{
                setImageResource(R.drawable.underline2);
                setLayoutParams(new ViewGroup.LayoutParams(341, Helper.convertToDp(4)));
                setScaleType(ScaleType.CENTER_CROP);
            }});
        }

        ListView preferencesView = (ListView) rootView.findViewById(R.id.preference);
        preferencesView.addHeaderView(happySanta,null,false);
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
        preferences.add(new PreferenceItem("Advanced settings") {
            @Override
            public void onClick() {
                startActivity(new Intent(context,SettingsActivity.class));
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

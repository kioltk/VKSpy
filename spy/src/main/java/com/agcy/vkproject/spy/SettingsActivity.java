package com.agcy.vkproject.spy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.agcy.vkproject.spy.Adapters.CustomItems.HeaderItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.Item;
import com.agcy.vkproject.spy.Adapters.CustomItems.PreferenceItem;
import com.agcy.vkproject.spy.Adapters.CustomItems.ToggleablePreferenceItem;
import com.agcy.vkproject.spy.Core.Helper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vk.sdk.VKUIHelper;

import java.util.ArrayList;

/**
 * Created by kiolt_000 on 10-May-14.
 */
public class SettingsActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        VKUIHelper.onCreate(this);

        String title = getResources().getString(R.string.settings);

            ActionBar bar = getSupportActionBar();
            bar.setTitle(title);
            bar.setDisplayHomeAsUpEnabled(true);


        ListView listView = (ListView) findViewById(R.id.list);
        SettingsAdapter adapter = new SettingsAdapter();

        View userView = getLayoutInflater().inflate(R.layout.user_view, null);
        View logoutButtonView = getLayoutInflater().inflate(R.layout.logout_button_view, null);
        listView.addHeaderView(userView,null,false);
        listView.addFooterView(logoutButtonView);

        listView.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences("user", MODE_MULTI_PROCESS);
        String name = prefs.getString("name", "");
        String photoUrl = prefs.getString("photo","");
        ImageView photoView = (ImageView) userView.findViewById(R.id.photo);
        TextView nameView = (TextView) userView.findViewById(R.id.name);

        nameView.setText(name);
        ImageLoader.getInstance().displayImage(photoUrl, photoView);

        logoutButtonView.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(v);
            }
        });
    }


    public void logout(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(getString(R.string.logout_message))
                .setTitle(getString(R.string.logout_title));

        builder.setPositiveButton(getString(R.string.logout_accept), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Helper.logout();
                finish();
            }
        });
        builder.setNegativeButton(getString(R.string.logout_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public String getHelp() {
        return getString(R.string.help);
    }


    private class SettingsAdapter extends BaseAdapter {


        ArrayList<Item> items = new ArrayList<Item>(){{



            final SharedPreferences notificationPreferences = getSharedPreferences("notification", Context.MODE_MULTI_PROCESS);
            boolean notificationEnable = notificationPreferences.getBoolean("status", true);
            boolean notificationVibrate = notificationPreferences.getBoolean("vibrate", true);
            boolean notificationSound = notificationPreferences.getBoolean("sound", true);

            boolean notificationOnline = notificationPreferences.getBoolean("notificationOnline", true);
            int wayToNotifyOnline = notificationPreferences.getInt("wayToNotifyOnline", 0);

            boolean notificationOffline = notificationPreferences.getBoolean("notificationOffline", true);
            int wayToNotifyOffline = notificationPreferences.getInt("wayToNotifyOffline", 0);

            add(new PreferenceItem(getHelp()) {
                @Override
                public void onClick() {

                    Intent browserIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://vk.com/topic-58810575_30129274")
                    );
                    startActivity(browserIntent);
                }
            });

            add(new HeaderItem(getNotifications()));
            add(new ToggleablePreferenceItem(getEnabled(), notificationEnable) {
                @Override
                public void onToggle(Boolean isChecked) {

                    SharedPreferences.Editor editor = notificationPreferences.edit();
                    editor.putBoolean("status", isChecked);
                    editor.commit();

                }
            });
            add(new ToggleablePreferenceItem(getVibrate(),notificationVibrate) {
                @Override
                public void onToggle(Boolean isChecked) {

                    SharedPreferences.Editor editor = notificationPreferences.edit();
                    editor.putBoolean("vibrate",isChecked);
                    editor.commit();
                }
            });
            add(new ToggleablePreferenceItem(getSound(),notificationSound) {
                @Override
                public void onToggle(Boolean isChecked) {

                    SharedPreferences.Editor editor = notificationPreferences.edit();
                    editor.putBoolean("sound",isChecked);
                    editor.commit();
                }
            });

            add(new HeaderItem(getOnlineNotifications()));
            add(new ToggleablePreferenceItem(getEnabled(),notificationOnline) {
                @Override
                public void onToggle(Boolean isChecked) {

                    SharedPreferences.Editor editor = notificationPreferences.edit();
                    editor.putBoolean("notificationOnline", isChecked);
                    editor.commit();
                    if(isChecked){

                    }
                }
            });
            add(new PreferenceItem(getWayToNotify(),wayToNotifyOnline>0?getPopupText():getNotificationText()) {
                @Override
                public void onClick() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    final AlertDialog selector;
                    builder.setTitle(R.string.way_to_notify)
                            .setItems(R.array.ways_to_notify, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences.Editor editor = notificationPreferences.edit();
                                    editor.putInt("wayToNotifyOnline", which);
                                    editor.commit();

                                    String text = which> 0 ? getPopupText() : getNotificationText();
                                    setDescription(text);


                                }
                            });
                    selector = builder.create();
                    selector.show();

                }
            });

            add(new HeaderItem(getOfflineNotifications()));
            add(new ToggleablePreferenceItem(getEnabled(),notificationOffline) {
                @Override
                public void onToggle(Boolean isChecked) {

                    SharedPreferences.Editor editor = notificationPreferences.edit();
                    editor.putBoolean("notificationOffline", isChecked);
                    editor.commit();
                }
            });
            add(new PreferenceItem(getWayToNotify(),wayToNotifyOffline>0?getPopupText():getNotificationText()) {
                @Override
                public void onClick() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    final AlertDialog selector;
                    builder.setTitle(R.string.way_to_notify)
                            .setItems(R.array.ways_to_notify, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences.Editor editor = notificationPreferences.edit();
                                    editor.putInt("wayToNotifyOffline", which);
                                    editor.commit();

                                    String text = which> 0 ? getPopupText() : getNotificationText();
                                    setDescription(text);


                                }
                            });
                    selector = builder.create();
                    selector.show();

                }
            });


        }};

        @Override
        public boolean isEnabled(int position) {
            return getItem(position).isEnabled();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Item getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Item item = getItem(position);
            View view = item.getView(getBaseContext());
            if(
                    item instanceof PreferenceItem &&
                    position+1 != items.size() &&
                    !(getItem(position+1) instanceof HeaderItem)
                    ){
                view.findViewById(R.id.divider).setVisibility(View.VISIBLE);
            }
            return view;
        }
    }

    public String getNotifications(){
        return getResources().getString(R.string.notifications);
    }
    public String getOnlineNotifications() {
        return getResources().getString(R.string.online_notifications);
    }
    public String getOfflineNotifications() {
        return getResources().getString(R.string.offline_notifications);
    }
    private String getSound(){
        return getResources().getString(R.string.sound);
    }
    private String getVibrate(){
        return getResources().getString(R.string.vibrate);
    }

    private String getEnabled(){

        return getResources().getString(R.string.enabled);
    }
    private String getWayToNotify(){
        return getResources().getString(R.string.way_to_notify);
    }
    private String getPopupText(){
        return getResources().getString(R.string.popup);
    }
    private String getNotificationText(){
        return getResources().getString(R.string.notification);
    }


}

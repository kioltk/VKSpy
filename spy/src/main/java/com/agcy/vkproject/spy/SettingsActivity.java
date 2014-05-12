package com.agcy.vkproject.spy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.agcy.vkproject.spy.Core.Helper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vk.sdk.VKUIHelper;

/**
 * Created by kiolt_000 on 10-May-14.
 */
public class SettingsActivity extends PreferenceActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        VKUIHelper.onCreate(this);

        String title = getResources().getString(R.string.settings);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.HONEYCOMB) {

            android.app.ActionBar bar = getActionBar();
            bar.setTitle(title);
            bar.setDisplayHomeAsUpEnabled(true);
        }
        //ListView listView = (ListView) findViewById(R.id.list);
        //SettingsAdapter adapter = new SettingsAdapter();
        //listView.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences("user", MODE_MULTI_PROCESS);
        String name = prefs.getString("name", "");
        String photoUrl = prefs.getString("photo","");
        ImageView photoView = (ImageView) findViewById(R.id.photo);
        TextView nameView = (TextView) findViewById(R.id.name);

        nameView.setText(name);
        ImageLoader.getInstance().displayImage(photoUrl, photoView);

        //SharedPreferences notification


    }


    public void logout(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(getString(R.string.logout_message))
                .setTitle(getString(R.string.logout_title));

        builder.setPositiveButton(getString(R.string.logout_accept), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Helper.DESTROYALL();
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

    private class SettingsAdapter extends BaseAdapter {






        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }
    }
}

package com.agcy.vkproject.spy;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.agcy.vkproject.spy.Adapters.CustomItems.PreferenceItem;
import com.agcy.vkproject.spy.Core.Helper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vk.sdk.VKUIHelper;

import org.focuser.sendmelogs.LogCollector;

import java.util.ArrayList;

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


        ListView listView = (ListView) findViewById(R.id.list);
        SettingsAdapter adapter = new SettingsAdapter();
        listView.setAdapter(adapter);

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

    private class SettingsAdapter extends BaseAdapter {


        ArrayList<PreferenceItem> items = new ArrayList<PreferenceItem>(){{
            add(new PreferenceItem("Report","Send logs") {
                @Override
                public void onClick() {
                    new AsyncTask<Void, Void, Boolean>() {

                        LogCollector mLogCollector = new LogCollector(SettingsActivity.this);
                        ProgressDialog progressDialog = new ProgressDialog(SettingsActivity.this);
                        @Override
                        protected Boolean doInBackground(Void... params) {
                            return mLogCollector.collect();
                        }
                        @Override
                        protected void onPreExecute() {
                            progressDialog.setMessage("Collecting..");
                            progressDialog.setTitle("Sending logs");
                            progressDialog.setIndeterminate(true);
                            progressDialog.show();
                        }
                        @Override
                        protected void onPostExecute(Boolean result) {
                            progressDialog.dismiss();
                            if (result)
                                mLogCollector.sendLog("kioltk@gmail.com", "VK Spy - Application log", "Log report");
                            else
                                Toast.makeText(getBaseContext(),"Sending error",Toast.LENGTH_SHORT).show();
                        }

                    }.execute();
                }
            });
        }};



        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public PreferenceItem getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).getView(getBaseContext());
        }
    }
}

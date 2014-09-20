package com.happysanta.vkspy;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import com.happysanta.vkspy.Core.Helper;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        ListView listView = getListView();
        listView.setPadding(0,0,0,0);
        View userView = getLayoutInflater().inflate(R.layout.user_view, null);
        View logoutButtonView = getLayoutInflater().inflate(R.layout.logout_button_view, null);
        listView.addHeaderView(userView, null, false);
        listView.addFooterView(logoutButtonView);
        listView.setDivider(getResources().getDrawable(R.drawable.list_item_divider));
        listView.setFooterDividersEnabled(false);
        listView.setHeaderDividersEnabled(false);



        SharedPreferences prefs = getSharedPreferences("user", MODE_MULTI_PROCESS);
        String name = prefs.getString("name", "");
        String photoUrl = prefs.getString("photo","");
        ImageView photoView = (ImageView) userView.findViewById(R.id.photo);
        TextView nameView = (TextView) userView.findViewById(R.id.name);

        nameView.setText(name);
        ImageLoader.getInstance().displayImage(photoUrl, photoView);
        photoView.setOnClickListener(new View.OnClickListener() {
            int taps = 0;
            @Override
            public void onClick(View v) {
             if(taps < 10){
                 //taps++;
             }else{
                 taps = 0;
                startActivity(new Intent(getBaseContext(), TestFilterActivity.class));
             }
            }
        });

        logoutButtonView.findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {

        View view = super.onCreateView(name, context, attrs);
        if(view!=null) {
            view.setPadding(0, 0, 0, 0);
            ListView listView = getListView();
            listView.setPadding(0,0,0,0);
        }
        return view;
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view = super.onCreateView(parent, name, context, attrs);
        if(view!=null) {
            view.setPadding(0, 0, 0, 0);
            ListView listView = getListView();
            listView.setPadding(0, 0, 0, 0);
        }
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // TODO: If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }


    private void setupSimplePreferencesScreen() {


        addPreferencesFromResource(R.xml.pref_general);

        addPreferencesFromResource(R.xml.pref_notification);

        //addPreferencesFromResource(R.xml.pref_additional);

        bindPreferenceSummaryToValue(findPreference("notifications_ringtone"));
        bindPreferenceSummaryToValue(findPreference("notifications_offline_type"));
        bindPreferenceSummaryToValue(findPreference("notifications_online_type"));

        bindPreferenceSummaryToValue(findPreference("connection_external_interval"));

        Preference helpPreference =  findPreference("help");
        helpPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                Intent browserIntent = new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://vk.com/topic-58810575_30129274")
                );
                startActivity(browserIntent);

                return true;
            }
        });
        /*
        Preference licenseButton = (Preference) findPreference("connection_additional");
        licenseButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
                builder.setTitle(R.string.connection_additional);
                builder.setMessage(R.string.connection_additional_description);
                return true;
            }
        });
        //Preference sendLogPreference = (Preference) findPreference("send_logs");

        sendLogPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                File F = Logs.getFile();
                Uri U = Uri.fromFile(F);
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_STREAM, U);
                startActivity(Intent.createChooser(i,"What should we do with logs?"));
                return true;
            }
        });
        */
    }


    public void logout() {
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

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

}

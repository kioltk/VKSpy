package com.happysanta.vkspy.Fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.happysanta.vkspy.Adapters.CustomItems.PreferenceItem;
import com.happysanta.vkspy.Adapters.CustomItems.ToggleablePreferenceItem;
import com.happysanta.vkspy.Adapters.PreferenceAdapter;
import com.happysanta.vkspy.Core.Helper;
import com.happysanta.vkspy.Core.Logs;
import com.happysanta.vkspy.Core.UberFunktion;
import com.happysanta.vkspy.InfoActivity;
import com.happysanta.vkspy.Longpoll.LongPollService;
import com.happysanta.vkspy.R;
import com.happysanta.vkspy.SettingsActivity;

import java.io.File;
import java.util.ArrayList;


public class MainFragment extends android.support.v4.app.Fragment {

    private Context context;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();


        if(savedInstanceState!=null && UberFunktion.loading){
            ProgressDialog dialog = new ProgressDialog(context);
            dialog.setMessage(context.getString(R.string.durov_function_activating_message));
            UberFunktion.putNewDialogWindow(dialog);
            dialog.show();
        }

        View rootView = inflater.inflate(R.layout.fragment_main,null);

        View happySanta = inflater.inflate(R.layout.main_santa, null);

        TextView happySantaText = (TextView) happySanta.findViewById(R.id.happySantaText);
        happySantaText.setText(Html.fromHtml(context.getString(R.string.app_description)));
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
        happySanta.setOnClickListener(new View.OnClickListener() {
            int i = 1;
            @Override
            public void onClick(View v) {
                if(i<10) i++;
                else {
                    i = 0;
                    File F = Logs.getFile();
                    Uri U = Uri.fromFile(F);
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_STREAM, U);
                    startActivity(Intent.createChooser(i, "What should we do with logs?"));
                }
            }
        });

        SharedPreferences longpollPreferences = context.getSharedPreferences("longpoll", Context.MODE_MULTI_PROCESS);
        boolean longpollStatus = longpollPreferences.getBoolean("status", true);



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
        /*
        preferences.add(new PreferenceItem(getUberfunction(), getUberfunctionDescription()) {
            @Override
            public void onClick() {

                if(UberFunktion.loading) {
                    ProgressDialog dialog = new ProgressDialog(context);
                    dialog.setMessage(context.getString(R.string.durov_function_activating_message));
                    UberFunktion.putNewDialogWindow(dialog);
                    dialog.show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final AlertDialog selector;
                View durov = getActivity().getLayoutInflater().inflate(R.layout.durov, null);
                SharedPreferences durovPreferences = context.getSharedPreferences("durov", Context.MODE_MULTI_PROCESS);
                boolean updateOnly = durovPreferences.getBoolean("loaded", false);
                if(updateOnly)
                    ((TextView)durov.findViewById(R.id.description)).setText(R.string.durov_function_activated);


                if(Memory.users.getById(1)!=null && !updateOnly) {
                    builder.setTitle(R.string.durov_joke_title);
                    ((TextView)durov.findViewById(R.id.description)).setText(R.string.durov_joke_message);
                    ( durov.findViewById(R.id.cat)).setVisibility(View.VISIBLE);

                    builder.setNegativeButton(R.string.durov_joke_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            BugSenseHandler.sendEvent("Не следим за дуровым");
                        }
                    });
                    BugSenseHandler.sendEvent("DUROV FRIEND CATCHED!!1");
                }else{
                    builder.setTitle(R.string.durov_start_title);

                    ( durov.findViewById(R.id.photo)).setVisibility(View.VISIBLE);
                    ImageLoader.getInstance().displayImage("http://cs9591.vk.me/v9591001/70/VPSmUR954fQ.jpg",(ImageView) durov.findViewById(R.id.photo));
                    builder.
                            setPositiveButton(updateOnly ? R.string.durov_start_update : R.string.durov_start_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    BugSenseHandler.sendEvent("Следим за Дуровым");
                                    ProgressDialog uberfunctionDialog = ProgressDialog.show(getActivity(),
                                            context.getString(R.string.durov_function_activating_title),
                                            context.getString(R.string.durov_function_activating_message),
                                            true,
                                            false);
                                    UberFunktion.initialize(uberfunctionDialog);

                                }
                            });
                    builder.setNegativeButton(R.string.durov_start_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            try {
                                Intent browserIntent = new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://vk.com/id1")
                                );
                                startActivity(browserIntent);

                            }catch(Exception exp){
                                AlertDialog.Builder errorShower = new AlertDialog.Builder(getActivity());
                                if (exp instanceof ActivityNotFoundException) {
                                    errorShower.setTitle(R.string.error);
                                    errorShower.setMessage(R.string.no_browser);

                                } else {
                                    errorShower.setTitle(R.string.error);
                                    errorShower.setMessage(R.string.unknown_error);
                                }
                                errorShower.show();
                            }
                            BugSenseHandler.sendEvent("Не следим за дуровым");
                        }
                    });
                }

                builder.setView(durov);
                selector = builder.create();
                selector.show();
            }
        });
        */

        preferences.add(new ToggleablePreferenceItem(getEnableSpy(), null, longpollStatus) {

            @Override
            public void onToggle(Boolean isChecked) {
                longpollToggle(isChecked);
            }
        });

        preferences.add(new PreferenceItem(getAdvancedSettings()) {
            @Override
            public void onClick() {
                startActivity(new Intent(context,SettingsActivity.class));
            }
        });

        preferences.add(new PreferenceItem(getAbout()) {
            @Override
            public void onClick() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final AlertDialog aboutDialog;
                builder.setTitle(R.string.app_about)
                        .setCancelable(true)
                        .setPositiveButton(R.string.app_about_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                View aboutView = getActivity().getLayoutInflater().inflate(R.layout.about, null);

                TextView aboutDescription = (TextView) aboutView.findViewById(R.id.description);
                aboutDescription.setText(Html.fromHtml(getResources().getString(R.string.app_about_description)));
                aboutDescription.setMovementMethod(LinkMovementMethod.getInstance());

                aboutView.findViewById(R.id.license).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent browserIntent = new Intent(context, InfoActivity.class);
                        startActivity(browserIntent);
                    }
                });

                builder.setView(aboutView);
                aboutDialog = builder.create();
                aboutDialog.setCanceledOnTouchOutside(true);
                aboutDialog.show();
            }
        });
        preferencesView.setAdapter(new PreferenceAdapter(context,preferences));

        return rootView;


    }



    private String getUberfunction() {
        return getResources().getString(R.string.durov_function_title);
    }
    private String getUberfunctionDescription() {
        return getResources().getString(R.string.durov_function_decription);
    }

    private String getAbout(){

        return getResources().getString(R.string.app_about);
    }

    public String getAdvancedSettings() {
        return getResources().getString(R.string.settings);
    }



    public void longpollToggle(Boolean active) {

        SharedPreferences preferences = context.getSharedPreferences("longpoll", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("status", active);
        editor.commit();


        Intent longPollService = new Intent(context, LongPollService.class);
        Bundle bundle  = new Bundle();
        bundle.putInt(LongPollService.ACTION,(active? LongPollService.ACTION_START:LongPollService.ACTION_STOP));
        longPollService.putExtras(bundle);
        context.startService(longPollService);

    }

    public String getEnableSpy() {
        return getResources().getString(R.string.enable_spy);
    }
}

package com.happysanta.crazytyping;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.happysanta.crazytyping.Core.Helper;
import com.happysanta.crazytyping.Core.Memory;
import com.happysanta.crazytyping.Fragments.DialogsFragment;
import com.happysanta.crazytyping.Longpoll.CrazyTypingService;
import com.happysanta.crazytyping.Receivers.NetworkStateReceiver;
import com.vk.sdk.VKUIHelper;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiMessage;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.api.model.VKUsersArray;

import org.json.JSONException;

import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivity extends ActionBarActivity {

    public static final int ONLINES = 1;
    public static boolean isOpened = false;
    private static MainActivity activity;
    private DialogsFragment dialogsFragment;
    private Button togglerButton;
    private static boolean loading = false;


    @Override
    protected void onPause() {
        super.onPause();
        isOpened = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        VKUIHelper.onResume(this);
        isOpened = true;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VKUIHelper.onDestroy(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKUIHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        VKUIHelper.onCreate(this);
        if(!Helper.isInitialized())
            Helper.initialize(this);
        Helper.setMainActivity(this);
        dialogsFragment = new DialogsFragment();
        getSupportFragmentManager().beginTransaction()

                .replace(R.id.container, dialogsFragment)
                .commit();
        if(savedInstanceState!=null)
            invalidateOptionsMenu();

        activity = this;

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(savedInstanceState==null) {
            //updateDialogs();
            new Thread(new Runnable() {
                @Override
                public void run() {

                    Memory.loadUsers();
                    Memory.loadDialogs();
                    if(Memory.dialogs.isEmpty()) {
                        updateDialogs(new Runnable() {
                            @Override
                            public void run() {
                                for(int i = 0; i <5 && i < Memory.dialogs.size(); i++){
                                   Memory.targetDialog(Memory.dialogs.get(i).getId());
                                }
                                checkTogglerReady();
                                dialogsFragment.createContent();

                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {

                                checkTogglerReady();
                                dialogsFragment.createContent();

                            }
                        });
                    }
                    else{
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                checkTogglerReady();
                                dialogsFragment.createContent();
                            }
                        });
                    }

                }
            }).start();
        }else{
            dialogsFragment.createContent();
            if(loading)
                DialogsFragment.showRefresh();
        }
        checkTogglerReady();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.about:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final AlertDialog aboutDialog;
                builder.setTitle(R.string.app_about)
                        .setCancelable(true)
                        .setPositiveButton(R.string.app_about_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                View aboutView = getLayoutInflater().inflate(R.layout.about, null);

                TextView aboutDescription = (TextView) aboutView.findViewById(R.id.description);
                aboutDescription.setText(Html.fromHtml(getResources().getString(R.string.app_about_description)));
                aboutDescription.setMovementMethod(LinkMovementMethod.getInstance());

                aboutView.findViewById(R.id.license).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent browserIntent = new Intent(getBaseContext(), InfoActivity.class);
                        startActivity(browserIntent);
                    }
                });

                builder.setView(aboutView);
                aboutDialog = builder.create();
                aboutDialog.setCanceledOnTouchOutside(true);
                aboutDialog.show();
                break;
        }

        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        final MenuItem toggler = menu.findItem(R.id.toggler);
        toggler.setActionView(R.layout.toggler);
        togglerButton = ((Button) toggler.getActionView().findViewById(R.id.title));
        togglerButton.setText(CrazyTypingService.isTyping() ? R.string.stop : R.string.start);
        togglerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean enabled = toggle();
                ((Button) toggler.getActionView().findViewById(R.id.title)).setText(enabled ? R.string.stop : R.string.start);
            }
        });
        checkTogglerReady();
        return true;
    }

    public void checkTogglerReady() {
            ready(!Memory.getTargetedDialogs().isEmpty() && !loading);

    }

    private Boolean toggle(){
        if(Memory.getTargetedDialogs().isEmpty()) {
            Toast.makeText(this,"select some dialogs first", Toast.LENGTH_LONG).show();
            return false;
        }
        return CrazyTypingService.toggle();
    }
    static Handler handler = new Handler();

    public static void updateDialogs(final Runnable successCallback, final Runnable failCallback) {

        if(loading || activity == null)
            return;
        loading = true;
        activity.ready(false);
        CrazyTypingService.stop();
        VKParameters userParameters = new VKParameters();
        try {
            PackageInfo thisPackage = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            userParameters.put("versionCode",thisPackage.versionCode);
            userParameters.put("versionName",thisPackage.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        new VKRequest("execute.getUser", userParameters).executeWithListener(new VKRequest.VKRequestListener() {


            @Override
            public void onComplete(VKResponse response) {
                try {
                    VKApiUserFull user = new VKList<VKApiUserFull>(response.json.getJSONArray("response"), VKApiUserFull.class).get(0);
                    SharedPreferences.Editor editor = activity.getSharedPreferences("user", MODE_MULTI_PROCESS).edit();
                    editor.putString("name", user.first_name + " " + user.last_name);
                    editor.putString("photo", user.getBiggestPhoto());
                    editor.putInt("id", user.id);
                    editor.commit();

                } catch (Exception exp) {
                    Context baseContext = activity;
                    AlertDialog.Builder successDialogBuilder = new AlertDialog.Builder(baseContext);
                    successDialogBuilder.setCancelable(true);


                    successDialogBuilder.setTitle((R.string.important_notification));
                    TextView notifcationView = new TextView(baseContext);
                    notifcationView.setTextColor(0xff333333);
                    notifcationView.setTextSize(16);
                    notifcationView.setPadding(0, Helper.convertToDp(20), 0, Helper.convertToDp(20));
                    notifcationView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    notifcationView.setGravity(Gravity.CENTER);
                    try {
                        notifcationView.setText(Html.fromHtml(response.json.getString("response")));
                    } catch (JSONException e) {
                        return;
                    }
                    notifcationView.setMovementMethod(LinkMovementMethod.getInstance());

                    successDialogBuilder.setView(notifcationView);
                    successDialogBuilder.setPositiveButton(baseContext.getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    successDialogBuilder.show();
                }
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);

            }
        });


        VKParameters dialogsParams = new VKParameters();
        dialogsParams.put("count", 200);

        new VKRequest("execute.getDialogs", dialogsParams).executeWithListener(
                new VKRequest.VKRequestListener() {
                    @Override
                    public void onError(VKError error) {
                        super.onError(error);

                        loading = false;
                        activity.checkTogglerReady();
                                failCallback.run();
                    }

                    @Override
                    public void onComplete(VKResponse response) {
                        try {
                            final VKList<VKApiMessage> dialogs;
                            dialogs = new VKList<VKApiMessage>();
                            dialogs.fill(response.json, VKApiMessage.class);



                            final VKRequest dialogOwnersRequest = new VKRequest("execute.getDialogsOwners");
                            dialogOwnersRequest.executeWithListener(


                                    new VKRequest.VKRequestListener() {

                                        @Override
                                        public void onError(VKError error) {
                                            super.onError(error);

                                            loading = false;
                                            activity.checkTogglerReady();
                                            failCallback.run();
                                        }

                                        @Override
                                        public void onComplete(final VKResponse response) {
                                            VKUsersArray users = null;
                                            try {
                                                users = (VKUsersArray) new VKUsersArray().parse(response.json);
                                            } catch (JSONException e) {
                                                e.printStackTrace();

                                                loading = false;
                                                activity.checkTogglerReady();
                                                failCallback.run();

                                                return;
                                            }
                                            final VKUsersArray finalUsers = users;
                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        Memory.saveUsers(finalUsers);
                                                        Memory.saveDialogs(dialogs);
                                                        Memory.loadUsers();
                                                        //Memory.loadDialogs();
                                                        handler.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                loading = false;
                                                                activity.dialogsFragment.recreateContent();

                                                                activity.checkTogglerReady();
                                                                successCallback.run();
                                                            }
                                                        });
                                                    } catch (Exception exp) {
                                                        handler.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                loading = false;
                                                                activity.checkTogglerReady();
                                                                failCallback.run();
                                                            }
                                                        });
                                                    }
                                                }
                                            }).start();

                                        }
                                    }
                            );
                        }catch (Exception exp) {
                            activity.checkTogglerReady();
                            failCallback.run();
                        }



                    }
                }
        );

    }

    private void ready(boolean ready) {
        if (togglerButton != null) {
            togglerButton.setEnabled(ready);

        }
    }

    private void downloadData() {
        // всё сложно..

        VKParameters friendsParameters = new VKParameters();
        friendsParameters.put("order", "hints");
        friendsParameters.put("fields", "sex,photo_200,photo_200_orig,photo_50,photo_100");

        final VKRequest friendsRequest = VKApi.friends().get(friendsParameters);

        // начинаем поток, который загружает всех с бд. друзей может быть много, поэтому нужен фон
        // когда загрузились, уведомляем всех, что загрузились. все сразу начинают юзать данные
        // после чего скачиваем новый список друзей, и заливаем его в бд. По итогу обновляем
        // списки, уведомляем, что закачка завершена.
        // если какие-то друзья загрузились из бд, значит мы можем за ними следить => включаем лонпгол
        // иначе ждём, пока они загрузятся

        new Thread(new Runnable() {
            @Override
            public void run() {
                Memory.loadUsers();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Helper.loadingEnded();
                    }
                });
                friendsRequest.executeWithListener(
                        new VKRequest.VKRequestListener() {

                            public NetworkStateReceiver.NetworkStateChangeListener connectionListener;

                            @Override
                            public void onComplete(final VKResponse response) {
                                // асинхронно сохраняем, чтобы никого не задеть...
                                // мы ведь внутри обычного потока
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        boolean firstLoading = false;
                                        if(Memory.users.isEmpty()){
                                            firstLoading = true;
                                        }
                                        VKUsersArray friends = (VKUsersArray) response.parsedModel;
                                                /*
                                                final VKUsersArray friendsArrayCopy = new VKUsersArray();
                                                for (VKApiUserFull vkApiUserFull : friends) {
                                                    friendsArrayCopy.add(vkApiUserFull);
                                                }
                                                Helper.fetchOnlines(friends,0);
                                                */
                                        Memory.saveFriends(friends);

                                        final boolean finalFirstLoading = firstLoading;

                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                if(Memory.users.getById(1)!=null){
                                                    //UberFunktion.initializeBackground(getBaseContext());
                                                }
                                                if(finalFirstLoading){
                                                    Helper.trackedUpdated();
                                                }
                                                Helper.downloadingEnded();
                                                startCrazyTyper();
                                            }
                                        });
                                    }
                                }).start();

                                if (connectionListener != null)
                                    connectionListener.remove();
                            }

                            @Override
                            public void onError(VKError error) {
                                super.onError(error);

                                switch (error.errorCode) {
                                    case VKError.VK_API_REQUEST_HTTP_FAILED:
                                        if (error.httpError instanceof SocketException || error.httpError instanceof UnknownHostException) {

                                            //((TextView)findViewById(R.id.status)).setText("Проверьте подключение");
                                            connectionListener = new NetworkStateReceiver.NetworkStateChangeListener(Helper.START_LOADER_ID) {

                                                @Override
                                                public void onConnected() {

                                                    //((TextView)findViewById(R.id.status)).setText("Подключение восстановленно");
                                                    downloadData();

                                                }

                                                @Override
                                                public void onLost() {
                                                    //((TextView)findViewById(R.id.status)).setText("Проверьте подключение");
                                                }

                                            };
                                        }
                                        break;
                                    default:
                                        Log.e("AGCY SPY", error.toString());
                                        break;
                                }
                            }

                        }
                );


            }
        }).start();
    }

    private void startCrazyTyper(){

        Intent longPollService = new Intent(getBaseContext(), CrazyTypingService.class);
        startService(longPollService);
    }

    private boolean isLongPollServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (CrazyTypingService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false;
        } else
            return true;
    }


    /*
    private class MainPagerAdapter extends FragmentPagerAdapter implements ContentPagerAdapter<Integer> {
        public MainPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Integer getContent(int index) {
            switch (index) {
                case 0:
                    return R.drawable.tab_typings;
                case 1:
                  //  return R.drawable.tab_onlines;
                case 2:
                    return R.drawable.tab_friends;
                default:
                    return R.drawable.tab_vkpsy;
            }

        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new DialogsFragment();
                case 1:
                    //dialogsFragment = new OnlinesFragment();
                    //return dialogsFragment;
                case 2:
                    UsersListFragment fragment = new UsersListFragment();
                    Memory.addUsersListener(fragment.getListener());
                    return fragment;
                default:
                    return new MainFragment();

            }


        }

    }

*/
}

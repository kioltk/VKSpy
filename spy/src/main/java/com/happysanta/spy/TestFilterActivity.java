package com.happysanta.spy;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.happysanta.spy.Adapters.CustomItems.FilterUserItem;
import com.happysanta.spy.Adapters.CustomItems.Item;
import com.happysanta.spy.Adapters.ItemHelper;
import com.happysanta.spy.Adapters.UserListAdapter;
import com.happysanta.spy.Core.Helper;
import com.happysanta.spy.Core.Memory;
import com.happysanta.spy.Fragments.Interfaces.LoadImagesOnScrollListener;
import com.happysanta.spy.Fragments.UsersListFragment;
import com.happysanta.spy.Receivers.NetworkStateReceiver;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.viewpagerindicator.ContentPagerAdapter;
import com.viewpagerindicator.TabPageIndicator;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;


public class TestFilterActivity extends ActionBarActivity {


    protected MenuItem applyButton;
    protected ArrayList<VKApiUserFull> selectedUsers = new ArrayList<VKApiUserFull>();
    protected ArrayList<VKApiUserFull> selectedFriends = new ArrayList<VKApiUserFull>();
    protected ArrayList<VKApiUserFull> selectedExternals = new ArrayList<VKApiUserFull>();
    protected FilterFriendsFragment friendsFragment;
    protected FilterExternalsFragment externalsFragment;
    protected Menu menu;
    protected MenuItem deselectItem;
    protected MenuItem selectItem;
    protected boolean loading = false;
    protected boolean changed = false;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    private FilterFriendsFragment currentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_filter);

        // Set up the action bar.
        //final ActionBar actionBar = getActionBar();
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        TabPageIndicator tabPager = (TabPageIndicator) findViewById(R.id.pager_indicator);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabPager.setViewPager(mViewPager);

        tabPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if(position==0)
                    currentItem = friendsFragment;
                else
                    currentItem = externalsFragment;
                updateActionBarButtons();

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        /*
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });
        */


    }




    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends android.support.v4.app.FragmentPagerAdapter implements ContentPagerAdapter<String> {

        public SectionsPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            switch (position) {
                case 1:
                    externalsFragment = new FilterExternalsFragment();
                    return externalsFragment;
                case 0:
                default:
                    friendsFragment =  new FilterFriendsFragment();
                    return friendsFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public String getContent(int position) {
            return String.valueOf(getPageTitle(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.friends).toUpperCase(l);
                case 1:
                    return getString(R.string.external).toUpperCase(l);
            }
            return null;
        }
    }

    public boolean trackClicked(VKApiUserFull user) {
        Boolean tracked = false;

        if (user.isFriend) {
            if (selectedFriends.contains(user))
                selectedFriends.remove(user);
            else {
                selectedFriends.add(user);
                tracked = true;
            }
        }
        else
            if (selectedExternals.contains(user))
                selectedExternals.remove(user);
            else {
                selectedExternals.add(user);
                tracked = true;
            }
        return tracked;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filter, menu);
        applyButton = menu.getItem(0);
        applyButton.setActionView(R.layout.action_bar_apply);
        applyButton.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(applyButton);

            }
        });
        this.menu = menu;
        updateActionBarInfo();
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(loading)
            return true;
        if(friendsFragment ==null)
            return true;
        int id = item.getItemId();

        switch (id) {
            //case R.id.addUser
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.addButton:
                showAddUser(null);
                break;
            case R.id.select:
                changed = true;
                selectedFriends.clear();
                for (VKApiUserFull user : friendsFragment.users) {
                    trackClicked(user);
                }
                friendsFragment.selectAll();
                updateActionBarInfo();
                break;
            case R.id.deselect:
                changed = true;
                selectedFriends.clear();
                friendsFragment.deselectAll();
                updateActionBarInfo();
                break;

            case R.id.apply:

                loading = true;
                if(!changed) {
                    finish();
                    break;
                }
                View applyView = applyButton.getActionView();
                applyView.findViewById(R.id.image).setVisibility(View.GONE);
                applyView.findViewById(R.id.loading).setVisibility(View.VISIBLE);
                findViewById(R.id.block).setVisibility(View.VISIBLE);
                final Handler handler = new Handler();
                new Thread(new Runnable() {
                    @Override
                    public void run() {


                        Memory.setTracked(selectedUsers);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Helper.trackedUpdated();
                                finish();
                            }
                        });
                    }
                }).start();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void updateActionBarButtons() {
        if (menu != null && friendsFragment !=null && externalsFragment != null)
            if (currentItem instanceof FilterExternalsFragment) {
                menu.removeItem(R.id.select);
                menu.removeItem(R.id.deselect);
                menu.removeItem(R.id.addButton);
                menu.add(0, R.id.addButton, 1, R.string.adduser);
                menu.getItem(1).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.getItem(1).setIcon(R.drawable.ic_ab_add);
            } else {
                menu.removeItem(R.id.addButton);
                if (selectedUsers.size() == friendsFragment.users.size()) {
                    menu.removeItem(R.id.select);
                    if (menu.findItem(R.id.deselect) == null) {
                        menu.add(0, R.id.deselect, 2, R.string.deselect_all);
                    }
                } else {
                    if (menu.findItem(R.id.select) == null) {
                        menu.add(0, R.id.select, 1, R.string.select_all);
                    }
                    if (selectedUsers.size() == 0) {
                        menu.removeItem(R.id.deselect);
                    } else {

                        if (menu.findItem(R.id.deselect) == null) {
                            menu.add(0, R.id.deselect, 2, R.string.deselect_all);
                        }
                    }

                }
            }
    }
    private void updateActionBarInfo() {
        if (applyButton != null) {
            selectedUsers.clear();
            selectedUsers.addAll(selectedFriends);
            selectedUsers.addAll(selectedExternals);

            getSupportActionBar().setTitle((Helper.getQuantityString(R.plurals.usersSelected, selectedUsers.size(), selectedUsers.size())));
            updateActionBarButtons();
        }
        /*

            */
    }

    public static class FilterFriendsFragment extends UsersListFragment {
        TestFilterActivity activity;
        private ListView listView;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            activity = (TestFilterActivity) getActivity();
        }
        protected ListView getListView(){
            if(rootView!=null){
                return (ListView) rootView.findViewById(com.happysanta.spy.R.id.list);
            }
            return null;
        }

        @Override
        protected UserListAdapter adapter() {
            return new UserListAdapter(new ItemHelper.ObservableFilterUsersArray(users, true), getActivity());
        }

        @Override
        protected void create() {
            if(rootView==null)
                return;
            listView = getListView();
            if(this instanceof FilterExternalsFragment){
                activity.selectedExternals.clear();
            }else{
                activity.selectedFriends.clear();
            }

            if(listView!=null)
                if (!users.isEmpty()) {

                    for (VKApiUserFull user : users) {
                        if(user.isTracked()) {
                            if (user.isFriend) {
                                activity.selectedFriends.add(user);
                            }else{
                                activity.selectedExternals.add(user);
                            }
                        }
                    }
                    listView.setAdapter(null);
                    //listView = (ListView) rootView.findViewById(R.id.list);
                    adapter = adapter();
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (id == 0)
                                return;
                            VKApiUserFull user = Memory.getUserById((int) id);
                            if(user==null)
                                return;
                            activity.changed = true;
                            boolean tracked = activity.trackClicked(user);
                            for (Item item : adapter.getItems()) {
                                if (item instanceof FilterUserItem) {


                                    FilterUserItem filterItem = (FilterUserItem) item;
                                    if (filterItem.getId() == id) {
                                        filterItem.setTracked(tracked, view);
                                    }
                                }
                            }
                            activity.updateActionBarInfo();


                        }
                    });
                    listView.setOnScrollListener(new LoadImagesOnScrollListener(listView));
                    listView.setAdapter(adapter);
                    listView.setVisibility(View.VISIBLE);
                    rootView.findViewById(R.id.loading).setVisibility(View.GONE);
                    rootView.findViewById(R.id.status).setVisibility(View.GONE);
                } else {
                    showNoUsers();
                }
        }

        @Override
        public void recreate() {
            super.recreate();
            activity.updateActionBarInfo();
        }

        @Override
        public int getListEmpty() {
            return com.happysanta.spy.R.string.no_friends;
        }

        public void selectAll() {
            //adapter.
            listView = getListView();
            if(listView==null){
                return;
            }
            int arrayPosition = listView.getFirstVisiblePosition();
            int viewPosition = 0;
            int visibleCount = listView.getLastVisiblePosition() - arrayPosition + 1;
            for(int i = 0; i < visibleCount;i++){
                Object item = listView.getItemAtPosition(arrayPosition+i);
                if(item instanceof FilterUserItem){
                    FilterUserItem filter = (FilterUserItem) item;
                    filter.setTracked(true,listView.getChildAt(i));
                }
            }
            for(Object item: adapter.getItems()){
                if(item instanceof FilterUserItem){
                    ((FilterUserItem)item).setTracked(true,null);
                }
            }
        }
        public void deselectAll() {
            //adapter.
            listView = getListView();
            if(listView==null){
                return;
            }
            int arrayPosition = listView.getFirstVisiblePosition();
            int viewPosition = 0;
            int visibleCount = listView.getLastVisiblePosition() - arrayPosition + 1;
            for(int i = 0; i < visibleCount;i++){
                Object item = listView.getItemAtPosition(arrayPosition+i);
                if(item instanceof FilterUserItem){
                    FilterUserItem filter = (FilterUserItem) item;
                    filter.setTracked(false,listView.getChildAt(i));
                }
            }

            for(Object item: adapter.getItems()){
                if(item instanceof FilterUserItem){
                    ((FilterUserItem)item).setTracked(false,null);
                }
            }
        }
    }


    public static class FilterExternalsFragment extends FilterFriendsFragment{
        @Override
        public void loadUsers() {
            users = Memory.getUnfriends();
        }

        @Override
        public int getListEmpty() {
            return R.string.no_tracks;
        }

        @Override
        protected UserListAdapter adapter() {
            return new UserListAdapter(new ItemHelper.ObservableFilterUsersArray(users, false), getActivity());
        }

        @Override
        public void showNoUsers() {
            {
                ((TextView) rootView.findViewById(R.id.status)).setText(getListEmpty());
                rootView.findViewById(R.id.status).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.loading).setVisibility(View.GONE);
            }
        }
    }



    private AlertDialog addingDialog;
    EditText idBox;
    public void showAddUser(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View idBoxHolder = getIdBox();
        builder.setView(idBoxHolder);
        builder.setTitle(R.string.adduser);
        builder.setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String parsedId = parseId(String.valueOf(idBox.getText()));
                idBox.clearFocus();
                if(parsedId!=null){
                    loadUser(parsedId);
                }else{
                    showError(ADDING_ERROR_PARSER);
                }
            }
        });
        addingDialog = builder.create();
        addingDialog.setCancelable(true);
        addingDialog.setCanceledOnTouchOutside(true);
        addingDialog.show();


    }
    private void loadUser(String parsedId) {
        final ProgressDialog loadingDialog = new ProgressDialog(this);
        loadingDialog.setIndeterminate(true);
        loadingDialog.setMessage(getString(R.string.loading));
        loadingDialog.setTitle(R.string.adduser);
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);
        VKParameters parameters = new VKParameters();
        parameters.put(VKApiConst.USER_IDS, parsedId);
        parameters.put("fields", "sex,photo_200,photo_200_orig,photo_50,photo_100,online,last_seen");

        VKRequest donwloadUserRequest = VKApi.users().get(parameters);
        donwloadUserRequest.executeWithListener(new VKRequest.VKRequestListener() {
                    public NetworkStateReceiver.NetworkStateChangeListener connectionListener;

                    @Override
                    public void onComplete(final VKResponse response) {

                        VKApiUserFull user = ((VKList<VKApiUserFull>) response.parsedModel).get(0);
                        if(user.id==1)
                            showError(ADDING_ERROR_DUROV);
                        else
                        if(user.is_deleted || user.is_banned){
                            showError(ADDING_ERROR_DELETED);
                        }else
                        if(Memory.users.getById(user.id)==null){
                            showUser(user);
                        }else
                            showError(ADDING_ERROR_EXISTS);
                        loadingDialog.dismiss();

                        //   if (connectionListener != null)
                        //     connectionListener.remove();
                    }

                    @Override
                    public void onError(VKError error) {
                        super.onError(error);
                        //unknownhost
                        loadingDialog.dismiss();
                        showError(ADDING_ERROR_LOADING);
                        if (error.httpError instanceof SocketException || error.httpError instanceof UnknownHostException) {

                              /*  connectionListener = new NetworkStateReceiver.NetworkStateChangeListener(USER_DOWNLOADER_ID) {
                                    @Override
                                    public void onConnected() {

                                        downloadUser(userid);

                                    }

                                    @Override
                                    public void onLost() {
                                    }

                                };
                                */
                            }

                            if (error.httpError != null)
                                Log.e("AGCY SPY", error.httpError.toString());
                            else

                                Log.e("AGCY SPY", error.toString());

                            }

                    });

        addingDialog = loadingDialog;
        addingDialog.show();

    }

    public String parseId(String id){
        String parsedId = null;

        if(id.contains(" "))
            return parsedId;

        if(!id.contains("vk.com")){
            parsedId = id;
            return parsedId;
        }
        if(id.contains("vk.com/id")){
            parsedId = id.substring(id.indexOf("id")+2);
            return parsedId;
        }

        return parsedId;
    }

    /**
     *

    public void searchUser(TextView v){

        String id = String.valueOf(v.getText());
        String parsedId = parseId(id);
        if(parsedId!=null && !parsedId.equals("")){
            idBox.findViewById(R.id.search_holder).setVisibility(View.GONE);
            idBox.findViewById(R.id.loading).setVisibility(View.VISIBLE);
            VKParameters parameters = new VKParameters();
            parameters.put(VKApiConst.USER_IDS, parsedId);
            parameters.put("fields", "sex,photo_200,photo_200_orig,photo_50,photo_100,online,last_seen");

            VKRequest donwloadUserRequest = VKApi.users().get(parameters);
            donwloadUserRequest.executeWithListener(
                    new VKRequest.VKRequestListener() {
                        public NetworkStateReceiver.NetworkStateChangeListener connectionListener;

                        @Override
                        public void onComplete(final VKResponse response) {

                            VKApiUserFull user = ((VKList<VKApiUserFull>) response.parsedModel).get(0);
                            showUser(user);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {

                                    //Memory.saveUser();
                                    //Memory.downloadingUsersIds.remove((Integer) userid);

                                }
                            }).start();
                         //   if (connectionListener != null)
                           //     connectionListener.remove();
                        }

                        @Override
                        public void onError(VKError error) {
                            super.onError(error);
                            //unknownhost

                            if (error.httpError instanceof SocketException || error.httpError instanceof UnknownHostException) {

                              /*  connectionListener = new NetworkStateReceiver.NetworkStateChangeListener(USER_DOWNLOADER_ID) {
                                    @Override
                                    public void onConnected() {

                                        downloadUser(userid);

                                    }

                                    @Override
                                    public void onLost() {
                                    }

                                };
                            }

                            if (error.httpError != null)
                                Log.e("AGCY SPY", error.httpError.toString());
                            else

                                Log.e("AGCY SPY", error.toString());

                            }
                        }
                    }
            );

        }else{
            v.setText("");
        }

    }

    /**

     */
    private static final int ADDING_ERROR_PARSER = 10;
    private static final int ADDING_ERROR_LOADING = 11;
    private static final int ADDING_ERROR_EXISTS = 12;
    private static final int ADDING_ERROR_DELETED = 13;
    private static final int ADDING_ERROR_DUROV = 14;
    private void showError(int errorCode){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(R.string.error);
        builder.setNegativeButton(R.string.ok,null);
        switch (errorCode){
            case ADDING_ERROR_EXISTS:
                builder.setMessage(R.string.error_user_exists);
                break;
            case ADDING_ERROR_LOADING:
                builder.setMessage(R.string.error_user_loading);
                break;
            case ADDING_ERROR_PARSER:
                builder.setMessage(R.string.error_user_idparsing);
                break;
            case ADDING_ERROR_DELETED:
                builder.setMessage(R.string.error_user_deleted);
                break;
            case ADDING_ERROR_DUROV:
                builder.setMessage(R.string.error_user_durov);
                break;
        }
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(true);
        alert.show();
    }
    private void showUser(final VKApiUserFull user) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View userView = getUserView(user);
        builder.setView(userView);
        builder.setTitle(R.string.adduser);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(R.string.spyon, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                user.tracked= true;
                final Handler handler = new Handler();
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Memory.saveUser(user);
                        //Memory.downloadingUsersIds.remove((Integer) userid);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                Memory.setStatus(user,user.online,user.last_seen, null);
                                externalsFragment.recreate();
                            }
                        });

                    }
                }).start();

            }
        });
        addingDialog = builder.create();
        addingDialog.setCancelable(true);
        addingDialog.setCanceledOnTouchOutside(true);
        addingDialog.show();






    }

    private View getUserView(VKApiUserFull user) {
        LayoutInflater inflater = getLayoutInflater();

        View userView = inflater.inflate(R.layout.addnew_userview, null);
        /*
        final EditText idBox = (EditText) addUserView.findViewById(R.id.idbox);
        idBox.setImeActionLabel("Search", KeyEvent.KEYCODE_ENTER);
        idBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                    //searchUser(v);

                    return true;
                }
                return false;
            }
        });
        Button searchButton = (Button) addUserView.findViewById(R.id.search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //searchUser(idBox);
            }
        });
        */

        ImageView photoView = (ImageView) userView.findViewById(R.id.photo);
        TextView nameView = (TextView) userView.findViewById(R.id.name);

        nameView.setText(user.toString());
        ImageLoader.getInstance().displayImage(user.getBiggestPhoto(), photoView);
        return userView;

    }
    public View getIdBox(){

        LayoutInflater inflater = getLayoutInflater();

        View idBoxHolder = inflater.inflate(R.layout.addnew, null);
        idBox = (EditText) idBoxHolder.findViewById(R.id.idbox);
        /*
        final EditText idBox = (EditText) addUserView.findViewById(R.id.idbox);
        idBox.setImeActionLabel("Search", KeyEvent.KEYCODE_ENTER);
        idBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                    //searchUser(v);

                    return true;
                }
                return false;
            }
        });
        Button searchButton = (Button) addUserView.findViewById(R.id.search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //searchUser(idBox);
            }
        });
        */
        return idBoxHolder;


    }
}

package com.agcy.vkproject.spy;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.agcy.vkproject.spy.Adapters.CustomItems.FilterUserItem;
import com.agcy.vkproject.spy.Adapters.ItemHelper;
import com.agcy.vkproject.spy.Adapters.UserListAdapter;
import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.Fragments.Interfaces.LoadImagesOnScrollListener;
import com.agcy.vkproject.spy.Fragments.UsersListFragment;
import com.vk.sdk.api.model.VKApiUserFull;

import java.util.ArrayList;


public class FilterActivity extends ActionBarActivity {

    private MenuItem applyButton;
    private ArrayList<VKApiUserFull> ids = new ArrayList<VKApiUserFull>();
    private FilterUsersFragment fragment;
    private Menu menu;
    private MenuItem deselectItem;
    private MenuItem selectItem;
    private boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);
        closeOptionsMenu();
        if (savedInstanceState == null) {

            fragment = new FilterUsersFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }else{

        }
    }

    public boolean trackClicked(VKApiUserFull user){
        Boolean tracked = false;
        if(ids.contains(user))
            ids.remove(user);
        else{
            ids.add(user);
            tracked = true;
        }
        return tracked;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filter, menu);
        applyButton = menu.getItem(0);
        applyButton.setActionView(R.layout.apply);
        applyButton.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(applyButton);
            }
        });
        this.menu = menu;
        updateActionBarInfo();
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(loading)
            return true;
        int id = item.getItemId();

        switch (id) {

            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.select:

                ids.clear();
                for (VKApiUserFull user : fragment.users) {
                    trackClicked(user);
                }
                fragment.selectAll();
                updateActionBarInfo();
                break;
            case R.id.deselect:
                ids.clear();
                fragment.deselectAll();
                updateActionBarInfo();
                break;

            case R.id.apply:

                loading = true;

                View applyView = applyButton.getActionView();
                applyView.findViewById(R.id.image).setVisibility(View.GONE);
                applyView.findViewById(R.id.loading).setVisibility(View.VISIBLE);
                findViewById(R.id.block).setVisibility(View.VISIBLE);
                final Handler handler = new Handler();
                new Thread(new Runnable() {
                    @Override
                    public void run() {


                        Memory.setTracked(ids);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class FilterUsersFragment extends UsersListFragment {
        FilterActivity activity;
        private ListView listView;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            activity = (FilterActivity) getActivity();
        }

        @Override
        protected void create() {
            if (!users.isEmpty()) {

                for (VKApiUserFull user : users) {
                    if(user.isTracked())
                        activity.ids.add(user);
                }

                activity.updateActionBarInfo();
                listView = (ListView) rootView.findViewById(R.id.list);
                adapter = new UserListAdapter(new ItemHelper.ObservableFilterUsersArray(users), getActivity());
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        FilterUserItem item = (FilterUserItem) parent.getItemAtPosition(position);
                        item.setTracked(activity.trackClicked(item.getContent()), view);
                        activity.updateActionBarInfo();

                    }
                });
                listView.setOnScrollListener(new LoadImagesOnScrollListener(listView));
                listView.setAdapter(adapter);
                rootView.findViewById(R.id.loading).setVisibility(View.GONE);
            } else {
                rootView.findViewById(R.id.loading).setVisibility(View.VISIBLE);
            }
        }
        public void selectAll() {
            //adapter.
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
    private void updateActionBarInfo() {
        if (applyButton != null) {
            getSupportActionBar().setTitle((Helper.getQuantityString(R.plurals.usersSelected, ids.size(), ids.size())));

        }
        if(menu!=null)
            if(ids.size() == fragment.users.size()) {
                menu.removeItem(R.id.select);
                if(menu.findItem(R.id.deselect)==null) {
                    menu.add(0, R.id.deselect, 2, R.string.deselect_all);
                }
            }else{
                if(menu.findItem(R.id.select)==null) {
                    menu.add(0, R.id.select, 1, R.string.select_all);
                }
                if(ids.size()==0){
                    menu.removeItem(R.id.deselect);
                }else{

                    if(menu.findItem(R.id.deselect)==null) {
                        menu.add(0, R.id.deselect, 2, R.string.deselect_all);
                    }
                }

            }
    }
}

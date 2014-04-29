package com.agcy.vkproject.spy;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.agcy.vkproject.spy.Adapters.UpdatesWithOwnerAdapter;
import com.agcy.vkproject.spy.Core.Helper;
import com.agcy.vkproject.spy.Core.Memory;
import com.agcy.vkproject.spy.Models.Online;

import java.util.ArrayList;


public class AllActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }



    /**
     * A placeholder fragment containing a simple view.
     */
    public class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_all, container, false);

            final ArrayList<Online> onlines = Helper.orderOnlines(Memory.getOnlines(),false);

            final ListView list = (ListView) rootView.findViewById(R.id.list);
            list.setAdapter(new UpdatesWithOwnerAdapter(onlines, getBaseContext()));

            Button showOnlines = (Button) rootView.findViewById(R.id.showOnlines);
            showOnlines.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    list.setAdapter(new UpdatesWithOwnerAdapter(onlines, getBaseContext()));
                }
            });
            Button showTypings = (Button) rootView.findViewById(R.id.showTypings);
            showTypings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    list.setAdapter(new UpdatesWithOwnerAdapter(Memory.getTyping(), getBaseContext()));
                }
            });
            return rootView;
        }
    }
}

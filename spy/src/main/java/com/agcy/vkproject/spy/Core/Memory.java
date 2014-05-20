package com.agcy.vkproject.spy.Core;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.agcy.vkproject.spy.Adapters.UpdatesAdapter;
import com.agcy.vkproject.spy.Models.Online;
import com.agcy.vkproject.spy.Models.Status;
import com.agcy.vkproject.spy.Models.Track;
import com.agcy.vkproject.spy.Models.Typing;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKUsersArray;

import java.util.ArrayList;


public class Memory {


    public static VKUsersArray users = new VKUsersArray();
    public static ArrayList<Track> tracks = new ArrayList<Track>();
    private static Context context;
    private static DatabaseConnector databaseConnector;
    private static SQLiteDatabase database;
    public static ArrayList<Integer> downloadingIds = new ArrayList<Integer>();


    public static void initialize(Context context) {

        Log.i("AGCY SPY SQL","Initialization");
        Memory.context = context;
        databaseConnector = new DatabaseConnector(context);
        open();
        close();

    }

    public static void DESTROY() {
        context.deleteDatabase(DatabaseConnector.DATABASE);
        context = null;
    }
    public static void clearAll(){

        Log.i("AGCY SPY SQL","Clearing databases.");
        clearUsers();
        clearTypings();
        clearOnlines();
    }

    private static ArrayList<UpdatesAdapter.NewItemListener> onlineListeners = new ArrayList<UpdatesAdapter.NewItemListener>();
    private static ArrayList<UpdatesAdapter.NewItemListener> onlineOnceListeners = new ArrayList<UpdatesAdapter.NewItemListener>();

    public static void addOnlineOnceListener(UpdatesAdapter.NewItemListener onlineOnceListener) {
        if(!onlineOnceListeners.contains(onlineOnceListener))
            onlineOnceListeners.add(onlineOnceListener);
    }
    public static void addOnlineListener(UpdatesAdapter.NewItemListener onlineListener) {
        if(!onlineListeners.contains(onlineListener))
            onlineListeners.add(onlineListener);
    }
    public static void removeOnlineListener(UpdatesAdapter.NewItemListener onlineListener){
        onlineListeners.remove(onlineListener);
    }

    //region Interface
    public static Boolean loadUsers() {

        ArrayList<VKApiUserFull> loadedUsers = new ArrayList<VKApiUserFull>();
        final Cursor cursor = getCursor(DatabaseConnector.USER_DATABASE,
                DatabaseConnector.USER_DATABASE_FIELDS, null, "hint");

        final int useridColumnIndex = cursor.getColumnIndex("userid");
        final int sexColumnIndex = cursor.getColumnIndex("sex");
        final int photoColumnIndex = cursor.getColumnIndex("photo");
        final int first_nameColumnIndex = cursor.getColumnIndex("first_name");
        final int last_nameColumnIndex = cursor.getColumnIndex("last_name");
        final int isFriend_nameColumnIndex = cursor.getColumnIndex("isFriend");
        if (cursor.moveToFirst())
            do {
                VKApiUserFull user = new VKApiUserFull() {{
                    this.id = cursor.getInt(useridColumnIndex);
                    this.photo_200 = cursor.getString(photoColumnIndex);
                    this.first_name = cursor.getString(first_nameColumnIndex);
                    this.last_name = cursor.getString(last_nameColumnIndex);
                    this.sex = cursor.getInt(sexColumnIndex);
                    this.isFriend = cursor.getInt(isFriend_nameColumnIndex)>0;
                }};
                if (users.getById(user.id) == null)
                    loadedUsers.add(user);

            } while (cursor.moveToNext());
        close();
        users.addAll(loadedUsers);
        return !users.isEmpty();
    }

    public static void loadTracks() {

        Cursor cursor = getCursor(DatabaseConnector.TRACK_DATABASE,
                DatabaseConnector.TRACK_DATABASE_FIELDS,
                null,
                null);
        int useridColumnIndex = cursor.getColumnIndex("userid");
        int notificationColumnIndex = cursor.getColumnIndex("notification");


        Track track;
        if (cursor.moveToFirst())
            do {
                int userid = cursor.getInt(useridColumnIndex);
                Boolean notification = cursor.getInt(notificationColumnIndex) > 0;
                track = new Track(userid, notification);
                tracks.add(track);
            } while (cursor.moveToNext());
        close();
    }

    public static VKApiUserFull getUserById(int userid) {
        if (users.isEmpty()) {
            loadUsers();
        }
        VKApiUserFull user = users.getById(userid);
        if (user == null) {
            if(!downloadingIds.contains(userid)) {
                Helper.downloadUser(userid);
                downloadingIds.add(userid);
            }
            Log.w("AGCY SPY", "No such userid in memory: userid = " + userid);
            user = new VKApiUserFull();
            user.id = userid;

        }
        return user;
    }

    public static ArrayList<VKApiUserFull> getUsers() {
        ArrayList<VKApiUserFull> arrayUsers = new ArrayList<VKApiUserFull>(users);
        return arrayUsers;
    }
    public static ArrayList<VKApiUserFull> getFriends(){

        ArrayList<VKApiUserFull> friends = new ArrayList<VKApiUserFull>();
        for(VKApiUserFull user: users){
            if(user.isFriend)
                friends.add(user);
        }

        return friends;
    }
    public static Track getTrackById(int userid) {
        if (tracks.isEmpty())
            loadTracks();
        for (Track track : tracks) {
            if (track.userid == userid) {
                return track;
            }
        }
        return null;
    }

    public static Boolean isTracked(int userid) {

        if (tracks.isEmpty())
            loadTracks();
        Track track = getTrackById(userid);
        if (track == null)
            return false;
        return track.notification;

    }

    public static Boolean isTracked(VKApiUserFull user) {
        return isTracked(user.id);
    }

    //endregion
    //region Getters
    public static Cursor getCursor(String databaseName, String[] fields, String selector, String orderby) {
        open();
        Cursor cursor;
        try {
            cursor = database.query(databaseName, fields, selector, null, null, null, orderby);
        }
        catch(Exception exp) {
            Log.e("AGCY SPY SQL", "Cursor error " + exp.toString());
            close();
            open();
            return getCursor(databaseName,fields,selector,orderby);
        }
        return cursor;
        // don't forget to close !!1
    }

    public static ArrayList<Online> getOnlines() {

        Log.i("AGCY SPY SQL", "Loading onlines");
        return getOnlines(null, "id desc");
    }

    public static ArrayList<Online> getOnlines(final int userid) {

        Log.i("AGCY SPY SQL", "Loading onlines by userid:"+ userid);
        return getOnlines("userid = " + userid, "id desc");
    }

    public static ArrayList<Online> getOnlines(String selector, String order) {

        Cursor cursor = getCursor(DatabaseConnector.ONLINE_DATABASE,
                DatabaseConnector.ONLINE_DATABASE_FIELDS,
                selector,
                order);
        int useridColumnIndex = cursor.getColumnIndex("userid");
        int sinceColumnIndex = cursor.getColumnIndex("since");
        int tillColumnIndex = cursor.getColumnIndex("till");

        ArrayList<Online> onlines = new ArrayList<Online>();

        if (cursor.moveToFirst())
            do {
                int userid = cursor.getInt(useridColumnIndex);
                int since = cursor.getInt(sinceColumnIndex);
                int till = cursor.getInt(tillColumnIndex);
                onlines.add(new Online(userid,since,till));
                } while (cursor.moveToNext());

        close();
        return onlines;

    }

    public static Track getTrack(int userid) {

        Log.i("AGCY SPY SQL", "Loading track");
        Cursor cursor = getCursor(DatabaseConnector.TRACK_DATABASE,
                DatabaseConnector.TRACK_DATABASE_FIELDS,
                "userid = " + userid,
                null);

        int useridColumnIndex = cursor.getColumnIndex("userid");
        int notificationColumnIndex = cursor.getColumnIndex("notification");

        Track track = null;
        if (cursor.moveToFirst()) {

            Boolean notification = cursor.getInt(notificationColumnIndex) > 0;
            track = new Track(userid, notification);
        }
        close();
        return track;
    }

    public static ArrayList<Typing> getTyping() {
        Log.i("AGCY SPY SQL", "Loading typings");
        return getTyping(null, null);
    }

    public static ArrayList<Typing> getTyping(int userid) {
        Log.i("AGCY SPY SQL", "Loading typings by userid:" + userid);
        return getTyping("userid = " + userid, null);
    }

    public static ArrayList<Typing> getTyping(String selector, String order) {

        Cursor cursor = getCursor(DatabaseConnector.TYPING_DATABASE,
                DatabaseConnector.TYPING_DATABASE_FIELDS,
                selector,
                "time desc");
        int useridColumnIndex = cursor.getColumnIndex("userid");
        int timeColumnIndex = cursor.getColumnIndex("time");

        ArrayList<Typing> typings = new ArrayList<Typing>();

        if (cursor.moveToFirst())
            do {
                int userid = cursor.getInt(useridColumnIndex);
                int time = cursor.getInt(timeColumnIndex);
                Typing typing = new Typing(userid, time);
                typings.add(typing);
            } while (cursor.moveToNext());
        close();
        return typings;
    }
    //endregion
    //region Setters

    public static Boolean setTracked(int userid) {
        Track track = getTrackById(userid);
        if (track == null) {
            track = new Track(userid, true);
            saveTrack(track);
            tracks.add(track);
        } else
            updateTrack(track.toggle());
        return track.notification;
    }


    public static void setStatus(VKApiUserFull user, Boolean online, boolean timeout) {

        user.online = online;
        long time = Helper.getUnixNow() - (timeout ? 15 * 60 : 0);
        if (online) {
            saveStatus(user, true, (int) time);
        } else {
            updateStatus(user, online, time);
            user.last_seen = Helper.getUnixNow();
        }

        Status status = new Status(user.id, (int) time, online);
        for(UpdatesAdapter.NewItemListener onlineListener: onlineListeners){
            onlineListener.newItem(status);
        }
        for(UpdatesAdapter.NewItemListener onlineListener: onlineOnceListeners){
            onlineListener.newItem(status);
        }
        onlineOnceListeners.clear();

    }

    //endregion
    //region Updaters

    private static void update(String databaseName, ContentValues values, String primaryKeyName, String primaryKeyValue) {
        open();
        database.update(databaseName, values, primaryKeyName + " = " + primaryKeyValue, null);
        close();
    }

    public static void updateTrack(Track track) {


        Log.i("AGCY SPY SQL","Updating track.");
        ContentValues values = new ContentValues();
        values.put("notification", track.notification);

        update(DatabaseConnector.TRACK_DATABASE, values, "userid", String.valueOf(track.userid));


    }
    public static void updateStatus(VKApiUserFull user, Boolean online, long time){


        Log.i("AGCY SPY SQL","Updating status.");
        SharedPreferences preferences = context.getSharedPreferences("longpoll", Context.MODE_MULTI_PROCESS);

        int lastUpdate = preferences.getInt("lastUpdate",0);
        if(Helper.getUnixNow() - lastUpdate > 30*60){
            saveStatus(user,online, (int) time);
        }else{
            Cursor cursor = getCursor(DatabaseConnector.ONLINE_DATABASE,
                    DatabaseConnector.ONLINE_DATABASE_FIELDS,
                    "userid = " + user.id,
                    "id desc");
            int idColumnIndex = cursor.getColumnIndex("id");
            int useridColumnIndex = cursor.getColumnIndex("userid");
            int sinceColumnIndex = cursor.getColumnIndex("since");
            int tillColumnIndex = cursor.getColumnIndex("till");

            if (cursor.moveToFirst()) {
                int lastOnline = cursor.getInt(sinceColumnIndex);
                int lastOffline = cursor.getInt(tillColumnIndex);

                if (lastOffline != 0) {
                    saveStatus(user, online, (int) time);
                }else {
                    int id = cursor.getInt(idColumnIndex);
                    ContentValues updateValues = new ContentValues();
                    updateValues.put("userid", cursor.getInt(useridColumnIndex));
                    updateValues.put("since", lastOnline);
                    updateValues.put("till", time);
                    update(DatabaseConnector.ONLINE_DATABASE, updateValues, "id", String.valueOf(id));
                }

            }else{
                saveStatus(user,online, (int) time);
            }
            close();
        }
        // did we lost connection half hour ago?

    }
    //endregion
    //region Savers
    private static void save(String databaseName, ContentValues values) {
        open();

        database.insert(databaseName, null, values);

        close();
    }

    private static void save(String databaseName, ArrayList<ContentValues> valuesList) {

        open();
        for (ContentValues contentValues : valuesList) {
            database.insert(databaseName, null, contentValues);
        }
        close();
    }

    public static void saveUser(VKApiUserFull user) {

        Log.i("AGCY SPY SQL","Saving user.");
        ContentValues values = new ContentValues();
        values.put("userid", user.id);
        values.put("sex", user.sex);
        values.put("photo", user.getBiggestPhoto());
        values.put("first_name", user.first_name);
        values.put("last_name", user.last_name);
        values.put("isFriend", user.isFriend);
        save(DatabaseConnector.USER_DATABASE, values);
        users.add(user);
    }

    public static void saveFriends(VKUsersArray friends){
        for(VKApiUserFull friend : friends){
            friend.isFriend = true;
        }
        saveUsers(friends);
    }

    public static void saveUsers(VKUsersArray newUsers) {
        VKUsersArray alreadyStoredUsers = new VKUsersArray(Memory.users);
        for (VKApiUserFull alreadyStoredUser : alreadyStoredUsers) {
            VKApiUserFull storedOld = newUsers.getById(alreadyStoredUser.id);
            if (storedOld == null) {
                newUsers.add(alreadyStoredUser);
            }
        }
        Memory.users = newUsers;

        Log.i("AGCY SPY SQL","Saving new users. Count: "+newUsers.size());
        ArrayList<ContentValues> valuesList = new ArrayList<ContentValues>();
        for (int i = 0; i < Memory.users.size(); i++) {
            VKApiUserFull user = Memory.users.get(i);
            ContentValues values = new ContentValues();
            values.put("userid", user.id);
            values.put("sex", user.sex);
            values.put("photo", user.getBiggestPhoto());
            values.put("first_name", user.first_name);
            values.put("last_name", user.last_name);
            values.put("isFriend",user.isFriend);
            values.put("hint", i);
            valuesList.add(values);
        }

        clearDatabase(DatabaseConnector.USER_DATABASE, DatabaseConnector.USER_DATABASE_CREATE);
        save(DatabaseConnector.USER_DATABASE, valuesList);

    }

    public static void saveStatus(VKApiUserFull user, boolean status, int time) {


        Log.i("AGCY SPY SQL","Saving online.");
        ContentValues values = new ContentValues();
        values.put("userid", user.id);
        values.put(status? "since":"till", time);
        save(DatabaseConnector.ONLINE_DATABASE, values);
    }

    public static void saveTrack(Track track) {


        Log.i("AGCY SPY SQL","Saving track.");
        ContentValues values = new ContentValues();
        values.put("userid", track.userid);
        values.put("notification", track.notification);
        save(DatabaseConnector.TRACK_DATABASE, values);
    }

    public static void saveTyping(VKApiUserFull user) {

        Log.i("AGCY SPY SQL","Saving typing.");
        ContentValues values = new ContentValues();
        values.put("userid", user.id);
        values.put("time", Helper.getUnixNow() - 3 * 60);

        save(DatabaseConnector.TYPING_DATABASE, values);
    }
    //endregion
    //region Clearer

    public static void clearUsers() {
        users.clear();
        clearDatabase(DatabaseConnector.USER_DATABASE, DatabaseConnector.USER_DATABASE_CREATE);
    }

    public static void clearTypings() {
        clearDatabase(DatabaseConnector.TYPING_DATABASE, DatabaseConnector.TYPING_DATABASE_CREATE);
    }

    public static void clearOnlines() {
        clearDatabase(DatabaseConnector.ONLINE_DATABASE, DatabaseConnector.ONLINE_DATABASE_CREATE);
    }

    private static void clearDatabase(String databaseName, String createDatabase) {
        open();

        database.execSQL("DROP TABLE IF EXISTS " + databaseName);
        database.execSQL(createDatabase);

        close();
    }

    //endregion

    public static int dbOperations = 0;

    public static void open() {
        dbOperations++;
        if (database == null) {
            database = databaseConnector.getWritableDatabase();
        } else {
            if (database.isOpen()) {
                if (dbOperations == 0) {
                    Log.wtf("AGCY SPY SQL", "DB was opened Oo who did leave it opened?");
                    close();
                    database = databaseConnector.getWritableDatabase();
                }
            }

        }
        Log.i("AGCY SPY SQL", "New operation. Count of operation: " + dbOperations);
    }

    private static void close() {
        if (dbOperations == 0) {
            if (database != null) {
                database.close();
                database = null;
            }
            databaseConnector.close();
        }
        dbOperations--;
        Log.i("AGCY SPY SQL", "Operation ended. Count of operation: " + dbOperations);
    }





    private static class DatabaseConnector extends SQLiteOpenHelper {

        private static final String DATABASE = "vkspy.db";

        private static final String ONLINE_DATABASE = "online_database";
        private static final String TYPING_DATABASE = "typing_database";
        private static final String USER_DATABASE = "user_database";
        private static final String TRACK_DATABASE = "track_database";
        private static final String[] TRACK_DATABASE_FIELDS = {
                "userid",
                "notification"
        };
        private static final String[] ONLINE_DATABASE_FIELDS = {
                "id",
                "userid",
                "since",
                "till"
        };
        private static final String[] TYPING_DATABASE_FIELDS = {
                "id",
                "userid",
                "time"
        };
        private static final String[] USER_DATABASE_FIELDS = {
                "userid",
                "sex",
                "photo",
                "first_name",
                "last_name",
                "isFriend"
        };
        private static final String TRACK_DATABASE_CREATE =
                "create table " +
                        TRACK_DATABASE +
                        " ( " +
                        "userid integer primary key," +
                        "notification integer not null" +
                        " ) ";
        private static final String USER_DATABASE_CREATE =
                "create table " +
                        USER_DATABASE +
                        " ( " +
                        "userid integer primary key," +
                        "sex integer not null," +
                        "photo text not null," +
                        "first_name text not null," +
                        "last_name text not null," +
                        "hint int DEFAULT 99999," +
                        "isFriend int default 0"+
                        " ) ";
        private static final String ONLINE_DATABASE_CREATE =
                "create table " +
                        ONLINE_DATABASE +
                        " ( " +
                        "id integer primary key autoincrement," +
                        "userid integer not null," +
                        "since integer default 0," +
                        "till integer default 0" +
                        " ) ";
        private static final String TYPING_DATABASE_CREATE =
                "create table " +
                        TYPING_DATABASE +
                        " ( " +
                        "id integer primary key autoincrement," +
                        "userid integer not null," +
                        "time integer not null" +
                        " ) ";

        public DatabaseConnector(Context context) {
            super(context, DATABASE, null, 6);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {

            database.execSQL(ONLINE_DATABASE_CREATE);
            database.execSQL(TYPING_DATABASE_CREATE);
            database.execSQL(USER_DATABASE_CREATE);
            database.execSQL(TRACK_DATABASE_CREATE);


        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

            database.execSQL("DROP TABLE IF EXISTS " + USER_DATABASE);
            database.execSQL(USER_DATABASE_CREATE);
        }

    }
}
package com.agcy.vkproject.spy.Core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.agcy.vkproject.spy.Models.Online;
import com.agcy.vkproject.spy.Models.Track;
import com.agcy.vkproject.spy.Models.Typing;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKUsersArray;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by kiolt_000 on 26-Apr-14.
 */
public class Memory {


    public static VKUsersArray friends = new VKUsersArray();
    public static ArrayList<Track> tracks = new ArrayList<Track>();
    private static Context context;
    private static DatabaseConnector databaseConnector;
    private static SQLiteDatabase database;

    public static void initialize(Context context) {

        Memory.context = context;
        databaseConnector = new DatabaseConnector(context);
        open();
        close();

    }

    public static void DESTROY() {
        context.deleteDatabase(DatabaseConnector.DATABASE);
        context = null;
    }
    //region Interface
    public static void loadFriends() {

        final Cursor cursor = getCursor(DatabaseConnector.USER_DATABASE,
                DatabaseConnector.USER_DATABASE_FIELDS, null, null);

        final int useridColumnIndex = cursor.getColumnIndex("userid");
        final int sexColumnIndex = cursor.getColumnIndex("sex");
        final int photo_100ColumnIndex = cursor.getColumnIndex("photo_100");
        final int first_nameColumnIndex = cursor.getColumnIndex("first_name");
        final int last_nameColumnIndex = cursor.getColumnIndex("last_name");
        if (cursor.moveToFirst())
            do {
                VKApiUserFull user = new VKApiUserFull() {{
                    this.id = cursor.getInt(useridColumnIndex);
                    this.photo_100 = cursor.getString(photo_100ColumnIndex);
                    this.first_name = cursor.getString(first_nameColumnIndex);
                    this.last_name = cursor.getString(last_nameColumnIndex);
                    this.sex = cursor.getInt(sexColumnIndex);
                }};
                friends.add(user);
            } while (cursor.moveToNext());
        close();
    }
    public static void loadTracks(){

        Cursor cursor = getCursor(DatabaseConnector.TRACK_DATABASE,
                DatabaseConnector.TRACK_DATABASE_FIELDS,
                null,
                null);
        int useridColumnIndex = cursor.getColumnIndex("userid");
        int notificationColumnIndex = cursor.getColumnIndex("notification");


        Track track;
        if(cursor.moveToFirst())
            do {
                int userid = cursor.getInt(useridColumnIndex);
                Boolean notification = cursor.getInt(notificationColumnIndex)>0;
                track = new Track(userid, notification);
                tracks.add(track);
            } while (cursor.moveToNext());
        close();
    }

    public static VKApiUserFull getUserById(int userId) {
        if (friends.isEmpty()) {
            loadFriends();
        }
        VKApiUserFull user = friends.getById(userId);
        if(user==null) {
            user = new VKApiUserFull() {{
                first_name = "НЕИЗВЕСТНО";
                last_name = "";
            }};
            Log.e("AGCY SPY", "No such userid in database: userid = "+userId);
        }
        return user;
    }
    public static Track getTrackById(int userid){
        if(tracks.isEmpty())
            loadTracks();
        for(Track track: tracks) {
            if(track.userid == userid) {
                return track;
            }
        }
        return null;
    }
    public static Boolean isTracked(int userid){

        if(tracks.isEmpty())
            loadTracks();
        Track track = getTrackById(userid);
        if(track==null)
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
        Cursor cursor = database.query(databaseName, fields, selector, null, null, null, orderby);

        return cursor;
        // don't forget to close !!1
    }
    public static ArrayList<Online> getOnlines(){
        return getOnlines(null, "userid, time");
    }
    public static ArrayList<Online> getOnlines(final int userid){
        return getOnlines("userid = " + userid,"time");
    }
    public static ArrayList<Online> getOnlines(String selector,String order) {

        Cursor cursor = getCursor(DatabaseConnector.ONLINE_DATABASE,
                DatabaseConnector.ONLINE_DATABASE_FIELDS,
                selector,
                order);
        int useridColumnIndex = cursor.getColumnIndex("userid");
        int onlineColumnIndex = cursor.getColumnIndex("online");
        int timeColumnIndex = cursor.getColumnIndex("time");

        ArrayList<Online> onlines = new ArrayList<Online>();

        Online online;
        boolean wasOffline = true;
        int since = 0;
        int till = 0;
        int lastUserId = 0;
        if (cursor.moveToFirst())
            do {
                int userid = cursor.getInt(useridColumnIndex);
                if(lastUserId!=0 && lastUserId!=userid){
                    if(since!=0){
                        onlines.add(0, new Online(lastUserId, since, -1));
                    }
                }
                if (cursor.getInt(onlineColumnIndex) > 0) {
                    // online
                    if (!wasOffline) {
                        onlines.add(0, new Online(userid, since, till));
                    }
                    since = cursor.getInt(timeColumnIndex);
                    wasOffline = false;
                } else {
                    // offline

                    till = cursor.getInt(timeColumnIndex);
                    onlines.add(0, new Online(userid, since, till));
                    wasOffline = true;
                    till = 0;
                    since = 0;
                }
            } while (cursor.moveToNext());
        if(since!=0){
            onlines.add(0, new Online(lastUserId, since, -1));
        }
        close();
        return onlines;

    }

    public static Track getTrack(int userid){

        Cursor cursor = getCursor(DatabaseConnector.TRACK_DATABASE,
                DatabaseConnector.TRACK_DATABASE_FIELDS,
                "userid = "+userid,
                null);

        int useridColumnIndex = cursor.getColumnIndex("userid");
        int notificationColumnIndex = cursor.getColumnIndex("notification");

        Track track = null;
        if(cursor.moveToFirst()) {

            Boolean notification = cursor.getInt(notificationColumnIndex) >0;
            track = new Track(userid, notification);
        }
        close();
        return track;
    }

    public static ArrayList<Typing> getTyping(){
        return getTyping(null,null);
    }
    public static ArrayList<Typing> getTyping(int userid){
        return getTyping("userid = " + userid,null);
    }
    public static ArrayList<Typing> getTyping(String selector, String order) {

        Cursor cursor = getCursor(DatabaseConnector.TYPING_DATABASE,
                DatabaseConnector.TYPING_DATABASE_FIELDS,
                selector,
                "time desc");
        int useridColumnIndex = cursor.getColumnIndex("userid");
        int timeColumnIndex = cursor.getColumnIndex("time");

        ArrayList<Typing> typings = new ArrayList<Typing>();

        Online online;
        int since = 0;
        int till = 0;
        if (cursor.moveToFirst())
            do {
                int userid = cursor.getInt(useridColumnIndex);
                int time = cursor.getInt(timeColumnIndex);
                Typing typing = new Typing(userid,time);
                typings.add(typing);
            } while (cursor.moveToNext());
        close();
        return typings;
    }
    //endregion
    //region Setters

    public static Boolean setTracked(int userid){
        Track track = getTrackById(userid);
        if (track == null) {
            track = new Track(userid,true);
            saveTrack(track);
            tracks.add(track);
        }else
            updateTrack(track.toggle());
        return track.notification;
    }
    public static void setStatus(VKApiUserFull user, Boolean status,int offset){

        user.online = status;
        saveStatus(user,status,offset);

    }

    //endregion
    //region Updaters

    private static void update(String databaseName, ContentValues values, String primaryKeyName,String primaryKeyValue) {
        open();
        database.update(databaseName, values, primaryKeyName + " = " + primaryKeyValue, null);
        close();
    }
    public static void updateTrack(Track track){


        ContentValues values = new ContentValues();
        values.put("notification", track.notification);

        update(DatabaseConnector.TRACK_DATABASE,values,"userid", String.valueOf(track.userid));


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

    public static void saveFriends(VKUsersArray friends) {
        Memory.friends = friends;
        ArrayList<ContentValues> valuesList = new ArrayList<ContentValues>();

        for (VKApiUserFull user : friends) {

            ContentValues values = new ContentValues();
            values.put("userid", user.id);
            values.put("sex", user.sex);
            values.put("photo_100", user.photo_100);
            values.put("first_name", user.first_name);
            values.put("last_name", user.last_name);
            valuesList.add(values);
        }
        clearDatabase(DatabaseConnector.USER_DATABASE, DatabaseConnector.USER_DATABASE_CREATE);
        save(DatabaseConnector.USER_DATABASE, valuesList);

    }



    public static void saveStatus(VKApiUserFull user, boolean status, int offset) {


        ContentValues values = new ContentValues();
        values.put("userid", user.id);
        values.put("online", status);
        values.put("time", unixNow()-offset);
        save(DatabaseConnector.ONLINE_DATABASE, values);
    }
    public static void saveTrack(Track track) {


        ContentValues values = new ContentValues();
        values.put("userid", track.userid);
        values.put("notification", track.notification);
        save(DatabaseConnector.TRACK_DATABASE, values);
    }

    public static void saveTyping(VKApiUserFull user) {

        ContentValues values = new ContentValues();
        values.put("userid", user.id);
        values.put("time", unixNow());

        save(DatabaseConnector.TYPING_DATABASE, values);
    }
    //endregion
    //region Clearer
    private static void clearDatabase(String databaseName, String createDatabase) {
        open();

            database.execSQL("DROP TABLE IF EXISTS " + databaseName);
            database.execSQL(createDatabase);

        close();
    }
    //endregion
    //region Helpers
    public static long unixNow() {
        return (System.currentTimeMillis() / 1000L);
    }

    public static Date fromUnix(long unixtime) {
        return new Date(unixtime);
    }

    public static int dbOperations = 0;
    public static void open() {
        if(database==null) {
            database = databaseConnector.getWritableDatabase();
        }else{
           if(database.isOpen()) {
               if(dbOperations==0) {
                   Log.wtf("AGCY SPY SQL", "DB was opened Oo who did leave it opened?");
                   close();
                   database = databaseConnector.getWritableDatabase();
               }
           }

        }
        dbOperations++;
        Log.i("AGCY SPY SQL","New operation. Count of operation: " + dbOperations);
    }
    private static void close() {
        dbOperations--;
        if(dbOperations==0) {
            if (database != null) {
                database.close();
                database = null;
            }
            databaseConnector.close();
        }
        Log.i("AGCY SPY SQL","Operation ended. Count of operation: " + dbOperations);
    }



    //endregion

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
                "time",
                "online"
        };
        private static final String[] TYPING_DATABASE_FIELDS = {
                "id",
                "userid",
                "time"
        };
        private static final String[] USER_DATABASE_FIELDS = {
                "userid",
                "sex",
                "photo_100",
                "first_name",
                "last_name"
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
                        "photo_100 text not null," +
                        "first_name text not null," +
                        "last_name text not null" +
                        " ) ";
        private static final String ONLINE_DATABASE_CREATE =
                "create table " +
                        ONLINE_DATABASE +
                        " ( " +
                        "id  integer primary key autoincrement," +
                        "userid  integer not null," +
                        "time integer not null," +
                        "online integer not null" +
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
            super(context, DATABASE, null, 3);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(ONLINE_DATABASE_CREATE);
            db.execSQL(TYPING_DATABASE_CREATE);
            db.execSQL(USER_DATABASE_CREATE);
            db.execSQL(TRACK_DATABASE_CREATE);
            

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(TRACK_DATABASE_CREATE);
        }

    }
}

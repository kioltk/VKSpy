package com.agcy.vkproject.spy.Core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.agcy.vkproject.spy.Longpoll.LongPollService;
import com.agcy.vkproject.spy.Models.Online;
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

    private static Context context;
    private static DatabaseConnector databaseConnector;
    private static SQLiteDatabase database;
    public static void initialize(Context context) {

        Memory.context = context;
        databaseConnector = new DatabaseConnector(context);
        open();
        close();

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

    }

    public static Cursor getCursor(String databaseName, String[] fields, String selector, String orderby) {
        open();
        Cursor cursor = database.query(databaseName, fields, selector, null, null, null, orderby);

        return cursor;
        // don't forget to close !!1
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

    private static void clearDatabase(String databaseName, String createDatabase) {
        open();
        database.execSQL("DROP TABLE IF EXISTS " + databaseName);
        database.execSQL(createDatabase);
        close();
    }

    public static void saveUpdates(ArrayList<LongPollService.Update> updates) {
        for (LongPollService.Update update : updates) {
            if (update.getType() > 10) {
                saveTyping(update.getUser());
            } else {
                saveStatus(update.getUser(), (update.getType() == LongPollService.Update.TYPE_ONLINE));
            }
            //todo: maybe w8 3 minutes? filter
        }
    }

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

    public static void saveStatus(VKApiUserFull user, boolean status) {

        ContentValues values = new ContentValues();
        values.put("userid", user.id);
        values.put("online", status);
        values.put("time", unixNow());
        save(DatabaseConnector.ONLINE_DATABASE, values);
    }

    public static void saveTyping(VKApiUserFull user) {

        ContentValues values = new ContentValues();
        values.put("userid", user.id);
        values.put("time", unixNow());
        save(DatabaseConnector.TYPING_DATABASE, values);
    }

    public static long unixNow() {
        return (System.currentTimeMillis() / 1000L);
    }

    public static Date fromUnix(long unixtime) {
        return new Date(unixtime);
    }

    public static void open() {
        database = databaseConnector.getWritableDatabase();
    }

    private static void close() {
        databaseConnector.close();
    }

    public static VKApiUserFull getUserById(int userId) {
        if (friends.isEmpty()) {
            loadFriends();
        }
        return friends.getById(userId);

    }


    private static class DatabaseConnector extends SQLiteOpenHelper {

        private static final String ONLINE_DATABASE = "online_database";
        private static final String TYPING_DATABASE = "typing_database";
        private static final String USER_DATABASE = "user_database";
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
            super(context, "vkspy.db", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(ONLINE_DATABASE_CREATE);
            db.execSQL(TYPING_DATABASE_CREATE);
            db.execSQL(USER_DATABASE_CREATE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

    }
}

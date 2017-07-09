package com.islabs.ramafoto.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by shanky on 30/5/17.
 */

public class DatabaseHelper {


    public static final String IMAGE_DATA = "image_path";
    private SQLiteInnerHelper helper;
    private SQLiteDatabase database;

    //Database Name
    private static final String DB_NAME = "weed_junction.db";

    //Tables
    public static final String ALBUMS_TABLE = "albums";
    public static final String ALBUM_DETAILS = "album_details";

    //Columns
    public static final String ALBUM_ID = "album_id";
    public static final String ALBUM_NAME = "album_name";
    public static final String EVENT_DETAILS = "event_details";
    public static final String ALBUM_VERSION = "album_version";
    public static final String DATE_OPEN = "date_open";
    public static final String IMAGE_NUM = "image_num";
    public static final String COLUMN_ID = "id";
    public static final String INDEX = "position";
    public static final String NUM_IMAGES = "num_images";
    public static final String LAB_NAME = "lab_name";
    public static final String LAB_ADDRESS = "lab_address";
    public static final String LAB_CONTACT = "lab_contact";
    public static final String LAB_LOGO = "lab_logo";
    public static final String LAB_BANNER = "lab_banner";
    public static final String VIEW_COUNT = "view_count";
    public static final String PHOTOGRAPHER_NAME = "photographer_name";
    public static final String PHOTOGRAPHER_ADDRESS = "photographer_address";
    public static final String PHOTOGRAPHER_CONTACT = "photographer_contact";
    public static final String LAB_LINK = "lab_link";
    public static final String PHOTOGRAPHER_LINK = "photographer_link";

    private int DB_CURRENT_VERSION = 1;

    public DatabaseHelper(Context context) {
        this.helper = new SQLiteInnerHelper(context, DB_NAME, null, DB_CURRENT_VERSION);
    }

    public void open() {
        database = helper.getWritableDatabase();
    }

    public void close() {
        helper.close();
    }

    public void insertAlbum(String tableName, ContentValues contentValues) {
        try {
            database.insert(tableName, null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Cursor getAlbumDetailsById(String pin) {
        return database.rawQuery("select * from " + ALBUM_DETAILS + " where " + ALBUM_ID + " = " + pin + ";", null);
    }

    public Cursor getImagesOfAlbum(String albumId) {
        return database.rawQuery("select * from " + ALBUMS_TABLE + " where " + ALBUM_ID + " = " + albumId + " order by " + IMAGE_NUM + ";", null);
    }

    public Cursor getAllAlbums() {
        return database.rawQuery("select * from " + ALBUM_DETAILS + " order by " + INDEX + ";", null);
    }

    public void updateViewCount(String albumId, int viewCount) {
        database.execSQL("update " + ALBUM_DETAILS + " set " + VIEW_COUNT + " = " + viewCount + "  where " + ALBUM_ID + "=" + albumId + ";");
    }

    public void deleteAlbum(String albumPin) {
        Cursor cursor = database.rawQuery("select * from " + ALBUM_DETAILS + " where " + ALBUM_ID + " = " + albumPin + ";", null);
        cursor.moveToFirst();
        int index = cursor.getInt(cursor.getColumnIndex(INDEX));
        cursor.close();
        database.execSQL("delete from " + ALBUMS_TABLE + " where " + ALBUM_ID + "=" + albumPin + ";");
        database.execSQL("delete from " + ALBUM_DETAILS + " where " + ALBUM_ID + "=" + albumPin + ";");
        database.execSQL("update " + ALBUM_DETAILS + " set " + INDEX + " = " + INDEX + " - 1 where " + INDEX + ">" + index + ";");
    }

    public boolean isImageExist(String albumPin, int num) {
        Cursor cursor = database.rawQuery("select * from " + ALBUMS_TABLE + " where " + ALBUM_ID + "==" + albumPin + " and " + IMAGE_NUM
                + "==" + num + ";", null);
        return cursor.getCount() > 0;
    }

    public void setIndex(String albumPin, int to, int from) {
        if (to < from)
            database.execSQL("update " + ALBUM_DETAILS +
                    " set " + INDEX + " = " + INDEX + " + 1 where "
                    + INDEX + ">=" + to + " and " + INDEX + "<" + from + ";");
        else database.execSQL("update " + ALBUM_DETAILS +
                " set " + INDEX + " = " + INDEX + " - 1 where "
                + INDEX + "<=" + to + " and " + INDEX + "> " + from + ";");
        database.execSQL("update " + ALBUM_DETAILS + " set " + INDEX + " = " + to + " where " + ALBUM_ID + "=" + albumPin + ";");
    }

    public void resetAlbums() {
        database.execSQL("delete from " + ALBUMS_TABLE + ";");
        database.execSQL("delete from " + ALBUM_DETAILS + ";");
    }


    private class SQLiteInnerHelper extends SQLiteOpenHelper {

        private String CREATE_TABLE_ALBUMS = "create table " + ALBUMS_TABLE
                + "(" + ALBUM_ID + " string not null, "
                + IMAGE_NUM + " number not null, "
                + IMAGE_DATA + " string not null, "
                + COLUMN_ID + " number autoincreament primary key," +
                "FOREIGN KEY(" + ALBUM_ID + ") REFERENCES " + ALBUM_DETAILS + "(" + ALBUM_ID + "));";

        private String CREATE_TABLE_ALBUM_DETAILS = "create table " + ALBUM_DETAILS + "("
                + ALBUM_ID + " string primary key, "
                + ALBUM_NAME + " string not null, "
                + EVENT_DETAILS + " string not null, "
                + NUM_IMAGES + " number not null, "
                + VIEW_COUNT + " number not null, "
                + INDEX + " number not null, "
                + ALBUM_VERSION + " number not null, "
                + LAB_NAME + " string not null, "
                + LAB_ADDRESS + " string not null, "
                + LAB_CONTACT + " string not null, "
                + LAB_LOGO + " string not null, "
                + LAB_BANNER + " string not null, "
                + PHOTOGRAPHER_NAME + " string not null, "
                + PHOTOGRAPHER_ADDRESS + " string not null, "
                + PHOTOGRAPHER_LINK + " string not null, "
                + LAB_LINK + " string not null, "
                + PHOTOGRAPHER_CONTACT + " string not null);";


        SQLiteInnerHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_ALBUM_DETAILS);
            db.execSQL(CREATE_TABLE_ALBUMS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + ALBUMS_TABLE + ";");
            db.execSQL("DROP TABLE IF EXISTS " + ALBUM_DETAILS + ";");
            onCreate(db);
        }
    }
}

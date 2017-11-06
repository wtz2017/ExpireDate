package com.wtz.expiredate.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;


import com.wtz.expiredate.data.GoodsItem;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private final static String TAG = DatabaseHelper.class.getName();

    private static final String DATABASE_NAME = "main.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate...database " + DATABASE_NAME + " is created!");
        // For new user
        // Just create the newest tables
        createCurrentNewestTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade...oldVersion = " + oldVersion + ", newVersion = " + newVersion);
        // For old user
        // Loop upgrade old version to new version
        for (int version = oldVersion + 1; version <= newVersion; version++) {
            upgradeTo(db, version);
        }
    }

    private void createCurrentNewestTables(SQLiteDatabase db) {
        // TODO Update this when database version change
        createTable(db);
    }

    private void createTable(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE " + Table.NAME + "("
                    + Table._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + Table.GOODS_NAME + " TEXT, "
                    + Table.COUNT + " INTEGER, "
                    + Table.EXPIRE_DATE + " INTEGER, "
                    + Table.ICON_PATH + " TEXT); ");
        } catch (SQLException ex) {
            Log.e(TAG, "couldn't create table " + Table.NAME);
            throw ex;
        }
    }

    private void upgradeTo(SQLiteDatabase db, int version) {
        Log.d(TAG, "upgradeTo version " + version);
        switch (version) {
            case 2:
                // Do something
                break;

            case 3:
                // Do something
                break;

            default:
                throw new IllegalStateException("Don't know how to upgrade to " + version);
        }
    }

    public long insert(GoodsItem record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Table.GOODS_NAME, record.getName());
        values.put(Table.COUNT, record.getCount());
        values.put(Table.EXPIRE_DATE, record.getExpireDate());
        values.put(Table.ICON_PATH, record.getIconPath());
        // 如果第二个参数传递的是null，那么系统则不会对那些没有提供数据的列进行填充
        long rowid = db.insert(Table.NAME, null, values);
        return rowid;
    }

    public int deleteById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Define 'where' part of query.
        String selection = Table._ID + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selelectionArgs = {String.valueOf(id)};
        // Issue SQL statement.
        int count = db.delete(Table.NAME, selection, selelectionArgs);
        return count;
    }

    public int update(GoodsItem record) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Table.GOODS_NAME, record.getName());
        values.put(Table.COUNT, record.getCount());
        values.put(Table.EXPIRE_DATE, record.getExpireDate());
        values.put(Table.ICON_PATH, record.getIconPath());

        // Define 'where' part of query.
        String selection = Table._ID + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selelectionArgs = {String.valueOf(record.getId())};

        // Issue SQL statement.
        int count = db.update(Table.NAME, values, selection, selelectionArgs);
        return count;
    }

    public Cursor queryCursor(String selection, String[] selectionArgs) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = null;// Passing null will return all columns
        // String selection = null;// Passing null will return all columns
        // String[] selectionArgs = null;
        String sortOrder = Table.EXPIRE_DATE + " asc";// 降序desc,升序asc
        Cursor c = db.query(
                Table.NAME,       // The table to query
                projection,       // The columns to return
                selection,        // The columns for the WHERE clause
                selectionArgs,    // The values for the WHERE clause
                null,             // don't group the rows
                null,             // don't filter by row groups
                sortOrder         // The sort order
        );
        return c;
    }

    public GoodsItem queryItem(int id) {
        // Define 'where' part of query.
        String selection = Table._ID + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selelectionArgs = {String.valueOf(id)};
        Cursor c = queryCursor(selection, selelectionArgs);
        if (c == null || c.moveToFirst() == false) {
            return null;
        }

        c.moveToFirst();
        GoodsItem record = new GoodsItem();
        record.setId(c.getInt(c.getColumnIndex(Table._ID)));
        record.setName(c.getString(c.getColumnIndex(Table.GOODS_NAME)));
        record.setCount(c.getInt(c.getColumnIndex(Table.COUNT)));
        record.setExpireDate(c.getLong(c.getColumnIndex(Table.EXPIRE_DATE)));
        record.setIconPath(c.getString(c.getColumnIndex(Table.ICON_PATH)));

        return record;
    }

    public List<GoodsItem> queryList() {
        List<GoodsItem> list = null;
        Cursor c = queryCursor(null, null);
        if (c == null || c.moveToFirst() == false) {
            return list;
        }

        list = new ArrayList<GoodsItem>();
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            GoodsItem record = new GoodsItem();
            record.setId(c.getInt(c.getColumnIndex(Table._ID)));
            record.setName(c.getString(c.getColumnIndex(Table.GOODS_NAME)));
            record.setCount(c.getInt(c.getColumnIndex(Table.COUNT)));
            record.setExpireDate(c.getLong(c.getColumnIndex(Table.EXPIRE_DATE)));
            record.setIconPath(c.getString(c.getColumnIndex(Table.ICON_PATH)));
            list.add(record);
        }

        return list;
    }

    class Table implements BaseColumns {
        public static final String NAME = "goods_list";

        public static final String EXPIRE_DATE = "expire_date";
        public static final String GOODS_NAME = "name";
        public static final String COUNT = "count";
        public static final String ICON_PATH = "icon_path";
    }
}

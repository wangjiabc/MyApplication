package com.safety.android.safety.SQLite3;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by WangJing on 2017/6/10.
 */

public class SafeBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION=1;
    private static final String DATABASE_NAME="crimeBase.db";

    public SafeBaseHelper(Context context ){
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+ SafeDbSchema.CrimeTable.NAME+"("+
                    "_id integer primary key autoincrement, "+
                    SafeDbSchema.CrimeTable.Cols.UUID+", "+
                SafeDbSchema.CrimeTable.Cols.TITLE+", "+
                SafeDbSchema.CrimeTable.Cols.DATE+", "+
                SafeDbSchema.CrimeTable.Cols.SOLVED+", "+
                SafeDbSchema.CrimeTable.Cols.SUSPECT+
                ")"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

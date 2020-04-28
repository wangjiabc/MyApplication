package com.safety.android.safety.SQLite3;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserBaseHelper extends SQLiteOpenHelper {

    private static final int VERSION=1;
    private static final String DATABASE_NAME="userBase.db";

    public UserBaseHelper(Context context){
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+UserDbSchema.UserTable.NAME+"("+
                        "_id integer primary key autoincrement, "+
                        UserDbSchema.UserTable.Cols.UUID+","+
                        UserDbSchema.UserTable.Cols.name+","+
                        UserDbSchema.UserTable.Cols.password+","+
                        UserDbSchema.UserTable.Cols.date+","+
                        UserDbSchema.UserTable.Cols.token+
                        ")"
                );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

package com.safety.android.safety.SQLite3;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.Date;
import java.util.UUID;

/**
 * Created by WangJing on 2017/6/12.
 */

public class SafeInfoCusorWrapper extends CursorWrapper {
    public SafeInfoCusorWrapper(Cursor cursor) {
        super(cursor);
    }

    public SafeInfo getSafeInfo(){
        String uuidString=getString(getColumnIndex(SafeDbSchema.CrimeTable.Cols.UUID));
        String title=getString(getColumnIndex(SafeDbSchema.CrimeTable.Cols.TITLE));
        long date=getLong(getColumnIndex(SafeDbSchema.CrimeTable.Cols.DATE));
        int isSolved=getInt(getColumnIndex(SafeDbSchema.CrimeTable.Cols.SOLVED));
        String suspect=getString(getColumnIndex(SafeDbSchema.CrimeTable.Cols.SUSPECT));

        SafeInfo crime=new SafeInfo(UUID.fromString(uuidString));
        crime.setTitle(title);
        crime.setDate(new Date(date));
        crime.setmSolved(isSolved!=0);
        crime.setmSuspect(suspect);

        return crime;
    }
}

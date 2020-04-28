package com.safety.android.safety.SQLite3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by WangJing on 2017/5/28.
 */

public class SafeLab {
    private static SafeLab msafeLab;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static SafeLab get(Context context){
        if(msafeLab==null){
            msafeLab=new SafeLab(context);
        }
        return msafeLab;
    }

    private SafeLab(Context context){
        mContext=context.getApplicationContext();
        mDatabase=new SafeBaseHelper(mContext).getWritableDatabase();

    }

    public List<SafeInfo> getSafeInfo(){
        //return mCrimes;
        List<SafeInfo> crimes=new ArrayList<>();

        SafeInfoCusorWrapper cursor=queryCrimes(null,null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()){
                crimes.add(cursor.getSafeInfo());
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
        return crimes;
    }

    public SafeInfo getSafeInfo(UUID id){

        SafeInfoCusorWrapper cusor=queryCrimes(
                SafeDbSchema.CrimeTable.Cols.UUID+"=?",
                new String[]{id.toString()}
        );

        try {
            if (cusor.getCount() == 0) {

                return null;
            }
            cusor.moveToFirst();
            return cusor.getSafeInfo();
        }finally {
            cusor.close();
        }
    }

    public File getPhotoFile(SafeInfo crime){
        File externalFilesDir=mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file=null;
        if(externalFilesDir==null){
            return null;
        }
        try {
             file= new File(externalFilesDir, crime.getPhotoFilename());
        }catch (Exception e){
            e.printStackTrace();
        }
        return file;
    }

    public void addSafeInfo(SafeInfo s){
        ContentValues values=getContentValues(s);

        mDatabase.insert(SafeDbSchema.CrimeTable.NAME,null,values);
    }



    public  void updateCrime(SafeInfo crime){
        String uuidString=crime.getmId().toString();
        ContentValues values=getContentValues(crime);

        mDatabase.update(SafeDbSchema.CrimeTable.NAME,values,
                SafeDbSchema.CrimeTable.Cols.UUID+"=?",
                new String[]{uuidString});
    }

    private static ContentValues getContentValues(SafeInfo crime){
        ContentValues values=new ContentValues();
        values.put(SafeDbSchema.CrimeTable.Cols.UUID,crime.getmId().toString());
        values.put(SafeDbSchema.CrimeTable.Cols.TITLE,crime.getTitle());
        values.put(SafeDbSchema.CrimeTable.Cols.DATE,crime.getmDate().getTime());
        values.put(SafeDbSchema.CrimeTable.Cols.SOLVED,crime.ismSolved()?1:0);
        values.put(SafeDbSchema.CrimeTable.Cols.SUSPECT,crime.getmSuspect());

        return  values;
    }

    private SafeInfoCusorWrapper queryCrimes(String whereClause, String[] whereArgs){
        Cursor cursor=mDatabase.query(
                SafeDbSchema.CrimeTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return  new SafeInfoCusorWrapper(cursor);
    }

    public void delCrime(UUID id){
        /*
        Iterator<Crime> iter=mCrimes.iterator();
        while (iter.hasNext()){
            if(iter.next().getId().equals(id)){
                iter.remove();
            }
        }
        */
        mDatabase.delete(SafeDbSchema.CrimeTable.NAME,
                SafeDbSchema.CrimeTable.Cols.UUID+"=?",
                new String[]{id.toString()});
    }

}

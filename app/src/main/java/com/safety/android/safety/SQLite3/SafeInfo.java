package com.safety.android.safety.SQLite3;

import java.util.Date;
import java.util.UUID;

public class SafeInfo {

    private int id;
    private String mName;
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    private String mSuspect;
    private String phoneNumber;

    public SafeInfo() {
        this(UUID.randomUUID());
    }

    public SafeInfo(UUID id) {
        mId=id;
        mDate=new Date();
    }

    public UUID getmId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getmDate(){
        return mDate;
    }

    public void setDate(Date date){
        mDate=date;
    }

    public boolean ismSolved(){
        return mSolved;
    }

    public void setmSolved(boolean solved){
        mSolved=solved;
    }

    public String getmSuspect() {
        return mSuspect;
    }

    public void setmSuspect(String mSuspect) {
        this.mSuspect = mSuspect;
    }

    public String getPhotoFilename(){
        return "IMG_"+getmId().toString()+".jpg";
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId(){
        return  id;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }
}

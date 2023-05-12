package com.daclink.gymlog_v_sp22;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.daclink.gymlog_v_sp22.DB.AppDataBase;

@Entity(tableName = AppDataBase.USER_TABLE)
public class User  {

    @PrimaryKey(autoGenerate = true)
    private int umUserId;


    private String mUserName;

    private String mPassword;

    public User(String userName, String password) {

        mUserName = userName;
        mPassword = password;
    }

    public int getUmUserId() {
        return umUserId;
    }

    public void setUmUserId(int umUserId) {
        this.umUserId = umUserId;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }
}

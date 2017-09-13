package com.friends.zingradio.entity;

import com.parse.ParseACL;
import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by Caten on 5/16/13.
 */
public class User extends ParseUser
{
    protected String mObjId;
    protected String mUsername;
    protected String mAuthData;
    protected boolean mEmailVerified;
    protected String mEmail;
    protected Date mCreatedAt;
    protected Date mUpdatedAt;
    protected ParseACL mACL;
    protected String mUserId;
    protected String mFullname;

    public String getFullname()
    {
        return mFullname;
    }

    public void setFullname(String fn)
    {
        this.mFullname = fn;
    }

    public String getUserId()
    {
        return mUserId;
    }

    public void setUserId(String mUserId)
    {
        this.mUserId = mUserId;
    }

    public String getObjId()
    {
        return mObjId;
    }

    public void setObjId(String mObjId)
    {
        this.mObjId = mObjId;
    }

    public String getUsername()
    {
        return mUsername;
    }

    public void setUsername(String mUsername)
    {
        this.mUsername = mUsername;
    }

    public String getAuthData()
    {
        return mAuthData;
    }

    public void setAuthData(String mAuthData)
    {
        this.mAuthData = mAuthData;
    }

    public boolean isEmailVerified()
    {
        return mEmailVerified;
    }

    public void setEmailVerified(boolean mEmailVerified)
    {
        this.mEmailVerified = mEmailVerified;
    }

    public String getEmail()
    {
        return mEmail;
    }

    public void setEmail(String mEmail)
    {
        this.mEmail = mEmail;
    }

    public Date getCreatedAt()
    {
        return mCreatedAt;
    }

    public void setCreatedAt(Date mCreatedAt)
    {
        this.mCreatedAt = mCreatedAt;
    }

    public Date getUpdatedAt()
    {
        return mUpdatedAt;
    }

    public void setUpdatedAt(Date mUpdatedAt)
    {
        this.mUpdatedAt = mUpdatedAt;
    }

    public ParseACL getACL()
    {
        return mACL;
    }

    public void setACL(ParseACL mACL)
    {
        this.mACL = mACL;
    }

    public User(){}

    public User(ParseUser user)
    {
        super();
        mObjId = user.getObjectId();
        mUsername = user.getUsername();
        mEmail = user.getEmail();
        mCreatedAt = user.getCreatedAt();
        mUpdatedAt = user.getUpdatedAt();
        mACL = user.getACL();
    }
}

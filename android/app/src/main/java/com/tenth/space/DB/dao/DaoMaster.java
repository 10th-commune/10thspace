package com.tenth.space.DB.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import de.greenrobot.dao.AbstractDaoMaster;
import de.greenrobot.dao.identityscope.IdentityScopeType;

import com.tenth.space.DB.dao.DepartmentDao;
import com.tenth.space.DB.dao.UserDao;
import com.tenth.space.DB.dao.GroupDao;
import com.tenth.space.DB.dao.MessageDao;
import com.tenth.space.DB.dao.SessionDao;
import com.tenth.space.DB.dao.BlogDao;
import com.tenth.space.DB.dao.CommentDao;
import com.tenth.space.DB.dao.RequesterDao;
import com.tenth.space.DB.dao.GroupRequesterDao;
import com.tenth.space.DB.dao.SysPushMsgDao;
import com.tenth.space.DB.dao.IdCardDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * Master of DAO (schema version 12): knows all DAOs.
*/
public class DaoMaster extends AbstractDaoMaster {
    public static final int SCHEMA_VERSION = 12;

    /** Creates underlying database table using DAOs. */
    public static void createAllTables(SQLiteDatabase db, boolean ifNotExists) {
        DepartmentDao.createTable(db, ifNotExists);
        UserDao.createTable(db, ifNotExists);
        GroupDao.createTable(db, ifNotExists);
        MessageDao.createTable(db, ifNotExists);
        SessionDao.createTable(db, ifNotExists);
        BlogDao.createTable(db, ifNotExists);
        CommentDao.createTable(db, ifNotExists);
        RequesterDao.createTable(db, ifNotExists);
        GroupRequesterDao.createTable(db, ifNotExists);
        SysPushMsgDao.createTable(db, ifNotExists);
        IdCardDao.createTable(db, ifNotExists);
    }
    
    /** Drops underlying database table using DAOs. */
    public static void dropAllTables(SQLiteDatabase db, boolean ifExists) {
        DepartmentDao.dropTable(db, ifExists);
        UserDao.dropTable(db, ifExists);
        GroupDao.dropTable(db, ifExists);
        MessageDao.dropTable(db, ifExists);
        SessionDao.dropTable(db, ifExists);
        BlogDao.dropTable(db, ifExists);
        CommentDao.dropTable(db, ifExists);
        RequesterDao.dropTable(db, ifExists);
        GroupRequesterDao.dropTable(db, ifExists);
        SysPushMsgDao.dropTable(db, ifExists);
        IdCardDao.dropTable(db, ifExists);
    }
    
    public static abstract class OpenHelper extends SQLiteOpenHelper {

        public OpenHelper(Context context, String name, CursorFactory factory) {
            super(context, name, factory, SCHEMA_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i("greenDAO", "Creating tables for schema version " + SCHEMA_VERSION);
            createAllTables(db, false);
        }
    }
    
    /** WARNING: Drops all table on Upgrade! Use only during development. */
    public static class DevOpenHelper extends OpenHelper {
        public DevOpenHelper(Context context, String name, CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i("greenDAO", "Upgrading schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");
            dropAllTables(db, true);
            onCreate(db);
        }
    }

    public DaoMaster(SQLiteDatabase db) {
        super(db, SCHEMA_VERSION);
        registerDaoClass(DepartmentDao.class);
        registerDaoClass(UserDao.class);
        registerDaoClass(GroupDao.class);
        registerDaoClass(MessageDao.class);
        registerDaoClass(SessionDao.class);
        registerDaoClass(BlogDao.class);
        registerDaoClass(CommentDao.class);
        registerDaoClass(RequesterDao.class);
        registerDaoClass(GroupRequesterDao.class);
        registerDaoClass(SysPushMsgDao.class);
        registerDaoClass(IdCardDao.class);
    }
    
    public DaoSession newSession() {
        return new DaoSession(db, IdentityScopeType.Session, daoConfigMap);
    }
    
    public DaoSession newSession(IdentityScopeType type) {
        return new DaoSession(db, type, daoConfigMap);
    }
    
}

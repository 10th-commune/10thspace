package com.tenth.space.DB.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.tenth.space.DB.entity.GroupEntity;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table GroupInfo.
*/
public class GroupDao extends AbstractDao<GroupEntity, String> {

    public static final String TABLENAME = "GroupInfo";

    /**
     * Properties of entity GroupEntity.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, String.class, "id", true, "_id");
        public final static Property PeerId = new Property(1, String.class, "peerId", false, "PEER_ID");
        public final static Property GroupType = new Property(2, int.class, "groupType", false, "GROUP_TYPE");
        public final static Property MainName = new Property(3, String.class, "mainName", false, "MAIN_NAME");
        public final static Property Avatar = new Property(4, String.class, "avatar", false, "AVATAR");
        public final static Property CreatorId = new Property(5, String.class, "creatorId", false, "CREATOR_ID");
        public final static Property UserCnt = new Property(6, int.class, "userCnt", false, "USER_CNT");
        public final static Property UserList = new Property(7, String.class, "userList", false, "USER_LIST");
        public final static Property Version = new Property(8, int.class, "version", false, "VERSION");
        public final static Property Status = new Property(9, int.class, "status", false, "STATUS");
        public final static Property Created = new Property(10, int.class, "created", false, "CREATED");
        public final static Property Updated = new Property(11, int.class, "updated", false, "UPDATED");
    };


    public GroupDao(DaoConfig config) {
        super(config);
    }
    
    public GroupDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'GroupInfo' (" + //
                "'_id' TEXT PRIMARY KEY ," + // 0: id
                "'PEER_ID' TEXT NOT NULL UNIQUE ," + // 1: peerId
                "'GROUP_TYPE' INTEGER NOT NULL ," + // 2: groupType
                "'MAIN_NAME' TEXT NOT NULL ," + // 3: mainName
                "'AVATAR' TEXT NOT NULL ," + // 4: avatar
                "'CREATOR_ID' TEXT NOT NULL ," + // 5: creatorId
                "'USER_CNT' INTEGER NOT NULL ," + // 6: userCnt
                "'USER_LIST' TEXT NOT NULL ," + // 7: userList
                "'VERSION' INTEGER NOT NULL ," + // 8: version
                "'STATUS' INTEGER NOT NULL ," + // 9: status
                "'CREATED' INTEGER NOT NULL ," + // 10: created
                "'UPDATED' INTEGER NOT NULL );"); // 11: updated
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'GroupInfo'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, GroupEntity entity) {
        stmt.clearBindings();

        String id = entity.getId();
        if (id != "") {
            stmt.bindString(1, id);
        }
        stmt.bindString(2, entity.getPeerId());
        stmt.bindLong(3, entity.getGroupType());
        stmt.bindString(4, entity.getMainName());
        stmt.bindString(5, entity.getAvatar());
        stmt.bindString(6, entity.getCreatorId());
        stmt.bindLong(7, entity.getUserCnt());
        stmt.bindString(8, entity.getUserList());
        stmt.bindLong(9, entity.getVersion());
        stmt.bindLong(10, entity.getStatus());
        stmt.bindLong(11, entity.getCreated());
        stmt.bindLong(12, entity.getUpdated());
    }

    /** @inheritdoc */
    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public GroupEntity readEntity(Cursor cursor, int offset) {
        GroupEntity entity = new GroupEntity( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // id
            cursor.getString(offset + 1), // peerId
            cursor.getInt(offset + 2), // groupType
            cursor.getString(offset + 3), // mainName
            cursor.getString(offset + 4), // avatar
            cursor.getString(offset + 5), // creatorId
            cursor.getInt(offset + 6), // userCnt
            cursor.getString(offset + 7), // userList
            cursor.getInt(offset + 8), // version
            cursor.getInt(offset + 9), // status
            cursor.getInt(offset + 10), // created
            cursor.getInt(offset + 11) // updated
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, GroupEntity entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setPeerId(cursor.getString(offset + 1));
        entity.setGroupType(cursor.getInt(offset + 2));
        entity.setMainName(cursor.getString(offset + 3));
        entity.setAvatar(cursor.getString(offset + 4));
        entity.setCreatorId(cursor.getString(offset + 5));
        entity.setUserCnt(cursor.getInt(offset + 6));
        entity.setUserList(cursor.getString(offset + 7));
        entity.setVersion(cursor.getInt(offset + 8));
        entity.setStatus(cursor.getInt(offset + 9));
        entity.setCreated(cursor.getInt(offset + 10));
        entity.setUpdated(cursor.getInt(offset + 11));
     }
    
    /** @inheritdoc */
    @Override
    protected String updateKeyAfterInsert(GroupEntity entity, long rowId) {
        //entity.setId(rowId);
        return "1";//rowId;
    }
    
    /** @inheritdoc */
    @Override
    public String getKey(GroupEntity entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
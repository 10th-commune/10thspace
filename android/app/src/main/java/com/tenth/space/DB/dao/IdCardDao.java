package com.tenth.space.DB.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.tenth.space.DB.entity.IdCardEntity;
import com.tenth.space.DB.entity.UserEntity;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

public class IdCardDao extends AbstractDao<IdCardEntity, Long> {

    public static final String TABLENAME = "IdCard";

    /**
     * Properties of entity IdCardEntity.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Address = new Property(0, String.class, "address", false, "ADDRESS");
        public final static Property Encrypt_private_key = new Property(1, Long.class, "encrypt_private_key", true, "ENCRYPT_PRIVATE_KEY");
        public final static Property Pub_key = new Property(2, String.class, "pub_key", false, "PUB_KEY");
        public final static Property Key_parameter = new Property(3, String.class, "key_parameter", false, "KEY_PARAMETER");
        public final static Property Created = new Property(4, int.class, "created", false, "CREATED");
    };

    public IdCardDao(DaoConfig config) {
        super(config);
    }

    public IdCardDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    @Override
    protected IdCardEntity readEntity(Cursor cursor, int offset) {
        IdCardEntity entity = new IdCardEntity(
                cursor.getString(offset + 0), // Address
                cursor.getString(offset + 1), // Encrypt_private_key
                cursor.getString(offset + 2), // Pub_key
                cursor.getString(offset + 3), // Key_parameter
                cursor.getInt(offset + 4) // Created
        );
        return entity;
    }

    @Override
    protected Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1);
    }

    @Override
    protected void readEntity(Cursor cursor, IdCardEntity entity, int offset) {
        entity.setAddress(cursor.getString(offset + 0));
        entity.setEncryptPrivakey(cursor.getString(offset + 1));
        entity.setPubKey(cursor.getString(offset + 2));
        entity.setKeyParameter(cursor.getString(offset + 3));
        entity.setCreated(cursor.getInt(offset + 4));
    }

    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'IdCard' (" + //
                "'ADDRESS' TEXT PRIMARY KEY," + // 0: relation
                "'ENCRYPT_PRIVATE_KEY' TEXT NOT NULL ," + // 1: id
                "'PUB_KEY' TEXT NOT NULL ," + // 2: peerId
                "'KEY_PARAMETER' TEXT NOT NULL ," + // 3: gender
                "'CREATED' INTEGER NOT NULL );" ); // 14: created

    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'IdCard'";
        db.execSQL(sql);
    }

    @Override
    protected void bindValues(SQLiteStatement stmt, IdCardEntity entity) {
        stmt.clearBindings();

        stmt.bindString(1, entity.getAddress());
        stmt.bindString(2, entity.getEncryptPrivakey());
        stmt.bindString(3, entity.getPubKey());
        stmt.bindString(4, entity.getKeyParameter());
        stmt.bindLong(5, entity.getCreated());
    }

    @Override
    protected Long updateKeyAfterInsert(IdCardEntity entity, long rowId) {
        return null;
    }

    @Override
    protected Long getKey(IdCardEntity entity) {
        return null;
    }

    @Override
    protected boolean isEntityUpdateable() {
        return true;
    }
}

package com.tenth.space.DB.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import com.tenth.space.DB.entity.BlogEntity;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table Blog.
*/
public class BlogDao extends AbstractDao<BlogEntity, Long> {

    public static final String TABLENAME = "Blog";

    /**
     * Properties of entity BlogEntity.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property BlogId = new Property(1, int.class, "blogId", false, "BLOG_ID");
        public final static Property BlogText = new Property(2, String.class, "blogText", false, "BLOG_TEXT");
        public final static Property BlogAudio = new Property(3, String.class, "blogAudio", false, "BLOG_AUDIO");
        public final static Property BlogImages = new Property(4, String.class, "blogImages", false, "BLOG_IMAGES");
        public final static Property AvatarUrl = new Property(5, String.class, "avatarUrl", false, "AVATAR_URL");
        public final static Property NickName = new Property(6, String.class, "nickName", false, "NICK_NAME");
        public final static Property CommentCnt = new Property(7, Integer.class, "commentCnt", false, "COMMENT_CNT");
        public final static Property LikeCnt = new Property(8, Integer.class, "likeCnt", false, "LIKE_CNT");
        public final static Property WriterUserId = new Property(9, Long.class, "writerUserId", false, "WRITER_USER_ID");
        public final static Property Created = new Property(10, int.class, "created", false, "CREATED");
        public final static Property Updated = new Property(11, int.class, "updated", false, "UPDATED");
    };


    public BlogDao(DaoConfig config) {
        super(config);
    }
    
    public BlogDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'Blog' (" + //
                "'_id' INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "'BLOG_ID' INTEGER NOT NULL UNIQUE ," + // 1: blogId
                "'BLOG_TEXT' TEXT," + // 2: blogText
                "'BLOG_AUDIO' TEXT," + // 3: blogAudio
                "'BLOG_IMAGES' TEXT," + // 4: blogImages
                "'AVATAR_URL' TEXT," + // 5: avatarUrl
                "'NICK_NAME' TEXT," + // 6: nickName
                "'COMMENT_CNT' INTEGER," + // 7: commentCnt
                "'LIKE_CNT' INTEGER," + // 8: likeCnt
                "'WRITER_USER_ID' INTEGER," + // 9: writerUserId
                "'CREATED' INTEGER NOT NULL ," + // 10: created
                "'UPDATED' INTEGER NOT NULL );"); // 11: updated
        // Add Indexes
        db.execSQL("CREATE INDEX " + constraint + "IDX_Blog_BLOG_ID ON Blog" +
                " (BLOG_ID);");
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'Blog'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, BlogEntity entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getBlogId());
 
        String blogText = entity.getBlogText();
        if (blogText != null) {
            stmt.bindString(3, blogText);
        }
 
        String blogAudio = entity.getBlogAudio();
        if (blogAudio != null) {
            stmt.bindString(4, blogAudio);
        }
 
        String blogImages = entity.getBlogImages();
        if (blogImages != null) {
            stmt.bindString(5, blogImages);
        }
 
        String avatarUrl = entity.getAvatarUrl();
        if (avatarUrl != null) {
            stmt.bindString(6, avatarUrl);
        }
 
        String nickName = entity.getNickName();
        if (nickName != null) {
            stmt.bindString(7, nickName);
        }
 
        Integer commentCnt = entity.getCommentCnt();
        if (commentCnt != null) {
            stmt.bindLong(8, commentCnt);
        }
 
        Integer likeCnt = entity.getLikeCnt();
        if (likeCnt != null) {
            stmt.bindLong(9, likeCnt);
        }
 
        Long writerUserId = entity.getWriterUserId();
        if (writerUserId != null) {
            stmt.bindLong(10, writerUserId);
        }
        stmt.bindLong(11, entity.getCreated());
        stmt.bindLong(12, entity.getUpdated());
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public BlogEntity readEntity(Cursor cursor, int offset) {
        BlogEntity entity = new BlogEntity( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getInt(offset + 1), // blogId
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // blogText
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // blogAudio
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // blogImages
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // avatarUrl
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // nickName
            cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7), // commentCnt
            cursor.isNull(offset + 8) ? null : cursor.getInt(offset + 8), // likeCnt
            cursor.isNull(offset + 9) ? null : cursor.getLong(offset + 9), // writerUserId
            cursor.getInt(offset + 10), // created
            cursor.getInt(offset + 11) // updated
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, BlogEntity entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setBlogId(cursor.getInt(offset + 1));
        entity.setBlogText(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setBlogAudio(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setBlogImages(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setAvatarUrl(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setNickName(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setCommentCnt(cursor.isNull(offset + 7) ? null : cursor.getInt(offset + 7));
        entity.setLikeCnt(cursor.isNull(offset + 8) ? null : cursor.getInt(offset + 8));
        entity.setWriterUserId(cursor.isNull(offset + 9) ? null : cursor.getLong(offset + 9));
        entity.setCreated(cursor.getInt(offset + 10));
        entity.setUpdated(cursor.getInt(offset + 11));
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(BlogEntity entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(BlogEntity entity) {
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
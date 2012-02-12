package cef.egliseactu.providers;

import java.util.Date;
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import cef.egliseactu.EgliseActuActivity;

public class DatabaseContentProvider extends ContentProvider {
    private static final String TAG = "ItemContentProvider";

    private static final String DATABASE_NAME = "egliseactu.db";
    private static final int DATABASE_VERSION = 2;
    private static final String ACTUS_TABLE_NAME = "actus";
    private static final String FOLDER_TABLE_NAME = "folder";
    private static final String WEBPAGE_TABLE_NAME = "webpage";

    private static HashMap<String, String> sActusProjectionMap;
    private static HashMap<String, String> sFolderProjectionMap;
    private static HashMap<String, String> sWebpageProjectionMap;

    private static final int ACTUS = 1;
    private static final int FOLDER = 2;
    private static final int WEBPAGE = 3;

    private static final UriMatcher sUriMatcher;

    /**
     * This class helps open, create, and upgrade the database file.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + ACTUS_TABLE_NAME + " ("
            		+ Database._ID + " VARCHAR(180) PRIMARY KEY,"
            		+ Database.LINK + " VARCHAR(180),"
                    + Database.TITLE + " VARCHAR(80),"
                    + Database.DESCRIPTION + " TEXT,"
                    + Database.CATEGORY + " VARCHAR(80),"
                    + Database.THUMBNAILS + " VARCHAR(180),"
                    + Database.DATE + " LONG"
                    + ");");
            db.execSQL("CREATE TABLE " + FOLDER_TABLE_NAME + " ("
            		+ Database._ID + " VARCHAR(180) PRIMARY KEY,"
            		+ Database.LINK + " VARCHAR(180),"
            		+ Database.TITLE + " VARCHAR(80),"
            		+ Database.THUMBNAILS + " VARCHAR(180),"
            		+ Database.DATE + " LONG"
            		+ ");");
            db.execSQL("CREATE TABLE " + WEBPAGE_TABLE_NAME + " ("
            		+ Database._ID + " VARCHAR(180) PRIMARY KEY,"
            		+ Database.LINK + " VARCHAR(180),"
            		+ Database.DESCRIPTION + " TEXT"
            		+ ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + ACTUS_TABLE_NAME + ";");
            db.execSQL("DROP TABLE IF EXISTS " + FOLDER_TABLE_NAME + ";");
            db.execSQL("DROP TABLE IF EXISTS " + WEBPAGE_TABLE_NAME + ";");
            onCreate(db);
        }
    }

    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
        case ACTUS:
            qb.setTables(ACTUS_TABLE_NAME);
            qb.setProjectionMap(sActusProjectionMap);
            break;
        case FOLDER:
        	qb.setTables(FOLDER_TABLE_NAME);
        	qb.setProjectionMap(sFolderProjectionMap);
        	break;
        case WEBPAGE:
        	qb.setTables(WEBPAGE_TABLE_NAME);
        	qb.setProjectionMap(sWebpageProjectionMap);
        	if (TextUtils.isEmpty(sortOrder)) {
        		sortOrder = Database._ID + " ASC";
        	}
        	break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = Database.DATE + " DESC";
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case ACTUS:
            return Database.CONTENT_TYPE_ACTUS;

        case FOLDER:
            return Database.CONTENT_TYPE_FOLDER;

        case WEBPAGE:
        	return Database.CONTENT_TYPE_FOLDER;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
    	ContentValues values;
    	if (initialValues != null) {
    		values = new ContentValues(initialValues);
    	} else {
    		values = new ContentValues();
    	}
    	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    	long rowId;
        switch (sUriMatcher.match(uri)) {
        case ACTUS:
            // Make sure that the fields are all set
            if (values.containsKey(Database._ID) == false) {
                values.put(Database._ID, "");
            }
            
            if (values.containsKey(Database.TITLE) == false) {
            	values.put(Database.TITLE, "");
            }
            
            if (values.containsKey(Database.LINK) == false) {
                values.put(Database.LINK, "");
            }
            
            if (values.containsKey(Database.DESCRIPTION) == false) {
                values.put(Database.DESCRIPTION, "");
            }
            
            if (values.containsKey(Database.CATEGORY) == false) {
                values.put(Database.CATEGORY, "");
            }
            
            if (values.containsKey(Database.THUMBNAILS) == false) {
                values.put(Database.THUMBNAILS, "");
            }
            
            if (values.containsKey(Database.DATE) == false) {
                values.put(Database.DATE, new Date().getTime());
            }
            rowId = db.insert(ACTUS_TABLE_NAME, Database.ID, values);
            Log.w("egliseactu", "insert " + rowId + " actus " + uri);
            if (rowId > 0) {
            	Uri itemUri = ContentUris.withAppendedId(Database.CONTENT_ACTUS_URI, rowId);
            	getContext().getContentResolver().notifyChange(itemUri, null);
            	return itemUri;
            }
            break;
        	
        case FOLDER:
            // Make sure that the fields are all set
            if (values.containsKey(Database._ID) == false) {
                values.put(Database._ID, "");
            }
            
            if (values.containsKey(Database.TITLE) == false) {
            	values.put(Database.TITLE, "");
            }
            
            if (values.containsKey(Database.LINK) == false) {
                values.put(Database.LINK, "");
            }
            
            if (values.containsKey(Database.THUMBNAILS) == false) {
                values.put(Database.THUMBNAILS, "");
            }
            
            if (values.containsKey(Database.DATE) == false) {
                values.put(Database.DATE, new Date().getTime());
            }
            rowId = db.insert(FOLDER_TABLE_NAME, Database.ID, values);
            Log.w("egliseactu", "insert " + rowId + " folder " + uri);
            if (rowId > 0) {
            	Uri itemUri = ContentUris.withAppendedId(Database.CONTENT_FOLDER_URI, rowId);
            	getContext().getContentResolver().notifyChange(itemUri, null);
            	return itemUri;
            }
            break;
        case WEBPAGE:
            // Make sure that the fields are all set
            if (values.containsKey(Database._ID) == false) {
                values.put(Database._ID, "");
            }
            
            if (values.containsKey(Database.LINK) == false) {
                values.put(Database.LINK, "");
            }
            
            if (values.containsKey(Database.DESCRIPTION) == false) {
                values.put(Database.DESCRIPTION, "");
            }
            
            rowId = db.insert(WEBPAGE_TABLE_NAME, null, values);
            Log.w("egliseactu", "insert " + rowId + " webpage " + uri);
            if (rowId > 0) {
            	Uri itemUri = ContentUris.withAppendedId(Database.CONTENT_WEBPAGE_URI, rowId);
            	getContext().getContentResolver().notifyChange(itemUri, null);
            	return itemUri;
            }
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case ACTUS:
        	count = db.delete(ACTUS_TABLE_NAME, (!TextUtils.isEmpty(where) ? " (" + where + ')' : ""), whereArgs);
            break;
        case FOLDER:
        	count = db.delete(FOLDER_TABLE_NAME, (!TextUtils.isEmpty(where) ? " (" + where + ')' : ""), whereArgs);
            break;
        case WEBPAGE:
        	count = db.delete(WEBPAGE_TABLE_NAME, (!TextUtils.isEmpty(where) ? " (" + where + ')' : ""), whereArgs);
        	break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        Log.w("egliseactu", "delete " + count + " items " + uri);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case ACTUS:
            count = db.update(ACTUS_TABLE_NAME, values, where, whereArgs);
            break;
        case FOLDER:
        	count = db.update(FOLDER_TABLE_NAME, values, where, whereArgs);
        	break;
        case WEBPAGE:
        	count = db.update(WEBPAGE_TABLE_NAME, values, where, whereArgs);
        	break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(EgliseActuActivity.AUTHORITY, Database.CONTENT_URI_ACTUS_NAME, ACTUS);
        sUriMatcher.addURI(EgliseActuActivity.AUTHORITY, Database.CONTENT_URI_FOLDER_NAME, FOLDER);
        sUriMatcher.addURI(EgliseActuActivity.AUTHORITY, Database.CONTENT_URI_WEBPAGE_NAME, WEBPAGE);

        sActusProjectionMap = new HashMap<String, String>();
        sActusProjectionMap.put(Database._ID, Database._ID);
        sActusProjectionMap.put(Database.ID, Database.ID);
        sActusProjectionMap.put(Database.TITLE, Database.TITLE);
        sActusProjectionMap.put(Database.LINK, Database.LINK);
        sActusProjectionMap.put(Database.DESCRIPTION, Database.DESCRIPTION);
        sActusProjectionMap.put(Database.CATEGORY, Database.CATEGORY);
        sActusProjectionMap.put(Database.DATE, Database.DATE);
        sActusProjectionMap.put(Database.THUMBNAILS, Database.THUMBNAILS);
        sFolderProjectionMap = new HashMap<String, String>();
        sFolderProjectionMap.put(Database._ID, Database._ID);
        sFolderProjectionMap.put(Database.ID, Database.ID);
        sFolderProjectionMap.put(Database.TITLE, Database.TITLE);
        sFolderProjectionMap.put(Database.LINK, Database.LINK);
        sFolderProjectionMap.put(Database.DATE, Database.DATE);
        sFolderProjectionMap.put(Database.THUMBNAILS, Database.THUMBNAILS);
        sWebpageProjectionMap = new HashMap<String, String>();
        sWebpageProjectionMap.put(Database._ID, Database._ID);
        sWebpageProjectionMap.put(Database.LINK, Database.LINK);
        sWebpageProjectionMap.put(Database.DESCRIPTION, Database.DESCRIPTION);
    }

}

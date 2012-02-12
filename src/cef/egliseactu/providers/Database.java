package cef.egliseactu.providers;

import android.net.Uri;
import android.provider.BaseColumns;
import cef.egliseactu.EgliseActuActivity;

public class Database implements BaseColumns {
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String CATEGORY = "category";
    public static final String LINK = "link";
    public static final String URL = "url";
    public static final String THUMBNAILS = "thumbnails";
    public static final String DATE = "date";
    public static final String CONTENT_TYPE_ACTUS = "vnd.android.cursor.dir/vnd.egliseactu.actus";
    public static final String CONTENT_TYPE_FOLDER = "vnd.android.cursor.dir/vnd.egliseactu.actus";
    public static final String CONTENT_TYPE_WEBPAGE = "vnd.android.cursor.dir/vnd.egliseactu.webpage";
    public static final String CONTENT_ITEM_TYPE_ACTUS = "vnd.android.cursor.item/vnd.egliseactu.actus";
    public static final String CONTENT_ITEM_TYPE_FOLDER = "vnd.android.cursor.item/vnd.egliseactu.actus";
    public static final String CONTENT_ITEM_TYPE_WEBPAGE = "vnd.android.cursor.item/vnd.egliseactu.webpage";
    public static final String CONTENT_URI_ACTUS_NAME = "actus";
    public static final String CONTENT_URI_FOLDER_NAME = "folder";
    public static final String CONTENT_URI_WEBPAGE_NAME = "webpage";
    public static final Uri CONTENT_ACTUS_URI = Uri.parse("content://" + EgliseActuActivity.AUTHORITY + "/" + CONTENT_URI_ACTUS_NAME);
    public static final Uri CONTENT_FOLDER_URI = Uri.parse("content://" + EgliseActuActivity.AUTHORITY + "/" + CONTENT_URI_FOLDER_NAME);
    public static final Uri CONTENT_WEBPAGE_URI = Uri.parse("content://" + EgliseActuActivity.AUTHORITY + "/" + CONTENT_URI_WEBPAGE_NAME);
}

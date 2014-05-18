package ru.mail.app;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class NoteStoreContentProvider extends ContentProvider {
    final String LOG_TAG = "NoteStoreContentProvider";

    // // Константы для БД
    // БД
    static final String DB_NAME = "NoteStore";
    static final int DB_VERSION = 1;

    // Таблица
//    static final String NOTEBOOK_TABLE = "notebooks";
    static final String NOTE_TABLE = "notes";

    // Поля
//    static final String NOTEBOOK_GUID = "_id";
//    static final String NOTEBOOK_NAME = "name";
//    static final String NOTEBOOK_DEFAULTNOTEBOOK = "defaultNotebook";

    public static final String NOTE_ID = "_id";
    public static final String NOTE_GUID = "guid";
    public static final String NOTE_TITLE = "title";
    public static final String NOTE_CONTENT = "content";
    public static final String NOTE_NEW = "newNote";
    public static final String NOTE_UPDATE = "updateNote";
    public static final String NOTE_DELETE = "deleteNote";
    public static final String NOTE_CREATED_DATE = "createdDate";
    public static final String NOTE_CREATED_HH_MM_SS = "noteCreatedHHmmSS";
    public static final String NOTE_CREATED_DD_MM_YYYY = "noteCreatedDDmmYYYY";
    public static final String NOTE_NOTEBOOK_GUID = "notebookGuid";
    // Скрипт создания таблицы
//    static final String DB_CREATE_NOTEBOOK = "create table " + NOTEBOOK_TABLE + "("
//            + NOTE_ID + " integer primary key autoincrement, "
//            + NOTEBOOK_GUID + " text, "
//            + NOTEBOOK_NAME + " text, "
//            + NOTEBOOK_DEFAULTNOTEBOOK + " boolean"
//            + ");";

    static final String DB_CREATE_NOTE = "create table " + NOTE_TABLE + "("
            + NOTE_ID + " integer primary key autoincrement, "
            + NOTE_GUID + " text, "
            + NOTE_TITLE + " text, "
            + NOTE_CONTENT + " text, "
            + NOTE_NEW + " boolean, "
            + NOTE_UPDATE + " boolean, "
            + NOTE_DELETE + " boolean, "
            + NOTE_CREATED_DATE + " long, "
            + NOTE_CREATED_HH_MM_SS + " text, "
            + NOTE_CREATED_DD_MM_YYYY + " text, "
            + NOTE_NOTEBOOK_GUID + " text "
            + ");";

    // // Uri
    // authority
    static final String AUTHORITY = "ru.mail.app.provider";

    // path
//    static final String NOTEBOOK_PATH = "notebooks";
    static final String NOTE_PATH = "notes";

    // Общий Uri
//    public static final Uri NOTEBOOK_CONTENT_URI = Uri.parse("content://"
//            + AUTHORITY + "/" + NOTEBOOK_PATH);

    public static final Uri NOTE_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + NOTE_PATH);

    //// UriMatcher
    // общий Uri
    static final int URI_NOTES = 1;

    // Uri с указанным ID
    static final int URI_NOTES_ID = 2;

    // описание и создание UriMatcher
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, NOTE_PATH, URI_NOTES);
        uriMatcher.addURI(AUTHORITY, NOTE_PATH + "/#", URI_NOTES_ID);
    }

    DBHelper dbHelper;
    SQLiteDatabase db;

    public boolean onCreate() {
        Log.d(LOG_TAG, "onCreate");
        dbHelper = new DBHelper(getContext());
        return true;
    }

    // чтение
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.d(LOG_TAG, "contentProviderQuery, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_NOTES:
                Log.d(LOG_TAG, "URI_NOTES");
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = NOTE_GUID + " ASC";
                }
                break;
            case URI_NOTES_ID:
                String id = uri.getLastPathSegment();
                Log.d(LOG_TAG, "id of note = " + id);
                if (TextUtils.isEmpty(selection)) {
                    selection = NoteStoreContentProvider.NOTE_ID + " = " + id;
                } else {
                    selection = selection + " AND " + NoteStoreContentProvider.NOTE_ID + " = " + id;
                }
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri + "\n" + uriMatcher.match(uri));
        }
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(NOTE_TABLE, projection, selection,
                selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(),
                NOTE_CONTENT_URI);
        return cursor;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        Log.d(LOG_TAG, "insert");

        String tableName = uri.getPath().substring(1, uri.getPath().length());
        db = dbHelper.getWritableDatabase();
        long rowID = db.insert(tableName, null, contentValues);
        Log.d(LOG_TAG, "ROWID" + rowID);
        Uri resultUri = ContentUris.withAppendedId(uri, rowID);

        // уведомляем ContentResolver, что данные по адресу resultUri изменились
        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {

        Log.d(LOG_TAG, "delete, " + uri.toString());
        String id = uri.getLastPathSegment();

        if (TextUtils.isEmpty(selection)) {
            selection = NOTE_ID + " = " + id;
        } else {
            selection = selection + " AND " + NOTE_ID + " = " + id;
        }

        db = dbHelper.getWritableDatabase();
        int cnt = db.delete(NOTE_TABLE, selection, selectionArgs);

        // уведомляем ContentResolver, что данные по адресу uri изменились
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        if(uriMatcher.match(uri) == URI_NOTES_ID) {

            db = dbHelper.getWritableDatabase();
            String id = uri.getLastPathSegment();
            if (TextUtils.isEmpty(selection)){
                selection = NOTE_ID + " = \"" + id + "\"";
            } else {
                selection = selection + " and " + NOTE_ID + " = " + id;
            }

            // уведомляем ContentResolver, что данные по адресу uri изменились
            db.update(NOTE_TABLE, values, selection, selectionArgs);
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return 0;
    }

    public String getType(Uri uri) {
        return null;
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE_NOTE);
//            db.execSQL(DB_CREATE_NOTEBOOK);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}

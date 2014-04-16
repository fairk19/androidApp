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
    static final String NOTEBOOK_TABLE = "notebook";
    static final String NOTE_TABLE = "note";

    // Поля
    static final String NOTEBOOK_GUID = "_id";
    static final String NOTEBOOK_NAME = "name";
    static final String NOTEBOOK_DEFAULTNOTEBOOK = "defaultNotebook";

    static final String NOTE_GUID = "_id";
    static final String NOTE_TITLE = "title";
    static final String NOTE_CONTENT = "content";

    // Скрипт создания таблицы
    static final String DB_CREATE_NOTEBOOK = "create table " + NOTEBOOK_TABLE + "("
            + NOTEBOOK_GUID + " text primary key, "
            + NOTEBOOK_NAME + " text, "
            + NOTEBOOK_DEFAULTNOTEBOOK + " boolean"
            + ");";

    static final String DB_CREATE_NOTE = "create table " + NOTE_TABLE + "("
            + NOTE_GUID + " text primary key, "
            + NOTE_TITLE + " text, "
            + NOTE_CONTENT + " text "
            + ");";

    // // Uri
    // authority
    static final String AUTHORITY = "ru.mail.app.provider";

    // path
    static final String NOTEBOOK_PATH = "notebooks";
    static final String NOTE_PATH = "notes";

    // Общий Uri
    public static final Uri NOTEBOOK_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + NOTEBOOK_PATH);

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
                    sortOrder = NOTE_TITLE + " ASC";
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

    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
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
            ContentValues cv = new ContentValues();
            for (int i = 1; i <= 3; i++) {
                cv.put(NOTE_GUID, "guid " + i);
                cv.put(NOTE_TITLE, "title " + i);
                cv.put(NOTE_CONTENT, "content " + i);
                db.insert(NOTE_TABLE, null, cv);
            }
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}

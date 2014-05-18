package ru.mail.app;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;


public class MainActivity extends ParentActivity {

    private static final String LOG_TAG = "MainActivity";

    private GridView gridView;
    private SimpleCursorAdapter scAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final Cursor cursor = getContentResolver().query(NoteStoreContentProvider.NOTE_CONTENT_URI, null, null,
                null, null);

        String[] from = new String[]{ NoteStoreContentProvider.NOTE_GUID, NoteStoreContentProvider.NOTE_TITLE, NoteStoreContentProvider.NOTE_CONTENT };
        int[] to = new int[]{ R.id.guid, R.id.title, R.id.content };
        scAdapter = new SimpleCursorAdapter(this,
                R.layout.item_note_list, cursor, from, to);

        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(scAdapter);
        gridView.setOnItemClickListener(new ItemClickListener(this, cursor));
        

    }

    @Override
    public void onResume() {
        super.onResume();
        updateAuthUi();
        if (mEvernoteSession.isLoggedIn()) {
            startService(new Intent(this, ServiceSynchronization.class));
        }
    }

    private void updateAuthUi() {

    }

    private void deleteAllNotesFromDB(){

        Cursor cursor = getContentResolver().query(NoteStoreContentProvider.NOTE_CONTENT_URI, null, null,
                null, null);

        long count = 0;
        while (cursor.moveToNext()){

            ++count;
            Uri uri = ContentUris.withAppendedId(NoteStoreContentProvider.NOTE_CONTENT_URI, Long.parseLong(cursor.getString(cursor.getColumnIndex(NoteStoreContentProvider.NOTE_ID))));
            getContentResolver().delete(uri, cursor.getString(cursor.getColumnIndex(NoteStoreContentProvider.NOTE_ID)), null);

            Log.d(LOG_TAG, "delete id = " +  Integer.getInteger(cursor.getString(cursor.getColumnIndex(NoteStoreContentProvider.NOTE_ID))) + "\n");
        }
        Log.d(LOG_TAG, "deleted " + count + " count notes\n");
    }

    public void logout(View view) {
//        try {
              deleteAllNotesFromDB();
//              mEvernoteSession.logOut(this);
//        } catch (InvalidAuthenticationException e) {
//            Log.e(LOG_TAG, "Tried to call logout with not logged in", e);
//        }
        updateAuthUi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_logout).setVisible(mEvernoteSession.isLoggedIn());
        menu.findItem(R.id.action_login).setVisible(!mEvernoteSession.isLoggedIn());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_logout:
                if (mEvernoteSession.isLoggedIn()){
                    logout(null);
                }
                return true;
            case R.id.action_login:
                login(null);
        }

        return super.onOptionsItemSelected(item);
    }

    public void login(View view) {
        mEvernoteSession.authenticate(this);
    }

    public void startNoteAddingActivity(View v) {
        Intent intent = new Intent(this, NoteAddingActivity.class);
        startActivity(intent);
    }

}

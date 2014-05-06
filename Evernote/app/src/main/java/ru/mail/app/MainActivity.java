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
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class MainActivity extends ParentActivity {

    private static final String LOGTAG = "MainActivity";

    private Button mLoginButton;
    private Button mLogoutButton;
    private Button mCreateNoteButton;
    private ListView lvNotes;
    private SimpleCursorAdapter scAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoginButton = (Button) findViewById(R.id.login);
        mLogoutButton = (Button) findViewById(R.id.logout);
        mCreateNoteButton = (Button) findViewById(R.id.createNote);

        final Cursor cursor = getContentResolver().query(NoteStoreContentProvider.NOTE_CONTENT_URI, null, null,
                null, null);

        String[] from = new String[]{ NoteStoreContentProvider.NOTE_GUID, NoteStoreContentProvider.NOTE_TITLE, NoteStoreContentProvider.NOTE_CONTENT };
        int[] to = new int[]{ R.id.guid, R.id.title, R.id.content };
        scAdapter = new SimpleCursorAdapter(this,
                R.layout.item_note_list, cursor, from, to);

        lvNotes = (ListView) findViewById(R.id.noteList);
        lvNotes.setAdapter(scAdapter);
        lvNotes.setOnItemClickListener(new ItemClickListener(this, cursor));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAuthUi();
        startService(new Intent(this, ServiceSynchronization.class));
    }

    private void updateAuthUi() {

        mLoginButton.setEnabled(!mEvernoteSession.isLoggedIn());
        mLogoutButton.setEnabled(mEvernoteSession.isLoggedIn());
        mCreateNoteButton.setEnabled(mEvernoteSession.isLoggedIn());
    }

    private void deleteAllNotesFromDB(){

        Cursor cursor = getContentResolver().query(NoteStoreContentProvider.NOTE_CONTENT_URI, null, null,
                null, null);

        int count = 0;
        while (cursor.moveToNext()){

            ++count;
            Uri uri = ContentUris.withAppendedId(NoteStoreContentProvider.NOTE_CONTENT_URI, Long.getLong(cursor.getString(cursor.getColumnIndex(NoteStoreContentProvider.NOTE_ID))));
            Log.d(LOGTAG, "deleted " + count + " count notes\n");
            getContentResolver().delete(uri, cursor.getString(cursor.getColumnIndex(NoteStoreContentProvider.NOTE_ID)), null);

            Log.d(LOGTAG, "delete id = " +  Integer.getInteger(cursor.getString(cursor.getColumnIndex(NoteStoreContentProvider.NOTE_ID))) + "\n");
        }
        Log.d(LOGTAG, "deleted " + count + " count notes\n");
    }

    public void logout(View view) {
//        try {
              deleteAllNotesFromDB();
//            mEvernoteSession.logOut(this);
//        } catch (InvalidAuthenticationException e) {
//            Log.e(LOGTAG, "Tried to call logout with not logged in", e);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
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

package ru.mail.app;

import android.app.Activity;
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

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.InvalidAuthenticationException;

public class MainActivity extends ParentActivity {

    private static final String LOGTAG = "MainActivity";

    private Button mLoginButton;
    private Button mLogoutButton;
    private Button mCreateNoteButton;
    private ListView lvNotes;
    private SimpleCursorAdapter scAdapter;

    final Uri NOTE_URI = Uri
            .parse("content://ru.mail.app.provider/notes");

    static final String NOTE_GUID = "_id";
    static final String NOTE_TITLE = "title";
    static final String NOTE_CONTENT = "content";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoginButton = (Button) findViewById(R.id.login);
        mLogoutButton = (Button) findViewById(R.id.logout);
        mCreateNoteButton = (Button) findViewById(R.id.createNote);

        Cursor cursor = getContentResolver().query(NOTE_URI, null, null,
                null, null);

        String[] from = new String[]{ NOTE_GUID, NOTE_TITLE, NOTE_CONTENT };
        int[] to = new int[]{ R.id.guid, R.id.title, R.id.content };
        scAdapter = new SimpleCursorAdapter(this,
                R.layout.item_note_list, cursor, from, to);

        lvNotes = (ListView) findViewById(R.id.noteList);
        lvNotes.setAdapter(scAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAuthUi();
        startService(new Intent(this, ServiceSynchronous.class));
    }

    private void updateAuthUi() {

        mLoginButton.setEnabled(!mEvernoteSession.isLoggedIn());
        mLogoutButton.setEnabled(mEvernoteSession.isLoggedIn());
        mCreateNoteButton.setEnabled(mEvernoteSession.isLoggedIn());
    }

    public void logout(View view) {
        try {
            mEvernoteSession.logOut(this);
        } catch (InvalidAuthenticationException e) {
            Log.e(LOGTAG, "Tried to call logout with not logged in", e);
        }
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



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //Update UI when oauth activity returns result
            case EvernoteSession.REQUEST_CODE_OAUTH:
                if (resultCode == Activity.RESULT_OK) {
                        updateAuthUi();
                        startService(new Intent(this, ServiceSynchronous.class));
                }
                break;
        }
    }

    public void startNoteAddingActivity(View v) {
        Intent intent = new Intent(this, NoteAddingActivity.class);
        startActivity(intent);
    }
}

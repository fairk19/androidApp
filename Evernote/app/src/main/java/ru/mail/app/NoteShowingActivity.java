package ru.mail.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by vanik on 27.04.14.
 */
public class NoteShowingActivity extends Activity {
    private static final String LOGTAG = "NoteShowingActivity";
    private Intent intent;
    private NoteShowingFragment noteShowingFragment;
    private NoteEditFragment noteEditFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_showing_activity);
        intent = getIntent();
        noteShowingFragment = new NoteShowingFragment(intent, getContentResolver());
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.fragment, noteShowingFragment);
        ft.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOGTAG,"onResume");
    }

    public void btnEditClick(View v) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        noteEditFragment = new NoteEditFragment(intent, getContentResolver());
        ft.replace(R.id.fragment, noteEditFragment);
        ft.addToBackStack(null);
        ft.commit();
        Log.d(LOGTAG, "btnEdit was clicked");
    }
    public void btnSaveChangesClick(View v) {
        FragmentManager fm = getFragmentManager();
        fm.popBackStack();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
//        ft.remove(noteEditFragment);

        ft.replace(R.id.fragment, noteShowingFragment);
//        ft.addToBackStack(null);
        ft.commit();
        EditText etTitle = (EditText) noteEditFragment.getView().findViewById(R.id.etTitle);
        EditText etContent = (EditText) noteEditFragment.getView().findViewById(R.id.etContent);

        int _id = intent.getIntExtra("_id",0);
        String title = etTitle.getText().toString();
        String content = etContent.getText().toString();
        ContentValues cv = new ContentValues();
        cv.put(NoteStoreContentProvider.NOTE_GUID, _id);
        cv.put(NoteStoreContentProvider.NOTE_TITLE, title);
        cv.put(NoteStoreContentProvider.NOTE_CONTENT, content);
        Uri uri = Uri.parse(NoteStoreContentProvider.NOTE_CONTENT_URI+"/" +_id);
        getContentResolver().update(uri, cv, null, null);
    }


}

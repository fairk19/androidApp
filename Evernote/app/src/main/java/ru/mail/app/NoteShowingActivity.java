package ru.mail.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by vanik on 27.04.14.
 */
public class NoteShowingActivity extends Activity {
    private static final String LOG_TAG = "NoteShowingActivity";
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
    }

    public void btnEditClick(View v) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        noteEditFragment = new NoteEditFragment(intent, getContentResolver());
        ft.replace(R.id.fragment, noteEditFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void btnDeleteClick(View v) {

        int _id = intent.getIntExtra("_id", 0);
        ContentValues cv = new ContentValues();
        cv.put(NoteStoreContentProvider.NOTE_DELETE, true);
        Uri uri = Uri.parse(NoteStoreContentProvider.NOTE_CONTENT_URI+"/" +_id);
        getContentResolver().update(uri, cv, null, null);

        //выходим наглавное окно
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        //сообщаем пользователю об успешном далении заметки
        Toast.makeText(getApplicationContext(), R.string.success_deleting_note, Toast.LENGTH_LONG).show();
    }

    public void btnSaveChangesClick(View v) {
        FragmentManager fm = getFragmentManager();
        fm.popBackStack();
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        ft.replace(R.id.fragment, noteShowingFragment);
        ft.commit();
        EditText etTitle = (EditText) noteEditFragment.getView().findViewById(R.id.etTitle);
        EditText etContent = (EditText) noteEditFragment.getView().findViewById(R.id.etContent);

        int _id = intent.getIntExtra("_id", 0);
        String title = etTitle.getText().toString();
        String content = etContent.getText().toString();
        ContentValues cv = new ContentValues();
        cv.put(NoteStoreContentProvider.NOTE_ID, _id);
        cv.put(NoteStoreContentProvider.NOTE_TITLE, title);
        cv.put(NoteStoreContentProvider.NOTE_CONTENT, content);
        cv.put(NoteStoreContentProvider.NOTE_UPDATE, true);
        Uri uri = Uri.parse(NoteStoreContentProvider.NOTE_CONTENT_URI+"/" +_id);
        getContentResolver().update(uri, cv, null, null);

        //выходим на главное окно
        finish();

        //сообщаем пользователю об успешном изменении заметки
        Toast.makeText(getApplicationContext(), R.string.success_editing_note, Toast.LENGTH_LONG).show();
    }

    public void btnCancelSaveChanges(View v){

    }
}

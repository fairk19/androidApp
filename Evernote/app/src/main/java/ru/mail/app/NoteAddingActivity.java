package ru.mail.app;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class NoteAddingActivity extends ParentActivity{

    private static final String LOG_TAG = "NoteAddingActivity";

    private EditText mEditTextTitle;
    private EditText mEditTextContent;
    private Button mBtnSave;
    private Button mBtnSelect;
    private SimpleCursorAdapter scAdapter;
    private ListView lvAllNotes;
    final Uri NOTE_URI = Uri
            .parse("content://ru.mail.app.provider/notes");
    private String mSelectedNotebookGuid;
    static final String NOTE_GUID = "_id";
    static final String NOTE_TITLE = "title";
    static final String NOTE_CONTENT = "content";


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_adding_activity);

        mEditTextTitle = (EditText) findViewById(R.id.etTitle);
        mEditTextContent = (EditText) findViewById(R.id.etBody);
        mBtnSelect = (Button) findViewById(R.id.btnSelectNotebook);
        mBtnSave = (Button) findViewById(R.id.btnSend);

        Cursor cursor = getContentResolver().query(NOTE_URI, null, null,
                null, null);
        String[] from = new String[] {NOTE_GUID, NOTE_TITLE, NOTE_CONTENT };
        int[] to = new int[]{ R.id.title, R.id.content };
        scAdapter = new SimpleCursorAdapter(this,
                R.layout.item_note_list, cursor, from, to);
        lvAllNotes = (ListView) findViewById(R.id.lvAllNotes);
        lvAllNotes.setAdapter(scAdapter);


    }

    public void saveNote(View view) {

        String title = mEditTextTitle.getText().toString();
        String content = mEditTextContent.getText().toString();
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(getApplicationContext(), R.string.empty_content_error, Toast.LENGTH_LONG).show();
        }

        //добавление записи в локальную базу
        ContentValues cv = new ContentValues();
        String guid = "0";
        cv.put(NoteStoreContentProvider.NOTE_TITLE,  title);
        cv.put(NoteStoreContentProvider.NOTE_CONTENT, content);
        cv.put(NoteStoreContentProvider.NOTE_GUID, guid);
        cv.put(NoteStoreContentProvider.NOTE_NEW, true);
        getContentResolver().insert(NoteStoreContentProvider.NOTE_CONTENT_URI,cv);

        //выходим наглавное окно
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        //сообщаем пользователю об успешном добалении заметки
        Toast.makeText(getApplicationContext(), R.string.success_saving_note, Toast.LENGTH_LONG).show();
    }
}

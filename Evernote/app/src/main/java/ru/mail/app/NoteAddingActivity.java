package ru.mail.app;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class NoteAddingActivity extends ParentActivity{

    private static final String LOG_TAG = "NoteAddingActivity";

    private EditText mEditTextTitle;
    private EditText mEditTextContent;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_adding_activity);

        mEditTextTitle = (EditText) findViewById(R.id.etTitle);
        mEditTextContent = (EditText) findViewById(R.id.etBody);
    }

    public void cancelCreateNote(View view){
        //выходим наглавное окно
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void saveNote(View view) {

        String title = mEditTextTitle.getText().toString();
        String content = mEditTextContent.getText().toString();
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(getApplicationContext(), R.string.empty_content_error, Toast.LENGTH_LONG).show();
        }

        //добавление записи в локальную базу
        ContentValues cv = new ContentValues();
        cv.put(NoteStoreContentProvider.NOTE_TITLE,  title);
        cv.put(NoteStoreContentProvider.NOTE_CONTENT, content);
        cv.put(NoteStoreContentProvider.NOTE_NEW, true);
        cv.put(NoteStoreContentProvider.NOTE_UPDATE, false );
        cv.put(NoteStoreContentProvider.NOTE_DELETE, false );
        getContentResolver().insert(NoteStoreContentProvider.NOTE_CONTENT_URI, cv);

        //выходим наглавное окно
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        //сообщаем пользователю об успешном добалении заметки
        Toast.makeText(getApplicationContext(), R.string.success_saving_note, Toast.LENGTH_LONG).show();
    }
}

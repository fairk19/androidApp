package ru.mail.app;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by vanik on 27.04.14.
 */
public class NoteEditFragment extends Fragment {
    private Intent intent;
    private ContentResolver contentResolver;
    private Cursor cursor;
    private String title;
    private String content;
    private EditText etTitle;
    private EditText etContent;
    public NoteEditFragment(Intent intent, ContentResolver contentResolver) {
        this.intent = intent;
        this.contentResolver = contentResolver;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.note_edit_fragment, null);
        etTitle = (EditText) v.findViewById(R.id.etTitle);
        etContent = (EditText) v.findViewById(R.id.etContent);

        //_id редактируемой записи
        int _id = intent.getIntExtra(NoteStoreContentProvider.NOTE_ID, 0);
        Uri uri = Uri.parse(NoteStoreContentProvider.NOTE_CONTENT_URI+"/"+ _id);

        //для отображения редактируемого текста идем в базу и по _id получаем данные
        cursor = contentResolver.query(uri, null, null, null, null);
        cursor.moveToFirst();

        int titleIndex = cursor.getColumnIndex(NoteStoreContentProvider.NOTE_TITLE);
        int contentIndex = cursor.getColumnIndex(NoteStoreContentProvider.NOTE_CONTENT);

        title = cursor.getString(titleIndex);
        content = cursor.getString(contentIndex);

        //выводим редактируемую запись на экран
        etTitle.setText(title);
        etContent.setText(content);

        return v;

    }

    @Override
    public void onStop() {
        super.onStop();
        //после редактирования скрываем клавиатуру
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

    }
}

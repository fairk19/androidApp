package ru.mail.app;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.net.URI;

/**
 * Created by vanik on 27.04.14.
 */
public class NoteShowingFragment extends Fragment {
    private static final String LOG_TAG = "NoteShowingFragment";
    private TextView tvTitle;
    private TextView tvContent;
    private String title;
    private String content;
    private Intent intent; // интент используется, чтобы получить _id отображаемой записи из Активити
    private ContentResolver contentResolver; // для доступа в базу
    private Cursor cursor;

    public NoteShowingFragment(Intent intent, ContentResolver contentResolver) {
        this.intent = intent;
        this.contentResolver = contentResolver;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.note_showing_fragment, null);


        tvTitle = (TextView) v.findViewById(R.id.tvTitle);
        tvContent = (TextView) v.findViewById(R.id.tvContent);

        //_id отображаемой записи
        int _id = intent.getIntExtra(NoteStoreContentProvider.NOTE_ID, 0);
        Uri uri = Uri.parse(NoteStoreContentProvider.NOTE_CONTENT_URI+"/"+ _id);

        //по известному _id получим саму запись запросом в базу
        cursor = contentResolver.query(uri, null, null, null, null);
        cursor.moveToFirst();

        int titleIndex = cursor.getColumnIndex(NoteStoreContentProvider.NOTE_TITLE);
        int contentIndex = cursor.getColumnIndex(NoteStoreContentProvider.NOTE_CONTENT);

        title = cursor.getString(titleIndex);
        content = cursor.getString(contentIndex);
        //выводим полученные из базы данные на экран
        tvTitle.setText(title);
        tvContent.setText(content);
        return v;
    }
}

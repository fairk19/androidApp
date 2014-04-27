package ru.mail.app;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * Created by vanik on 27.04.14.
 */
public class NoteEditFragment extends Fragment {
    private Intent intent;
    private EditText etTitle;
    private EditText etContent;
    public NoteEditFragment(Intent intent) {
        this.intent = intent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.note_edit_fragment, null);
        etTitle = (EditText) v.findViewById(R.id.etTitle);
        etTitle.setText(intent.getStringExtra("title"));

        etContent = (EditText) v.findViewById(R.id.etContent);
        etContent.setText(intent.getStringExtra("content"));

        return v;


    }
}

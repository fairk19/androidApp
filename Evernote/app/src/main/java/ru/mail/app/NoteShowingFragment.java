package ru.mail.app;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by vanik on 27.04.14.
 */
public class NoteShowingFragment extends Fragment {
    private TextView tvTitle;
    private TextView tvContent;
    private Intent intent;
    public NoteShowingFragment(Intent intent) {
        this.intent = intent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.note_showing_fragment, null);

        tvTitle = (TextView) v.findViewById(R.id.tvTitle);
        tvContent = (TextView) v.findViewById(R.id.tvContent);
        String guid = intent.getStringExtra("guid");
        tvTitle.setText(intent.getStringExtra("title"));
        tvContent.setText(intent.getStringExtra("content"));
        return v;
    }
}

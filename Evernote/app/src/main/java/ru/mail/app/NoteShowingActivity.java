package ru.mail.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by vanik on 27.04.14.
 */
public class NoteShowingActivity extends Activity {
    private static final String LOGTAG = "myLogger";
    private TextView tvTitle;
    private TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_showing_activity);
//        tvTitle = (TextView) findViewById(R.id.tvTitle);
//        tvContent = (TextView) findViewById(R.id.tvContent);
        Intent intent = getIntent();
//        String guid = intent.getStringExtra("guid");
//        tvTitle.setText(intent.getStringExtra("title"));
//        tvContent.setText(intent.getStringExtra("content"));
        NoteShowingFragment noteShowingFragment = new NoteShowingFragment(intent);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.fragment, noteShowingFragment);
        ft.commit();
    }
    public void btnEditClick(View v) {
        Log.d(LOGTAG, "btnEdit was clicked");
    }
}

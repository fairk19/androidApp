package ru.mail.app;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentUris;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class MainActivity extends ParentActivity implements View.OnTouchListener {

    private static final String LOG_TAG = "MainActivity";

    private GridView gridView;
    private SimpleCursorAdapter scAdapter;


    public static float firstTouchX;
    public static float firstTouchY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Cursor cursor = getContentResolver().query(NoteStoreContentProvider.NOTE_CONTENT_URI, null, null,
                null, null);

        String[] from = new String[]{ NoteStoreContentProvider.NOTE_GUID, NoteStoreContentProvider.NOTE_TITLE,
                NoteStoreContentProvider.NOTE_CONTENT, NoteStoreContentProvider.NOTE_CREATED_HH_MM_SS };
        int[] to = new int[]{ R.id.guid, R.id.title, R.id.content, R.id.time };
        scAdapter = new SimpleCursorAdapter(this,
                R.layout.item_note_list, cursor, from, to);

        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(scAdapter);
        gridView.setOnItemClickListener(new ItemClickListener(this, cursor));
        if( !mEvernoteSession.isLoggedIn()) {
            findViewById(R.id.buttonPanel).setVisibility(View.INVISIBLE);
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                findViewById(R.id.createNewNoteTextView).setVisibility(View.INVISIBLE);
            }
        } else {
            findViewById(R.id.buttonPanel).setVisibility(View.VISIBLE);
            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                findViewById(R.id.createNewNoteTextView).setVisibility(View.VISIBLE);
            }
        }

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            findViewById(R.id.mainLayout).setOnTouchListener(this);
            getFragmentManager().findFragmentById(R.id.menu_left).getView()
                    .setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            Log.d("opopop","3");
                            return false;
                        }
                    });
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        updateAuthUi();
        if (mEvernoteSession.isLoggedIn()) {
            startService(new Intent(this, ServiceSynchronization.class));
        }
    }

    private void updateAuthUi() {

    }

    private void deleteAllNotesFromDB(){

        Cursor cursor = getContentResolver().query(NoteStoreContentProvider.NOTE_CONTENT_URI, null, null,
                null, null);

        long count = 0;
        while (cursor.moveToNext()){

            ++count;
            Uri uri = ContentUris.withAppendedId(NoteStoreContentProvider.NOTE_CONTENT_URI, Long.parseLong(cursor.getString(cursor.getColumnIndex(NoteStoreContentProvider.NOTE_ID))));
            getContentResolver().delete(uri, cursor.getString(cursor.getColumnIndex(NoteStoreContentProvider.NOTE_ID)), null);

            Log.d(LOG_TAG, "delete id = " +  Integer.getInteger(cursor.getString(cursor.getColumnIndex(NoteStoreContentProvider.NOTE_ID))) + "\n");
        }
        Log.d(LOG_TAG, "deleted " + count + " count notes\n");
    }

    public void logout(View view) {
//        try {
              deleteAllNotesFromDB();
//              mEvernoteSession.logOut(this);
//        } catch (InvalidAuthenticationException e) {
//            Log.e(LOG_TAG, "Tried to call logout with not logged in", e);
//        }
        updateAuthUi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_logout).setVisible(mEvernoteSession.isLoggedIn());
        menu.findItem(R.id.action_login).setVisible(!mEvernoteSession.isLoggedIn());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_logout:
                if (mEvernoteSession.isLoggedIn()){
                    logout(null);
                }
                return true;
            case R.id.action_login:
                login(null);
        }

        return super.onOptionsItemSelected(item);
    }

    public void login(View view) {
        mEvernoteSession.authenticate(this);
    }

    public void startNoteAddingActivity(View v) {
        Intent intent = new Intent(this, NoteAddingActivity.class);
        startActivity(intent);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        View view = getFragmentManager().findFragmentById(R.id.menu_left).getView();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();

        if(event.getAction() == MotionEvent.ACTION_UP) {
            MyViewGroup myViewGroup = (MyViewGroup) findViewById(R.id.mainLayout);
            firstTouchX = myViewGroup.firstTouchX;
            firstTouchY = myViewGroup.firstTouchY;
            Log.i("op", String.valueOf(event.getX()) + " " + String.valueOf(firstTouchX) );
            if( event.getX() - firstTouchX > 150  ) {
                params.addRule(RelativeLayout.ALIGN_PARENT_START);
                params.removeRule(RelativeLayout.LEFT_OF);
            } else if (event.getX() - firstTouchX < -150) {
                params.removeRule(RelativeLayout.ALIGN_PARENT_START);
                params.addRule(RelativeLayout.LEFT_OF, R.id.buttonPanel);
            }
            view.setLayoutParams(params);
        }


        return true;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }
}

package ru.mail.app;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


public class NotesListActivity extends Activity {

    private ListView lvNotes;
    private SimpleCursorAdapter scAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_list);

        final Cursor cursor = getContentResolver().query(NoteStoreContentProvider.NOTE_CONTENT_URI, null, null,
                null, null);

        String[] from = new String[]{ NoteStoreContentProvider.NOTE_GUID, NoteStoreContentProvider.NOTE_TITLE, NoteStoreContentProvider.NOTE_CONTENT };
        int[] to = new int[]{ R.id.guid, R.id.title, R.id.content };
        scAdapter = new SimpleCursorAdapter(this,
                R.layout.item_note_list, cursor, from, to);

        lvNotes = (ListView) findViewById(R.id.noteList);
        lvNotes.setAdapter(scAdapter);
        lvNotes.setOnItemClickListener(new ItemClickListener(this, cursor));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notes_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

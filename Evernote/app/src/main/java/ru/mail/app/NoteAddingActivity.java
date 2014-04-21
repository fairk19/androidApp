package ru.mail.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.evernote.client.android.ClientFactory;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.OnClientCallback;
import com.evernote.client.oauth.EvernoteAuthToken;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.transport.TTransportException;

import java.util.List;

/**
 * Created by vanik on 16.04.14.
 */
public class NoteAddingActivity extends ParentActivity{


    /**
     * *************************************************************************
     * The following values and code are simply part of the demo application.  *
     * *************************************************************************
     */

    private static final String LOGTAG = "SimpleNote";

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
        int[] to = new int[]{ R.id.guid, R.id.title, R.id.content };
        scAdapter = new SimpleCursorAdapter(this,
                R.layout.item_note_list, cursor, from, to);
        lvAllNotes = (ListView) findViewById(R.id.lvAllNotes);
        lvAllNotes.setAdapter(scAdapter);


    }
    public void showAllNotesClick(View v) {
        if (v.getId() == R.id.btnShowAllNotes) {
//            mEvernoteSession.getClientFactory().createNoteStoreClient().listNotebooks(new OnClientCallback<List<Notebook>>() {
//                @Override
//                public void onSuccess(List<Notebook> data) {
//                    for ()
//                }
//
//                @Override
//                public void onException(Exception exception) {
//
//                }
//            });
        }
    }

    /**
     * Saves text field content as note to selected notebook, or default notebook if no notebook select
     */
    public void saveNote(View view) {
        String title = mEditTextTitle.getText().toString();
        String content = mEditTextContent.getText().toString();
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            Toast.makeText(getApplicationContext(), R.string.empty_content_error, Toast.LENGTH_LONG).show();
        }
////добавление записи в локальную базу перед отправкой(проблемы с guid)
//        ContentValues cv = new ContentValues();
//        String guid = "0000ffff-00ff-00ff-00ff-000000ffffff";
//        cv.put(NoteStoreContentProvider.NOTE_TITLE,  title);
//        cv.put(NoteStoreContentProvider.NOTE_CONTENT, content);
//        cv.put(NoteStoreContentProvider.NOTE_GUID, guid);
//        getContentResolver().insert(NoteStoreContentProvider.NOTE_CONTENT_URI,cv);

        Note note = new Note();
        note.setTitle(title);

        //TODO: line breaks need to be converted to render in ENML
        note.setContent(EvernoteUtil.NOTE_PREFIX + content + EvernoteUtil.NOTE_SUFFIX);

        //If User has selected a notebook guid, assign it now
        if (!TextUtils.isEmpty(mSelectedNotebookGuid)) {
            note.setNotebookGuid(mSelectedNotebookGuid);
        }
        showDialog(DIALOG_PROGRESS);
        try {
            mEvernoteSession.getClientFactory().createNoteStoreClient().createNote(note, new OnClientCallback<Note>() {
                @Override
                public void onSuccess(Note data) {
                    Toast.makeText(getApplicationContext(), R.string.note_saved, Toast.LENGTH_LONG).show();
                    removeDialog(DIALOG_PROGRESS);
                }

                @Override
                public void onException(Exception exception) {
                    Log.e(LOGTAG, "Error saving note", exception);
                    Toast.makeText(getApplicationContext(), R.string.error_saving_note, Toast.LENGTH_LONG).show();
                    removeDialog(DIALOG_PROGRESS);
                }
            });
        } catch (TTransportException exception) {
            Log.e(LOGTAG, "Error creating notestore", exception);
            Toast.makeText(getApplicationContext(), R.string.error_creating_notestore, Toast.LENGTH_LONG).show();
            removeDialog(DIALOG_PROGRESS);
        }

    }

    /**
     * Select notebook, create AlertDialog to pick notebook guid
     */
    public void selectNotebook(View view) {

        try {
            mEvernoteSession.getClientFactory().createNoteStoreClient().listNotebooks(new OnClientCallback<List<Notebook>>() {
                int mSelectedPos = -1;

                @Override
                public void onSuccess(final List<Notebook> notebooks) {
                    CharSequence[] names = new CharSequence[notebooks.size()];
                    int selected = -1;
                    Notebook notebook = null;
                    for (int index = 0; index < notebooks.size(); index++) {
                        notebook = notebooks.get(index);
                        names[index] = notebook.getName();
                        if (notebook.getGuid().equals(mSelectedNotebookGuid)) {
                            selected = index;
                        }
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(NoteAddingActivity.this);

                    builder
                            .setSingleChoiceItems(names, selected, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mSelectedPos = which;
                                }
                            })
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (mSelectedPos > -1) {
                                        mSelectedNotebookGuid = notebooks.get(mSelectedPos).getGuid();
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .create()
                            .show();
                }

                @Override
                public void onException(Exception exception) {
                    Log.e(LOGTAG, "Error listing notebooks", exception);
                    Toast.makeText(getApplicationContext(), R.string.error_listing_notebooks, Toast.LENGTH_LONG).show();
                    removeDialog(DIALOG_PROGRESS);
                }
            });
        } catch (TTransportException exception) {
            Log.e(LOGTAG, "Error creating notestore", exception);
            Toast.makeText(getApplicationContext(), R.string.error_creating_notestore, Toast.LENGTH_LONG).show();
            removeDialog(DIALOG_PROGRESS);
        }
    }
}

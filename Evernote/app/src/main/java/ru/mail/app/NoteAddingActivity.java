package ru.mail.app;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.evernote.client.android.ClientFactory;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.OnClientCallback;
import com.evernote.client.oauth.EvernoteAuthToken;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.type.Note;
import com.evernote.thrift.transport.TTransportException;

/**
 * Created by vanik on 16.04.14.
 */
public class NoteAddingActivity extends ParentActivity{

    private static final String MY_LOGGER = "myLogger";
    private EvernoteSession mEvernoteSession;
    private String mSelectedNotebookGuid = "Quality";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_adding_activity);
        mEvernoteSession = ParentActivity.getEvernoteSession();
    }

    public void onSendClick(View v) {
        EditText etTitle = (EditText) findViewById(R.id.etTitle);
        String title = etTitle.getText().toString();

        EditText etBody = (EditText) findViewById(R.id.etBody);
        String body =  etBody.getText().toString();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(body)) {
            Toast.makeText(getApplicationContext(), R.string.empty_content_error, Toast.LENGTH_LONG).show();
        }

        Note note = new Note();
        //TODO: line breaks need to be converted to render in ENML
        note.setContent(EvernoteUtil.NOTE_PREFIX + body + EvernoteUtil.NOTE_SUFFIX);

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
                    Log.e(MY_LOGGER, "Error saving note", exception);
                    Toast.makeText(getApplicationContext(), R.string.error_saving_note, Toast.LENGTH_LONG).show();
                    removeDialog(DIALOG_PROGRESS);
                }
            });
        } catch (TTransportException exception) {
            Log.e(MY_LOGGER, "Error creating notestore", exception);
            Toast.makeText(getApplicationContext(), R.string.error_creating_notestore, Toast.LENGTH_LONG).show();
            removeDialog(DIALOG_PROGRESS);
        }


    }
}

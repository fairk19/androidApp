package ru.mail.app;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.transport.TTransportException;

import java.util.ArrayList;
import java.util.List;

public class ServiceSynchronous extends Service {

    final String LOG_TAG = "ServiceSynchronous";
    private static EvernoteSession mEvernoteSession;
    final Uri NOTE_URI = Uri
            .parse("content://ru.mail.app.provider/notes");

    public void listNotebooks() throws TTransportException {
        if (mEvernoteSession.isLoggedIn()) {

            mEvernoteSession.getClientFactory().createNoteStoreClient().listNotebooks(new OnClientCallback<List<Notebook>>() {
                @Override
                public void onSuccess(final List<Notebook> notebooks) {

                    List<String> namesList = new ArrayList<String>(notebooks.size());
                    for (Notebook notebook : notebooks) {

                        namesList.add(notebook.getName());
                    }
                    String notebookNames = TextUtils.join(", ", namesList);
                }

                @Override
                public void onException(Exception exception) {
                    Log.e(LOG_TAG, "Error retrieving notebooks", exception);
                }
            });
        }
    }

    public ServiceSynchronous() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.d(LOG_TAG, "onBind");
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public void onCreate() {
        super.onCreate();
        mEvernoteSession = ParentActivity.getEvernoteSession();
        Log.d(LOG_TAG, "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Start synchronous with server");
        synchronization();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }

    void synchronization() {
        try {

            listNotes();

        } catch (Exception e) {
            Log.v(LOG_TAG, e.toString());
        }
    }

    public void listNotes() {
        int offset = 0;
        int pageSize = 10;

        NoteFilter filter = new NoteFilter();
        filter.setOrder(NoteSortOrder.UPDATED.getValue());
        NotesMetadataResultSpec spec = new NotesMetadataResultSpec();
        spec.setIncludeTitle(true);
        try{
            mEvernoteSession.getClientFactory().createNoteStoreClient()
                    .findNotesMetadata(filter, offset, pageSize, spec, new OnClientCallback<NotesMetadataList>() {
                        @Override
                        public void onSuccess(NotesMetadataList data) {
                            Toast.makeText(getApplicationContext(), R.string.success_creating_notestore, Toast.LENGTH_LONG).show();

                            for(NoteMetadata note : data.getNotes()) {
                                String title = note.getTitle();
                                int content = note.getContentLength();
                                String guid = note.getGuid();
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(NoteStoreContentProvider.NOTE_TITLE, title);
                                contentValues.put(NoteStoreContentProvider.NOTE_CONTENT, content);
                                contentValues.put(NoteStoreContentProvider.NOTE_GUID, guid);

                                Cursor cursor = getContentResolver().query(NOTE_URI, null, null,
                                        null, null);

                                Uri newUri = getContentResolver().insert(NOTE_URI, contentValues);
                                Log.d(LOG_TAG, "Add new uri for notebook = " + newUri);
                            }
                        }

                        @Override
                        public void onException(Exception exception) {
                            onError(exception, "Error listing notes. ", R.string.error_creating_notestore);
                        }
                    });
        } catch (TTransportException exception){
            onError(exception, "Error creating notestore. ", R.string.error_creating_notestore);
        }
    }

    public void onError(Exception exception, String logstr, int id){
        Log.e(LOG_TAG, logstr + exception);
        Toast.makeText(getApplicationContext(), id, Toast.LENGTH_LONG).show();
    }
}

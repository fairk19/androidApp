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

import com.evernote.client.android.AsyncBusinessNoteStoreClient;
import com.evernote.client.android.AsyncNoteStoreClient;
import com.evernote.client.android.ClientFactory;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Note;
import com.evernote.edam.userstore.UserStore;
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
        int maxNotes = 100;

        NoteFilter filter = new NoteFilter();
        filter.setOrder(NoteSortOrder.UPDATED.getValue());
        NotesMetadataResultSpec spec = new NotesMetadataResultSpec();
        spec.setIncludeTitle(true);
            try {

                ClientFactory clientFactory = mEvernoteSession.getClientFactory();
                AsyncNoteStoreClient client =  clientFactory.createNoteStoreClient();
                NoteFilter noteFilter = new NoteFilter();
                noteFilter.setOrder(NoteSortOrder.UPDATED.getValue());


                mEvernoteSession.getClientFactory().createNoteStoreClient().findNotes(noteFilter,offset, maxNotes, new OnClientCallback<NoteList>() {
                    @Override
                    public void onSuccess(NoteList noteList) {
                        for(final Note note: noteList.getNotes()) {
                            String noteGuid = note.getGuid();
                            String[] args = {noteGuid};
                            //проверяем, есть ли в локальной базе запись с таким guid
                            Cursor cursor =  getContentResolver().query(NoteStoreContentProvider.NOTE_CONTENT_URI, null, "guid = ?", args, null );
                            if (!cursor.moveToFirst()) {
                                //такой записи нет=> запись с сервера можно добавить в локальную базу
                                Log.d(LOG_TAG, "Записи с guid  = " + noteGuid + " в локальной базе нет");

                                try {
                                    //запрашиваем с сервера текст(контент) записи
                                    mEvernoteSession.getClientFactory().createNoteStoreClient().getNoteContent(note.getGuid(),new OnClientCallback<String>() {
                                        @Override
                                        public void onSuccess(String content) {
                                            String content_from_note = note.getContent();
                                            Log.d(LOG_TAG, content);
                                            content = content.replace(EvernoteUtil.NOTE_PREFIX,"");
                                            content = content.replace(EvernoteUtil.NOTE_SUFFIX, "");
                                            ContentValues cv = new ContentValues();
                                            cv.put(NoteStoreContentProvider.NOTE_GUID, note.getGuid());
                                            cv.put(NoteStoreContentProvider.NOTE_TITLE, note.getTitle());
                                            cv.put(NoteStoreContentProvider.NOTE_CONTENT, content );
                                            getContentResolver().insert(NoteStoreContentProvider.NOTE_CONTENT_URI, cv);
                                        }

                                        @Override
                                        public void onException(Exception exception) {
                                            exception.printStackTrace();
                                        }
                                    });

                                } catch (TTransportException e) {
                                    e.printStackTrace();
                                }

                            }

                        }
                    }

                    @Override
                    public void onException(Exception exception) {
                    }
                });
                mEvernoteSession.getClientFactory().createNoteStoreClient().listNotebooks(new OnClientCallback<List<Notebook>>() {
                    @Override
                    public void onSuccess(final List<Notebook> notebooks) {
                        //This is running in the UI Thread
                        Log.d(LOG_TAG, "Notebook count=" + notebooks.size());
                        for (Notebook notebook : notebooks) {
                            Log.d(LOG_TAG, notebook.getGuid());
                        }
                    }

                    @Override
                    public void onException(Exception exception) {
                        //This is running in the UI Thread
                        Log.e(LOG_TAG, "Error listing notebooks", exception);
                    }
                });
            } catch (TTransportException e) {
                e.printStackTrace();
            }
    }

    public void onError(Exception exception, String logstr, int id){
        Log.e(LOG_TAG, logstr + exception);
        Toast.makeText(getApplicationContext(), id, Toast.LENGTH_LONG).show();
    }
}

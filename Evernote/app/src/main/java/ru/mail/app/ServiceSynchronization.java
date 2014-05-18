package ru.mail.app;

import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.evernote.client.android.AsyncNoteStoreClient;
import com.evernote.client.android.ClientFactory;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.EvernoteUtil;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Note;
import com.evernote.thrift.transport.TTransportException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServiceSynchronization extends Service {

    final String LOG_TAG = "ServiceSynchronous";
    private static SimpleDateFormat hoursFormat = new SimpleDateFormat("HH:mm:ss");
    private static SimpleDateFormat dayFormat = new SimpleDateFormat("dd-MM-yyyy");
    private static EvernoteSession mEvernoteSession;
    private String mSelectedNotebookGuid;
    private Cursor cursor;

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

    public ServiceSynchronization() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public void onCreate() {
        super.onCreate();
        mEvernoteSession = ParentActivity.getEvernoteSession();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Start synchronous with server");
        synchronization();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
    }

    void synchronization() {

        try {

            //добавляем все новые заметки на сервер
            addNewNodesToServer();
            //получаем список заметок с сервера
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
                                try {
                                    //запрашиваем с сервера текст(контент) записи
                                    mEvernoteSession.getClientFactory().createNoteStoreClient().getNoteContent(note.getGuid(),new OnClientCallback<String>() {
                                        @Override
                                        public void onSuccess(String content) {
                                            content =  content.replaceAll("<.*?>", "");
                                            Log.d(LOG_TAG, content);
                                            Long createdDateAsLong = note.getCreated();
                                            ContentValues cv = new ContentValues();
                                            cv.put(NoteStoreContentProvider.NOTE_GUID, note.getGuid());
                                            cv.put(NoteStoreContentProvider.NOTE_TITLE, note.getTitle());
                                            cv.put(NoteStoreContentProvider.NOTE_CONTENT, content );
                                            cv.put(NoteStoreContentProvider.NOTE_CREATED_DATE, createdDateAsLong);
                                            cv.put(NoteStoreContentProvider.NOTE_CREATED_HH_MM_SS, hoursFormat.format(createdDateAsLong));
                                            cv.put(NoteStoreContentProvider.NOTE_CREATED_DD_MM_YYYY, dayFormat.format(createdDateAsLong));
                                            cv.put(NoteStoreContentProvider.NOTE_NOTEBOOK_GUID, note.getNotebookGuid());
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

    public void addNewNodesToServer(){

        cursor = getContentResolver().query(NoteStoreContentProvider.NOTE_CONTENT_URI, null, null,
                null, null);

        while (cursor.moveToNext()) {


            String noteNew = cursor.getString(cursor.getColumnIndex(NoteStoreContentProvider.NOTE_NEW));
            String noteUpdate = cursor.getString(cursor.getColumnIndex(NoteStoreContentProvider.NOTE_UPDATE));
            String noteDelete = cursor.getString(cursor.getColumnIndex(NoteStoreContentProvider.NOTE_DELETE));

            if ( noteNew != null || noteUpdate != null || noteDelete != null) {

                String title = cursor.getString(cursor.getColumnIndex(NoteStoreContentProvider.NOTE_TITLE));
                String content = cursor.getString(cursor.getColumnIndex(NoteStoreContentProvider.NOTE_CONTENT));
                String guid = cursor.getString(cursor.getColumnIndex(NoteStoreContentProvider.NOTE_GUID));
                final Long noteID = Long.parseLong(cursor.getString(cursor.getColumnIndex(NoteStoreContentProvider.NOTE_ID)));

                final Note note = new Note();
                note.setTitle(title);

                //TODO: line breaks need to be converted to render in ENML
                note.setContent(EvernoteUtil.NOTE_PREFIX + content + EvernoteUtil.NOTE_SUFFIX);

                if (!TextUtils.isEmpty(mSelectedNotebookGuid)) {
                    note.setNotebookGuid(mSelectedNotebookGuid);
                }

                if (noteNew != null) {
                    try {

                        mEvernoteSession.getClientFactory().createNoteStoreClient().createNote(note, new OnClientCallback<Note>() {
                            @Override
                            public void onSuccess(Note data) {

                                // удаляем заметку с флагом new из базы
                                Uri uri = ContentUris.withAppendedId(NoteStoreContentProvider.NOTE_CONTENT_URI, noteID);
                                getContentResolver().delete(uri, noteID.toString(), null);

                                // сообщение пользователю о успешном сохранение заметки на сервер
                                Toast.makeText(getApplicationContext(), R.string.success_sync_with_server, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onException(Exception exception) {
                                Log.e(LOG_TAG, "Error saving note to server", exception);
                                Toast.makeText(getApplicationContext(), R.string.error_sync_with_server, Toast.LENGTH_LONG).show();

                            }
                        });
                    } catch (TTransportException exception) {
                        Log.e(LOG_TAG, "Error creating notestore on server", exception);
                        Toast.makeText(getApplicationContext(), R.string.error_sync_with_server, Toast.LENGTH_LONG).show();

                    }
                }

                if (noteUpdate != null) {
                    try {

                        note.setGuid(guid);
                        mEvernoteSession.getClientFactory().createNoteStoreClient().createNote(note, new OnClientCallback<Note>() {
                            @Override
                            public void onSuccess(Note data) {

                                // удаляем флаг update с заметки
                                Uri uri = ContentUris.withAppendedId(NoteStoreContentProvider.NOTE_CONTENT_URI, noteID);

                                ContentValues cv = new ContentValues();
                                cv.put(NoteStoreContentProvider.NOTE_UPDATE, false);
                                getContentResolver().update(uri, cv, null, null);

                                // сообщение пользователю о успешном изменении заметки на сервере
                                Toast.makeText(getApplicationContext(), R.string.success_sync_with_server, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onException(Exception exception) {
                                Log.e(LOG_TAG, "Error saving note to server", exception);
                                Toast.makeText(getApplicationContext(), R.string.error_sync_with_server, Toast.LENGTH_LONG).show();

                            }
                        });
                    } catch (TTransportException exception) {
                        Log.e(LOG_TAG, "Error creating notestore on server", exception);
                        Toast.makeText(getApplicationContext(), R.string.error_sync_with_server, Toast.LENGTH_LONG).show();

                    }
                }

                if (noteDelete != null) {
                    try {

                        note.setGuid(guid);
                        mEvernoteSession.getClientFactory().createNoteStoreClient().deleteNote(guid, new OnClientCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer data) {

                                // удаляем заметку с флагом delete
                                Uri uri = ContentUris.withAppendedId(NoteStoreContentProvider.NOTE_CONTENT_URI, noteID);
                                getContentResolver().delete(uri, noteID.toString(), null);

                                // сообщение пользователю о успешном удалении заметки с сервера
                                Toast.makeText(getApplicationContext(), R.string.success_sync_with_server, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onException(Exception exception) {
                                Log.e(LOG_TAG, "Error saving note to server", exception);
                                Toast.makeText(getApplicationContext(), R.string.error_sync_with_server, Toast.LENGTH_LONG).show();

                            }
                        });
                    } catch (TTransportException exception) {
                        Log.e(LOG_TAG, "Error creating notestore on server", exception);
                        Toast.makeText(getApplicationContext(), R.string.error_sync_with_server, Toast.LENGTH_LONG).show();

                    }
                }
            }
        }
    }

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
                }

                @Override
                public void onException(Exception exception) {
                    Log.e(LOG_TAG, "Error listing notebooks", exception);
                    Toast.makeText(getApplicationContext(), R.string.error_listing_notebooks, Toast.LENGTH_LONG).show();
                }
            });
        } catch (TTransportException exception) {
            Log.e(LOG_TAG, "Error creating notestore", exception);
            Toast.makeText(getApplicationContext(), R.string.error_creating_notestore, Toast.LENGTH_LONG).show();
        }
    }

    public void onError(Exception exception, String logstr, int id){
        Log.e(LOG_TAG, logstr + exception);
        Toast.makeText(getApplicationContext(), id, Toast.LENGTH_LONG).show();
    }
}

package ru.mail.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.InvalidAuthenticationException;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.transport.TTransportException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ParentActivity {

    private static final String LOGTAG = "MainActivity";

    private Button mLoginButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        mLoginButton = (Button) findViewById(R.id.login);

    }

    private void updateAuthUi() {

        //show login button if logged out
        if (!mEvernoteSession.isLoggedIn()) {
            mLoginButton.setText(R.string.action_logout);
        }
    }

    public void logout(View view) {
        try {
            mEvernoteSession.logOut(this);
        } catch (InvalidAuthenticationException e) {
            Log.e(LOGTAG, "Tried to call logout with not logged in", e);
        }
        updateAuthUi();
    }

    public class MyService extends Service {

        final String LOG_TAG = "ServiceSynchronous";

        public void onCreate() {
            super.onCreate();
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

        public IBinder onBind(Intent intent) {
            Log.d(LOG_TAG, "onBind");
            return null;
        }

        void synchronization() {
            try {
                listNotebooks();
            } catch (Exception e) {
                Log.v(LOG_TAG, e.toString());
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    public void login(View view) {
        mEvernoteSession.authenticate(this);
    }

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
                    Log.i(LOGTAG, notebookNames + " notebooks have been retrieved");
                }

                @Override
                public void onException(Exception exception) {
                    Log.e(LOGTAG, "Error retrieving notebooks", exception);
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //Update UI when oauth activity returns result
            case EvernoteSession.REQUEST_CODE_OAUTH:
                if (resultCode == Activity.RESULT_OK) {
                        startService(new Intent(this, MyService.class));
                        updateAuthUi();
                }
                break;
        }
    }
}

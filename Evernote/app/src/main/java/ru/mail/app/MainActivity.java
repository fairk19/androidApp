package ru.mail.app;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.User;
import com.evernote.thrift.transport.TTransportException;

public class MainActivity extends ParentActivity {

    private static final String LOGTAG = "MainActivity";
    private static final String CONSUMER_KEY = "fairk19";
    private static final String CONSUMER_SECRET = "b5d50ac0249441a8";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
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
//        mEvernoteSession.authenticate(this);
        try {
            mEvernoteSession.getClientFactory().createUserStoreClient().authenticate("fairk19@gmail.com", "qwerty", CONSUMER_KEY, CONSUMER_SECRET, true, null);
        }catch (TTransportException e){
            Log.e(LOGTAG, "Error login or password", e);
        }
        try {
            mEvernoteSession.getClientFactory().createUserStoreClient().getUser(
                    new OnClientCallback<User>() {
                        @Override
                        public void onSuccess(User user){
                            Log.v(LOGTAG, "User: "+user.getUsername());
                        }

                        @Override
                        public void onException(Exception exception) {
                            Log.e(LOGTAG, exception.toString());
                        }
                    }
            );
        } catch (Exception e) {
            Log.v(LOGTAG, e.toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            //Update UI when oauth activity returns result
            case EvernoteSession.REQUEST_CODE_OAUTH:
                if (resultCode == Activity.RESULT_OK) {
                    Log.e(LOGTAG, "Authentication was successful");
                }
                break;
        }
    }
}

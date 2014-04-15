package ru.mail.app;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import com.evernote.client.android.EvernoteSession;

/**
 * Created by Александр on 13.04.2014.
 */
public class ParentActivity extends Activity {

    // Your Evernote API key. See http://dev.evernote.com/documentation/cloud/
    // Please obfuscate your code to help keep these values secret.
    private static final String CONSUMER_KEY = "fairk19";
    private static final String CONSUMER_SECRET = "b5d50ac0249441a8";

    // Initial development is done on Evernote's testing service, the sandbox.
    // Change to HOST_PRODUCTION to use the Evernote production service
    // once your code is complete, or HOST_CHINA to use the Yinxiang Biji
    // (Evernote China) production service.
    protected static final EvernoteSession.EvernoteService EVERNOTE_SERVICE = EvernoteSession.EvernoteService.SANDBOX;


    protected EvernoteSession mEvernoteSession;
    protected final int DIALOG_PROGRESS = 101;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set up the Evernote Singleton Session
        mEvernoteSession = EvernoteSession.getInstance(this, CONSUMER_KEY, CONSUMER_SECRET, EVERNOTE_SERVICE);
    }

    // using createDialog, could use Fragments instead
    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_PROGRESS:
                return new ProgressDialog(ParentActivity.this);
        }
        return super.onCreateDialog(id);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_PROGRESS:
                ((ProgressDialog) dialog).setIndeterminate(true);
                dialog.setCancelable(false);
                ((ProgressDialog) dialog).setMessage(getString(R.string.esdk__loading));
        }
    }
}

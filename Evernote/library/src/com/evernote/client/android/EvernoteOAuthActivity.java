/*
 * Copyright 2012 Evernote Corporation
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.evernote.client.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.evernote.androidsdk.R;
import com.evernote.client.oauth.EvernoteAuthToken;
import com.evernote.client.oauth.YinxiangApi;
import com.evernote.edam.userstore.BootstrapInfo;
import com.evernote.edam.userstore.BootstrapProfile;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.EvernoteApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * An Android Activity for authenticating to Evernote using OAuth.
 * Third parties should not need to use this class directly.
 *
 *
 * class created by @tylersmithnet
 */
public class EvernoteOAuthActivity extends Activity {
  private static final String LOGTAG = "EvernoteOAuthActivity";

  static final String EXTRA_EVERNOTE_SERVICE = "EVERNOTE_HOST";
  static final String EXTRA_CONSUMER_KEY = "CONSUMER_KEY";
  static final String EXTRA_CONSUMER_SECRET = "CONSUMER_SECRET";
  static final String EXTRA_REQUEST_TOKEN = "REQUEST_TOKEN";
  static final String EXTRA_REQUEST_TOKEN_SECRET = "REQUEST_TOKEN_SECRET";
  static final String EXTRA_BOOTSTRAP_SELECTED_PROFILE_POS = "BOOTSTRAP_SELECTED_PROFILE_POS";
  static final String EXTRA_BOOTSTRAP_SELECTED_PROFILE = "BOOTSTRAP_SELECTED_PROFILE";
  static final String EXTRA_BOOTSTRAP_SELECTED_PROFILES = "BOOTSTRAP_SELECTED_PROFILES";

  private EvernoteSession.EvernoteService mEvernoteService = null;

  private BootstrapProfile mSelectedBootstrapProfile;
  private int mSelectedBootstrapProfilePos = 0;
  private ArrayList<BootstrapProfile> mBootstrapProfiles = new ArrayList<BootstrapProfile>();

  private String mConsumerKey = null;
  private String mConsumerSecret = null;
  private String mRequestToken = null;
  private String mRequestTokenSecret = null;

  private final int DIALOG_PROGRESS = 101;

  private Activity mActivity;

//  private WebView mWebView;

  private AsyncTask mBeginAuthSyncTask = null;
//  private AsyncTask mCompleteAuthSyncTask = null;

  /**
   * Overrides the callback URL and authenticate
   */
//  private WebViewClient mWebViewClient = new WebViewClient() {
//
//    @Override
//    public boolean shouldOverrideUrlLoading(WebView view, String url) {
//        Uri uri = Uri.parse(url);
//        if (uri.getScheme().equals(getCallbackScheme())) {
//            if (mCompleteAuthSyncTask == null) {
//                mCompleteAuthSyncTask = new CompleteAuthAsyncTask().execute(uri);
//            }
//            return true;
//        }
//        return super.shouldOverrideUrlLoading(view, url);
//    }
//};

  /**
   * Allows for showing progress
   */
//  private WebChromeClient mWebChromeClient = new WebChromeClient() {
//    @Override
//    public void onProgressChanged(WebView view, int newProgress) {
//      super.onProgressChanged(view, newProgress);
//      mActivity.setProgress(newProgress * 1000);
//    }
//  };


  public EditText login;
  public EditText password;
  private ProgressDialog dialog;
  private InputStream is;
  private Button btnLogin;
//  SecondActivity url;

  @SuppressLint("SetJavaScriptEnabled")
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_login);
      mActivity = this;

      btnLogin = (Button) findViewById(R.id.login);
      login = (EditText) findViewById(R.id.editText);
      password = (EditText) findViewById(R.id.editText2);

     //Show web loading progress
//     getWindow().requestFeature(Window.FEATURE_PROGRESS);

//    mWebView = (WebView) findViewById(R.id.esdk__webview);
//    mWebView.setWebViewClient(mWebViewClient);
//    mWebView.setWebChromeClient(mWebChromeClient);
//    mWebView.getSettings().setJavaScriptEnabled(true);

    if (savedInstanceState != null) {
      mEvernoteService = savedInstanceState.getParcelable(EXTRA_EVERNOTE_SERVICE);
      mConsumerKey = savedInstanceState.getString(EXTRA_CONSUMER_KEY);
      mConsumerSecret = savedInstanceState.getString(EXTRA_CONSUMER_SECRET);
      mRequestToken = savedInstanceState.getString(EXTRA_REQUEST_TOKEN);
      mRequestTokenSecret = savedInstanceState.getString(EXTRA_REQUEST_TOKEN_SECRET);
      mSelectedBootstrapProfile = (BootstrapProfile) savedInstanceState.getSerializable(EXTRA_BOOTSTRAP_SELECTED_PROFILE);
      mSelectedBootstrapProfilePos = savedInstanceState.getInt(EXTRA_BOOTSTRAP_SELECTED_PROFILE_POS);
      mBootstrapProfiles = (ArrayList<BootstrapProfile>) savedInstanceState.getSerializable(EXTRA_BOOTSTRAP_SELECTED_PROFILES);

      //      mWebView.restoreState(savedInstanceState);

    } else {
      Intent intent = getIntent();
      mEvernoteService = intent.getParcelableExtra(EXTRA_EVERNOTE_SERVICE);
      mConsumerKey = intent.getStringExtra(EXTRA_CONSUMER_KEY);
      mConsumerSecret = intent.getStringExtra(EXTRA_CONSUMER_SECRET);

    }
  }

  class RequestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                //создаем запрос на сервер
                DefaultHttpClient hc = new DefaultHttpClient();
                ResponseHandler<String> res = new BasicResponseHandler();
                //он у нас будет посылать post запрос
                HttpPost postMethod = new HttpPost(params[0]);
                //будем передавать два параметра
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                //передаем параметры из наших текстбоксов
                //лоигн
                nameValuePairs.add(new BasicNameValuePair("username", login.getText().toString()));
                //пароль
                nameValuePairs.add(new BasicNameValuePair("password", password.getText().toString()));
                //собераем их вместе и посылаем на сервер
                postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                //получаем ответ от сервера
                String response = hc.execute(postMethod, res);
                //посылаем на вторую активность полученные параметры
                Log.e(LOGTAG, "RESPONSE: " + response);
//                Intent intent = new Intent(EvernoteOAuthActivity.this, SecondActivity.class);
//                //то что куда мы будем передавать и что, putExtra(куда, что);
//                intent.putExtra(SecondActivity.JsonURL, response.toString());
//                startActivity(intent);
            } catch (Exception e) {
                Log.e(LOGTAG, "Error request: " , e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            dialog.dismiss();
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {

            dialog = new ProgressDialog(EvernoteOAuthActivity.this);
            dialog.setMessage("Loading...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(true);
            dialog.show();
            super.onPreExecute();
        }
    }
  @Override
  protected void onResume() {
    super.onResume();

    if (TextUtils.isEmpty(mConsumerKey) ||
        TextUtils.isEmpty(mConsumerSecret)) {
      exit(false);
      return;
    }

    if (mSelectedBootstrapProfile == null) {
      mBeginAuthSyncTask = new BootstrapAsyncTask().execute();
    }
  }

  /**
   * Not needed because of conficChanges, but leaving in case developer does not add to manifest
   * @param outState
   */
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putParcelable(EXTRA_EVERNOTE_SERVICE, mEvernoteService);
    outState.putString(EXTRA_CONSUMER_KEY, mConsumerKey);
    outState.putString(EXTRA_CONSUMER_SECRET, mConsumerSecret);
    outState.putString(EXTRA_REQUEST_TOKEN, mRequestToken);
    outState.putString(EXTRA_REQUEST_TOKEN_SECRET, mRequestTokenSecret);
    outState.putSerializable(EXTRA_BOOTSTRAP_SELECTED_PROFILE, mSelectedBootstrapProfile);
    outState.putInt(EXTRA_BOOTSTRAP_SELECTED_PROFILE_POS, mSelectedBootstrapProfilePos);
    outState.putSerializable(EXTRA_BOOTSTRAP_SELECTED_PROFILES, mBootstrapProfiles);
//    mWebView.saveState(outState);

    super.onSaveInstanceState(outState);
  }

  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
      case DIALOG_PROGRESS:
        return new ProgressDialog(EvernoteOAuthActivity.this);
    }
    // TODO onCreateDialog(int) is deprecated
    return super.onCreateDialog(id);
  }

  @Override
  protected void onPrepareDialog(int id, Dialog dialog) {
    switch(id) {
      case DIALOG_PROGRESS:
        ((ProgressDialog)dialog).setIndeterminate(true);
        dialog.setCancelable(false);
        ((ProgressDialog) dialog).setMessage(getString(R.string.esdk__loading));
    }
  }

  /**
   * Specifies a URL scheme that uniquely identifies callbacks
   * to this application after a user authorizes access to their
   * Evernote account in our WebView.
   */
  private String getCallbackScheme() {
    return "en-oauth";
  }

  /**
   * Create a Scribe OAuthService object that can be used to
   * perform OAuth authentication with the appropriate Evernote
   * service.
   */
  @SuppressWarnings("unchecked")
  private OAuthService createService() {
    OAuthService builder = null;
    @SuppressWarnings("rawtypes")
    Class apiClass = null;
    String host = mSelectedBootstrapProfile.getSettings().getServiceHost();

    if (host != null && !host.startsWith("http")) {
      host = "https://" + host;
    }

    if (host.equals(EvernoteSession.HOST_SANDBOX)) {
      apiClass = EvernoteApi.Sandbox.class;
    } else if (host.equals(EvernoteSession.HOST_PRODUCTION)) {
      apiClass = EvernoteApi.class;
    } else if (host.equals(EvernoteSession.HOST_CHINA)) {
      apiClass = YinxiangApi.class;
    } else {
      throw new IllegalArgumentException("Unsupported Evernote host: " +
                                         host);
    }
    builder = new ServiceBuilder()
        .provider(apiClass)
        .apiKey(mConsumerKey)
        .apiSecret(mConsumerSecret)
        .callback(getCallbackScheme() + "://callback")
        .build();

    return builder;
  }

  /**
   * Exit the activity and display a toast message.
   * @param success Whether the OAuth process completed successfully.
   */
  private void exit(final boolean success) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(mActivity, success ? R.string.esdk__evernote_login_successful : R.string.esdk__evernote_login_failed, Toast.LENGTH_LONG).show();
        setResult(success ? RESULT_OK : RESULT_CANCELED);
        finish();
      }
    });
  }

  /**
   * On honeycomb and above this will create an actionbar with the item to switch services
   * Below honeycomb it will create the options menu bound to a hardware key
   * @param menu
   * @return
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.esdk__oauth, menu);

    return super.onCreateOptionsMenu(menu);
  }

  /**
   * On Honeycomb and above this is called when we invalidate, this happens when the {@link ArrayList} of
   * {@link BootstrapProfile} are updated.
   *
   * Below Honeycomb this is called when the user presses the menu button.
   *
   * This detects the number of bootstrap items and sets the UI element appropriately.
   *
   * @param menu
   * @return
   */
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    MenuItem itemSwitchService = menu.findItem(R.id.esdk__switch_service);

    if(mBootstrapProfiles != null && mBootstrapProfiles.size() > 1) {
      if(BootstrapManager.CHINA_PROFILE.equals(mSelectedBootstrapProfile.getName())) {
        itemSwitchService.setTitle(BootstrapManager.DISPLAY_EVERNOTE_INTL);
      } else {
        itemSwitchService.setTitle(BootstrapManager.DISPLAY_YXBIJI);
      }

      itemSwitchService.setVisible(true);
    } else {
      itemSwitchService.setVisible(false);
    }

    return super.onPrepareOptionsMenu(menu);
  }

  /**
   * This will select the next {@link BootstrapProfile} in {@link #mBootstrapProfiles} and start a new
   * webview load request.
   * @param item
   * @return
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if(item.getItemId() == R.id.esdk__switch_service) {
      if((mBeginAuthSyncTask == null || mBeginAuthSyncTask.getStatus() != AsyncTask.Status.RUNNING) &&
          (mSelectedBootstrapProfile != null && mBootstrapProfiles != null)) {

        mSelectedBootstrapProfilePos = (mSelectedBootstrapProfilePos + 1) % mBootstrapProfiles.size();
        mBootstrapProfiles = null;
        mSelectedBootstrapProfile = null;

        mBeginAuthSyncTask = new BootstrapAsyncTask().execute();
      }
    }
    return true;
  }


  /**
   * Get a request token from the Evernote service and send the user
   * to our WebView to authorize access.
   */
  private class BootstrapAsyncTask extends AsyncTask<Void, Void, String> {

    @Override
    protected void onPreExecute() {
      // TODO deprecated
      showDialog(DIALOG_PROGRESS);
    }

    @Override
    protected String doInBackground(Void... params) {
      String url = null;
      try {

        EvernoteSession session = EvernoteSession.getOpenSession();
        if (session != null) {
          //Network request
          BootstrapManager.BootstrapInfoWrapper infoWrapper = session.getBootstrapSession().getBootstrapInfo();
          Log.e(LOGTAG, "INFO WRAPPER: " + infoWrapper.getServerUrl());

          if (infoWrapper != null){
            Log.e(LOGTAG, "INFO WRAPPER != NULL: " + infoWrapper.getServerUrl());
            BootstrapInfo info = infoWrapper.getBootstrapInfo();
            if(info != null) {
              mBootstrapProfiles = (ArrayList<BootstrapProfile>) info.getProfiles();
              if (mBootstrapProfiles != null &&
                  mBootstrapProfiles.size() > 0 &&
                  mSelectedBootstrapProfilePos < mBootstrapProfiles.size()){

                mSelectedBootstrapProfile = mBootstrapProfiles.get(mSelectedBootstrapProfilePos);
              }
            }
          }
        }

        if(mSelectedBootstrapProfile == null || TextUtils.isEmpty(mSelectedBootstrapProfile.getSettings().getServiceHost())) {
          Log.d(LOGTAG, "Bootstrap did not return a valid host");
          return null;
        }

        OAuthService service = createService();

        Log.i(LOGTAG, "Retrieving OAuth request token...");
        Token reqToken = service.getRequestToken();
        mRequestToken = reqToken.getToken();
        mRequestTokenSecret = reqToken.getSecret();

        Log.i(LOGTAG, "Redirecting user for authorization...");
        url = service.getAuthorizationUrl(reqToken);
      } catch(BootstrapManager.ClientUnsupportedException cue) {

        return null;
      } catch (Exception ex) {
        Log.e(LOGTAG, "Failed to obtain OAuth request token", ex);
      }

      Log.e(LOGTAG, "BOOTSTRAP URL: " + url);
      return url;
    }

      /**
     * Open a WebView to allow the user to authorize access to their account.
     * @param url The URL of the OAuth authorization web page.
     */

    @Override
    protected void onPostExecute(final String url) {
      // TODO deprecated
      removeDialog(DIALOG_PROGRESS);
      if (!TextUtils.isEmpty(url)) {

//        mWebView.loadUrl(url);
          Log.e(LOGTAG, "URL: " + url);
          btnLogin.setOnClickListener(new View.OnClickListener() {

              @Override
              public void onClick(View v) {
                  //тут указываем куда будем конектится, для примера я привел удаленных хост если у вас не получилось освоить wamp (:
                  new RequestTask().execute(url);
              }
          });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
          invalidateOptionsMenu();
        }
      } else {
        exit(false);
      }
    }
  }

  /**
   * An AsyncTask to complete the OAuth process after successful user authorization.
   */
  private class CompleteAuthAsyncTask extends AsyncTask<Uri, Void, EvernoteAuthToken> {

    @Override
    protected void onPreExecute() {
      // TODO deprecated
      showDialog(DIALOG_PROGRESS);
    }

    @Override
    protected EvernoteAuthToken doInBackground(Uri... uris) {
      EvernoteAuthToken authToken = null;
      if (uris == null || uris.length == 0) {
        return null;
      }
      Uri uri = uris[0];

      if (!TextUtils.isEmpty(mRequestToken)) {
        OAuthService service = createService();
        String verifierString = uri.getQueryParameter("oauth_verifier");
        if (TextUtils.isEmpty(verifierString)) {
          Log.i(LOGTAG, "User did not authorize access");
        } else {
          Verifier verifier = new Verifier(verifierString);
          Log.i(LOGTAG, "Retrieving OAuth access token...");
          try {
            Token reqToken = new Token(mRequestToken, mRequestTokenSecret);
            authToken = new EvernoteAuthToken(service.getAccessToken(reqToken, verifier));
          } catch (Exception ex) {
            Log.e(LOGTAG, "Failed to obtain OAuth access token", ex);
          }
        }
      } else {
        Log.d(LOGTAG, "Unable to retrieve OAuth access token, no request token");
      }

      return authToken;
    }

    /**
     * Save the authentication information resulting from a successful
     * OAuth authorization and complete the activity.
     */

    @Override
    protected void onPostExecute(EvernoteAuthToken authToken) {
      // TODO deprecated
      removeDialog(DIALOG_PROGRESS);
      if (EvernoteSession.getOpenSession() == null) {
        exit(false);
        return;
      }

      exit(EvernoteSession.getOpenSession().persistAuthenticationToken(
      getApplicationContext(), authToken, mSelectedBootstrapProfile.getSettings().getServiceHost()));
    }
  }

}

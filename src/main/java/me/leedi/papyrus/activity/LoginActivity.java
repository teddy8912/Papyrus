package me.leedi.papyrus.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ProgressBar;
import com.facebook.*;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import me.leedi.papyrus.R;
import me.leedi.papyrus.utils.ServerUtils;


public class LoginActivity extends ActionBarActivity {
    TwitterLoginButton twitterLogin;
    LoginButton facebookLogin;
    String userId, userName, SNSType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(me.leedi.papyrus.R.layout.activity_login);

        twitterLogin = (TwitterLoginButton) findViewById(R.id.twitter_login);
        twitterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SNSType = "TWTR";
            }
        });
        twitterLogin.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                TwitterSession session = result.data;
                userId = "TWTR_" + Long.toString(session.getUserId());
                userName = session.getUserName();
                String[] userInfo = new String[2];
                userInfo[0] = userId;
                userInfo[1] = userName;
                new loginTask().execute(userInfo);
            }

            @Override
            public void failure(TwitterException exception) {
                exception.printStackTrace();
            }
        });

        facebookLogin = (LoginButton) findViewById(R.id.facebook_login);
        facebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SNSType = "FB";
            }
        });
        facebookLogin.setOnErrorListener(new LoginButton.OnErrorListener() {
            @Override
            public void onError(FacebookException e) {
                e.printStackTrace();
            }
        });
        facebookLogin.setSessionStatusCallback(new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState sessionState, Exception e) {
                if (session.isOpened()) {
                    // make request to the /me API
                    Request.newMeRequest(session, new Request.GraphUserCallback() {
                        // callback after Graph API response with user object
                        @Override
                        public void onCompleted(GraphUser user, Response response) {
                            if (user != null) {
                                String userId = "FB_" + user.getId();
                                String userName = user.getName();
                                String[] userInfo = new String[2];
                                userInfo[0] = userId;
                                userInfo[1] = userName;
                                new loginTask().execute(userInfo);
                            }
                        }
                    }).executeAsync();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (SNSType.equals("TWTR")) {
            twitterLogin.onActivityResult(requestCode, resultCode, data);
        }
        else if (SNSType.equals("FB")) {
            Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
        }
    }
    
    public class loginTask extends AsyncTask<String, Void, Boolean> {
        ProgressBar progressCircle = (ProgressBar) findViewById(R.id.progress_circle);
        @Override
        protected void onPreExecute() {
            progressCircle.setVisibility(View.VISIBLE);
        }
        
        @Override
        protected Boolean doInBackground(String... params) {
            return ServerUtils.login(params[0], params[1], LoginActivity.this);
        }
        
        @Override
        protected void onPostExecute(Boolean login) {
            if (!login) {
                new AlertDialog.Builder(LoginActivity.this)
                        .setMessage(ServerUtils.StatusMsg)
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
            }
            else {
                progressCircle.setVisibility(View.INVISIBLE);
                finish();
            }            
        }
    }
}

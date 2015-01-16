package me.leedi.papyrus.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
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
    
    // TODO : ServerUtils 를 이용한 로그인을 AsyncTask 로 처리하도록 수정!

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
                userId = "TWTR" + Long.toString(session.getUserId());
                userName = session.getUserName();
                boolean login = ServerUtils.login(userId, userName, LoginActivity.this);
                if (!login) {
                    new AlertDialog.Builder(LoginActivity.this)
                            .setMessage(ServerUtils.StatusMsg)
                            .setPositiveButton(android.R.string.ok, null)
                            .create();
                }
                else {
                    finish();
                }
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
                                String userId = "FB" + user.getId();
                                String userName = user.getName();
                                boolean login = ServerUtils.login(userId, userName, LoginActivity.this);
                                if (!login) {
                                    new AlertDialog.Builder(LoginActivity.this)
                                            .setMessage(ServerUtils.StatusMsg)
                                            .setPositiveButton(android.R.string.ok, null)
                                            .create();
                                }
                                else {
                                    finish();
                                }
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
}

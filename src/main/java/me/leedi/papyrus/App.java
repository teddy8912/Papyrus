package me.leedi.papyrus;

import android.app.Application;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 트위터 API 설정
        TwitterAuthConfig authConfig = new TwitterAuthConfig(BuildConfig.twitterKey, BuildConfig.twitterSecret);
        Fabric.with(this, new Twitter(authConfig)); // Fabric!!
    }
}

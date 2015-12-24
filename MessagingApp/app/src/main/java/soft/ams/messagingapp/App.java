package soft.ams.messagingapp;

import android.app.Application;

import com.parse.Parse;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // initialize the app keys
        Parse.initialize(this, "BObn1SXVIZ8oOxPfF4yZhlsXjitLTjMeDylpyTFJ", "sGBaMzwcgVAp7DmXpCqRrqmftdn46FpXb2jeu4WO");
    }
}

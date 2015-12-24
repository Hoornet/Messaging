package soft.ams.messagingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    /**
     * initialize the output instance
     */
    private final Operations OPS = new Operations(this);

    /**
     * Username and Password edittexts
     */
    private EditText username, password;

    /**
     * TextView to show errors to the user
     */
    private TextView tvError;

    /**
     * ImageView to handle loading spinner to the user
     */
    private ImageView ivWheel;
    /**
     * Animation to rotate the spinner
     */
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get saved user in the app
        ParseUser curUser = ParseUser.getCurrentUser();
        // check if it's empty or not
        if (curUser != null) {
            // if not empty move to the UsersAcitivty then finish this one
            Intent intent = new Intent(this, ContactsActivity.class);
            startActivity(intent);
            finish();
        }

        // if no user logged in, initialize local views and variables to login or signup
        username = (EditText) findViewById(R.id.etUsername);
        password = (EditText) findViewById(R.id.etPassword);

        // initialize the signup error textview
        tvError = (TextView) findViewById(R.id.tvLoginActivityError);

        // initialize wheel ImageView and the animation
        ivWheel = (ImageView) this.findViewById(R.id.ivRotateWheel);
        animation = AnimationUtils.loadAnimation(this, R.anim.rotate_wheel);

        // initialize the login button click listener
        findViewById(R.id.bLogin).setOnClickListener(this);
        // initialize the sign up button click listener
        findViewById(R.id.bSignup).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        // check user input
        String user = username.getText().toString();
        String pass = password.getText().toString();

        // check input data
        if (!checkInput(user, pass))
            return;

            // check network
        else if (!OPS.isOnline()) {
            tvError.setText(getString(R.string.error_network));
            return;
        }

        // show the ImageView and start animation
        ivWheel.setVisibility(View.VISIBLE);
        ivWheel.startAnimation(animation);

        switch (v.getId()) {
            case R.id.bLogin:
                login(user, pass);
                break;
            case R.id.bSignup:
                signUp(user, pass);
                break;
        }
    }

    /**
     * Login to app with the given parameters. If success move to next activity
     * otherwise show toast error
     *
     * @param username Name of user to login
     * @param password Password of use
     */
    private void login(final String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                // check the returned result
                if (user == null) {
                    // error occurred, show toast
                    tvError.setText(getString(R.string.error_login));

                    // stop the loading wheel
                    stopAnimation();
                } else
                    // save username
                    saveUsername(username);
            }
        });
    }

    /**
     * Sign up to app with the given parameters. If success move to next activity
     * otherwise show toast error
     *
     * @param username Name of new user
     * @param password Password of new user
     */
    private void signUp(final String username, String password) {
        // initialize the new user and set the values
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                // check the returned exception
                if (e == null)
                    // save username
                    saveUsername(username);
                else {
                    // error occurred, show toast
                    tvError.setText(getString(R.string.error_signup));

                    // stop the loading wheel
                    stopAnimation();
                }
            }
        });
    }

    /**
     * save the username in the installation.
     *
     * @param username username to be saved
     */
    private void saveUsername(final String username) {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.addUnique("username", username);
        installation.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                // check the returned exception
                if (e == null) {
                    // start the users activity
                    Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
                    startActivity(intent);

                    // stop the loading wheel
                    stopAnimation();

                    // end this activity
                    finish();
                } else
                    // error occurred, show toast
                    tvError.setText(getString(R.string.error));
            }
        });
    }

    /**
     * Check the entered username and password.
     *
     * @param username username entered in fist EditText
     * @param password password entered in second EditText
     * @return true if input passes the tests or false otherwise
     */
    private boolean checkInput(String username, String password) {
        boolean returned = true;
        // check username and password length
        if (username.length() < 6) {
            tvError.setText(getString(R.string.short_name));
            returned = false;
        } else if (password.length() < 6) {
            tvError.setText(getString(R.string.short_pass));
            returned = false;
        }
        // clear entered password and error textview
        if (returned)
            tvError.setText("");
        else
            this.password.setText("");

        return returned;
    }

    /**
     * Stop the spinning wheel animation and clear password text.
     */
    private void stopAnimation() {
        // hide the ImageView and stop animation
        ivWheel.setVisibility(View.INVISIBLE);
        ivWheel.clearAnimation();

        // clear entered password
        password.setText("");
    }
}

package soft.ams.messagingapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class ContactsActivity extends AppCompatActivity {
    /**
     * Instantiate the operations class to show toasts and logs
     */
    private final Operations OPS = new Operations(this);

    /**
     * Create object from the search fragment that handles user searches
     */
    private SearchFragment searchFrag;

    /**
     * EditText in the toolbar to take user search
     */
    private EditText search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        // set the toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add default fragment
        getSupportFragmentManager().beginTransaction()
                .add(R.id.frameContacts, new ContactsFragment()).commit();

        // handle up clicks
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // act like back button
                onBackPressed();
            }
        });

        // toolbar edittext
        search = (EditText) findViewById(R.id.etSearch);
        // handle text changes to filter
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // start filtering on text change
                if(searchFrag != null)
                    searchFrag.filterList(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate the menu
        getMenuInflater().inflate(R.menu.menu_contacts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bSearch:
                // show the edittext
                search.setVisibility(View.VISIBLE);
                // show up button in toolbar
                //noinspection ConstantConditions
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                // move to search fragment and the keep the previous one in the backstack
                searchFrag = new SearchFragment();
                android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager()
                        .beginTransaction();
                ft.replace(R.id.frameContacts, searchFrag);
                ft.addToBackStack(null);
                ft.commit();

                return true;
            case R.id.bSettings:

                return true;
            case R.id.bLogout:
                // check network connection
                if (OPS.isOnline())
                    // remove the username
                    removeUser();
                else
                    OPS.toast(getString(R.string.error_network), false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Remove the username from the installation.
     */
    private void removeUser() {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.remove("username");
        installation.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                // check the returned exception
                if (e == null)
                    // logout
                    logout();
                else
                    // error occurred, show toast
                    OPS.toast(getString(R.string.error_logout), false);
            }
        });
    }

    /**
     * Logout from app.
     */
    private void logout() {
        // logout the user
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // logged out successfully
                    // clear the shared preferences
                    RecentContacts contacts = RecentContacts.getInstance(ContactsActivity.this);
                    contacts.clearData();

                    // start the login activity
                    Intent intent = new Intent(ContactsActivity.this, MainActivity.class);
                    startActivity(intent);
                    // end this activity
                    finish();
                } else
                    // error happened, show toast
                    OPS.toast(getString(R.string.error_logout), false);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // hide actionbar home button
        // hide the search edittext if it's visible
        if (search.getVisibility() == View.VISIBLE) {
            //noinspection ConstantConditions
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            search.setVisibility(View.GONE);
        }
    }
}

package soft.ams.messagingapp;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SendCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {
    /**
     * Create object to handle showing toast to the user
     */
    private final Operations OPS = new Operations(this);

    public static final String KEY_SENDER = "sender";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_MESSAGING_TO = "soft.ams.messagingapp.CONTACT";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_ACTION_NOTIFY = "notify";

    /**
     * The current username
     */
    public static final String CUR_USERNAME = ParseUser.getCurrentUser().getUsername();

    /**
     * Current username
     */
    private static String curContact;

    /**
     * Create object of the receiver to update the messages list
     */
    private ParseReceiver receiver = new ParseReceiver();

    /**
     * List of messages
     */
    private List<HashMap<String, String>> messages;
    /**
     * Adapter of messages
     */
    private MessagesAdapter adapter;
    /**
     * ListView to view messages
     */
    private ListView lv;

    /**
     * List of positions in the ListView which message is being sent
     */
    private List<Integer> sendingInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        // set the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // set back button click listener
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start the contacts activity
                Intent intent = new Intent(MessagesActivity.this, ContactsActivity.class);
                startActivity(intent);
                // end this activity
                finish();
            }
        });

        // messages list and adapter
        messages = new ArrayList<>();
        sendingInProgress = new ArrayList<>();
        adapter = new MessagesAdapter(this,
                R.layout.message_sender, R.layout.message_receiver, messages);

        // set the conversation
        updateContact();

        // message edittext
        final EditText msg = (EditText) findViewById(R.id.etMessage);

        // handle send button clicks
        findViewById(R.id.bSendMsg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!OPS.isOnline()) {
                    OPS.toast(getString(R.string.error_network), false);
                    return;
                }
                // text to be send
                final String message = msg.getText().toString();

                if (message.length() == 0)
                    return;

                // initialize JSONObject with the data
                JSONObject data = new JSONObject();

                msg.setText("");
                try {
                    data.put(KEY_SENDER, CUR_USERNAME);
                    data.put(KEY_ACTION_NOTIFY, false);
                    data.put(KEY_MESSAGE, message);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                // add the message to the list and update the adapter
                HashMap<String, String> map = new HashMap<>();
                map.put(KEY_SENDER, CUR_USERNAME);
                map.put(KEY_MESSAGE, message);
                sendingInProgress.add(messages.size());
                messages.add(map);
                adapter.notifyDataSetChanged();
                lv.smoothScrollToPosition(messages.size());

                // initialize the push then send it
                ParsePush parsePush = new ParsePush();
                // send normal message
                parsePush.setMessage(getString(R.string.error));
                // send json
                parsePush.setData(data);

                // query installations based on the username
                ParseQuery<ParseInstallation> parseQuery = ParseInstallation.getQuery();
                parseQuery.whereEqualTo(KEY_USERNAME, curContact);

                // send the push
                parsePush.setQuery(parseQuery);
                parsePush.sendInBackground(new SendCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            // push sent
                            // hide the wheel image
                            View v = getViewByPosition(sendingInProgress.get(0));
                            sendingInProgress.remove(0);
                            ImageView wheel = (ImageView) v.findViewById(R.id.ivRotateWheel);
                            wheel.setVisibility(View.GONE);
                        } else
                            // error happened, show toast
                            OPS.toast(getString(R.string.error_push), false);
                    }
                });
            }
        });


        // messages ListView
        lv = (ListView) findViewById(R.id.lvMessages);
        lv.setAdapter(adapter);

    }

    public View getViewByPosition(int pos) {
        final int firstListItemPosition = lv.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + lv.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return lv.getAdapter().getView(pos, null, lv);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return lv.getChildAt(childIndex);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // update the conversation
        updateContact();
    }

    /**
     * Called when activity created or on new intent received to update the conversation
     * and set the new data to it.
     */
    private void updateContact() {
        // get chattingTo user
        curContact = getIntent().getStringExtra(KEY_MESSAGING_TO);

        // keep chattingTo data
        StateKeeper.setChattingTo(curContact);
        OPS.log(Operations.LOG_ERROR, "changed", "contact changed " + StateKeeper.getChattingTo());

        // set its title the selected item from the intent extra
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(curContact);

        // get saved messages
        new MessagesGetter().execute();
    }

    /**
     * Get messages from database in background
     */
    private class MessagesGetter extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // clear the list and the adapter
            messages.clear();
            adapter.notifyDataSetChanged();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // get the notified messages from the keeper then add them to the list
            MessagesKeeper keeper = new MessagesKeeper(MessagesActivity.this, curContact);
            keeper.getAll(messages);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // update the adapter
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Receiver to notifications from the current conversation.
     */
    private class ParseReceiver extends ParsePushBroadcastReceiver {
        @Override
        protected void onPushReceive(Context context, Intent intent) {
            try {
                // get json from intent
                JSONObject json = new JSONObject(intent.getStringExtra(KEY_PUSH_DATA));
                // get saved extras
                String username = json.getString(KEY_SENDER);

                // check chattingTo username
                if (!StateKeeper.getChattingTo().equals(username))
                    // notification from another conversation, leave it to the other receiver
                    return;

                String message = json.getString(KEY_MESSAGE);

                // add the message to the list and update the adapter
                HashMap<String, String> map = new HashMap<>();
                map.put(KEY_SENDER, username);
                map.put(KEY_MESSAGE, message);
                messages.add(map);
                adapter.notifyDataSetChanged();
                lv.smoothScrollToPosition(messages.size());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // activity is not running
        StateKeeper.setMessagesActivityRunning(false);
        // remove receving current messages
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // activity is running
        StateKeeper.setMessagesActivityRunning(true);
        // receive current messages
        registerReceiver(receiver, new IntentFilter(ParsePushBroadcastReceiver.ACTION_PUSH_RECEIVE));
    }
}

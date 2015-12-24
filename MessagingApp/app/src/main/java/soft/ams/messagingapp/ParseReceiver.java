package soft.ams.messagingapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseReceiver extends ParsePushBroadcastReceiver {
    @Override
    protected void onPushReceive(Context context, Intent intent) {
        JSONObject json;
        boolean notify;
        String username, message;
        try {
            // get json from intent
            json = new JSONObject(intent.getStringExtra(KEY_PUSH_DATA));
            // get message sender
            username = json.getString(MessagesActivity.KEY_SENDER);
            // notify user
            notify = json.getBoolean(MessagesActivity.KEY_ACTION_NOTIFY);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        if (notify) {
            // create the notification ticker
            String ticker = context.getString(R.string.noti_from) + ' ' + username;
            message = context.getString(R.string.notified);

            // show notification to the current user from this username
            UserNotifier.showNotification(context, new Intent(), ticker, username, message,
                    (int) System.currentTimeMillis(), true);
        }
        // check message activity state and chattingTo username
        else if (StateKeeper.isMessagesActivityRunning() && StateKeeper.getChattingTo().equals(username))
            // notification from the current conversation, leave it to the other receiver
            return;

        try {
            // get message content
            message = json.getString(MessagesActivity.KEY_MESSAGE);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // save message then show notification
        new MessageSaver(context, username, message).execute();
    }

    /**
     * Save messages in database and get the user notification id
     */
    private class MessageSaver extends AsyncTask<Void, Void, Integer> {
        private Context context;
        private String username;
        private String message;

        private int userId;

        public MessageSaver(Context context, String username, String message) {
            this.context = context;
            this.username = username;
            this.message = message;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            // append this notification to the keeper
            MessagesKeeper keeper = new MessagesKeeper(context, username);
            keeper.insert(message);

            userId = NotificationIdKeeper.getId(username);

            // add the username to the recent list
            RecentContacts recent = RecentContacts.getInstance(context);
            recent.add(username);

            return keeper.count();
        }

        @Override
        protected void onPostExecute(Integer aInteger) {
            super.onPostExecute(aInteger);

            // create the intent to open messages activity
            Intent intent = new Intent(context, MessagesActivity.class);
            intent.putExtra(MessagesActivity.KEY_MESSAGING_TO, username);

            // create the notification ticker
            String ticker = context.getString(R.string.noti_msg_from) + ' ' + username;
            // first check if the user has one message or many saved
            // substring the notification message if it's longer than 25 letters
            if (aInteger > 1)
                message = context.getResources().getQuantityString(
                        R.plurals.messages, aInteger, aInteger);

            // show the notification
            UserNotifier.showNotification(context, intent, ticker, username, message, userId, false);
        }
    }
}

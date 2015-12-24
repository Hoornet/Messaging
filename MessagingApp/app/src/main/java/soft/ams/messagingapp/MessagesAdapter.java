package soft.ams.messagingapp;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.HashMap;
import java.util.List;

public class MessagesAdapter extends BaseAdapter {
    /**
     * List of messages
     */
    private List<HashMap<String, String>> messages;
    /**
     * Context object
     */
    private Context context;
    /**
     * Resources id of 2 views
     */
    private int res1, res2;

    /**
     * Current username
     */
    private static String curUsername;

    /**
     * Screen width
     */
    private int maxWidth;

    /**
     * Construct an adapter with the given parameters.
     *
     * @param context  The context of the adapter
     * @param res1     Id refers to sender resource layout
     * @param res2     Id refers to receiver resource layout
     * @param messages List<HashMap> that carries each message_receiver with the sender name
     */
    public MessagesAdapter(Context context, int res1, int res2, List<HashMap<String, String>> messages) {
        this.messages = messages;
        this.context = context;
        this.res1 = res1;
        this.res2 = res2;

        // set get the width of the screen to set max width of message textview
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        maxWidth = (int)(0.85 * metrics.widthPixels);

        // get the current username
        curUsername = ParseUser.getCurrentUser().getUsername();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public HashMap<String, String> getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).get(MessagesActivity.KEY_SENDER).equals(curUsername) ? 0 : 1;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        HashMap<String, String> map = getItem(position);
        MessageHolder holder;
        // inflate the layout according to the message_receiver sender
        // then set the data in the viewholder if the view is null
        if (view == null) {
            int res = 0;
            int direction = getItemViewType(position);
            if (direction == 0)
                res = res1;
            else if (direction == 1)
                res = res2;
            view = ((Activity) context).getLayoutInflater().inflate(res, parent, false);

            holder = new MessageHolder();
            holder.message = (TextView) view.findViewById(R.id.tvMessage);

            holder.message.setMaxWidth(maxWidth);

            view.setTag(holder);
        } else
            holder = (MessageHolder) view.getTag();

        // set the message_receiver to the textview
        String text = map.get(MessagesActivity.KEY_MESSAGE);
        holder.message.setText(text);

        return view;
    }

    /**
     * ViewHolder of message textview
     */
    static class MessageHolder {
        TextView message;
    }
}

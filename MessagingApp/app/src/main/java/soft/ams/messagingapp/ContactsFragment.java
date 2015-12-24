package soft.ams.messagingapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SendCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {
    /**
     * Create object to handle showing toast to the user
     */
    private Operations ops;

    /**
     * Object of recent contacts to get saved users
     */
    private RecentContacts contacts;

    /**
     * List of users and adapter
     */
    private List<String> recents;
    private ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        final Context CONTEXT = getActivity();

        // initialize the logging class
        ops = new Operations(CONTEXT);

        // initialize the list of rescent users
        recents = new ArrayList<>();

        // check if the app ran before. if not, set it
        contacts = RecentContacts.getInstance(CONTEXT);
        if (contacts.isFirstRun())
            contacts.setRun();

        // initialize the listview and adapter
        adapter = new ArrayAdapter<>(CONTEXT, R.layout.single_contact, recents);
        ListView lv = (ListView) view.findViewById(R.id.lvRecentUsers);

        // set the adapter to listview
        lv.setAdapter(adapter);

        // set setOnItemClickListener to the ListView
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // update the selected index position
                contacts.add(recents.get(position));
                // start the messages activity
                Intent intent = new Intent(CONTEXT, MessagesActivity.class);
                intent.putExtra(MessagesActivity.KEY_MESSAGING_TO, recents.get(position));
                startActivity(intent);
            }
        });
        // register the lv to a context menu
        registerForContextMenu(lv);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // get saved recents in the sharedpreferences then update the adapter
        contacts.getRecentContacts(recents);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, v.getId(), 0, getText(R.string.notify));
        menu.add(0, v.getId(), 1, getText(R.string.b_context_remove));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // get the listview selected index
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        switch (item.getOrder()) {
            case 0:
                if (!ops.isOnline())
                    ops.toast(getString(R.string.error_network), false);
                else
                    // send notification to the user
                    notifyUser(index);
                return true;
            case 1:
                // remove contact from the list
                if (contacts.remove(index)) {
                    recents.remove(index);
                    adapter.notifyDataSetChanged();
                }
                return true;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * Send notification to the user at the specific position.
     *
     * @param position the position of the user in the list
     */

    private void notifyUser(int position) {
        // initialize JSONObject with the data
        JSONObject data = new JSONObject();

        try {
            data.put(MessagesActivity.KEY_SENDER, MessagesActivity.CUR_USERNAME);
            data.put(MessagesActivity.KEY_ACTION_NOTIFY, true);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // initialize the push then send it
        ParsePush parsePush = new ParsePush();
        // send normal message
        parsePush.setMessage(getString(R.string.error));
        // send json
        parsePush.setData(data);

        // query installations based on the username
        ParseQuery<ParseInstallation> parseQuery = ParseInstallation.getQuery();
        parseQuery.whereEqualTo(MessagesActivity.KEY_USERNAME, recents.get(position));

        // send the push
        parsePush.setQuery(parseQuery);
        parsePush.sendInBackground(new SendCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null)
                    // done show toast
                    ops.toast(getString(R.string.done), true);
                else
                    // error happened, show toast
                    ops.toast(getString(R.string.error_push), false);
            }
        });
    }
}

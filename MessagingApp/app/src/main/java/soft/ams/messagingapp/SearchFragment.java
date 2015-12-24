package soft.ams.messagingapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    private List<String> users;
    private ArrayAdapter<String> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // get the context
        final Context CONTEXT = getActivity();

        // initialize the logging class
        final Operations OPS = new Operations(CONTEXT);

        // initialize the list of users
        users = new ArrayList<>();

        // initialize the adapter
        adapter = new ArrayAdapter<>(CONTEXT, R.layout.single_contact, users);

        // query all users and add the username to the list
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo(MessagesActivity.KEY_USERNAME, ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    // put data in the list then update the adapter
                    for (ParseUser user : objects)
                        users.add(user.getUsername());
                    adapter.notifyDataSetChanged();
                } else
                    // error occurred, show error toast
                    OPS.toast(getString(R.string.error_query_users), false);
            }
        });

        // initialize listview
        ListView lv = (ListView) view.findViewById(R.id.lvAllUsers);

        // set the adapter to listview
        lv.setAdapter(adapter);

        // set setOnItemClickListener to the ListView
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // add the selected user to the recent contacts
                RecentContacts contacts = RecentContacts.getInstance(CONTEXT);
                contacts.add(users.get(position));

                // start the messages activity
                Intent intent = new Intent(CONTEXT, MessagesActivity.class);
                intent.putExtra(MessagesActivity.KEY_MESSAGING_TO, users.get(position));
                startActivity(intent);
            }
        });

        return view;
    }

    /**
     * Filter the list according to the item in the edittext
     */
    public void filterList(CharSequence s) {
        adapter.getFilter().filter(s);
    }
}

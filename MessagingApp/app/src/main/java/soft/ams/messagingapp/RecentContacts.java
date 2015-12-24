package soft.ams.messagingapp;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;

public class RecentContacts {
    private SharedPreferences prefs;

    private static RecentContacts contacts;

    private RecentContacts(Context context) {
        // initialize the sharedpreferences
        prefs = context.getSharedPreferences("recent", Context.MODE_PRIVATE);
    }

    /**
     * Get instance of the shared preferences class.
     *
     * @param context the calling context
     * @return instance of the class
     */
    public static RecentContacts getInstance(Context context) {
        if (contacts == null)
            contacts = new RecentContacts(context);
        return contacts;
    }

    /**
     * Check if it's the first time to run the app or not.
     *
     * @return true if it's the first time or false otherwise
     */
    public boolean isFirstRun() {
        return prefs.getBoolean("firstRun", true);
    }

    /**
     * Set that the app already ran before.
     */
    public void setRun() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstRun", false);
        setLength(0);
        editor.apply();
    }

    /**
     * Get length of recent users saved in prefs.
     *
     * @return the number of users
     */
    private int getLength() {
        return prefs.getInt("length", 0);
    }

    /**
     * Update the length of saved items.
     *
     * @param len the new length
     */
    private void setLength(int len) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("length", len);
        editor.apply();
    }

    /**
     * Insert recent user in the prefs.
     *
     * @param username the username to be inserted
     */
    public void add(String username) {
        int index = contains(username);
        if (index == -1) {
            int len = getLength();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("" + len++, username);
            setLength(len);
            editor.apply();
            swapPlacesDown(len - 1);
        } else
            swapPlacesDown(index);
    }

    /**
     * Remove item at index.
     *
     * @param position the index of item to be removed.
     */
    public boolean remove(int position) {
        SharedPreferences.Editor editor = prefs.edit();
        swapPlaces(position);
        editor.remove("" + getLength());

        return editor.commit();
    }

    /**
     * Swap all items after the removed start to keep the length simple.
     *
     * @param start Swap starting from this index
     */
    private void swapPlaces(int start) {
        int len = getLength();
        SharedPreferences.Editor editor = prefs.edit();
        for (int i = start; i < len - 1; i++) {
            String temp = prefs.getString("" + (i + 1), "");
            editor.putString("" + i, temp);
        }
        editor.apply();
        setLength(len - 1);
    }

    /**
     * Get the recent contacts.
     *
     * @param list Reference to the list to be updated
     */
    public void getRecentContacts(List<String> list) {
        list.clear();

        for (int i = 0; i < getLength(); i++)
            list.add(prefs.getString("" + i, ""));
    }

    /**
     * Clear the saved data in shared preferences
     */
    public void clearData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * Check if the data has a value that matches the given parameter.
     *
     * @param username username to search in the list for
     * @return true if the data contains that user or false otherwise
     */
    public int contains(String username) {
        for (int i = 0; i < getLength(); i++)
            if (prefs.getString("" + i, "").equals(username))
                return i;
        return -1;
    }

    /**
     * Swap all items down to make the most recent on the top
     */
    public void swapPlacesDown(int index) {
        String recent = prefs.getString("" + (index), "");

        SharedPreferences.Editor editor = prefs.edit();
        for (int i = index; i > 0; i--) {
            String temp = prefs.getString("" + (i - 1), "");
            editor.putString("" + i, temp);
        }
        editor.putString("" + 0, recent);

        editor.apply();
    }
}

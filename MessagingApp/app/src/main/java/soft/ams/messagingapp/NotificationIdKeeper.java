package soft.ams.messagingapp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationIdKeeper {
    /**
     * Usernames who's notified the user
     */
    private static final List<String> USERNAME_LIST = new ArrayList<>();
    /**
     * Username's temporary id
     */
    private static final List<Integer> ID_LIST = new ArrayList<>();

    /**
     * Returns unique id to the username
     * @param username Username who's notified the user
     * @return unique id to the user
     */
    public static int getId(String username) {
        // iterators to loop over the 2 lists
        Iterator<String> userItr = USERNAME_LIST.iterator();
        Iterator<Integer> idItr = ID_LIST.iterator();

        // loop to get the id of the user
        while (userItr.hasNext()) {
            int id = idItr.next();
            if (userItr.next().equals(username))
                return id;
        }

        // if the user doesn't have an id, create it
        return insertUsername(username);
    }

    /**
     * Inserts username in the list and returns its id
     * @param username the username to be inserted
     * @return the inserted user id
     */
    private static int insertUsername(String username) {
        // use current time
        int randInt = (int) System.currentTimeMillis();
        // add the id and username to the list
        ID_LIST.add((int)System.currentTimeMillis());
        USERNAME_LIST.add(username);

        return randInt;
    }
}

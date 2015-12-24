package soft.ams.messagingapp;

public class StateKeeper {
    /**
     * boolean to check if the activity is running or not
     */
    private static boolean messagesActivityRunning = false;
    /**
     * String refers to the name of the current conversation
     */
    private static String chattingTo = "";

    // prevent instantiating this class
    private StateKeeper() {
    }

    /**
     * Check if the activity is running or not
     */
    public static boolean isMessagesActivityRunning() {
        return messagesActivityRunning;
    }

    /**
     * Set current activity state
     *
     * @param messagesActivityRunning true means activity is running and false otherwise
     */
    public static void setMessagesActivityRunning(boolean messagesActivityRunning) {
        StateKeeper.messagesActivityRunning = messagesActivityRunning;
    }

    /**
     * Set that the user is currently chatting to this user
     *
     * @param username the user from current conversation
     */
    public static void setChattingTo(String username) {
        chattingTo = username;
    }

    /**
     * Returns the username of current conversation
     *
     * @return username of the current conversation
     */
    public static String getChattingTo() {
        return chattingTo;
    }

}

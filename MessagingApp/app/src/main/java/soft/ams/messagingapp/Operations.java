package soft.ams.messagingapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

public class Operations {
    // keep the context
    private Context context;

    // log const types
    public static final int LOG_ERROR = 0;
    public static final int LOG_INFO = 1;
    public static final int LOG_DEBUG = 2;

    public Operations(Context context) {
        this.context = context;
    }

    /**
     * Show toast to the user with the given parameter.
     *
     * @param message message_receiver to be shown
     * @param isShort true if the length is short or false otherwise
     */
    public void toast(String message, boolean isShort) {
        Toast.makeText(context, message, isShort ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
    }

    /**
     * Log to the logcat with given tag, msg and log type.
     *
     * @param type Log type 0 as error, 1 as info
     * @param tag  Log tag
     * @param msg  Log message_receiver
     */
    public void log(int type, String tag, Object msg) {
        // print log according to the suitable type
        switch (type) {
            case LOG_ERROR:
                Log.e(tag, msg.toString());
                break;
            case LOG_INFO:
                Log.i(tag, msg.toString());
                break;
            case LOG_DEBUG:
                Log.d(tag, msg.toString());
            default:
                toast("Wrong type", true);
        }
    }

    /**
     * Check if network is available or not.
     *
     * @return true if it's available or false otherwise
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isConnected();
    }
}

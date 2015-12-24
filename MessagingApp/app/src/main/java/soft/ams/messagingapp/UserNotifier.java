package soft.ams.messagingapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;

public class UserNotifier {

    /**
     * Called to show a notification to the user with given parameters.
     *
     * @param context  Context of the sender
     * @param intent   Intent to add to the notification
     * @param ticker   Text that appears in the status bar
     * @param username Notification title
     * @param message  Notification content
     * @param id       Notification user id
     */
    public static void showNotification(Context context, Intent intent, String ticker,
                                        String username, String message, int id, boolean isNotification) {

        // create the pending intent
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        id,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        // create the notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        Notification notification = mBuilder.setSmallIcon(isNotification ?
                R.drawable.attention : R.mipmap.ic_launcher)
                .setTicker(ticker)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentTitle(username)
                .setContentIntent(resultPendingIntent)
                .setSound(isNotification ? Uri.parse(
                        "android.resource://" + context.getPackageName() + "/" + R.raw.horn) :
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                .build();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }
}

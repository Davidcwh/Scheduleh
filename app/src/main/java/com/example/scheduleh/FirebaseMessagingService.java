package com.example.scheduleh;

import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String messageTitle = remoteMessage.getNotification().getTitle();
        String messageBody = remoteMessage.getNotification().getBody();

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.drawable.ic_home_default_profile_pic)
                .setContentTitle(messageTitle)
                .setContentText(messageBody);

        int mNotificationId = (int) System.currentTimeMillis();

        NotificationManager mNotifManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifManager.notify(mNotificationId, mBuilder.build());
    }
}

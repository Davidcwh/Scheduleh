package com.example.scheduleh;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        int id = (int) System.currentTimeMillis();
        //_____NOTIFICATION ID'S FROM FCF_____
        String messageTitle = remoteMessage.getNotification().getTitle();
        String messageBody = remoteMessage.getNotification().getBody();

        NotificationCompat.Builder builder =
                new NotificationCompat
                        .Builder(this, getString(R.string.default_notification_channel_id))
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(messageTitle)
                        .setContentText(messageBody);

        //_____REDIRECTING PAGE WHEN NOTIFICATION CLICKS_____
        Intent resultIntent = new Intent(this, NotificationsActivity.class);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(pendingIntent);

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {

            int importance = NotificationManager.IMPORTANCE_HIGH;
            String channelID = BuildConfig.APPLICATION_ID;
            NotificationChannel channel = new NotificationChannel
                    (getString(R.string.default_notification_channel_id), BuildConfig.APPLICATION_ID, importance);
            channel.setDescription(channelID);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            //assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }


        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(id, builder.build());
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}

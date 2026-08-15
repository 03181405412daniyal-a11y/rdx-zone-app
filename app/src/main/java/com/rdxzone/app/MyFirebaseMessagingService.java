package com.rdxzone.app;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Notification handling will be added here
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
    }
}

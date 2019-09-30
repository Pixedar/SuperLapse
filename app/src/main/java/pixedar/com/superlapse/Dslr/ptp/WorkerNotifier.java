/**
 * Copyright 2013 Nils Assbeck, Guersel Ayaz and Michael Zoech
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pixedar.com.superlapse.Dslr.ptp;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import pixedar.com.superlapse.Camera2.TimelapseStateListener;
import pixedar.com.superlapse.R;


public class WorkerNotifier implements Camera.CamerStateListener, TimelapseStateListener, Camera.WorkerListener {

    /*    private final NotificationManager notificationManager;
        private final Notification notification;
        private final int uniqueId;*/
    private final int uniqueId = 0;
    public static final String CHANNEL_ID = "11";
    public String text = "is Running";
    private Context context;
    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder builder;

    WorkerNotifier(Context context) {
        this.context = context;
        createNotificationChannel();
        builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Superlapse")

                /*     .setStyle(new NotificationCompat.BigTextStyle()
                             .bigText("Much longer text that cannot fit one line..."))*/
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager = NotificationManagerCompat.from(context);

// notificationId is a unique int for each notification that you must define
        notificationManager.notify(uniqueId, builder.build());

/*        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notification = new Notification(R.mipmap.ic_launcher,text ,
                System.currentTimeMillis());
        notification
                .setLatestEventInfo(context.getApplicationContext(), context.getString(text),
                        context.getString(R.string.worker_content_text), null);
        uniqueId = NotificationIds.getInstance().getUniqueIdentifier(WorkerNotifier.class.getName() + ":running")*/
        ;
    }


    @Override
    public void onTimelapseStarted() {

    }

    @Override
    public void onTimelapseEnded() {
        notificationManager.cancel(uniqueId);
    }

    @Override
    public void onTimelapseStatusChanged(String progressString) {
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(progressString));
        notificationManager.notify(uniqueId, builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "superlapse notificaito channel";
            String description = "";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onCameraStarted(PtpCamera camera) {

    }

    @Override
    public void onCameraStopped(Camera camera) {

    }

    @Override
    public void onNoCameraFound() {

    }

    @Override
    public void onWorkerStarted(int id) {

    }

    @Override
    public void onWorkerEnded() {

    }
}

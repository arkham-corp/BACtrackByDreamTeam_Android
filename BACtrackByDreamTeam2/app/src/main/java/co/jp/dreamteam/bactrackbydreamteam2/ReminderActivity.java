package co.jp.dreamteam.bactrackbydreamteam2;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import android.app.Activity;
import android.os.Bundle;

public class ReminderActivity extends Activity {
    private static final String CHANNEL_ID = "reminder_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final int REMINDER_HOUR = 12; // リマインダーの時
    private static final int REMINDER_MINUTE = 0; // リマインダーの分
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        // アラームをセット
        setReminderAlarm();
    }
    private void setReminderAlarm() {
        // アラームマネージャーの取得
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // ブロードキャストレシーバーのIntentを作成
        Intent intent = new Intent(this, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // カレンダーを使用してリマインダーの時間をセット
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, REMINDER_HOUR);
        calendar.set(Calendar.MINUTE, REMINDER_MINUTE);

        // アラームをセット
        if (alarmManager != null) {
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }

    public static class ReminderReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 通知を作成して表示
            createNotification(context);
        }

        private void createNotification(Context context) {
            // NotificationManagerの取得
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // Android Oreo以降はチャネルを設定
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Reminder Channel",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                notificationManager.createNotificationChannel(channel);
            }

            // 通知を作成
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("リマインダー")
                    .setContentText("何かを思い出しましょう！")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            // 通知を表示
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}
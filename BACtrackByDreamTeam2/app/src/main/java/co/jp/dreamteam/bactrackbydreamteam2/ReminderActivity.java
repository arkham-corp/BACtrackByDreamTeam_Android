package co.jp.dreamteam.bactrackbydreamteam2;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ReminderActivity extends FragmentActivity {
    private static final int PERMISSION_REQUEST_CODE = 1001;

    EditText remainder_txtStartYmd;
    EditText remainder_txtStartHm;
    private AlarmManager am;
    private PendingIntent pending;
    private final int requestCode = 1;

    @SuppressLint("ScheduleExactAlarm")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        remainder_txtStartYmd = this.findViewById(R.id.remainder_txtStartYmd);
        remainder_txtStartHm = this.findViewById(R.id.remainder_txtStartHm);

        remainder_txtStartYmd.setEnabled(true);
        remainder_txtStartHm.setEnabled(true);

        remainder_txtStartYmd.setOnClickListener(txtStartYmdClicked);
        remainder_txtStartHm.setOnClickListener(txtStartHmClicked);

        @SuppressLint("SimpleDateFormat") final DateFormat df_ymd = new SimpleDateFormat("yyyy/MM/dd");
        @SuppressLint("SimpleDateFormat") final DateFormat df_hm = new SimpleDateFormat("HH:mm");

        final Date date = new Date(System.currentTimeMillis());
        remainder_txtStartYmd.setText(df_ymd.format(date));
        remainder_txtStartHm.setText(df_hm.format(date));

        //実行権限判断
        checkNotificationPermissions();

        Button buttonStart = this.findViewById(R.id.remainder_btnDecision);
        buttonStart.setOnClickListener(v -> {

            String[] ymd = String.valueOf(remainder_txtStartYmd.getText()).split("/");
            String[] hm = String.valueOf(remainder_txtStartHm.getText()).split(":");
            TimeZone timeZone = TimeZone.getTimeZone("Asia/Tokyo");
            Calendar calendar = Calendar.getInstance(timeZone);
            calendar.setTimeInMillis(0);
            calendar.set(Calendar.YEAR, Integer.parseInt(ymd[0]));              // 任意の年を設定
            calendar.set(Calendar.MONTH, Integer.parseInt(ymd[1])-1);           // 任意の月を設定
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(ymd[2]));      // 任意の日を設定
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hm[0]));        // 任意の時を設定
            calendar.set(Calendar.MINUTE, Integer.parseInt(hm[1]));             // 任意の分を設定
            calendar.set(Calendar.SECOND, 0);               // 任意の秒を設定
            //long triggerTime = calendar.getTimeInMillis();  // 指定した日時のミリ秒表現を取得

            Intent intent = new Intent(this, AlarmNotification.class);
            intent.putExtra("RequestCode",requestCode);
            //pending = PendingIntent.getBroadcast(getApplicationContext(),requestCode, intent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            pending = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // アラームをセットする
            am = (AlarmManager) getSystemService(ALARM_SERVICE);

            if (am != null) {
                //am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime() + 5 * 1000, pending);
                if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (am.canScheduleExactAlarms()) {
                        am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending);
                    } else {
                        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending);
                    }
                } else {
                    am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending);
                }
                // トーストで設定されたことをを表示
                Toast.makeText(getApplicationContext(),getString(R.string.TEXT_REMAINDER_FINISH), Toast.LENGTH_SHORT).show();
                finish();
            }

        });
    }
    View.OnClickListener txtStartYmdClicked = v -> showDatePickerDialogStartYmd(
            "StartYmd", String.valueOf(remainder_txtStartYmd.getText()));
    View.OnClickListener txtStartHmClicked = v -> showTimePickerDialogStartHm(
            "StartHm", String.valueOf(remainder_txtStartHm.getText()));

    public void showDatePickerDialogStartYmd(String tag, String defaultValue) {
        DatePickerDialog.OnDateSetListener listener = (datePicker, year, month, day) -> {
            String ymd = String.format(Locale.JAPAN, "%04d", year)
                    + "/" + String.format(Locale.JAPAN, "%02d", month)
                    + "/" + String.format(Locale.JAPAN, "%02d", day);
            remainder_txtStartYmd.setText(ymd);
        };
        DatePickerFragment newFragment = new DatePickerFragment(listener);
        Bundle args = new Bundle();
        args.putString("default", defaultValue);
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), tag);
    }

    public void showTimePickerDialogStartHm(String tag, String defaultValue) {
        TimePickerDialog.OnTimeSetListener listener = (view, hourOfDay, minute) -> {
            String hm = String.format(Locale.JAPAN, "%02d", hourOfDay) + ":" + String.format(Locale.JAPAN, "%02d", minute);
            remainder_txtStartHm.setText(hm);
        };
        TimePickerFragment newFragment = new TimePickerFragment(listener);
        Bundle args = new Bundle();
        args.putString("default", defaultValue);
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), tag);

    }

    @SuppressLint({"ObsoleteSdkInt", "InlinedApi"})
    private void checkNotificationPermissions() {
        // Android 6.0（APIレベル23）以降で実行されている場合のみ権限の確認が必要
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 必要な権限が許可されているか確認
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED
            )
            {

                if (
                        shouldShowRequestPermissionRationale(android.Manifest.permission.VIBRATE) ||
                        shouldShowRequestPermissionRationale(android.Manifest.permission.SCHEDULE_EXACT_ALARM) ||
                        shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) ||
                        shouldShowRequestPermissionRationale(android.Manifest.permission.RECEIVE_BOOT_COMPLETED)
                ) {
                    android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);
                    alertDialog.setCancelable(false);
                    alertDialog.setTitle(getString(R.string.ALERT_TITLE_INFO));
                    alertDialog.setMessage(
                            "アルコールマネージャー業務用アプリ 写真撮影版が通知機能の使用を求めています。\n" +
                                    "通知機能を利用しリマインダーの通知表示に利用します。"
                    );
                    alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_YES), (dialog, which) -> {
                        // 権限が許可されていない場合、ユーザーに権限を要求
                        ActivityCompat.requestPermissions(this,
                                new String[]{
                                        android.Manifest.permission.VIBRATE,
                                        android.Manifest.permission.SCHEDULE_EXACT_ALARM,
                                        android.Manifest.permission.POST_NOTIFICATIONS,
                                        android.Manifest.permission.RECEIVE_BOOT_COMPLETED
                                },
                                PERMISSION_REQUEST_CODE);
                    });
                    alertDialog.setNegativeButton(getString(R.string.ALERT_BTN_NO), (dialog, which) -> finish());
                    alertDialog.show();
                }

            }
        }
    }

    // 権限リクエスト結果のハンドリング
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            // 権限が許可されたか確認
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults.length > 2 && grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                    grantResults.length > 3 && grantResults[3] == PackageManager.PERMISSION_GRANTED
            )
            {
                // 権限が許可された場合の処理
                Toast.makeText(this, "権限が許可されました", Toast.LENGTH_SHORT).show();
            } else {
                // 権限が許可されなかった場合の処理
                showPermissionDeniedDialog();
            }
        }
    }

    // 権限が拒否された場合のダイアログ表示
    private void showPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("権限が拒否されました")
                .setMessage("このアプリでは通知に関する権限が必要です。設定画面から権限を許可してください。")
                .setPositiveButton("設定画面へ", (dialog, which) -> {
                    // 設定画面を開く
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("キャンセル", (dialog, which) -> {
                    // キャンセル時の処理
                    Toast.makeText(ReminderActivity.this, "権限が拒否されました", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
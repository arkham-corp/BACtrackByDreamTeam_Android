package co.jp.dreamteam.bactrackbydreamteam2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ReminderActivity extends FragmentActivity {

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

        final DateFormat df_ymd = new SimpleDateFormat("yyyy/MM/dd");
        final DateFormat df_hm = new SimpleDateFormat("HH:mm");

        final Date date = new Date(System.currentTimeMillis());
        remainder_txtStartYmd.setText(df_ymd.format(date));
        remainder_txtStartHm.setText(df_hm.format(date));

        Button buttonStart = this.findViewById(R.id.remainder_btnDecision);
        buttonStart.setOnClickListener(v -> {

            String ymd[]= String.valueOf(remainder_txtStartYmd.getText()).split("/");
            String hm[]= String.valueOf(remainder_txtStartHm.getText()).split(":");
            TimeZone timeZone = TimeZone.getTimeZone("Asia/Tokyo");
            Calendar calendar = Calendar.getInstance(timeZone);
            calendar.setTimeInMillis(0);
            /*
            calendar.set(Calendar.YEAR, 2023);              // 任意の年を設定
            calendar.set(Calendar.MONTH, Calendar.DECEMBER);               // 任意の月を設定
            calendar.set(Calendar.DAY_OF_MONTH, 25);        // 任意の日を設定
            calendar.set(Calendar.HOUR_OF_DAY, 11);         // 任意の時を設定
            calendar.set(Calendar.MINUTE, 4);               // 任意の分を設定
            calendar.set(Calendar.SECOND, 0);               // 任意の秒を設定
            */
            calendar.set(Calendar.YEAR, Integer.parseInt(ymd[0].toString()));              // 任意の年を設定
            calendar.set(Calendar.MONTH, Integer.parseInt(ymd[1].toString())-1);               // 任意の月を設定
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(ymd[2].toString()));        // 任意の日を設定
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hm[0].toString()));         // 任意の時を設定
            calendar.set(Calendar.MINUTE, Integer.parseInt(hm[1].toString()));               // 任意の分を設定
            calendar.set(Calendar.SECOND, 0);               // 任意の秒を設定
            long triggerTime = calendar.getTimeInMillis();  // 指定した日時のミリ秒表現を取得

            Intent intent = new Intent(this, AlarmNotification.class);
            intent.putExtra("RequestCode",requestCode);
            //pending = PendingIntent.getBroadcast(getApplicationContext(),requestCode, intent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            pending = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            // アラームをセットする
            am = (AlarmManager) getSystemService(this.ALARM_SERVICE);

            if (am != null) {
                //am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime() + 5 * 1000, pending);
                am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending);
                // トーストで設定されたことをを表示
                Toast.makeText(getApplicationContext(),getString(R.string.TEXT_REMAINDER_FINISH), Toast.LENGTH_SHORT).show();
                finish();
            }

        });

    }
    View.OnClickListener txtStartYmdClicked = v -> showDatePickerDialogStartYmd(remainder_txtStartYmd,
            "StartYmd", String.valueOf(remainder_txtStartYmd.getText()));
    View.OnClickListener txtStartHmClicked = v -> showTimePickerDialogStartHm(remainder_txtStartHm,
            "StartHm", String.valueOf(remainder_txtStartHm.getText()));

    public void showDatePickerDialogStartYmd(View v, String tag, String defaultValue) {
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

    public void showTimePickerDialogStartHm(View v, String tag, String defaultValue) {
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
}
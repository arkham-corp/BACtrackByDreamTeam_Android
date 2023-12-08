package co.jp.dreamteam.bactrackbydreamteam2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Space;

public class MenuActivity extends Activity {

    SharedPreferences pref;

    private Space menu_spaceDrivingReport;
    private Space menu_spaceSendList;
    private Space menu_spaceReminder;

    private Button menu_btnInspection;
    private Button menu_btnDrivingReport;
    private Button menu_btnSendList;
    private Button menu_btnReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

        // ボタン有効確認
        String app_roll_call_enabled = pref.getString(getString(R.string.PREF_KEY_MENU＿DRIVING_REPORT_ENABLED), "0");
        String app_send_list_enabled = pref.getString(getString(R.string.PREF_KEY_MENU＿SEND_LIST_ENABLED), "0");
        String app_reminder_enabled = pref.getString(getString(R.string.PREF_KEY_MENU＿REMINDER_ENABLED), "0");

        // スペース参照
        menu_spaceDrivingReport = this.findViewById(R.id.menu_spaceDrivingReport);
        menu_spaceSendList = this.findViewById(R.id.menu_spaceSendList);
        menu_spaceReminder = this.findViewById(R.id.menu_spaceReminder);

        // ボタン参照
        menu_btnInspection = this.findViewById(R.id.menu_btnInspection);
        menu_btnDrivingReport = this.findViewById(R.id.menu_btnDrivingReport);
        menu_btnSendList = this.findViewById(R.id.menu_btnSendList);
        menu_btnReminder = this.findViewById(R.id.menu_btnReminder);

        // ボタン設定
        menu_btnInspection.setOnClickListener(btnInspectionClicked);

        if (app_roll_call_enabled.equals("1")) {
            menu_btnDrivingReport.setOnClickListener(btnDrivingReportClicked);
        } else {
            menu_spaceDrivingReport.setVisibility(View.INVISIBLE);
            menu_btnDrivingReport.setVisibility(View.INVISIBLE);
        }

        if (app_send_list_enabled.equals("1")) {
            menu_btnSendList.setOnClickListener(btnSendListClicked);
        } else {
            menu_spaceSendList.setVisibility(View.INVISIBLE);
            menu_btnSendList.setVisibility(View.INVISIBLE);
        }

        if (app_reminder_enabled.equals("1")) {
            menu_btnReminder.setOnClickListener(btnReminderClicked);
        } else {
            menu_spaceReminder.setVisibility(View.INVISIBLE);
            menu_btnReminder.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        menu_btnInspection.setEnabled(true);
        menu_btnDrivingReport.setEnabled(true);
        menu_btnSendList.setEnabled(true);
        menu_btnReminder.setEnabled(true);
    }

    View.OnClickListener btnInspectionClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            menu_btnInspection.setEnabled(false);
            Intent intent = new Intent(getApplication(), GPSActivity.class);
            startActivity(intent);
        }
    };

    View.OnClickListener btnDrivingReportClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            menu_btnDrivingReport.setEnabled(false);
            Intent intent = new Intent(getApplication(), DrivingReportActivity.class);
            startActivity(intent);
        }
    };

    View.OnClickListener btnSendListClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            menu_btnSendList.setEnabled(false);
            Intent intent = new Intent(getApplication(), SendListActivity.class);
            startActivity(intent);
        }
    };

    View.OnClickListener btnReminderClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            menu_btnReminder.setEnabled(false);
            Intent intent = new Intent(getApplication(), ReminderActivity.class);
            startActivity(intent);
        }
    };
}
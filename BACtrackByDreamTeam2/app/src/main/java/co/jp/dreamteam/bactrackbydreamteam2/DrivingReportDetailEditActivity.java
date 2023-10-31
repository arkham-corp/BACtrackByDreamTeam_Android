package co.jp.dreamteam.bactrackbydreamteam2;

import static android.text.InputType.TYPE_NULL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.util.Locale;

import io.realm.Realm;

public class DrivingReportDetailEditActivity extends FragmentActivity {

    public static final String EXTRA_MESSAGE = "co.jp.dreamteam.bactrackbydreamteam2.MESSAGE";
    private Realm realm;

    int driving_report_id;
    int driving_report_detail_id;

    EditText driving_report_detail_edit_txtDestination;
    EditText driving_report_detail_edit_txtDrivingStartHm;
    EditText driving_report_detail_edit_txtDrivingStartKm;
    EditText driving_report_detail_edit_txtDrivingEndHm;
    EditText driving_report_detail_edit_txtDrivingEndKm;
    EditText driving_report_detail_edit_txtCargoWeight;
    EditText driving_report_detail_edit_txtCargoStatus;
    EditText driving_report_detail_edit_txtNote;

    ImageButton driving_report_detail_edit_btnDrivingStartHm;
    ImageButton driving_report_detail_edit_btnDrivingEndHm;

    Button driving_report_detail_edit_btnDestinationSelect;
    Button driving_report_detail_edit_btnSave;
    Button driving_report_detail_edit_btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driving_report_detail_edit);

        realm = Realm.getDefaultInstance();

        driving_report_id = getIntent().getIntExtra("driving_report_id", 0);
        driving_report_detail_id = getIntent().getIntExtra("id", 0);

        // 行先
        driving_report_detail_edit_txtDestination = this.findViewById(R.id.driving_report_detail_edit_txtDestination);

        // 発時刻
        driving_report_detail_edit_txtDrivingStartHm = this.findViewById(R.id.driving_report_detail_edit_txtDrivingStartHm);

        // 発メーター
        driving_report_detail_edit_txtDrivingStartKm = this.findViewById(R.id.driving_report_detail_edit_txtDrivingStartKm);

        // 着時刻
        driving_report_detail_edit_txtDrivingEndHm = this.findViewById(R.id.driving_report_detail_edit_txtDrivingEndHm);

        // 着メーター
        driving_report_detail_edit_txtDrivingEndKm = this.findViewById(R.id.driving_report_detail_edit_txtDrivingEndKm);

        // 重量/個数
        driving_report_detail_edit_txtCargoWeight = this.findViewById(R.id.driving_report_detail_edit_txtCargoWeight);

        // 積載状況
        driving_report_detail_edit_txtCargoStatus = this.findViewById(R.id.driving_report_detail_edit_txtCargoStatus);

        // 備考
        driving_report_detail_edit_txtNote = this.findViewById(R.id.driving_report_detail_edit_txtNote);

        // 選択ボタン
        driving_report_detail_edit_btnDestinationSelect = this.findViewById(R.id.driving_report_detail_edit_btnDestinationSelect);

        // クリアボタン
        driving_report_detail_edit_btnDrivingStartHm = this.findViewById(R.id.driving_report_detail_edit_btnDrivingStartHm);

        driving_report_detail_edit_btnDrivingEndHm = this.findViewById(R.id.driving_report_detail_edit_btnDrivingEndHm);

        // 保存ボタン
        driving_report_detail_edit_btnSave = this.findViewById(R.id.driving_report_detail_edit_btnSave);
        driving_report_detail_edit_btnSave.setOnClickListener(btnSaveClicked);

        // 削除ボタン
        driving_report_detail_edit_btnDelete = this.findViewById(R.id.driving_report_detail_edit_btnDelete);
        driving_report_detail_edit_btnDelete.setOnClickListener(btnDeleteClicked);

        RealmLocalDataDrivingReport drivingReport = readRecordHeader();

        if (String.valueOf(drivingReport.getSendFlg()).equals("1")) {
            SetReadOnly(driving_report_detail_edit_txtDestination);
            SetReadOnly(driving_report_detail_edit_txtDrivingStartHm);
            SetReadOnly(driving_report_detail_edit_txtDrivingStartKm);
            SetReadOnly(driving_report_detail_edit_txtDrivingEndHm);
            SetReadOnly(driving_report_detail_edit_txtDrivingEndKm);
            SetReadOnly(driving_report_detail_edit_txtCargoWeight);
            SetReadOnly(driving_report_detail_edit_txtCargoStatus);
            SetReadOnly(driving_report_detail_edit_txtNote);

            driving_report_detail_edit_btnDestinationSelect.setVisibility(View.GONE);
            driving_report_detail_edit_btnDrivingStartHm.setVisibility(View.GONE);
            driving_report_detail_edit_btnDrivingEndHm.setVisibility(View.GONE);

            driving_report_detail_edit_btnSave.setVisibility(View.GONE);
            driving_report_detail_edit_btnDelete.setVisibility(View.GONE);
        } else {
            driving_report_detail_edit_btnDestinationSelect.setOnClickListener(btnDestinationSelectClearClicked);
            driving_report_detail_edit_txtDrivingStartHm.setOnClickListener(txtDrivingStartHmClicked);
            driving_report_detail_edit_btnDrivingStartHm.setOnClickListener(btnDrivingStartHmClearClicked);
            driving_report_detail_edit_txtDrivingEndHm.setOnClickListener(txtDrivingEndHmClicked);
            driving_report_detail_edit_btnDrivingEndHm.setOnClickListener(btnDrivingEndHmClearClicked);
        }

        // 値取得
        RealmLocalDataDrivingReportDetail drivingReportDetail = readRecord();

        if (drivingReportDetail == null) {
            driving_report_detail_edit_btnDelete.setEnabled(false);
        } else {
            // 値セット
            driving_report_detail_edit_txtDestination.setText(drivingReportDetail.getDestination());
            if (!drivingReportDetail.getDriving_start_hm().equals("")) {
                String strTime = drivingReportDetail.getDriving_start_hm().substring(0, 2) +
                        ":" + drivingReportDetail.getDriving_start_hm().substring(2, 4);
                driving_report_detail_edit_txtDrivingStartHm.setText(strTime);
            }
            driving_report_detail_edit_txtDrivingStartKm.setText(String.format(Locale.JAPAN, "%.0f", drivingReportDetail.getDriving_start_km()));
            if (!drivingReportDetail.getDriving_end_hm().equals("")) {
                String strTime = drivingReportDetail.getDriving_end_hm().substring(0, 2) +
                        ":" + drivingReportDetail.getDriving_end_hm().substring(2, 4);
                driving_report_detail_edit_txtDrivingEndHm.setText(strTime);
            }
            driving_report_detail_edit_txtDrivingEndKm.setText(String.format(Locale.JAPAN, "%.0f", drivingReportDetail.getDriving_end_km()));
            driving_report_detail_edit_txtCargoWeight.setText(drivingReportDetail.getCargo_weight());
            driving_report_detail_edit_txtCargoStatus.setText(drivingReportDetail.getCargo_status());
            driving_report_detail_edit_txtNote.setText(drivingReportDetail.getNote());
        }
    }

    private void SetReadOnly(EditText control) {
        control.setFocusable(false);
        control.setFocusableInTouchMode(false);
        control.setInputType(TYPE_NULL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                //new ActivityResultCallback() {
                //    @Override
                //    public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent != null) {
                        String res = intent.getStringExtra(DrivingReportDetailEditActivity.EXTRA_MESSAGE);
                        driving_report_detail_edit_txtDestination.setText(res);
                    }
                }
                // }
            });

    View.OnClickListener btnDestinationSelectClearClicked = v -> {
        Intent intent = new Intent(getApplication(), DrivingReportDestinationActivity.class);
        resultLauncher.launch(intent);
    };

    View.OnClickListener txtDrivingStartHmClicked = v -> showTimePickerDialogStartHm(driving_report_detail_edit_txtDrivingStartHm,
            "DrivingStartHm", String.valueOf(driving_report_detail_edit_txtDrivingStartHm.getText()));
    View.OnClickListener txtDrivingEndHmClicked = v -> showTimePickerDialogEndHm(driving_report_detail_edit_txtDrivingEndHm,
            "DrivingEndHm", String.valueOf(driving_report_detail_edit_txtDrivingEndHm.getText()));

    View.OnClickListener btnDrivingStartHmClearClicked = v -> {
        driving_report_detail_edit_txtDrivingStartHm.setText("");
    };

    View.OnClickListener btnDrivingEndHmClearClicked = v -> {
        driving_report_detail_edit_txtDrivingEndHm.setText("");
    };

    public void showTimePickerDialogStartHm(View v, String tag, String defaultValue) {
        TimePickerDialog.OnTimeSetListener listener = (view, hourOfDay, minute) -> {
            String hm = String.format(Locale.JAPAN, "%02d", hourOfDay) + ":" + String.format(Locale.JAPAN, "%2d", minute);
            driving_report_detail_edit_txtDrivingStartHm.setText(hm);
        };
        TimePickerFragment newFragment = new TimePickerFragment(listener);
        Bundle args = new Bundle();
        args.putString("default", defaultValue);
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), tag);
    }

    public void showTimePickerDialogEndHm(View v, String tag, String defaultValue) {
        TimePickerDialog.OnTimeSetListener listener = (view, hourOfDay, minute) -> {
            String hm = String.format(Locale.JAPAN, "%02d", hourOfDay) + ":" + String.format(Locale.JAPAN, "%2d", minute);
            driving_report_detail_edit_txtDrivingEndHm.setText(hm);
        };
        DialogFragment newFragment = new TimePickerFragment(listener);
        Bundle args = new Bundle();
        args.putString("default", defaultValue);
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), tag);
    }

    public RealmLocalDataDrivingReport readRecordHeader() {

        long recordCount = realm.where(RealmLocalDataDrivingReport.class)
                .equalTo("id", driving_report_id)
                .count();

        if (recordCount == 0) {
            return null;
        } else {
            return realm.where(RealmLocalDataDrivingReport.class)
                    .equalTo("id", driving_report_id)
                    .findFirst();
        }
    }

    public RealmLocalDataDrivingReportDetail readRecord() {

        long recordCount = realm.where(RealmLocalDataDrivingReportDetail.class)
                .equalTo("id", driving_report_detail_id)
                .count();

        if (recordCount == 0) {
            return null;
        } else {
            return realm.where(RealmLocalDataDrivingReportDetail.class)
                    .equalTo("id", driving_report_detail_id)
                    .findFirst();
        }
    }

    View.OnClickListener btnSaveClicked = v -> {
        if (!SaveData()) {
            return;
        }

        finish();
    };

    View.OnClickListener btnDeleteClicked = v -> {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DrivingReportDetailEditActivity.this);

        // ダイアログの設定
        alertDialog.setTitle(getString(R.string.ALERT_TITLE_CONFIRM));
        alertDialog.setMessage(getString(R.string.TEXT_QUESTION_DELETE));

        // OK(肯定的な)ボタンの設定
        alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
            // OKボタン押下時の処理
            if (DeleteData()) {
                finish();
            }
        });

        alertDialog.setNegativeButton(getString(R.string.ALERT_BTN_CANCEL), null);

        alertDialog.show();
    };

    private boolean SaveData() {
        realm.beginTransaction();
        RealmLocalDataDrivingReportDetail drivingReportDetail = readRecord();

        if (drivingReportDetail == null) {
            // 初期化
            int nextId = 1;
            // userIdの最大値を取得
            Number maxId = realm.where(RealmLocalDataDrivingReportDetail.class).max("id");
            // 1度もデータが作成されていない場合はNULLが返ってくるため、NULLチェックをする
            if (maxId != null) {
                nextId = maxId.intValue() + 1;
            }
            int driving_report_id = nextId;
            drivingReportDetail = realm.createObject(RealmLocalDataDrivingReportDetail.class, driving_report_id);
        }

        // 値セット
        drivingReportDetail.setDriving_report_id(driving_report_id);
        drivingReportDetail.setDestination(String.valueOf(driving_report_detail_edit_txtDestination.getText()));
        String strDrivingStartKm = String.valueOf(driving_report_detail_edit_txtDrivingStartKm.getText()).replaceAll(",", "");
        if (strDrivingStartKm.isEmpty()) {
            drivingReportDetail.setDriving_start_km(0D);
        } else {
            drivingReportDetail.setDriving_start_km(Double.valueOf(strDrivingStartKm));
        }
        String strDrivingStartHm = String.valueOf(driving_report_detail_edit_txtDrivingStartHm.getText()).replaceAll(":", "");
        drivingReportDetail.setDriving_start_hm(strDrivingStartHm);
        String strDrivingEndKm = String.valueOf(driving_report_detail_edit_txtDrivingEndKm.getText()).replaceAll(",", "");
        if (strDrivingEndKm.isEmpty()) {
            drivingReportDetail.setDriving_end_km(0D);
        } else {
            drivingReportDetail.setDriving_end_km(Double.valueOf(strDrivingEndKm));
        }
        String strDrivingEndHm = String.valueOf(driving_report_detail_edit_txtDrivingEndHm.getText()).replaceAll(":", "");
        drivingReportDetail.setDriving_end_hm(strDrivingEndHm);
        drivingReportDetail.setCargo_weight(String.valueOf(driving_report_detail_edit_txtCargoWeight.getText()));
        drivingReportDetail.setCargo_status(String.valueOf(driving_report_detail_edit_txtCargoStatus.getText()));
        drivingReportDetail.setNote(String.valueOf(driving_report_detail_edit_txtNote.getText()));

        realm.insertOrUpdate(drivingReportDetail);

        // 行先
        long recordCount = realm.where(RealmLocalDataDrivingReportDestination.class)
                .equalTo("destination", String.valueOf(driving_report_detail_edit_txtDestination.getText()))
                .count();

        if (recordCount == 0) {
            // 初期化
            int nextId = 1;
            // userIdの最大値を取得
            Number maxId = realm.where(RealmLocalDataDrivingReportDestination.class).max("id");
            // 1度もデータが作成されていない場合はNULLが返ってくるため、NULLチェックをする
            if (maxId != null) {
                nextId = maxId.intValue() + 1;
            }
            int id = nextId;
            RealmLocalDataDrivingReportDestination drivingReportDestination = realm.createObject(RealmLocalDataDrivingReportDestination.class, id);
            drivingReportDestination.setDestination(String.valueOf(driving_report_detail_edit_txtDestination.getText()));
            realm.insert(drivingReportDestination);
        }

        realm.commitTransaction();
        return true;
    }

    private boolean DeleteData() {
        realm.beginTransaction();
        RealmLocalDataDrivingReportDetail drivingReportDetail = readRecord();
        drivingReportDetail.deleteFromRealm();
        realm.commitTransaction();
        return true;
    }
}
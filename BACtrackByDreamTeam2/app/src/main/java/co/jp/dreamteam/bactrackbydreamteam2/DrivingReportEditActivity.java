package co.jp.dreamteam.bactrackbydreamteam2;

import static android.text.InputType.TYPE_NULL;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class DrivingReportEditActivity extends FragmentActivity {

    private SharedPreferences pref;

    private Realm realm;

    int driving_report_id;

    EditText driving_report_edit_txtDriverCode;
    EditText driving_report_edit_txtCarNumber;

    EditText driving_report_edit_txtDrivingStartYmd;
    EditText driving_report_edit_txtDrivingStartHm;
    EditText driving_report_edit_txtDrivingEndYmd;
    EditText driving_report_edit_txtDrivingEndHm;
    EditText driving_report_edit_txtDrivingStartKm;
    EditText driving_report_edit_txtDrivingEndKm;
    EditText driving_report_edit_txtRefuelingStatus;
    EditText driving_report_edit_txtAbnormalReport;
    EditText driving_report_edit_txtInstruction;

    ImageButton driving_report_edit_btnDrivingStartYmd;
    ImageButton driving_report_edit_btnDrivingStartHm;
    ImageButton driving_report_edit_btnDrivingEndYmd;
    ImageButton driving_report_edit_btnDrivingEndHm;

    Button driving_report_edit_btnSave;
    Button driving_report_edit_btnDetail;
    Button driving_report_edit_btnSend;
    Button driving_report_edit_btnDelete;

    int errorCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driving_report_edit);

        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

        realm = Realm.getDefaultInstance();

        driving_report_id = getIntent().getIntExtra("id", 0);

        // 運転手
        driving_report_edit_txtDriverCode = this.findViewById(R.id.driving_report_edit_txtDriverCode);

        // 車番
        driving_report_edit_txtCarNumber = this.findViewById(R.id.driving_report_edit_txtCarNumber);

        // 開始日付ピッカー
        driving_report_edit_txtDrivingStartYmd = this.findViewById(R.id.driving_report_edit_txtDrivingStartYmd);

        // 開始時刻ピッカー
        driving_report_edit_txtDrivingStartHm = this.findViewById(R.id.driving_report_edit_txtDrivingStartHm);

        // 終了日付ピッカー
        driving_report_edit_txtDrivingEndYmd = this.findViewById(R.id.driving_report_edit_txtDrivingEndYmd);

        // 終了時刻ピッカー
        driving_report_edit_txtDrivingEndHm = this.findViewById(R.id.driving_report_edit_txtDrivingEndHm);

        // 乗務開始メーター
        driving_report_edit_txtDrivingStartKm = this.findViewById(R.id.driving_report_edit_txtDrivingStartKm);

        // 乗務終了メーター
        driving_report_edit_txtDrivingEndKm = this.findViewById(R.id.driving_report_edit_txtDrivingEndKm);

        // 給油状況
        driving_report_edit_txtRefuelingStatus = this.findViewById(R.id.driving_report_edit_txtRefuelingStatus);

        // 異常報告
        driving_report_edit_txtAbnormalReport = this.findViewById(R.id.driving_report_edit_txtAbnormalReport);

        // 連絡事項
        driving_report_edit_txtInstruction = this.findViewById(R.id.driving_report_edit_txtInstruction);

        // クリアボタン
        driving_report_edit_btnDrivingStartYmd = this.findViewById(R.id.driving_report_edit_btnDrivingStartYmd);

        driving_report_edit_btnDrivingStartHm = this.findViewById(R.id.driving_report_edit_btnDrivingStartHm);

        driving_report_edit_btnDrivingEndYmd = this.findViewById(R.id.driving_report_edit_btnDrivingEndYmd);

        driving_report_edit_btnDrivingEndHm = this.findViewById(R.id.driving_report_edit_btnDrivingEndHm);

        // 保存ボタン
        driving_report_edit_btnSave = this.findViewById(R.id.driving_report_edit_btnSave);
        driving_report_edit_btnSave.setOnClickListener(btnSaveClicked);

        // 明細ボタン
        driving_report_edit_btnDetail = this.findViewById(R.id.driving_report_edit_btnDetail);
        driving_report_edit_btnDetail.setOnClickListener(btnDetailClicked);

        // 送信ボタン
        driving_report_edit_btnSend = this.findViewById(R.id.driving_report_edit_btnSend);
        driving_report_edit_btnSend.setOnClickListener(btnSendClicked);

        // 削除ボタン
        driving_report_edit_btnDelete = this.findViewById(R.id.driving_report_edit_btnDelete);
        driving_report_edit_btnDelete.setOnClickListener(btnDeleteClicked);

        // 値取得
        RealmLocalDataDrivingReport drivingReport = readRecord();

        if (drivingReport == null) {
            // ピッカーイベントセット
            driving_report_edit_txtDrivingStartYmd.setOnClickListener(txtDrivingStartYmdClicked);
            driving_report_edit_txtDrivingStartHm.setOnClickListener(txtDrivingStartHmClicked);
            driving_report_edit_txtDrivingEndYmd.setOnClickListener(txtDrivingEndYmdClicked);
            driving_report_edit_txtDrivingEndHm.setOnClickListener(txtDrivingEndHmClicked);
            // クリアボタンイベント
            driving_report_edit_btnDrivingStartYmd.setOnClickListener(btnDrivingStartYmdClearClicked);
            driving_report_edit_btnDrivingStartHm.setOnClickListener(btnDrivingStartHmClearClicked);
            driving_report_edit_btnDrivingEndYmd.setOnClickListener(btnDrivingEndYmdClearClicked);
            driving_report_edit_btnDrivingEndHm.setOnClickListener(btnDrivingEndHmClearClicked);

            driving_report_edit_btnDelete.setEnabled(false);
        } else {
            // 値セット
            driving_report_edit_txtDriverCode.setText(drivingReport.getDriver_code());
            driving_report_edit_txtCarNumber.setText(drivingReport.getCar_number());
            if (!drivingReport.getDriving_start_ymd().equals("")) {
                String strDate = drivingReport.getDriving_start_ymd().substring(0, 4) +
                        "/" + drivingReport.getDriving_start_ymd().substring(4, 6) +
                        "/" + drivingReport.getDriving_start_ymd().substring(6, 8);
                driving_report_edit_txtDrivingStartYmd.setText(strDate);
            }
            if (!drivingReport.getDriving_start_hm().equals("")) {
                String strTime = drivingReport.getDriving_start_hm().substring(0, 2) +
                        ":" + drivingReport.getDriving_start_hm().substring(2, 4);
                driving_report_edit_txtDrivingStartHm.setText(strTime);
            }
            if (!drivingReport.getDriving_end_ymd().equals("")) {
                String strDate = drivingReport.getDriving_end_ymd().substring(0, 4) +
                        "/" + drivingReport.getDriving_end_ymd().substring(4, 6) +
                        "/" + drivingReport.getDriving_end_ymd().substring(6, 8);
                driving_report_edit_txtDrivingEndYmd.setText(strDate);
            }
            if (!drivingReport.getDriving_end_hm().equals("")) {
                String strTime = drivingReport.getDriving_end_hm().substring(0, 2) +
                        ":" + drivingReport.getDriving_end_hm().substring(2, 4);
                driving_report_edit_txtDrivingEndHm.setText(strTime);
            }
            driving_report_edit_txtDrivingStartKm.setText(String.format(Locale.JAPAN, "%.0f", drivingReport.getDriving_start_km()));
            driving_report_edit_txtDrivingEndKm.setText(String.format(Locale.JAPAN, "%.0f", drivingReport.getDriving_end_km()));
            driving_report_edit_txtRefuelingStatus.setText(drivingReport.getRefueling_status());
            driving_report_edit_txtAbnormalReport.setText(drivingReport.getAbnormal_report());
            driving_report_edit_txtInstruction.setText(drivingReport.getInstruction());

            if (String.valueOf(drivingReport.getSendFlg()).equals("1")) {
                SetReadOnly(driving_report_edit_txtDriverCode);
                SetReadOnly(driving_report_edit_txtCarNumber);
                SetReadOnly(driving_report_edit_txtDrivingStartYmd);
                SetReadOnly(driving_report_edit_txtDrivingStartHm);
                SetReadOnly(driving_report_edit_txtDrivingEndYmd);
                SetReadOnly(driving_report_edit_txtDrivingEndHm);
                SetReadOnly(driving_report_edit_txtDrivingStartKm);
                SetReadOnly(driving_report_edit_txtDrivingEndKm);
                SetReadOnly(driving_report_edit_txtRefuelingStatus);
                SetReadOnly(driving_report_edit_txtAbnormalReport);
                SetReadOnly(driving_report_edit_txtInstruction);

                driving_report_edit_btnDrivingStartYmd.setEnabled(false);
                driving_report_edit_btnDrivingStartHm.setEnabled(false);
                driving_report_edit_btnDrivingEndYmd.setEnabled(false);
                driving_report_edit_btnDrivingEndHm.setEnabled(false);

                driving_report_edit_btnSave.setEnabled(false);
                driving_report_edit_btnSend.setEnabled(false);
            } else {
                // ピッカーイベントセット
                driving_report_edit_txtDrivingStartYmd.setOnClickListener(txtDrivingStartYmdClicked);
                driving_report_edit_txtDrivingStartHm.setOnClickListener(txtDrivingStartHmClicked);
                driving_report_edit_txtDrivingEndYmd.setOnClickListener(txtDrivingEndYmdClicked);
                driving_report_edit_txtDrivingEndHm.setOnClickListener(txtDrivingEndHmClicked);
                // クリアボタンイベント
                driving_report_edit_btnDrivingStartYmd.setOnClickListener(btnDrivingStartYmdClearClicked);
                driving_report_edit_btnDrivingStartHm.setOnClickListener(btnDrivingStartHmClearClicked);
                driving_report_edit_btnDrivingEndYmd.setOnClickListener(btnDrivingEndYmdClearClicked);
                driving_report_edit_btnDrivingEndHm.setOnClickListener(btnDrivingEndHmClearClicked);
            }
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

    View.OnClickListener txtDrivingStartYmdClicked = v -> showDatePickerDialogStartYmd(driving_report_edit_txtDrivingStartYmd,
            "DrivingStartYmd", String.valueOf(driving_report_edit_txtDrivingStartYmd.getText()));
    View.OnClickListener txtDrivingStartHmClicked = v -> showTimePickerDialogStartHm(driving_report_edit_txtDrivingStartHm,
            "DrivingStartHm", String.valueOf(driving_report_edit_txtDrivingStartHm.getText()));
    View.OnClickListener txtDrivingEndYmdClicked = v -> showDatePickerDialogEndYmd(driving_report_edit_txtDrivingEndYmd,
            "DrivingEndYmd", String.valueOf(driving_report_edit_txtDrivingEndYmd.getText()));
    View.OnClickListener txtDrivingEndHmClicked = v -> showTimePickerDialogEndHm(driving_report_edit_txtDrivingEndHm,
            "DrivingEndHm", String.valueOf(driving_report_edit_txtDrivingEndHm.getText()));

    View.OnClickListener btnDrivingStartYmdClearClicked = v -> driving_report_edit_txtDrivingStartYmd.setText("");

    View.OnClickListener btnDrivingStartHmClearClicked = v -> driving_report_edit_txtDrivingStartHm.setText("");

    View.OnClickListener btnDrivingEndYmdClearClicked = v -> driving_report_edit_txtDrivingEndYmd.setText("");

    View.OnClickListener btnDrivingEndHmClearClicked = v -> driving_report_edit_txtDrivingEndHm.setText("");

    public void showDatePickerDialogStartYmd(View v, String tag, String defaultValue) {
        DatePickerDialog.OnDateSetListener listener = (datePicker, year, month, day) -> {
            String ymd = String.format(Locale.JAPAN, "%04d", year)
                    + "/" + String.format(Locale.JAPAN, "%02d", month)
                    + "/" + String.format(Locale.JAPAN, "%02d", day);
            driving_report_edit_txtDrivingStartYmd.setText(ymd);
        };
        DatePickerFragment newFragment = new DatePickerFragment(listener);
        Bundle args = new Bundle();
        args.putString("default", defaultValue);
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), tag);
    }

    public void showTimePickerDialogStartHm(View v, String tag, String defaultValue) {
        TimePickerDialog.OnTimeSetListener listener = (view, hourOfDay, minute) -> {
            String hm = String.format(Locale.JAPAN, "%02d", hourOfDay) + ":" + String.format(Locale.JAPAN, "%2d", minute);
            driving_report_edit_txtDrivingStartHm.setText(hm);
        };
        TimePickerFragment newFragment = new TimePickerFragment(listener);
        Bundle args = new Bundle();
        args.putString("default", defaultValue);
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), tag);
    }

    public void showDatePickerDialogEndYmd(View v, String tag, String defaultValue) {
        DatePickerDialog.OnDateSetListener listener = (datePicker, year, month, day) -> {
            String ymd = String.format(Locale.JAPAN, "%04d", year)
                    + "/" + String.format(Locale.JAPAN, "%02d", month)
                    + "/" + String.format(Locale.JAPAN, "%02d", day);
            driving_report_edit_txtDrivingEndYmd.setText(ymd);
        };
        DatePickerFragment newFragment = new DatePickerFragment(listener);
        Bundle args = new Bundle();
        args.putString("default", defaultValue);
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), tag);
    }

    public void showTimePickerDialogEndHm(View v, String tag, String defaultValue) {
        TimePickerDialog.OnTimeSetListener listener = (view, hourOfDay, minute) -> {
            String hm = String.format(Locale.JAPAN, "%02d", hourOfDay) + ":" + String.format(Locale.JAPAN, "%2d", minute);
            driving_report_edit_txtDrivingEndHm.setText(hm);
        };
        DialogFragment newFragment = new TimePickerFragment(listener);
        Bundle args = new Bundle();
        args.putString("default", defaultValue);
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), tag);
    }

    public RealmLocalDataDrivingReport readRecord() {

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

    public RealmResults<RealmLocalDataDrivingReportDetail> readDetail(Integer driving_report_id) {
        return realm.where(RealmLocalDataDrivingReportDetail.class)
                .equalTo("driving_report_id", driving_report_id)
                .findAll()
                .sort("driving_start_hm", Sort.ASCENDING)
                .sort("driving_end_hm", Sort.ASCENDING);
    }

    View.OnClickListener btnSaveClicked = v -> {
        if (!SaveData()) {
            return;
        }

        finish();
    };

    View.OnClickListener btnDetailClicked = v -> {

        RealmLocalDataDrivingReport drivingReport = readRecord();
        if (drivingReport == null)
        {
            if (!SaveData()) {
                return;
            }
        }
        else
        {
            if (!drivingReport.getSendFlg().equals("1")) {
                if (!SaveData()) {
                    return;
                }
            }
        }

        Intent intent = new Intent(getApplication(), DrivingReportDetailActivity.class);
        intent.putExtra("id", driving_report_id);
        startActivity(intent);
    };

    View.OnClickListener btnSendClicked = v -> {

        if (!SaveData()) {
            return;
        }

        errorCount = 0;
        exec_post();
    };

    /**
     * 送信エラー
     */
    private void errorSending() {
        runOnUiThread(() -> {

            errorCount += 1;

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(DrivingReportEditActivity.this);
            alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));

            if (errorCount < 3) {
                // ダイアログの設定
                alertDialog.setMessage(getString(R.string.TEXT_SEND_ERROR));

                // OK(肯定的な)ボタンの設定
                alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                    // OKボタン押下時の処理
                    exec_post();
                });
            } else {
                // ダイアログの設定
                alertDialog.setMessage(getString(R.string.TEXT_SEND_FINISH_ERROR));

                // OK(肯定的な)ボタンの設定
                alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                    // OKボタン押下時の処理
                });
            }

            alertDialog.show();
        });
    }

    /**
     * HTTPコネクションエラー
     */
    private void errorHttp(final String response) {
        runOnUiThread(() -> {
            errorCount += 1;

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(DrivingReportEditActivity.this);
            alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));

            if (errorCount < 3) {
                // ダイアログの設定
                alertDialog.setMessage(getString(R.string.TEXT_SEND_ERROR));

                // OK(肯定的な)ボタンの設定
                alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                    // OKボタン押下時の処理
                    exec_post();
                });
            } else {
                // ダイアログの設定
                alertDialog.setMessage(getString(R.string.TEXT_SEND_FINISH_ERROR));

                // OK(肯定的な)ボタンの設定
                alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                    // OKボタン押下時の処理
                });
            }

            alertDialog.show();
        });
    }

    // POST通信を実行（AsyncTaskによる非同期処理を使うバージョン）
    public void exec_post() {
        // 接続先
        String strHttpUrl = pref.getString(getString(R.string.PREF_KEY_HTTP_URL), "");
        String strVerifyHostname = pref.getString(getString(R.string.PREF_KEY_VERIFY_HOSTNAME), "");
        // 非同期タスクを定義
        @SuppressLint("HandlerLeak") HttpPostTask task = new HttpPostTask(this,
                strHttpUrl + getString(R.string.HTTP_WRITE_DRIVING_REPORT),

                // タスク完了時に呼ばれるUIのハンドラ
                new HttpPostHandler() {

                    @Override
                    public void onPostCompleted(String jsonResponse) {
                        // 受信結果
                        try {
                            JSONObject jsonObject = new JSONObject(jsonResponse);

                            String status = jsonObject.getString("status");

                            if ("true".equals(status)) {
                                // 成功の場合の処理
                                // dataObject から必要なデータを取得
                                JSONObject dataObject = jsonObject.getJSONObject("data");
                                JSONArray drivingReportIds = dataObject.getJSONArray("driving_report_id");

                                realm.beginTransaction();
                                for (int i = 0; i < drivingReportIds.length(); i++) {
                                    int update_driving_report_id = drivingReportIds.getInt(i);

                                    long recordCount = realm.where(RealmLocalDataDrivingReport.class)
                                            .equalTo("id", update_driving_report_id)
                                            .count();

                                    if (recordCount != 0) {
                                        RealmLocalDataDrivingReport drivingReport = realm.where(RealmLocalDataDrivingReport.class)
                                                .equalTo("id", update_driving_report_id)
                                                .findFirst();

                                        drivingReport.setSendFlg("1");

                                        realm.insertOrUpdate(drivingReport);
                                    }
                                }
                                realm.commitTransaction();

                                finish();

                            } else {
                                // エラーの場合の処理
                                // error を処理
                                String error = jsonObject.getString("error");
                                JSONObject errorObject = new JSONObject(error);
                                String message = errorObject.getString("message");

                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(DrivingReportEditActivity.this);
                                alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));

                                // ダイアログの設定
                                alertDialog.setMessage(message);

                                // OK(肯定的な)ボタンの設定
                                alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                                    // OKボタン押下時の処理
                                });

                                alertDialog.show();
                            }
                        } catch (JSONException e) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(DrivingReportEditActivity.this);
                            alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));

                            // ダイアログの設定
                            alertDialog.setMessage(e.getMessage());

                            // OK(肯定的な)ボタンの設定
                            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                                // OKボタン押下時の処理
                            });

                            alertDialog.show();
                        }
                    }

                    @Override
                    public void onPostFailed(String response) {
                        errorHttp(response);
                    }
                });

        // パラメータセット
        task.setVerify_hostname(strVerifyHostname);
        task.setHttp_multipart(true);

        RealmLocalDataDrivingReport drivingReport = readRecord();

        JSONObject jsonData = new JSONObject();

        try {
            // ヘッダをJSONに変換
            JSONObject headerJson = new JSONObject();
            headerJson.put("id", drivingReport.getId());
            headerJson.put("driver_code", drivingReport.getDriver_code());
            headerJson.put("car_number", drivingReport.getCar_number());
            headerJson.put("driving_start_ymd", drivingReport.getDriving_start_ymd());
            headerJson.put("driving_start_hm", drivingReport.getDriving_start_hm());
            headerJson.put("driving_end_ymd", drivingReport.getDriving_end_ymd());
            headerJson.put("driving_end_hm", drivingReport.getDriving_end_hm());
            headerJson.put("driving_start_km", drivingReport.getDriving_start_km());
            headerJson.put("driving_end_km", drivingReport.getDriving_end_km());
            headerJson.put("refueling_status", drivingReport.getRefueling_status());
            headerJson.put("abnormal_report", drivingReport.getAbnormal_report());
            headerJson.put("instruction", drivingReport.getInstruction());
            headerJson.put("send_flg", drivingReport.getSendFlg());

            RealmResults<RealmLocalDataDrivingReportDetail> drivingReportDetailList = readDetail(drivingReport.getId());

            // 明細をJSONに変換
            JSONArray detailsJsonArray = new JSONArray();
            for (RealmLocalDataDrivingReportDetail detail : drivingReportDetailList) {
                JSONObject detailJson = new JSONObject();
                detailJson.put("id", detail.getId());
                detailJson.put("driving_report_id", detail.getDriving_report_id());
                detailJson.put("destination", detail.getDestination());
                detailJson.put("driving_start_hm", detail.getDriving_start_hm());
                detailJson.put("driving_start_km", detail.getDriving_start_km());
                detailJson.put("driving_end_hm", detail.getDriving_end_hm());
                detailJson.put("driving_end_km", detail.getDriving_end_km());
                detailJson.put("cargo_weight", detail.getCargo_weight());
                detailJson.put("cargo_status", detail.getCargo_status());
                detailJson.put("note", detail.getNote());
                detailsJsonArray.put(detailJson);
            }

            headerJson.put("detail", detailsJsonArray);

            jsonData.put("0", headerJson);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        task.addPostParam(getString(R.string.HTTP_PARAM_JSON_DATA), jsonData.toString());

        // タスクを開始
        task.execute();
    }

    View.OnClickListener btnDeleteClicked = v -> {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DrivingReportEditActivity.this);

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

    private int getByteCount(String str) {
        if (str == null || str.isEmpty()) {
            return 0;
        }
        // 文字列をUTF-8でエンコードしてバイト数を取得
        try {
            byte[] utf8Bytes = str.getBytes("UTF-8");
            return utf8Bytes.length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private boolean isValidDate(String inputDate) {
        // 日付として有効かどうか確認するためにパースを試みる
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN);
            sdf.parse(inputDate);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidTime(String inputTime) {
        // 日付として有効かどうか確認するためにパースを試みる
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.JAPAN);
            sdf.parse(inputTime);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isNumeric(String inputValue) {
        // 日付として有効かどうか確認するためにパースを試みる
        try {
            Integer.parseInt(inputValue);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean CheckData()
    {
        String errorMessage = "";

        if (String.valueOf(driving_report_edit_txtDriverCode.getText()).equals(""))
        {
            errorMessage = getString(R.string.TEXT_ERROR_DRIVER_CODE);
        }
        else if (String.valueOf(driving_report_edit_txtCarNumber.getText()).equals(""))
        {
            errorMessage = getString(R.string.TEXT_ERROR_CAR_NUMBER);
        }
        else if (String.valueOf(driving_report_edit_txtDrivingStartYmd.getText()).equals(""))
        {
            errorMessage = getString(R.string.TEXT_ERROR_START_YMD);
        }
        else if (String.valueOf(driving_report_edit_txtDrivingStartHm.getText()).equals(""))
        {
            errorMessage = getString(R.string.TEXT_ERROR_START_HM);
        }
        else if (getByteCount(String.valueOf(driving_report_edit_txtDriverCode.getText())) > 50)
        {
            errorMessage = getString(R.string.TEXT_ERROR_DRIVER_CODE_MAX_LENGTH);
        }
        else if (getByteCount(String.valueOf(driving_report_edit_txtCarNumber.getText())) > 50)
        {
            errorMessage = getString(R.string.TEXT_ERROR_CAR_NUMBER_MAX_LENGTH);
        }
        else if (getByteCount(String.valueOf(driving_report_edit_txtRefuelingStatus.getText())) > 100)
        {
            errorMessage = getString(R.string.TEXT_ERROR_REFUELINGSTATUS_MAX_LENGTH);
        }
        else if (getByteCount(String.valueOf(driving_report_edit_txtAbnormalReport.getText())) > 255)
        {
            errorMessage = getString(R.string.TEXT_ERROR_ABNORMALREPORT_MAX_LENGTH);
        }
        else if (getByteCount(String.valueOf(driving_report_edit_txtInstruction.getText())) > 255)
        {
            errorMessage = getString(R.string.TEXT_ERROR_INSTRUCTION_MAX_LENGTH);
        }

        if (!errorMessage.equals(""))
        {
            if (!String.valueOf(driving_report_edit_txtDrivingStartYmd.getText()).equals(""))
            {
                if (!isValidDate(String.valueOf(driving_report_edit_txtDrivingStartYmd.getText())))
                {
                    errorMessage = getString(R.string.TEXT_ERROR_START_YMD_INVALID);
                }
            }
        }

        if (!errorMessage.equals(""))
        {
            if (!String.valueOf(driving_report_edit_txtDrivingStartHm.getText()).equals(""))
            {
                if (!isValidTime(String.valueOf(driving_report_edit_txtDrivingStartHm.getText())))
                {
                    errorMessage = getString(R.string.TEXT_ERROR_START_HM_INVALID);
                }
            }
        }

        if (!errorMessage.equals(""))
        {
            if (!String.valueOf(driving_report_edit_txtDrivingEndYmd.getText()).equals(""))
            {
                if (!isValidDate(String.valueOf(driving_report_edit_txtDrivingEndYmd.getText())))
                {
                    errorMessage = getString(R.string.TEXT_ERROR_END_YMD_INVALID);
                }
            }
        }

        if (!errorMessage.equals(""))
        {
            if (!String.valueOf(driving_report_edit_txtDrivingEndHm.getText()).equals(""))
            {
                if (!isValidTime(String.valueOf(driving_report_edit_txtDrivingEndHm.getText())))
                {
                    errorMessage = getString(R.string.TEXT_ERROR_END_HM_INVALID);
                }
            }
        }

        if (!errorMessage.equals(""))
        {
            if (!String.valueOf(driving_report_edit_txtDrivingStartKm.getText()).equals(""))
            {
                if (!isNumeric(String.valueOf(driving_report_edit_txtDrivingStartKm.getText())))
                {
                    errorMessage = getString(R.string.TEXT_ERROR_START_KM_INVALID);
                }
            }
        }

        if (!errorMessage.equals(""))
        {
            if (!String.valueOf(driving_report_edit_txtDrivingEndKm.getText()).equals(""))
            {
                if (!isNumeric(String.valueOf(driving_report_edit_txtDrivingEndKm.getText())))
                {
                    errorMessage = getString(R.string.TEXT_ERROR_END_KM_INVALID);
                }
            }
        }

        if (!errorMessage.equals("")) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(DrivingReportEditActivity.this);
            alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));

            // ダイアログの設定
            alertDialog.setMessage(errorMessage);

            // OK(肯定的な)ボタンの設定
            alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), (dialog, which) -> {
                // OKボタン押下時の処理
            });

            alertDialog.show();

            return false;
        }
        else {
            return true;
        }
    }

    private boolean SaveData() {

        if (!CheckData())
        {
            return  false;
        }

        realm.beginTransaction();
        RealmLocalDataDrivingReport drivingReport = readRecord();

        if (drivingReport == null) {
            // 初期化
            int nextId = 1;
            // userIdの最大値を取得
            Number maxId = realm.where(RealmLocalDataDrivingReport.class).max("id");
            // 1度もデータが作成されていない場合はNULLが返ってくるため、NULLチェックをする
            if (maxId != null) {
                nextId = maxId.intValue() + 1;
            }
            int driving_report_id = nextId;
            drivingReport = realm.createObject(RealmLocalDataDrivingReport.class, driving_report_id);
        }

        // 値セット
        drivingReport.setDriver_code(String.valueOf(driving_report_edit_txtDriverCode.getText()));
        drivingReport.setCar_number(String.valueOf(driving_report_edit_txtCarNumber.getText()));
        String strDrivingStartYmd = String.valueOf(driving_report_edit_txtDrivingStartYmd.getText()).replaceAll("/", "");
        drivingReport.setDriving_start_ymd(strDrivingStartYmd);
        String strDrivingStartHm = String.valueOf(driving_report_edit_txtDrivingStartHm.getText()).replaceAll(":", "");
        drivingReport.setDriving_start_hm(strDrivingStartHm);
        String strDrivingEndYmd = String.valueOf(driving_report_edit_txtDrivingEndYmd.getText()).replaceAll("/", "");
        drivingReport.setDriving_end_ymd(strDrivingEndYmd);
        String strDrivingEndHm = String.valueOf(driving_report_edit_txtDrivingEndHm.getText()).replaceAll(":", "");
        drivingReport.setDriving_end_hm(strDrivingEndHm);
        String strDrivingStartKm = String.valueOf(driving_report_edit_txtDrivingStartKm.getText()).replaceAll(",", "");
        if (strDrivingStartKm.isEmpty()) {
            drivingReport.setDriving_start_km(0D);
        } else {
            drivingReport.setDriving_start_km(Double.valueOf(strDrivingStartKm));
        }
        String strDrivingEndKm = String.valueOf(driving_report_edit_txtDrivingEndKm.getText()).replaceAll(",", "");
        if (strDrivingEndKm.isEmpty()) {
            drivingReport.setDriving_end_km(0D);
        } else {
            drivingReport.setDriving_end_km(Double.valueOf(strDrivingEndKm));
        }
        drivingReport.setRefueling_status(String.valueOf(driving_report_edit_txtRefuelingStatus.getText()));
        drivingReport.setAbnormal_report(String.valueOf(driving_report_edit_txtAbnormalReport.getText()));
        drivingReport.setInstruction(String.valueOf(driving_report_edit_txtInstruction.getText()));
        drivingReport.setSendFlg("0");

        realm.insertOrUpdate(drivingReport);

        realm.commitTransaction();
        return true;
    }

    private boolean DeleteData() {
        realm.beginTransaction();
        RealmLocalDataDrivingReport drivingReport = readRecord();

        RealmResults<RealmLocalDataDrivingReportDetail> list = realm.where(RealmLocalDataDrivingReportDetail.class)
                .equalTo("driving_report_id", drivingReport.getId())
                .findAll();
        list.deleteAllFromRealm();

        drivingReport.deleteFromRealm();
        realm.commitTransaction();
        return true;
    }
}
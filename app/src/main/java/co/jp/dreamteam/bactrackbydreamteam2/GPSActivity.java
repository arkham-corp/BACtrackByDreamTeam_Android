package co.jp.dreamteam.bactrackbydreamteam2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GPSActivity extends Activity implements LocationListener {
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    LocationManager locationManagerGPS;
    LocationManager locationManagerNET;
    private TextView textViewAddress;
    private Button btnDecision;

    final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        // 設定ファイル取得
        pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

        // 設定初期化
        editor = pref.edit();
        editor.putString(getString(R.string.PREF_KEY_ADDRESS), "");
        editor.putString(getString(R.string.PREF_KEY_LAT), "");
        editor.putString(getString(R.string.PREF_KEY_LON), "");
        editor.commit();

        // テキスト割当
        textViewAddress = findViewById(R.id.gps_textViewAddress);

        // テキスト初期化
        textViewAddress.setText(R.string.TEXT_GPS_WAIT);

        btnDecision = findViewById(R.id.gps_btnDecision);

        this.findViewById(R.id.gps_btnDecision).setOnClickListener(btnDecisionClicked);

        // 位置情報許可判定
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            GetLocation();
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                // 権限チェックした結果、持っていない場合はダイアログを出す
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

                alertDialog.setCancelable(false);
                alertDialog.setTitle(getString(R.string.ALERT_TITLE_INFO));
                alertDialog.setMessage("アルコールマネージャー業務用アプリ 写真撮影版が位置情報の使用を求めています。\n" +
                        "アルコールマネージャーを利用し、アルコール測定をどこで行ったかを記録するために、位置情報を利用します。");

                alertDialog.setPositiveButton(android.R.string.ok, (dialog, which) -> ActivityCompat.requestPermissions(GPSActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION));

                alertDialog.setOnDismissListener(dialog -> ActivityCompat.requestPermissions(GPSActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION));

                alertDialog.create();
                alertDialog.show();

            } else {
                // 直接許可を求めることができます。
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

                alertDialog.setCancelable(false);
                alertDialog.setTitle(getString(R.string.ALERT_TITLE_INFO));
                alertDialog.setMessage("アルコールマネージャー業務用アプリ 写真撮影版が位置情報の使用を求めています。\n" +
                        "アルコールマネージャーを利用し、アルコール測定をどこで行ったかを記録するために、位置情報を利用します。");

                alertDialog.setPositiveButton(android.R.string.ok, (dialog, which) -> requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION));

                alertDialog.setOnDismissListener(dialog -> requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION));

                alertDialog.create();
                alertDialog.show();

            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        btnDecision.setEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 先ほどの独自定義したrequestCodeの結果確認
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length != 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ユーザーが許可したとき
                    // 許可が必要な機能を改めて実行する
                    GetLocation();
                } else {
                    // ユーザーが許可しなかったとき
                }
            }
        }
    }

    private void GetLocation() {
        // LocationManager インスタンス生成
        locationManagerGPS = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManagerNET = (LocationManager) getSystemService(LOCATION_SERVICE);

        final boolean gpsEnabled = locationManagerGPS.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            // GPSを設定するように促す
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.ALERT_TITLE_QUESTION))
                    .setMessage(getString(R.string.TEXT_GPS_SETTING))
                    .setPositiveButton(getString(R.string.ALERT_BTN_YES), (dialog, which) -> enableLocationSettings())
                    .setNegativeButton(getString(R.string.ALERT_BTN_NO), null)
                    .setCancelable(false)
                    .show();
        }
    }

    OnClickListener btnDecisionClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            btnDecision.setEnabled(false);
            Intent intent = new Intent(getApplication(), DrivingDivActivity.class);
            startActivity(intent);
        }
    };

    @Override
    protected void onResume() {
        if (locationManagerGPS != null) {
            if (locationManagerGPS.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                locationManagerGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            }
        }
        if (locationManagerNET != null) {
            if (locationManagerNET.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
                locationManagerNET.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
            }
        }

        super.onResume();
    }

    @Override
    protected void onPause() {
        if (locationManagerGPS != null) {
            locationManagerGPS.removeUpdates(this);
        }
        if (locationManagerNET != null) {
            locationManagerNET.removeUpdates(this);
        }

        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {
        double dblLatitude = location.getLatitude();
        double dblLongitude = location.getLongitude();

        // 緯度経度を住所に変換
        String strAddress = getAddressFromPoint(dblLatitude, dblLongitude);

        if (strAddress == null || strAddress.equals("")) {
            return;
        }

        // 住所表示
        textViewAddress.setText(strAddress);

        // 省略を決定ボタンに変更
        btnDecision.setText(R.string.BTN_DECISION);

        // 値保存
        editor = pref.edit();
        editor.putString(getString(R.string.PREF_KEY_ADDRESS), strAddress);
        editor.putString(getString(R.string.PREF_KEY_LAT), String.valueOf(dblLatitude));
        editor.putString(getString(R.string.PREF_KEY_LON), String.valueOf(dblLongitude));
        editor.commit();
    }

    private void enableLocationSettings() {
        btnDecision.setEnabled(false);
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

    /**
     * 緯度経度を住所に変換
     *
     * @param latitude  緯度
     * @param longitude 経度
     * @return 住所
     */
    private String getAddressFromPoint(double latitude, double longitude) {
        String addressValue = null;

        Geocoder geocoder = new Geocoder(this, Locale.JAPAN);

        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 5);

            if (!addressList.isEmpty()) {
                Address address = addressList.get(0);

                StringBuilder sb = new StringBuilder();

                String buf;

                for (int i = 0; (buf = address.getAddressLine(i)) != null; i++) {
                    if (buf.equals("日本")) {
                        continue;
                    }

                    if (buf.startsWith("日本、")) {
                        // 除去
                        buf = buf.substring(3);
                    }

                    if (buf.startsWith("〒")) {
                        // 郵便番号を除去
                        buf = buf.substring(buf.indexOf(" "));
                    }

                    sb.append(buf).append(" ");
                }

                addressValue = sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addressValue;
    }
}

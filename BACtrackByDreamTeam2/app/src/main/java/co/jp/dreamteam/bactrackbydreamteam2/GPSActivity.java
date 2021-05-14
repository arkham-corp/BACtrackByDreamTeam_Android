package co.jp.dreamteam.bactrackbydreamteam2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GPSActivity extends Activity implements LocationListener {
	BroadcastReceiver mReceiver;

	SharedPreferences pref;
	SharedPreferences.Editor editor;

	LocationManager locationManagerGPS;
	LocationManager locationManagerNET;
	private TextView textViewAddress;
	private Button btnDecesion;

	private double dblLatitude = 0;
	private double dblLongitude = 0;

	final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gps);

		// BroadcastRecieverを LocalBroadcastManagerを使って登録
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(getString(R.string.BLOADCAST_FINISH));
		mReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
				finish();
			}
		};
		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiver, intentFilter);

		// 設定ファイル取得
		pref = getSharedPreferences(getString(R.string.PREF_GLOBAL), Activity.MODE_PRIVATE);

		// 設定初期化
		editor = pref.edit();
		editor.putString(getString(R.string.PREF_KEY_ADDRESS), "");
		editor.putString(getString(R.string.PREF_KEY_LAT), "");
		editor.putString(getString(R.string.PREF_KEY_LON), "");
		editor.commit();

		// テキスト割当
		textViewAddress = (TextView) findViewById(R.id.gps_textViewAddress);

		// テキスト初期化
		textViewAddress.setText(R.string.TEXT_GPS_WAIT);

		btnDecesion = (Button) findViewById(R.id.gps_btnDecision);

		this.findViewById(R.id.gps_btnDecision).setOnClickListener(btnDecisionClicked);

		// 位置情報許可判定
		if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
		{
			GetLocation();
		}
		else
		{
			if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION))
			{
				// 権限チェックした結果、持っていない場合はダイアログを出す
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

				alertDialog.setCancelable(false);
				alertDialog.setTitle(getString(R.string.ALERT_TITLE_INFO));
				alertDialog.setMessage("アルコールマネージャー業務用アプリ 写真撮影版が位置情報の使用を求めています。\n" +
						"アルコールマネージャーを利用し、アルコール測定をどこで行ったかを記録するために、位置情報を利用します。");

				alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ActivityCompat.requestPermissions(GPSActivity.this,
								new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
								MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
					}
				});

				alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						ActivityCompat.requestPermissions(GPSActivity.this,
								new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
								MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
					}
				});

				alertDialog.create();
				alertDialog.show();

				return;
			}
			else
			{
				// 直接許可を求めることができます。
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

				alertDialog.setCancelable(false);
				alertDialog.setTitle(getString(R.string.ALERT_TITLE_INFO));
				alertDialog.setMessage("アルコールマネージャー業務用アプリ 写真撮影版が位置情報の使用を求めています。\n" +
						"アルコールマネージャーを利用し、アルコール測定をどこで行ったかを記録するために、位置情報を利用します。");

				alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
								MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
					}
				});

				alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
								MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
					}
				});

				alertDialog.create();
				alertDialog.show();

			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			// 先ほどの独自定義したrequestCodeの結果確認
			case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
				if (grantResults.length == 0)
				{
					return;
				}
				else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// ユーザーが許可したとき
					// 許可が必要な機能を改めて実行する
					GetLocation();
				} else {
					// ユーザーが許可しなかったとき
					// 許可されなかったため機能が実行できないことを表示する
					AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

					// ダイアログの設定
					alertDialog.setCancelable(false);
					alertDialog.setTitle(getString(R.string.ALERT_TITLE_ERROR));
					alertDialog.setMessage("位置情報の使用が許可されていないため続行できません");

					// OK(肯定的な)ボタンの設定
					alertDialog.setPositiveButton(getString(R.string.ALERT_BTN_OK), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// OKボタン押下時の処理
							finish();
						}
					});

					alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialogInterface) {
							// OKボタン押下時の処理
							finish();
						}
					});

					alertDialog.show();
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
					.setPositiveButton(getString(R.string.ALERT_BTN_YES), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							enableLocationSettings();
						}
					})
					.setNegativeButton(getString(R.string.ALERT_BTN_NO), null)
					.setCancelable(false)
					.show();
		}
	}

	OnClickListener btnDecisionClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getApplication(), CompanyActivity.class);
			startActivity(intent);
		}
	};

	@Override
	protected void onResume() {
		if (locationManagerGPS != null) {
			if (locationManagerGPS.getAllProviders().contains(LocationManager.GPS_PROVIDER)) {
				if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

					return;
				}
				locationManagerGPS.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
			}
		}
		if (locationManagerNET != null)
		{
			if (locationManagerNET.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
				locationManagerNET.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
			}
		}

		super.onResume();
	}

	@Override
	protected void onPause()
	{
		if (locationManagerGPS != null)
		{
			locationManagerGPS.removeUpdates(this);
		}
		if (locationManagerNET != null)
		{
			locationManagerNET.removeUpdates(this);
		}

		super.onPause();
	}

	@Override
	public void onLocationChanged(Location location)
	{
		dblLatitude = location.getLatitude();
		dblLongitude = location.getLongitude();

		// 緯度経度を住所に変換
		String strAddress = getAddressFromPoint(dblLatitude, dblLongitude);

		if (strAddress.equals(""))
		{
			return;
		}

		// 住所表示
		textViewAddress.setText(strAddress);

		// 省略を決定ボタンに変更
		btnDecesion.setText(R.string.BTN_DECISION);

		// 値保存
		editor = pref.edit();
		editor.putString(getString(R.string.PREF_KEY_ADDRESS), strAddress);
		editor.putString(getString(R.string.PREF_KEY_LAT), String.valueOf(dblLatitude));
		editor.putString(getString(R.string.PREF_KEY_LON), String.valueOf(dblLongitude));
		editor.commit();
	}

	@Override
	public void onProviderDisabled(String provider)
	{

	}

	@Override
	public void onProviderEnabled(String provider)
	{

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		switch (status)
		{
		case LocationProvider.AVAILABLE:

			break;
		case LocationProvider.OUT_OF_SERVICE:

			break;
		case LocationProvider.TEMPORARILY_UNAVAILABLE:

			break;
		}
	}

	private void enableLocationSettings()
	{
		Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(settingsIntent);
	}

	/**
	 * 緯度経度を住所に変換
	 * @param latitude
	 * @param longitude
	 * @return
	 */
	private String getAddressFromPoint(double latitude, double longitude)
	{
		String addressValue = null;

		Geocoder geocoder = new Geocoder(this, Locale.JAPAN);

		try
		{
			List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 5);

			if (!addressList.isEmpty())
			{
				Address address = addressList.get(0);

				StringBuilder sb = new StringBuilder();

				String buf;

				for (int i = 0; (buf = address.getAddressLine(i)) != null; i++)
				{
					if (buf.equals("日本"))
					{
						continue;
					}

					if (buf.startsWith("日本、"))
					{
						// 除去
						buf = buf.substring(3);
					}

					if (buf.startsWith("〒"))
					{
						// 郵便番号を除去
						buf = buf.substring(buf.indexOf(" "));
					}

					sb.append(buf + " ");
				}

				addressValue = sb.toString();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return addressValue;
	}
}

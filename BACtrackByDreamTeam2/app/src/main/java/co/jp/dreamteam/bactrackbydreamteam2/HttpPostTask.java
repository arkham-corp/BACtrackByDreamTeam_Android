package co.jp.dreamteam.bactrackbydreamteam2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * HTTP通信でPOSTリクエストを投げる処理を非同期で行うタスク。
 *
 */
public class HttpPostTask extends AsyncTask<Void, Void, Void>
{

    // 設定事項
    private String request_encoding = "UTF-8";
    private String response_encoding = "UTF-8";

    // 初期化事項
    private Activity parent_activity = null;
    private String post_url = null;
    private Handler ui_handler = null;
    private HashMap<String, String> post_headers = null;
    private HashMap<String, String> post_params = null;
    private HashMap<String, byte[]> post_params_jpeg = null;

    // 処理中に使うメンバ
    private String http_err_msg = null;
    private String http_ret_msg = null;
    private ProgressDialog dialog = null;

    // プロパティ
    private boolean http_multipart = false;

    public boolean isHttp_multipart() {
        return http_multipart;
    }

    public void setHttp_multipart(boolean http_multipart) {
        this.http_multipart = http_multipart;
    }

    // 生成時
    public HttpPostTask(Activity parent_activity, String post_url, Handler ui_handler)
    {
        // 初期化
        this.parent_activity = parent_activity;
        this.post_url = post_url;
        this.ui_handler = ui_handler;

        // 送信パラメータは初期化せず，new後にsetさせる
        post_headers = new HashMap<String, String>();
        post_params = new HashMap<String, String>();
        post_params_jpeg = new HashMap<String, byte[]>();
    }

	/* --------------------- POSTパラメータ --------------------- */

    // 追加
    public void addPostHeader(String key, String value)
    {
        post_headers.put(key, value);
    }

    // 追加
    public void addPostParam(String post_name, String post_value)
    {
        post_params.put(post_name, post_value);
    }

    // 追加
    public void addPostParamJpeg(String post_name, byte[] post_value)
    {
        post_params_jpeg.put(post_name, post_value);
    }

	/* --------------------- 処理本体 --------------------- */

    // タスク開始時
    protected void onPreExecute()
    {
        // ダイアログを表示
        dialog = new ProgressDialog(parent_activity);
        dialog.setMessage("通信中・・・");
        dialog.show();
    }

    // メイン処理
    protected Void doInBackground(Void... unused)
    {
        if (post_url.startsWith("https"))
        {
            doInBackgroundHttps();
        }
        else
        {
            doInBackgroundHttp();
        }

        return null;
    }

    private void WriteOutputStream(HttpURLConnection con) throws Exception
    {
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());

        for (String key : post_headers.keySet())
        {
            con.setRequestProperty(key, post_headers.get(key));
        }
        String param = "";
        for (String key : post_params.keySet())
        {
            if (param.equals(""))
            {
                param = key + "=" + post_params.get(key);
            }
            else
            {
                param = param + "&" + key + "=" + post_params.get(key);
            }
        }

        out.write(param);
        out.close();
    }

    private void WriteOutputStream(HttpsURLConnection con) throws Exception
    {
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());

        for (String key : post_headers.keySet())
        {
            con.setRequestProperty(key, post_headers.get(key));
        }
        String param = "";
        for (String key : post_params.keySet())
        {
            if (param.equals(""))
            {
                param = key + "=" + post_params.get(key);
            }
            else
            {
                param = param + "&" + key + "=" + post_params.get(key);
            }
        }

        out.write(param);
        out.close();
    }

    private void WriteOutputStreamMultipart(HttpURLConnection con) throws Exception
    {
        final String twoHyphens = "--";
        final String boundary =  "*****"+ UUID.randomUUID().toString()+"*****";
        final String lineEnd = "\r\n";
        final int maxBufferSize = 1024*1024*3;

        DataOutputStream outputStream;

        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);

        con.setRequestProperty("Connection", "Keep-Alive");
        con.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);

        for (String key : post_headers.keySet())
        {
            con.setRequestProperty(key, post_headers.get(key));
        }

        outputStream = new DataOutputStream(con.getOutputStream());

        for (String key : post_params_jpeg.keySet())
        {
            String fileName = key + ".jpg";
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + fileName +"\"" + lineEnd);
            outputStream.writeBytes("Content-Type: application/octet-stream" + lineEnd);
            outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
            outputStream.writeBytes(lineEnd);

            byte[] buffer = post_params_jpeg.get(key);

            for(int i = 0; i < buffer.length; i++)
            {
                outputStream.write(buffer[i]);
            }

            outputStream.writeBytes(lineEnd);
        }

        for (String key : post_params.keySet())
        {
            String value = post_params.get(key);
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
            outputStream.writeBytes(lineEnd);
            byte[] buffer = value.getBytes("UTF-8");
            for(int i = 0; i < buffer.length; i++)
            {
                outputStream.write(buffer[i]);
            }
            outputStream.writeBytes(lineEnd);
        }

        outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

        outputStream.close();
    }

    private void WriteOutputStreamMultipart(HttpsURLConnection con) throws Exception
    {

    }

    private void doInBackgroundHttp()
    {
        HttpURLConnection con = null;
        StringBuffer result = new StringBuffer();

        try {

            URL url = new URL(post_url);

            con = (HttpURLConnection) url.openConnection();

            con.setDoOutput(true);
            con.setRequestMethod("POST");
            if (http_multipart)
            {
                WriteOutputStreamMultipart(con);
            }
            else
            {
                WriteOutputStream(con);
            }
            con.connect();

            // HTTPレスポンスコード
            final int status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                // 通信に成功した
                // テキストを取得する
                final InputStream in = con.getInputStream();
                String encoding = con.getContentEncoding();
                if(null == encoding){
                    encoding = "UTF-8";
                }
                final InputStreamReader inReader = new InputStreamReader(in, encoding);
                final BufferedReader bufReader = new BufferedReader(inReader);
                String line = null;
                // 1行ずつテキストを読み込む
                while((line = bufReader.readLine()) != null) {
                    result.append(line);
                }
                bufReader.close();
                inReader.close();
                in.close();
            }else{
                System.out.println(status);
            }

        }catch (Exception e1) {
            HttpPostTask.this.http_err_msg = e1.getMessage();
        } finally {
            if (con != null) {
                // コネクションを切断
                con.disconnect();
            }
        }

        HttpPostTask.this.http_ret_msg = result.toString();
    }

    private void doInBackgroundHttps()
    {
        HttpsURLConnection con = null;
        StringBuffer result = new StringBuffer();

        try {

            URL url = new URL(post_url);

            con = (HttpsURLConnection) url.openConnection();

            // 証明書に書かれているCommon NameとURLのホスト名が一致していることの検証をスキップ
            con.setHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession sslSession) {
                    return true;
                }
            });

            // 証明書チェーンの検証をスキップ
            KeyManager[] keyManagers = null;
            TrustManager[] transManagers = { new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            } };
            SSLContext sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(keyManagers, transManagers, new SecureRandom());
            con.setSSLSocketFactory(sslcontext.getSocketFactory());

            con.setDoOutput(true);
            con.setRequestMethod("POST");
            if (http_multipart)
            {
                WriteOutputStreamMultipart(con);
            }
            else
            {
                WriteOutputStream(con);
            }
            con.connect();

            // HTTPレスポンスコード
            final int status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                // 通信に成功した
                // テキストを取得する
                final InputStream in = con.getInputStream();
                String encoding = con.getContentEncoding();
                if(null == encoding){
                    encoding = "UTF-8";
                }
                final InputStreamReader inReader = new InputStreamReader(in, encoding);
                final BufferedReader bufReader = new BufferedReader(inReader);
                String line = null;
                // 1行ずつテキストを読み込む
                while((line = bufReader.readLine()) != null) {
                    result.append(line);
                }
                bufReader.close();
                inReader.close();
                in.close();
            }else{
                System.out.println(status);
            }

        }catch (Exception e1) {
            HttpPostTask.this.http_err_msg = e1.getMessage();
        } finally {
            if (con != null) {
                // コネクションを切断
                con.disconnect();
            }
        }

        HttpPostTask.this.http_ret_msg = result.toString();
    }

    // タスク終了時
    protected void onPostExecute(Void unused)
    {
        // ダイアログを消す
        dialog.dismiss();

        // 受信結果をUIに渡すためにまとめる
        Message message = new Message();
        Bundle bundle = new Bundle();
        if (http_err_msg != null)
        {
            // エラー発生時
            bundle.putBoolean("http_post_success", false);
            bundle.putString("http_response", http_err_msg);
        }
        else
        {
            // 通信成功時
            bundle.putBoolean("http_post_success", true);
            bundle.putString("http_response", http_ret_msg);
        }
        message.setData(bundle);

        // 受信結果に基づいてUI操作させる
        ui_handler.sendMessage(message);
    }
}
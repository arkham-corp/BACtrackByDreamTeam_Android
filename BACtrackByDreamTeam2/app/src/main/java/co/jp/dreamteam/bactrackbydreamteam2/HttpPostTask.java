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
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

/**
 * HTTP通信でPOSTリクエストを投げる処理を非同期で行うタスク。
 *
 */
public class HttpPostTask extends AsyncTask<Void, Void, Void>
{
    // 初期化事項
    private final Activity parent_activity;
    private final String post_url;
    private final Handler ui_handler;

    // 送信パラメータは初期化せず，new後にsetさせる
    private final HashMap<String, String> post_headers = new HashMap<>();
    private final HashMap<String, String> post_params = new HashMap<>();
    private final HashMap<String, byte[]> post_params_jpeg = new HashMap<>();

    // 処理中に使うメンバ
    private String http_err_msg = null;
    private String http_ret_msg = null;
    private ProgressDialog dialog = null;

    // プロパティ
    private String verify_hostname = "";

    public void setVerify_hostname(String verify_hostname) {
        this.verify_hostname = verify_hostname;
    }

    private boolean http_multipart = false;

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
    }

	/* --------------------- POSTパラメータ --------------------- */

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
        StringBuilder param = new StringBuilder();
        for (String key : post_params.keySet())
        {
            if (param.toString().equals(""))
            {
                param = new StringBuilder(key + "=" + post_params.get(key));
            }
            else
            {
                param.append("&").append(key).append("=").append(post_params.get(key));
            }
        }

        out.write(param.toString());
        out.close();
    }

    private void WriteOutputStream(HttpsURLConnection con) throws Exception
    {
        OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());

        for (String key : post_headers.keySet())
        {
            con.setRequestProperty(key, post_headers.get(key));
        }
        StringBuilder param = new StringBuilder();
        for (String key : post_params.keySet())
        {
            if (param.toString().equals(""))
            {
                param = new StringBuilder(key + "=" + post_params.get(key));
            }
            else
            {
                param.append("&").append(key).append("=").append(post_params.get(key));
            }
        }

        out.write(param.toString());
        out.close();
    }

    private void WriteOutputStreamMultipart(HttpURLConnection con) throws Exception
    {
        final String twoHyphens = "--";
        final String boundary =  "*****"+ UUID.randomUUID().toString()+"*****";
        final String lineEnd = "\r\n";

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

            assert buffer != null;
            for (byte b : buffer) {
                outputStream.write(b);
            }

            outputStream.writeBytes(lineEnd);
        }

        for (String key : post_params.keySet())
        {
            String value = post_params.get(key);
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
            outputStream.writeBytes(lineEnd);
            assert value != null;
            byte[] buffer = value.getBytes(StandardCharsets.UTF_8);
            for (byte b : buffer) {
                outputStream.write(b);
            }
            outputStream.writeBytes(lineEnd);
        }

        outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

        outputStream.close();
    }

    private void WriteOutputStreamMultipart(HttpsURLConnection con) throws Exception
    {
        final String twoHyphens = "--";
        final String boundary =  "*****"+ UUID.randomUUID().toString()+"*****";
        final String lineEnd = "\r\n";

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

            assert buffer != null;
            for (byte b : buffer) {
                outputStream.write(b);
            }

            outputStream.writeBytes(lineEnd);
        }

        for (String key : post_params.keySet())
        {
            String value = post_params.get(key);
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
            outputStream.writeBytes(lineEnd);
            assert value != null;
            byte[] buffer = value.getBytes(StandardCharsets.UTF_8);
            for (byte b : buffer) {
                outputStream.write(b);
            }
            outputStream.writeBytes(lineEnd);
        }

        outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

        outputStream.close();
    }

    private void doInBackgroundHttp()
    {
        HttpURLConnection con = null;
        StringBuilder result = new StringBuilder();

        try {

            URL url = new URL(post_url);

            con = (HttpURLConnection) url.openConnection();

            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setReadTimeout(5000);
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
                String line;
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
        StringBuilder result = new StringBuilder();

        try {

            URL url = new URL(post_url);

            con = (HttpsURLConnection) url.openConnection();

            // 証明書に書かれているCommon NameとURLのホスト名が一致していることの検証
            con.setHostnameVerifier((hostname, sslSession) -> hostname.equals(verify_hostname));

            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setReadTimeout(5000);
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
                String line;
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

        }
        catch (CertificateException e) {
            HttpPostTask.this.http_err_msg = "証明書の検証に失敗しました";
        }
        catch (Exception e) {
            String message = e.getMessage();
            if (message == null)
            {
                HttpPostTask.this.http_err_msg = "SSL通信エラー";
            }
            else
            {
                HttpPostTask.this.http_err_msg = message;
            }
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
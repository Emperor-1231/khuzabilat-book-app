package com.khuzabilat.book;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.io.OutputStream;

public class MainActivity extends Activity {
  WebView web;
  @Override protected void onCreate(Bundle b) {
    super.onCreate(b);
    web = new WebView(this);
    setContentView(web);
    WebSettings s = web.getSettings();
    s.setJavaScriptEnabled(true);
    s.setDomStorageEnabled(true);
    s.setAllowFileAccess(true);
    web.setWebViewClient(new WebViewClient());
    web.addJavascriptInterface(new Object() {
      @JavascriptInterface public void savePdf(String name, String b64) {
        try {
          byte[] bytes = Base64.decode(b64, Base64.DEFAULT);
          ContentResolver r = getContentResolver();
          ContentValues v = new ContentValues();
          v.put(MediaStore.Downloads.DISPLAY_NAME, name);
          v.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
          Uri u = r.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
          OutputStream o = r.openOutputStream(u);
          o.write(bytes); o.close();
          Intent i = new Intent(Intent.ACTION_SEND);
          i.setType("application/pdf");
          i.putExtra(Intent.EXTRA_STREAM, u);
          i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
          runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, "حُفظ الكتاب في مجلد التنزيلات", Toast.LENGTH_LONG).show();
            startActivity(Intent.createChooser(i, "مشاركة الكتاب"));
          });
        } catch (Exception e) {
          runOnUiThread(() -> Toast.makeText(MainActivity.this, "تعذّر الحفظ", Toast.LENGTH_LONG).show());
        }
      }
    }, "Android");
    web.loadUrl("file:///android_asset/index.html");
  }
  @Override public void onBackPressed() { finish(); }
}

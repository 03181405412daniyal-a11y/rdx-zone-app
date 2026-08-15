package com.rdxzone.app;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    private String fcmToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        CookieManager.getInstance().setAcceptCookie(true);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(
                    WebView view,
                    String url
            ) {
                super.onPageFinished(view, url);

                /*
                 * Page load ہونے کے بعد token
                 * logged-in PHP session کے ساتھ save ہوگا۔
                 */

                saveTokenToWebsite();
            }
        });

        webView.loadUrl("https://rdxzone.xo.je/");

        requestNotificationPermission();

        getFirebaseToken();
    }


    /*
    |--------------------------------------------------------------------------
    | GET FIREBASE FCM TOKEN
    |--------------------------------------------------------------------------
    */

    private void getFirebaseToken() {

        FirebaseMessaging.getInstance()
                .getToken()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {

                        Toast.makeText(
                                this,
                                "Firebase Token Failed",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    fcmToken = task.getResult();

                    /*
                     * پہلے والا clipboard test برقرار رکھا ہے۔
                     */

                    ClipboardManager clipboard =
                            (ClipboardManager)
                                    getSystemService(
                                            Context.CLIPBOARD_SERVICE
                                    );

                    ClipData clip =
                            ClipData.newPlainText(
                                    "FCM Token",
                                    fcmToken
                            );

                    clipboard.setPrimaryClip(clip);

                    Toast.makeText(
                            this,
                            "FCM Token received",
                            Toast.LENGTH_LONG
                    ).show();

                    /*
                     * اگر website پہلے ہی load ہو چکی ہے
                     * تو token فوراً save کرنے کی کوشش کریں۔
                     */

                    saveTokenToWebsite();
                });
    }


    /*
    |--------------------------------------------------------------------------
    | SAVE TOKEN TO PHP / DATABASE
    |--------------------------------------------------------------------------
    */

    private void saveTokenToWebsite() {

        if (fcmToken == null || fcmToken.trim().isEmpty()) {
            return;
        }

        if (webView == null) {
            return;
        }

        try {

            String safeToken =
                    JSONObject.quote(fcmToken);

            String javascript =
                    "fetch('/save_fcm_token.php', {" +
                    "method: 'POST'," +
                    "headers: {" +
                    "'Content-Type': 'application/x-www-form-urlencoded'" +
                    "}," +
                    "body: 'token=' + encodeURIComponent(" +
                    safeToken +
                    ")" +
                    "})" +
                    ".then(response => response.json())" +
                    ".then(data => {" +
                    "console.log('FCM:', data);" +
                    "})" +
                    ".catch(error => {" +
                    "console.log('FCM save error:', error);" +
                    "});";

            webView.evaluateJavascript(
                    javascript,
                    null
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    /*
    |--------------------------------------------------------------------------
    | NOTIFICATION PERMISSION
    |--------------------------------------------------------------------------
    */

    private void requestNotificationPermission() {

        if (
                Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    100
            );
        }
    }


    /*
    |--------------------------------------------------------------------------
    | BACK BUTTON
    |--------------------------------------------------------------------------
    */

    @Override
    public void onBackPressed() {

        if (webView.canGoBack()) {

            webView.goBack();

        } else {

            super.onBackPressed();
        }
    }
}

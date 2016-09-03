package com.sun.webviewexample;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final String URL = "http://www.baidu.com";
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                logMsg(String.format("shouldOverrideUrlLoading url: %s", url));
//                view.loadUrl(url);
//                return true;
//            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                logMsg(String.format("onPageStarted url: %s", url));
                super.onPageStarted(view, url, favicon);
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
            }
        });

//        Button button = (Button) findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "WebView Example!", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    @Override
    protected void onResume() {
        logMsg("onResume");
        super.onResume();
        mWebView.loadUrl(URL);
    }

    private void customWebSettings(WebView webView) {
        WebSettings webSettings = webView.getSettings();
    }

    private void interactWithJavaScript(WebView webView) {
        /**
         * <script type="text/javascript">
         *     function readyToGo() {
         *         alert("Hello")
         *     }
         *
         *     function alertMessage(message) {
         *         alert(message)
         *     }
         *
         *     function getYourCar() {
         *         return "Car";
         *     }
         * </script>
         */
        String call = "javascript:readyToGo()";
        webView.loadUrl(call);

        call = "javascript:alertMessage(\"" + "content" + "\")";
        webView.loadUrl(call);

        evaluateJavaScript(webView);

        webView.addJavascriptInterface(this, "android");
        /**
         * <script type="text/javascript">
         *     function toastClick() {
         *         window.android.show("JavaScript called~");
         *     }
         * </script>
         */

        webView.addJavascriptInterface(this, "Android");
        /**
         * <script type="text/javascript">
         *     function showHello() {
         *         var str = window.Android.getMessage();
         *         console.log(str);
         *     }
         * </script>
         */
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void evaluateJavaScript(WebView webView) {
        webView.evaluateJavascript("getYourCar()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                logMsg("findCar: " + value);
            }
        });
    }

    @JavascriptInterface
    public void show(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public String getMessage() {
        return "Hello, boy~";
    }

    private void logMsg(String msg) {
        Log.d(TAG, msg);
    }
}

package com.emal.android.transport.spb.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.emal.android.transport.spb.R;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since: 1.5
 */
public class DonateActivity extends Activity {
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.donate);

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        mWebView.setPadding(0, 0, 0, 0);
        mWebView.setInitialScale(getScale());
        mWebView.loadUrl("https://money.yandex.ru/embed/shop.xml?account=41001119911711&quickpay=shop&payment-type-choice=on&writer=seller&targets=%D0%9D%D0%B0+%D1%80%D0%B0%D0%B7%D0%B2%D0%B8%D1%82%D0%B8%D0%B5+%D0%BF%D1%80%D0%BE%D0%B5%D0%BA%D1%82%D0%B0+%22%D0%93%D0%B4%D0%B5+%D0%90%D0%B2%D1%82%D0%BE%D0%B1%D1%83%D1%81%22&targets-hint=&default-sum=100&button-text=03&successURL=https%3A%2F%2Fplay.google.com%2Fstore%2Fapps%2Fdetails%3Fid%3Dcom.emal.android.transport.spb");
    }

    private int getScale(){
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        Double val = new Double(width)/new Double(450);
        val = val * 100d;
        return val.intValue();
    }
}

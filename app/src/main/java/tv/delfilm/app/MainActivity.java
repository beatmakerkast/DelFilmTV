package tv.delfilm.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.InputType;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private static final String HOME = "https://delfilm.pro/";
    private WebView webView;
    private EditText search;
    private TextView status;
    private LinearLayout toolbar;
    private FrameLayout root, fullScreenHolder;
    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private String pendingSearch;
    private boolean searchPageRequested = false;

    private int dp(float px) { return Math.round(getResources().getDisplayMetrics().density * px); }
    private LinearLayout.LayoutParams layout(int w, int h) {
        return new LinearLayout.LayoutParams(w < 0 ? w : dp(w), h < 0 ? h : dp(h));
    }

    @Override @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(11,18,32));
        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        root.addView(screen, new FrameLayout.LayoutParams(-1, -1));

        toolbar = new LinearLayout(this);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setPadding(dp(18), dp(12), dp(18), dp(12));
        toolbar.setBackgroundColor(Color.rgb(15,23,42));
        screen.addView(toolbar, layout(-1, 76));

        TextView logo = new TextView(this);
        logo.setText("▶  DelFilm TV");
        logo.setTextColor(Color.WHITE);
        logo.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        logo.setTextSize(22);
        toolbar.addView(logo, layout(205, -2));

        search = new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(19);
        search.setTextColor(Color.WHITE);
        search.setHintTextColor(Color.rgb(172,183,206));
        search.setHint("Название фильма или сериала");
        search.setPadding(dp(16), dp(8), dp(16), dp(8));
        search.setBackgroundColor(Color.rgb(43,54,75));
        search.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(0, dp(54), 1f);
        toolbar.addView(search, searchParams);
        search.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);
        search.setOnEditorActionListener((v, actionId, event) -> { runSearch(); return true; });

        Button go = makeButton("Искать");
        LinearLayout.LayoutParams goLp = layout(116, 54); goLp.leftMargin = dp(10);
        toolbar.addView(go, goLp);
        go.setOnClickListener(v -> runSearch());
        Button home = makeButton("⌂");
        LinearLayout.LayoutParams homeLp = layout(58, 54); homeLp.leftMargin = dp(8);
        toolbar.addView(home, homeLp);
        home.setOnClickListener(v -> { pendingSearch = null; searchPageRequested = false; webView.loadUrl(HOME); });

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(11,18,32));
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        screen.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1f));

        status = new TextView(this);
        status.setText("Введите название, нажмите «Искать», затем выберите фильм на сайте кнопками пульта.");
        status.setTextSize(13);
        status.setTextColor(Color.rgb(190,199,216));
        status.setGravity(Gravity.CENTER_VERTICAL);
        status.setPadding(dp(22), 0, dp(12), 0);
        status.setBackgroundColor(Color.rgb(15,23,42));
        screen.addView(status, layout(-1, 32));

        fullScreenHolder = new FrameLayout(this);
        fullScreenHolder.setBackgroundColor(Color.BLACK);
        fullScreenHolder.setVisibility(View.GONE);
        root.addView(fullScreenHolder, new FrameLayout.LayoutParams(-1, -1));

        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) { callback.onCustomViewHidden(); return; }
                customView = view; customViewCallback = callback;
                fullScreenHolder.addView(view, new FrameLayout.LayoutParams(-1, -1));
                fullScreenHolder.setVisibility(View.VISIBLE);
                getWindow().getDecorView().setSystemUiVisibility(5894 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                view.requestFocus();
            }
            @Override public void onHideCustomView() { hideFullScreen(); }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest req) {
                String scheme = req.getUrl().getScheme();
                return !("https".equals(scheme) || "http".equals(scheme));
            }
            @Override public void onPageFinished(WebView v, String url) {
                if (pendingSearch != null && url.startsWith(HOME) && searchPageRequested) {
                    searchPageRequested = false;
                    injectSearch(pendingSearch);
                    pendingSearch = null;
                }
                status.setText("Стрелки и OK — выбор • Назад — предыдущая страница • Полный экран — через плеер");
            }
            @Override public void onReceivedError(WebView v, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) status.setText("Не удалось загрузить страницу. Проверьте интернет или адрес сайта.");
            }
        });
        setContentView(root);
        webView.loadUrl(HOME);
        search.requestFocus();
    }

    private Button makeButton(String title) {
        Button b = new Button(this);
        b.setText(title);
        b.setTextColor(Color.WHITE);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(65,73,120)));
        b.setFocusable(true);
        return b;
    }

    private void runSearch() {
        String q = search.getText().toString().trim();
        if (q.isEmpty()) { search.requestFocus(); return; }
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(search.getWindowToken(), 0);
        status.setText("Ищем: " + q);
        pendingSearch = q;
        searchPageRequested = true;
        webView.loadUrl(HOME);
        webView.requestFocus();
    }

    // Uses the site's own live search, not a guessed undocumented API.
    private void injectSearch(String query) {
        String js = "(function(){"+
            "var a=[].slice.call(document.querySelectorAll('input'));"+
            "var e=a.find(x=>/поиск фильмов|поиск|search/i.test(x.placeholder||'') && x.type!=='hidden')||a.find(x=>x.type==='search');"+
            "if(!e)return 'not-found';"+
            "e.scrollIntoView({block:'center'});e.focus();"+
            "var setter=Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value').set;setter.call(e,"+JSONObject.quote(query)+");"+
            "e.dispatchEvent(new Event('input',{bubbles:true}));"+
            "e.dispatchEvent(new Event('change',{bubbles:true}));"+
            "e.dispatchEvent(new KeyboardEvent('keyup',{bubbles:true,key:'м'}));"+
            "return 'ok';})()";
        webView.evaluateJavascript(js, value -> {
            if ("\"not-found\"".equals(value)) {
                status.setText("Поле поиска на сайте не найдено. Выберите поиск на странице вручную.");
            } else {
                status.setText("Результаты поиска появятся на сайте. Выберите фильм стрелками и нажмите OK.");
            }
            webView.requestFocus();
        });
    }

    private void hideFullScreen() {
        if (customView == null) return;
        fullScreenHolder.removeView(customView);
        fullScreenHolder.setVisibility(View.GONE);
        customView = null;
        if (customViewCallback != null) customViewCallback.onCustomViewHidden();
        customViewCallback = null;
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        webView.requestFocus();
    }

    @Override public void onBackPressed() {
        if (customView != null) hideFullScreen();
        else if (webView.canGoBack()) webView.goBack();
        else { search.requestFocus(); }
    }
    @Override protected void onDestroy() {
        if (webView != null) { webView.stopLoading(); webView.destroy(); }
        super.onDestroy();
    }
}

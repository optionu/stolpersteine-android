package com.option_u.stolpersteine.activities.description;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.option_u.stolpersteine.R;
import com.option_u.stolpersteine.StolpersteineApplication;
import com.option_u.stolpersteine.helpers.PreferenceHelper;

public class DescriptionActivity extends Activity {

    private static final String EXTRA_NAME = "url";

    public enum ViewFormat {
        TEXT, WEB;

        public static ViewFormat toViewFormat(String viewFormatString) {
            try {
                return valueOf(viewFormatString);
            } catch (Exception e) {
                return TEXT;
            }
        }
    };

    private ViewFormat viewFormat;
    private PreferenceHelper preferenceHelper;
    private WebView browser;
    private WebSettings settings;
    private String bioUrl;
    private static final String CSS_QUERY = "div#biografie_seite";

    public static Intent createIntent(Context context, String url) {
        Intent intent = new Intent(context, DescriptionActivity.class);
        intent.putExtra(EXTRA_NAME, url);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_bio);
        Intent intent = getIntent();
        bioUrl = intent.getStringExtra(EXTRA_NAME);

        browser = (WebView) findViewById(R.id.webview);
        browser.setWebViewClient(new SimpleWebViewClient((ProgressBar) findViewById(R.id.progressBar)));
        settings = browser.getSettings();
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        preferenceHelper = new PreferenceHelper(this);
        viewFormat = preferenceHelper.readViewFormat();
    }

    @Override
    protected void onResume() {
        super.onResume();

        StolpersteineApplication stolpersteineApplication = (StolpersteineApplication) getApplication();
        stolpersteineApplication.trackView(this.getClass());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bio, menu);
        MenuItem itemViewFormat = menu.getItem(0);
        openUrlBasedOnDomain(itemViewFormat);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_view_format) {
            if (viewFormat == ViewFormat.TEXT) {
                switchToAndLoadInWebView(item);
            } else {
                switchToAndLoadInTextView(item);
            }
        } else if (itemId == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void loadContentInBrowser(WebView browser, String url, String cssQuery) {
        new HTMLContentLoader(browser).loadContent(this, url, cssQuery);
    }

    protected void loadUrlInBrowser(WebView browser, String url) {
        browser.loadUrl(url);
    }

    private void switchToAndLoadInTextView(MenuItem selectedItem) {
        viewFormat = ViewFormat.TEXT;
        preferenceHelper.saveViewFormat(viewFormat);
        setViewFormatMenuItemToWeb(selectedItem);
        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(false);
        loadContentInBrowser(browser, bioUrl, CSS_QUERY);
    }

    private void switchToAndLoadInWebView(MenuItem selectedItem) {
        viewFormat = ViewFormat.WEB;
        preferenceHelper.saveViewFormat(viewFormat);
        setViewFormatMenuItemToText(selectedItem);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        loadUrlInBrowser(browser, bioUrl);
    }

    private void setViewFormatMenuItemToText(MenuItem item) {
        item.setTitle(R.string.bio_action_item_text);
        item.setIcon(R.drawable.ic_action_view_as_text);
    }

    private void setViewFormatMenuItemToWeb(MenuItem item) {
        item.setTitle(R.string.bio_action_item_web);
        item.setIcon(R.drawable.ic_action_view_as_web);
    }

    private void openUrlBasedOnDomain(MenuItem itemViewFormat) {
        if (bioUrl.contains("wikipedia")) {
            // load in web only, disable item option
            loadUrlInBrowser(browser, bioUrl);
            disableMenuItem(itemViewFormat);
        } else {
            // load in whatever provided by ViewFormat
            // e.g.: www.stolpersteine-berlin.de
            loadViewBasedOnViewFormat(itemViewFormat);
        }
    }

    private void loadViewBasedOnViewFormat(MenuItem itemViewFormat) {
        if (viewFormat == ViewFormat.WEB) {
            switchToAndLoadInWebView(itemViewFormat);
        } else {
            switchToAndLoadInTextView(itemViewFormat);
        }
    }

    private void disableMenuItem(MenuItem menuItem) {
        menuItem.setEnabled(false);
        menuItem.setVisible(false);
    }

}
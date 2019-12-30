package se.tmeit.app.ui.onboarding;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import se.tmeit.app.R;

/**
 * Onboarding by logging in using SAML authentication or TMEIT.se local login.
 */
public final class WebOnboardingFragment extends Fragment {
	private static final String CAPTURE_URL = "https://tmeit.se/wiki/Special:TmeitServiceAuth/direct";
	private static final String LOGIN_NORMAL_URL = "https://tmeit.se/w/index.php?title=Special:Inloggning&returnto=Special%3ATmeitServiceAuth%2Fdirect";
	private static final String LOGIN_SAML_URL = "https://tmeit.se/w/index.php?title=Special:SAMLAuth&returnto=Special%3ATmeitServiceAuth%2Fdirect";
	private static final String LOGIN_URL_KEY = "loginUrl";
	private static final String TAG = WebOnboardingFragment.class.getSimpleName();
	protected OnboardingResultHandler mResultHandler;

	public static WebOnboardingFragment newInstance(boolean usingSaml) {
		WebOnboardingFragment fragment = new WebOnboardingFragment();
		Bundle args = new Bundle();
		args.putString(LOGIN_URL_KEY, (usingSaml ? LOGIN_SAML_URL : LOGIN_NORMAL_URL));
		fragment.setArguments(args);
		return fragment;
	}

	@SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_web, container, false);

		WebView webView = view.findViewById(R.id.web_view);
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		webView.addJavascriptInterface(new CaptureJsInterface(), "CAPTURE");
		webView.setWebViewClient(new CapturingWebViewClient());
		String url = getArguments().getString(LOGIN_URL_KEY);
		webView.loadUrl(url);
		return view;
	}

	public void setResultHandler(OnboardingResultHandler resultHandler) {
		mResultHandler = resultHandler;
	}

	public interface OnboardingResultHandler {
		void handleResult(String authCode);
	}

	private final class CaptureJsInterface {
		@JavascriptInterface
		public void capture(String authCode) {
			Log.i(TAG, "Captured the auth code: " + authCode);
			if (!"undefined".equals(authCode)) {
				mResultHandler.handleResult(authCode);
			}
		}
	}

	private static class CapturingWebViewClient extends WebViewClient {
		@Override
		public void onPageFinished(WebView view, String url) {
			if (url.startsWith(CAPTURE_URL)) {
				view.loadUrl("javascript:window.CAPTURE.capture($('#tmeit-qrcode-field').val());");
			}
		}
	}
}

package cef.egliseactu;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Stack;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import cef.egliseactu.providers.Database;
import cef.egliseactu.utils.DOMXmlUtils;

public class DisplayWebPageActivity extends Activity {
	/** Called when the activity is first created. */
	WebView web;
	ProgressBar progressBar;
	private HttpClient client;
	private String link;
	Logger log = Logger.getLogger(DisplayWebPageActivity.class.getName());
	private TextView titleBar;
	private Stack<String> history = new Stack<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		link = getIntent().getStringExtra("link");
		if (link == null && getIntent().getData() != null) {
			link = getIntent().getData().toString().replace("eglise.catholique.fr", "actus.catholique.fr");
		}
		titleBar = (TextView) findViewById(R.id.title_text);
		progressBar = (ProgressBar) findViewById(R.id.title_refresh_progress);
		web = (WebView) findViewById(R.id.webview);
		web.getSettings().setBuiltInZoomControls(true);
		web.getSettings().setJavaScriptEnabled(true);
		web.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
				titleBar.setText(title);
			}
		});
		web.setWebViewClient(new WebViewClient() {
			@Override
			public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
				if (isReload)
					web.stopLoading();
//				if (isReload) {
				loadUrl(url);
//				}
			}
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				progressBar.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				progressBar.setVisibility(View.GONE);
			}
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				log.warning(url);
				if (url.contains("eglise.catholique.fr"))
					url = url.replace("eglise.catholique.fr", "actus.catholique.fr");
				if (url.contains("actus.catholique.fr")) {
					history.push(link);
					loadUrl(url);
					return true;
				}
				return lauchUrlInNativeBrowser(url);
			}

			
			
		});
		loadUrl(link);
	}

	protected boolean lauchUrlInNativeBrowser(String url) {
		boolean override = false;
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		intent.addCategory(Intent.CATEGORY_BROWSABLE);
		// If another application is running a WebView and launches the
		// Browser through this Intent, we want to reuse the same window if
		// possible.
		intent.putExtra(Browser.EXTRA_APPLICATION_ID, DisplayWebPageActivity.this.getPackageName());
		try {
			DisplayWebPageActivity.this.startActivity(intent);
			override = true;
		} catch (ActivityNotFoundException ex) {
			// If no application can handle the URL, assume that the
			// browser can handle it.
		}
		return override;
	}

	@Override
	public void onBackPressed() {
		if (!history.empty()) {
			loadUrl(history.pop());
		} else {
			super.onBackPressed();
		}
	}

	private void loadUrl(final String url) {
		log.warning("display :" + link);
		this.link = url;
		new Runnable() {

			@Override
			public void run() {
				// final StringBuilder html = new
				// StringBuilder("<html><head><title>Actualit�</title></head><body><h1>Actualit�</h1><p>Texte de l'actualit�</p></body></html>");
				Cursor cursor = null;
				
				client = new DefaultHttpClient();
				try {
					cursor = getContentResolver().query(Database.CONTENT_WEBPAGE_URI, null,  Database._ID + " = ?", new String[]{link}, null);
					if (cursor.moveToFirst()) {
						String html = cursor.getString(cursor.getColumnIndex(Database.DESCRIPTION));
						loadData(html);
						return;
					}
					HttpResponse response = client.execute(new HttpGet(link));
					int statusCode = response.getStatusLine().getStatusCode();
					if (statusCode != HttpStatus.SC_OK) {
						if (statusCode == HttpStatus.SC_NOT_FOUND) {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									String url = link.replace("actus.catholique.fr", "eglise.catholique.fr").replace(".htmlevent=", ".html?event=");
									log.warning("404: load site = " + url);
//									web.loadUrl(url);
									lauchUrlInNativeBrowser(url);
								}
							});
							return;
						}
						else {
							throw new RuntimeException("HTTP status code: " + statusCode + " != " + HttpStatus.SC_OK);
						}
					}
					HttpEntity entity = response.getEntity();
					BufferedReader r = new BufferedReader(new InputStreamReader(entity.getContent()));
					final StringBuilder content = new StringBuilder();
					String line;
					while ((line = r.readLine()) != null) {
						if (line.contains("<DataContent>")) {
							line = line.replace("<DataContent>", "<DataContent><![CDATA[");
						}
						if (line.contains("</DataContent>")) {
							line = line.replace("</DataContent>", "]]></DataContent>");
						}
						content.append(line);
					}
					InputSource inSource = new InputSource(new StringReader(content.toString()));
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inSource);
					if ("rss".equals(doc.getFirstChild().getNodeName())) {
						Intent intent = new Intent(getApplicationContext(), FolderListActivity.class);
						intent.putExtra("link", link);
						intent.putExtra("content", content.toString());
						startActivity(intent);
						finish();
					}
					final StringBuilder html = new StringBuilder("<html><head>");
					String title = DOMXmlUtils.getElementText(DOMXmlUtils.getFirstElementByTagName(doc, "HeadLine"));
					html.append("<title>");
					html.append(title);
					html.append("</title><style> h1 { color:#6A9EC3;font-size:1.1em} h3 {color:#6A9EC3;} a, a:visited, a:active {color: #486faa;} </style></head><body>");
					titleBar.setText(title);
					html.append("<h1>").append(title).append("</h1>");
					String date = DOMXmlUtils.getElementText(DOMXmlUtils.getFirstElementByTagName(doc, "DateLine"));
					html.append("<h6>").append(date).append("</h6>");
					NodeList components = doc.getElementsByTagName("NewsComponent");
					for (int i = 0; i < components.getLength(); i++) {
						Element component = (Element) components.item(i);
						Node role = DOMXmlUtils.getFirstElement(component);
						log.warning("component " + i + " : " + role.getNodeName() + "(" + DOMXmlUtils.getAttribute(role, "FormalName") + ")");
						if (role.getNodeName().equals("Role")) {
							if ("Photo".equals(DOMXmlUtils.getAttribute(role, "FormalName"))) {
								String url = DOMXmlUtils.getAttribute(DOMXmlUtils.getFirstElementByTagName(component, "ContentItem"), "Href");
								html.append("<img src=\"").append(url).append("\" width=\"100%\"/>");
							} else {
								Element data = DOMXmlUtils.getFirstElementByTagName(component, "DataContent");
								if (data != null) {
									html.append(DOMXmlUtils.getElementText(data));
								}
							}
						}
					}
					html.append("</body></html>");
					String htmlBuild = html.toString();
					loadData(htmlBuild);
					ContentValues values = new ContentValues();
					values.put(Database._ID, link);
					values.put(Database.LINK, link);
					values.put(Database.DESCRIPTION, htmlBuild);
					getContentResolver().insert(Database.CONTENT_WEBPAGE_URI, values);
					EgliseActuActivity.getTracker(DisplayWebPageActivity.this).trackPageView(url);
				} catch (final Exception e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							web.loadDataWithBaseURL(null, getString(R.string.html_errors, url, e.toString()), "text/html", "UTF-8", null);
						}
					});
				}
				finally {
					if (cursor != null && !cursor.isClosed())
						cursor.close();
				}
			}

		}.run();
	}

	private void loadData(final String html) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				web.loadDataWithBaseURL("http://eglise.catholique.fr", html, "text/html", "UTF-8", link);
			}
		});
	}
	
	public void goHome(View v) {
		final Intent intent = new Intent(this, EgliseActuActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	public void onShareClick(View v) {
		CharSequence title = titleBar.getText();
		String text = getString(R.string.webpage_share, link.replace("actus.catholique.fr", "eglise.catholique.fr"), title);
		Intent i = new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, text).setType("text/plain").putExtra(Intent.EXTRA_SUBJECT, getString(R.string.webpage_share_subject, title));
		startActivityForResult(Intent.createChooser(i, getString(R.string.share_with)), 0);
	}
	
	public void onSearchClick(View v) {
		final Intent intent = new Intent(this, SearchActivity.class);
		startActivity(intent);
	}
}
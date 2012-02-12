package cef.egliseactu;

import java.util.List;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cef.egliseactu.adapters.FolderAdapter;
import cef.egliseactu.adapters.ItemAdapter;
import cef.egliseactu.providers.Database;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.sfeir.android.friendapps.FriendListActivity;

public class EgliseActuActivity extends ListActivity implements OnScrollListener, OnItemClickListener {
	public static final String AUTHORITY = "cef.egliseactu";
	private static final int MENU_ABOUT = 1;
	private static final int MENU_CONTACT = 2;
	private static final int MENU_WEBSITE = 3;
	private static final int MENU_FRIENDAPP = 4;
	private static final int MENU_CLEARCACHE = 5;
	private static final int MENU_SHARE = 6;
//	private RSSItemAdapter mAdapter;
	private TextView empty;
	private boolean isLoading = false;
	private int loadingCount = 0;
	private boolean isSearchEnd = false;
	private boolean isRunning = false;
	private int start = 0;
	private TextView loadMoreView;
	private Gallery gallery;
	private FolderAdapter folderAdapter;

	/** Called when the activity is first created. */
	// @Override
	// public void onCreate(Bundle savedInstanceState) {
	// super.onCreate(savedInstanceState);
	// setContentView(R.layout.main);
	// TextView box = (TextView) findViewById(R.id.text);
	// box.setClickable(true);
	// RSSReader reader = new RSSReader();
	// String uri = "http://feeds.bbci.co.uk/news/world/rss.xml";
	// try {
	// RSSFeed feed = reader.load(uri);
	// feed.getItems();
	// } catch (RSSReaderException e) {
	// e.printStackTrace();
	// }
	// box.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// startActivity(new Intent(getApplicationContext(),
	// DisplayWebPageActivity.class));
	// }
	// });
	// }
    private static final String[] ACTUS_PROJECTION = new String[] { Database._ID, // 0
    	Database.TITLE, // 1
    	Database.DESCRIPTION, // 2
    	Database.CATEGORY, // 3
    	Database.THUMBNAILS, // 4
    	Database.DATE, // 5
    };
    private static final String[] FOLDER_PROJECTION = new String[] { Database._ID, // 0
    	Database.TITLE, // 1
    	Database.THUMBNAILS, // 2
    };
	private ItemAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		empty = (TextView) findViewById(android.R.id.empty);
		loadMoreView = new TextView(this);
		loadMoreView.setText(getText(R.string.next));
		loadMoreView.setWidth(LayoutParams.FILL_PARENT);
		loadMoreView.setHeight(50);
		loadMoreView.setTextAppearance(this, android.R.attr.textAppearanceMedium);
		loadMoreView.setGravity(Gravity.CENTER);
		loadMoreView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				loadMore();
			}
		});
		getListView().addFooterView(loadMoreView);
//		mAdapter = new RSSItemAdapter(this);
		//setListAdapter(mAdapter);
//		gallery = (Gallery) findViewById(R.id.folder);
		View galleryView = getLayoutInflater().inflate(R.layout.folder_gallery, null);
		gallery = (Gallery) galleryView.findViewById(R.id.folder);
		getListView().addHeaderView(galleryView);
		Cursor folderCursor = managedQuery(Database.CONTENT_FOLDER_URI, FOLDER_PROJECTION, null, null, null);
		folderAdapter = new FolderAdapter(this, folderCursor);
		gallery.setAdapter(folderAdapter);
		gallery.setOnItemClickListener(this);
//		registerForContextMenu(getListView());
		getListView().setOnScrollListener(this);
//		final RetainNonConfigurationValue data = (RetainNonConfigurationValue) getLastNonConfigurationInstance();
//		mAdapter.clear();
//		if (data != null) {
//			mAdapter.appendList(data.list);
//			folderAdapter.setList(data.listFolder);
//		}
		loadActus();
		Cursor cursor = managedQuery(Database.CONTENT_ACTUS_URI, ACTUS_PROJECTION, null, null, null);
		adapter = new ItemAdapter(this, cursor);
		setListAdapter(adapter);
		loadFolder();
		isRunning = true;
		if (tracker == null) {
			trackUserInformation();
		}
	}
	
	@Override
	protected void onPause() {
		isRunning = false;
		super.onPause();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Cursor item = (Cursor) folderAdapter.getItem(position);
		Intent intent = new Intent(getApplicationContext(), FolderListActivity.class);
		intent.putExtra("link", getColumnString(item, Database._ID));
		intent.putExtra("title", getColumnString(item, Database.TITLE));
		startActivity(intent);
	}
	
	public String getColumnString(Cursor item, String columnName) {
		int index = item.getColumnIndex(columnName);
		if (index >= 0) {
			return item.getString(index);
		}
		return "";
	}

//	@Override
//	public Object onRetainNonConfigurationInstance() {
//		RetainNonConfigurationValue value = new RetainNonConfigurationValue();
////		value.list = mAdapter.getList(); //TODO
////		value.listFolder = folderAdapter.getList();
//		return value;
//	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		boolean loadMore = /* maybe add a padding */
		firstVisibleItem + visibleItemCount >= totalItemCount - 1;
//		Log.d("MESSESINFO onScroll:", "f=" + firstVisibleItem + ", vc=" + visibleItemCount + ", tc=" + totalItemCount);
		if (isRunning && loadMore) {
//			Log.d("MESSESINFO onScroll:", "loadMore");
			loadMore();
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (position < adapter.getCount()) {
			Cursor item = (Cursor) adapter.getItem(position - 1);
			Intent intent = new Intent(getApplicationContext(), DisplayWebPageActivity.class);
			intent.putExtra("link", getColumnString(item, Database._ID));
			intent.putExtra("title", getColumnString(item, Database.TITLE));
			startActivity(intent);
		}
	}

	private void loadActus() {
		loadActus(false);
	}

	public void onRefreshClick(View v) {
		loadActus();
		loadFolder();
	}

	private void setLoading(boolean loading) {
		if (loading)
			loadingCount++;
		else
			loadingCount--;
		isLoading = (loadingCount > 0);
		findViewById(R.id.title_refresh_progress).setVisibility(isLoading ? View.VISIBLE : View.INVISIBLE);
		// findViewById(R.id.btn_title_refresh).setVisibility(
		// loading ? View.GONE : View.VISIBLE);
	}

	private void loadMore() {
		if (!isLoading && !isSearchEnd) {
			Toast.makeText(this, R.string.loading, Toast.LENGTH_SHORT).show();
			loadActus(true);
		}
	}

	private void loadActus(final boolean loadMore) {
		setLoading(true);
		empty.setText(getString(R.string.loading));
		if (!loadMore) {
			//adapter.clear();
			isSearchEnd = false;
		}
		loadMoreView.setText(getText(R.string.loading));
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int pageSize = 10;
					if (!loadMore) {
						start = 0;
					}
					RSSReader reader = new RSSReader();
					String uri = "http://actus.catholique.fr/";
					RSSFeed feed = reader.load(uri);
					final List<RSSItem> list = feed.getItems();
					if (list != null) {
						start += list.size();
						isSearchEnd = list.size() < pageSize;
					}
					if (list != null && list.size() > 1) {
						for (RSSItem rssItem : list) {
							try {
								ContentValues values = new ContentValues();
								String link = rssItem.getLink().toString();
								values.put(Database._ID, link);
								values.put(Database.TITLE, rssItem.getTitle());
								values.put(Database.DESCRIPTION, rssItem.getDescription());
								values.put(Database.LINK, link);
								if (rssItem.getCategories().size() > 0) {
									values.put(Database.CATEGORY, rssItem.getCategories().get(0));
								} else {
									values.put(Database.CATEGORY, "");
								}
								if (rssItem.getThumbnails().size() > 0) {
									values.put(Database.THUMBNAILS, rssItem.getThumbnails().get(0).getUrl().toString());
								} else {
									values.put(Database.THUMBNAILS, "");
								}
								values.put(Database.DATE, rssItem.getPubDate().getTime());
								getContentResolver().insert(Database.CONTENT_ACTUS_URI, values);
							} catch (SQLException e) {

							}
						}
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setLoading(false);
						}
					});
//					runOnUiThread(new Runnable() {
//						@Override
//						public void run() {
//							if (list != null && list.size() > 1) {
//
//								//mAdapter.appendList(list);
//								if (loadMore) {
//									loadMoreView.setText(list.size() == 0 ? "" : getText(R.string.next));
//								}
//							} else {
//								empty.setText(getString(R.string.list_empty));
//							}
//							setLoading(false);
//						}
//
//					});
				} catch (Exception e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							empty.setText(R.string.error_loading_actus);
							setLoading(false);
						}
					});
				}

			}
		}).start();

	}

	private void loadFolder() {
		setLoading(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					RSSReader reader = new RSSReader();
					String uri = "http://iphone.catholique.fr/api/dossiers/";
					RSSFeed feed = reader.load(uri);
					final List<RSSItem> list = feed.getItems();
					getContentResolver().delete(Database.CONTENT_FOLDER_URI, null, null);
					for (RSSItem rssItem : list) {
						try {
							ContentValues values = new ContentValues();
							String link = rssItem.getLink().toString();
							Log.w("egliseactu", "add folder " + link);
							values.put(Database._ID, link);
							values.put(Database.TITLE, rssItem.getTitle());
							values.put(Database.LINK, link);
							if (rssItem.getThumbnails().size() > 0) {
								values.put(Database.THUMBNAILS, rssItem.getThumbnails().get(0).getUrl().toString());
							} else {
								values.put(Database.THUMBNAILS, "");
							}
//							values.put(Database.DATE, rssItem.getPubDate().getTime());
							getContentResolver().insert(Database.CONTENT_FOLDER_URI, values);
						} catch (SQLException e) {
	
						}
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
//							if (list != null && list.size() > 1) {
//								folderAdapter.setList(list);
//							}
							setLoading(false);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setLoading(false);
						}
					});
				}

			}
		}).start();

	}

//	@Override
//	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
//		if (v.getId() == android.R.id.list) {
//			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
//			Map<String, String> item = (Map<String, String>) mAdapter.getItem(info.position);
//			// menu.setHeaderTitle(item.get(Church.NAME));
//			// menu.add(Menu.NONE, MENU_DETAIL, Menu.NONE,
//			// R.string.menu_context_detail);
//			// menu.add(Menu.NONE, MENU_SCHEDULE, Menu.NONE,
//			// R.string.menu_context_schedules);
//			// menu.add(Menu.NONE, MENU_CENTER, Menu.NONE,
//			// R.string.menu_context_center);
//			// menu.add(Menu.NONE, MENU_NEAR, Menu.NONE,
//			// R.string.menu_context_near);
//			// menu.add(Menu.NONE, MENU_SCHEDULE_NEAR, Menu.NONE,
//			// R.string.menu_context_schedule_near);
//		}
//	}

//	@Override
//	public boolean onContextItemSelected(MenuItem menuItem) {
//		// AdapterView.AdapterContextMenuInfo info =
//		// (AdapterView.AdapterContextMenuInfo) menuItem
//		// .getMenuInfo();
//		// int menuItemIndex = menuItem.getItemId();
//		// Map<String, String> item = (Map<String, String>) mAdapter
//		// .getItem(info.position);
//		// String code = item.get(Church.ID);
//		// switch (menuItemIndex) {
//		// case MENU_DETAIL:
//		// ChurchActivity.activityStart(this, code);
//		// break;
//		// case MENU_SCHEDULE:
//		// ChurchActivity.activityStartSchedule(this, code);
//		// break;
//		// case MENU_CENTER:
//		// NearMapActivity.activityStart(this, item.get(Church.LAT),
//		// item.get(Church.LNG));
//		// break;
//		// case MENU_NEAR:
//		// searchText.setText("> " + item.get(Church.ZIPCODE));
//		// search("> " + item.get(Church.LAT) + ":" + item.get(Church.LNG));
//		// break;
//		// case MENU_SCHEDULE_NEAR:
//		// SearchScheduleActivity.activityStart(this, item.get(Church.LAT)
//		// + ":" + item.get(Church.LNG));
//		// default:
//		// break;
//		// }
//		return true;
//	}

	private void trackUserInformation() {
		setLoading(true);
		// Version
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					PackageManager pm = getPackageManager();
					PackageInfo pi;
					pi = pm.getPackageInfo(getPackageName(), 0);
					GoogleAnalyticsTracker tracker = getTracker(EgliseActuActivity.this);
					tracker.trackPageView("http://eglise.catholique.fr/");
					tracker.trackEvent("Application", "Version", pi.versionName, pi.versionCode);
					tracker.trackEvent("Android", "Device", Build.DEVICE, 1);
					tracker.trackEvent("Android", "Model", Build.MODEL, 1);
					tracker.trackEvent("Android", "Brand", Build.BRAND, 1);
					tracker.trackEvent("Android", "Product", Build.PRODUCT, 1);
					tracker.trackEvent("Android", "Display", Build.DISPLAY, 1);
					tracker.trackEvent("Android", "Board", Build.BOARD, 1);
					tracker.trackEvent("Android", "Version", Build.VERSION.RELEASE, 1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}).start();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean supRetVal = super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_ABOUT, 0, getString(R.string.main_menu_about)).setIcon(android.R.drawable.ic_menu_info_details);
		menu.add(0, MENU_CONTACT, 0, getString(R.string.main_menu_contact)).setIcon(android.R.drawable.ic_menu_send);
		menu.add(0, MENU_WEBSITE, 0, getString(R.string.main_menu_website)).setIcon(android.R.drawable.ic_menu_mapmode);
		menu.add(0, MENU_FRIENDAPP, 0, getString(R.string.main_menu_friendapps)).setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, MENU_CLEARCACHE, 0, getString(R.string.main_menu_clearcache)).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(0, MENU_SHARE, 0, getString(R.string.main_menu_share)).setIcon(android.R.drawable.ic_menu_share);
		return supRetVal;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_CONTACT:
			// Uri uri = Uri.parse("mailto://contact@messesinfo.cef.fr");
			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "android@cef.fr" });
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Contact depuis l'application egliseactu Android");
			// emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
			// "myBodyText");
			startActivity(Intent.createChooser(emailIntent, "Contact eglisecatu"));
			// startActivity(new Intent(Intent.ACTION_VIEW, uri));
			return true;
		case MENU_WEBSITE:
			final Intent urlIntent = new Intent(android.content.Intent.ACTION_VIEW);
			urlIntent.setData(Uri.parse(getString(R.string.egliseactu_url)));
			startActivity(urlIntent);
			return true;
		case MENU_ABOUT:
			final Intent aboutIntent = new Intent(this, AboutActivity.class);
			startActivity(aboutIntent);
			return true;
		case MENU_CLEARCACHE:
			setLoading(true);
			new Thread(new Runnable() {
				@Override
				public void run() {
					getContentResolver().delete(Database.CONTENT_FOLDER_URI, null, null);
					getContentResolver().delete(Database.CONTENT_ACTUS_URI, null, null);
					getContentResolver().delete(Database.CONTENT_WEBPAGE_URI, null, null);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setLoading(false);
							loadActus();
							loadFolder();
						}
					});
				}
			}).run();
			return true;
		case MENU_FRIENDAPP:
			Intent friendApp = new Intent(this, FriendListActivity.class);
			friendApp.putExtra("mail", "patou.de.saint.steban@gmail.com");
			friendApp.putExtra("listId", "messesinfo");
			startActivity(friendApp);
			return true;
		case MENU_SHARE:
			String text = getString(R.string.description_share);
			Intent shareIntent = new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, text).setType("text/plain").putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
			startActivityForResult(Intent.createChooser(shareIntent, getString(R.string.share_with)), 0);
			return true;
		default:
			break;
		}
		return false;
	}
	

	public void onShareClick(View v) {
		String text = getString(R.string.webpage_share);
		Intent i = new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_TEXT, text).setType("text/plain").putExtra(Intent.EXTRA_SUBJECT, getString(R.string.webpage_share_subject));
		startActivityForResult(Intent.createChooser(i, getString(R.string.share_with)), 0);
	}
	
	public void onSearchClick(View v) {
		final Intent intent = new Intent(this, SearchActivity.class);
		startActivity(intent);
	}
	
	static GoogleAnalyticsTracker tracker = null;
	public static GoogleAnalyticsTracker getTracker(Context context) {
		if (tracker == null) {
			tracker = GoogleAnalyticsTracker.getInstance();
			tracker.start("UA-27917653-1", 20, context); //TODO change UA ID
		}
		return tracker;
	}

	class RetainNonConfigurationValue {
		List<RSSItem> list;
		List<RSSItem> listFolder;
	}

}
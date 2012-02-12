package cef.egliseactu;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.SearchReader;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cef.egliseactu.adapters.RSSItemAdapter;

public class SearchActivity extends ListActivity implements
		OnScrollListener {
	Logger log = Logger.getLogger(SearchActivity.class.getName());
	private RSSItemAdapter mAdapter;
	private TextView empty;
	private boolean isLoading = false;
	private boolean isSearchEnd = false;
	private int start = 0;
	private int page = 0;
	//private TextView loadMoreView;
	private String uri;
	private EditText edittext;

	/** Called when the activity is first created. */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_list);
		empty = (TextView) findViewById(android.R.id.empty);
//		loadMoreView = new TextView(this);
//		loadMoreView.setText(getText(R.string.next));
//		loadMoreView.setWidth(LayoutParams.FILL_PARENT);
//		loadMoreView.setHeight(50);
//		loadMoreView.setTextAppearance(this,
//				android.R.attr.textAppearanceMedium);
//		loadMoreView.setGravity(Gravity.CENTER);
//		loadMoreView.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				loadMore();
//			}
//		});
//		getListView().addFooterView(loadMoreView);
		mAdapter = new RSSItemAdapter(this);
		setListAdapter(mAdapter);
		registerForContextMenu(getListView());
//		getListView().setOnScrollListener(this);
		final RetainNonConfigurationValue data = (RetainNonConfigurationValue) getLastNonConfigurationInstance();
		mAdapter.clear();
		if (data != null) {
			mAdapter.appendList(data.list);
		}
		edittext = (EditText) findViewById(R.id.title_text);
		edittext.setText(getIntent().getStringExtra("title"));
		//loadActus();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		RetainNonConfigurationValue value = new RetainNonConfigurationValue();
		value.list = mAdapter.getList();
		return value;
	}
	
	public void goHome(View v) {
		final Intent intent = new Intent(this, EgliseActuActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		boolean loadMore = /* maybe add a padding */
		firstVisibleItem + visibleItemCount >= totalItemCount - 1;
		Log.d("MESSESINFO onScroll:", "f=" + firstVisibleItem + ", vc="
				+ visibleItemCount + ", tc=" + totalItemCount);
		if (loadMore) {
			Log.d("MESSESINFO onScroll:", "loadMore");
			loadMore();
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (position < mAdapter.getCount()) {
			RSSItem item = (RSSItem) mAdapter
					.getItem(position);
			Intent intent = new Intent(getApplicationContext(),
					DisplayWebPageActivity.class);
			Uri link = item.getLink();
			if (link != null) {
				intent.putExtra("link", link.toString().replace("www.eglise.catholique.fr", "actus.catholique.fr"));
				startActivity(intent);
			}
		}
	}

	public void onSearchClick(View v) {
		loadSearch(false);
	}

	private void setLoading(boolean loading) {
		isLoading = loading;
		findViewById(R.id.title_refresh_progress).setVisibility(
				loading ? View.VISIBLE : View.GONE);
	}

	private void loadMore() {
		if (!isLoading && !isSearchEnd) {
			Toast.makeText(this, R.string.loading, Toast.LENGTH_SHORT).show();
			loadSearch(true);
		}
	}

	private void loadSearch(final boolean loadMore) {
		setLoading(true);
		empty.setText(getString(R.string.loading));
		if (!loadMore) {
			mAdapter.clear();
			isSearchEnd = false;
		}
		String search = edittext.getEditableText().toString();
		if (search == null)
			return;
		uri = getString(R.string.search_url, Uri.encode(search.trim()));
		EgliseActuActivity.getTracker(SearchActivity.this).trackPageView(uri);
		log.warning(uri);
	//	loadMoreView.setText(getText(R.string.loading));
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					int pageSize = 10;
					if (!loadMore) {
						start = 0;
					}
					SearchReader reader = new SearchReader();
					RSSFeed feed = reader.load(uri);
					final List<RSSItem> list = feed.getItems();
					if (list != null) {
						start += list.size();
						isSearchEnd = list.size() < pageSize;
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (list != null && list.size() > 1) {

								mAdapter.appendList(list);
								if (loadMore) {
//									loadMoreView.setText(list.size() == 0 ? ""
//											: getText(R.string.next));
								}
							} else {
								empty.setText(getString(R.string.list_empty));
							}
							setLoading(false);
						}

					});
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
	

	// private void showSelectionDepartementDialog() {
	// new AlertDialog.Builder( this )
	// .setTitle( "D�partements" )
	// .setItems(, , new DialogSelectionClickHandler() )
	// .setPositiveButton( "OK", new DialogButtonClickHandler() )
	// .create();
	// }

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == android.R.id.list) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			Map<String, String> item = (Map<String, String>) mAdapter.getItem(info.position);
			// menu.setHeaderTitle(item.get(Church.NAME));
			// menu.add(Menu.NONE, MENU_DETAIL, Menu.NONE,
			// R.string.menu_context_detail);
			// menu.add(Menu.NONE, MENU_SCHEDULE, Menu.NONE,
			// R.string.menu_context_schedules);
			// menu.add(Menu.NONE, MENU_CENTER, Menu.NONE,
			// R.string.menu_context_center);
			// menu.add(Menu.NONE, MENU_NEAR, Menu.NONE,
			// R.string.menu_context_near);
			// menu.add(Menu.NONE, MENU_SCHEDULE_NEAR, Menu.NONE,
			// R.string.menu_context_schedule_near);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {
		// AdapterView.AdapterContextMenuInfo info =
		// (AdapterView.AdapterContextMenuInfo) menuItem
		// .getMenuInfo();
		// int menuItemIndex = menuItem.getItemId();
		// Map<String, String> item = (Map<String, String>) mAdapter
		// .getItem(info.position);
		// String code = item.get(Church.ID);
		// switch (menuItemIndex) {
		// case MENU_DETAIL:
		// ChurchActivity.activityStart(this, code);
		// break;
		// case MENU_SCHEDULE:
		// ChurchActivity.activityStartSchedule(this, code);
		// break;
		// case MENU_CENTER:
		// NearMapActivity.activityStart(this, item.get(Church.LAT),
		// item.get(Church.LNG));
		// break;
		// case MENU_NEAR:
		// searchText.setText("> " + item.get(Church.ZIPCODE));
		// search("> " + item.get(Church.LAT) + ":" + item.get(Church.LNG));
		// break;
		// case MENU_SCHEDULE_NEAR:
		// SearchScheduleActivity.activityStart(this, item.get(Church.LAT)
		// + ":" + item.get(Church.LNG));
		// default:
		// break;
		// }

		return true;
	}

	class RetainNonConfigurationValue {
		List<RSSItem> list;
	}
}
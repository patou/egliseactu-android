package cef.egliseactu.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.mcsoxford.rss.RSSItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cef.egliseactu.R;

import com.loopj.android.image.SmartImageView;

public class RSSItemAdapter extends BaseAdapter {
	List<RSSItem> list = new ArrayList<RSSItem>();
	private ViewHolder holder;
	private LayoutInflater mInflater;
	private Context context;
	private Logger log = Logger.getLogger(RSSItemAdapter.class.getName());

	public RSSItemAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		this.context = context;
	}

	public List<RSSItem> getList() {
		return list;
	}
	
	public void clear() {
		list.clear();
		notifyDataSetChanged();
	}

	public void appendList(List<RSSItem> list) {
//		if (this.list == null) {
//			setList(list);
//		} else {
			this.list.addAll(list);
			notifyDataSetChanged();
//		}
	}

	@Override
	public int getCount() {
		return list != null ? list.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return list != null ? list.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_rss_item, null);

			holder = new ViewHolder();
			holder.thumbnail = (SmartImageView) convertView.findViewById(R.id.thumbnail);
			holder.title = (TextView) convertView.findViewById(R.id.title);
			holder.category = (TextView) convertView.findViewById(R.id.category);
			holder.description = (TextView) convertView
					.findViewById(R.id.description);
			holder.date = (TextView) convertView
					.findViewById(R.id.date);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		RSSItem item = list.get(position);
		if (item.getThumbnails().size() > 0) {
			log.warning(item.getThumbnails().get(0).getUrl().toString());
			holder.thumbnail.setImageUrl(item.getThumbnails().get(0).getUrl().toString(), android.R.drawable.gallery_thumb, R.anim.loader);
			holder.thumbnail.setVisibility(View.VISIBLE);
		}
		else {			
			holder.thumbnail.setVisibility(View.GONE);
		}
		holder.title.setText(item.getTitle());
		holder.description.setText(item.getDescription());
		if (item.getCategories().size() > 0) {
			holder.category.setText(item.getCategories().get(0));
		}
		else {
			holder.category.setText("");
		}
		Date date = item.getPubDate();
		if (date != null) {
			
			holder.date.setText(new SimpleDateFormat("d MMMM yyyy").format(date));
			holder.date.setVisibility(View.VISIBLE);
		} else {
			holder.date.setVisibility(View.GONE);
		}
		return convertView;
	}

	private static class ViewHolder {
		public TextView date;
		SmartImageView thumbnail;
		TextView title;
		TextView description;
		TextView category;
	}

}

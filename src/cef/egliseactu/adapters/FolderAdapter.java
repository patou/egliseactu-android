package cef.egliseactu.adapters;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import cef.egliseactu.R;
import cef.egliseactu.providers.Database;

import com.loopj.android.image.SmartImageView;

public class FolderAdapter extends CursorAdapter {

	private LayoutInflater mInflater;
	private ViewHolder holder;
	
	public FolderAdapter(Context context, Cursor c) {
		super(context, c);
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public void bindView(View view, Context context, Cursor item) {
		holder = (ViewHolder) view.getTag();
		bindItem(item);
	}

	private void bindItem(Cursor item) {
		String thumbnail = getColumnString(item, Database.THUMBNAILS);
		if (thumbnail.length() > 0) {
			holder.thumbnail.setImageUrl(thumbnail, android.R.drawable.gallery_thumb, R.anim.loader);
			holder.thumbnail.setVisibility(View.VISIBLE);
		}
		else {
			holder.thumbnail.setVisibility(View.GONE);
		}
		holder.title.setText(getColumnString(item, Database.TITLE));
	}

	@Override
	public View newView(Context context, Cursor item, ViewGroup parent) {
		View convertView = mInflater.inflate(R.layout.folder_item, null);
		
		holder = new ViewHolder();
		holder.thumbnail = (SmartImageView) convertView.findViewById(R.id.thumbnail);
		holder.title = (TextView) convertView.findViewById(R.id.title);

		convertView.setTag(holder);
		bindItem(item);
		return convertView;
	}
	
	public String getColumnString(Cursor item, String columnName) {
		int index = item.getColumnIndex(columnName);
		if (index >= 0) {
			return item.getString(index);
		}
		return "";
	}

	private static class ViewHolder {
		SmartImageView thumbnail;
		TextView title;
	}
}

package cef.egliseactu.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.loopj.android.image.SmartImageView;

public class ASmartImageView extends SmartImageView {

	public ASmartImageView(Context context, AttributeSet attribs, int defStyle) {
		super(context, attribs, defStyle);
	}

	public ASmartImageView(Context context, AttributeSet attribs) {
		super(context, attribs);
	}

	public ASmartImageView(Context context) {
		super(context);
	}
	
	@Override
	public void setImageResource(int resId) {
		super.setImageResource(resId);
		Drawable draw = getDrawable();
		if (draw instanceof AnimationDrawable) {
			((AnimationDrawable) draw).start();
		}
	}

}

/**
 * 
 */
package com.ace.gdufsassistant.interfacing.main;

import java.lang.ref.WeakReference;

import com.ace.gdufsassistant.R;
import com.ace.gdufsassistant.interfacing.specific.module.personal.center.UserInformationActivity;
import com.ace.gdufsassistant.util.TestNetworkState;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import entity.Information;
import main.API;

/**
 * @author wan
 *
 */
public class PersonalCenterFragment extends Fragment {
	private API api;
	private SharedPreferences defaultSP;

	private ImageView photoI;
	private TextView nameT;
	private TextView studentNumberT;
	private Handler handler = new MyHandler(this);
	private static class MyHandler extends Handler {
		private final WeakReference<PersonalCenterFragment> mpcf;
		private MyHandler(PersonalCenterFragment pcf) {
			mpcf = new WeakReference<PersonalCenterFragment>(pcf);
		}
		public void handleMessage(Message msg) {
			if (mpcf == null) return;
			PersonalCenterFragment pcf = mpcf.get();
			switch (msg.what) {
			case 0x123:
				try {
					Information information = pcf.api.getInformation();
					pcf.nameT.setText(information.getName());
					pcf.studentNumberT.setText(pcf.getResources().getString(R.string.student_number) + pcf.api.getStudentNumber());
				} catch (Exception e) {
				}
				break;
			case 0x124:
				try {
					Bitmap imgaeBm = UserInformationActivity.getPhoto();
					pcf.photoI.setImageBitmap(toRoundBitmap(imgaeBm));
				} catch(Exception e){
					UserInformationActivity.deletePhoto();
				}
				break;
			case 0x125:
				pcf.nameT.setText(R.string.not_logged2);
				pcf.studentNumberT.setText(R.string.student_number);
			case 0x126:
				pcf.photoI.setImageResource(R.drawable.not_logged_photo);
				break;
			}
		};
	};
	@Override
	public void onResume() {
		super.onResume();
		new Thread() {
			public void run() {
				TestNetworkState.waitForNetwork(getActivity());
				if (api.isLogged()) {
					try {
						api.getInformation();
					} catch (Exception e) {return;}
					handler.sendEmptyMessage(0x123);
					if (defaultSP.getBoolean("display_photo", true)) {
						try {
							UserInformationActivity.getPhoto();
							handler.sendEmptyMessage(0x124);
						} catch(Exception e){
							handler.sendEmptyMessage(0x126);
						}
					} else {
						handler.sendEmptyMessage(0x126);
					}
				} else {
					handler.sendEmptyMessage(0x125);
				}
			}
		}.start();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.personal_center_fragment, container, false);
		nameT = (TextView) view.findViewById(R.id.name);
		studentNumberT = (TextView) view.findViewById(R.id.student_number);
		photoI = (ImageView) view.findViewById(R.id.photo);
		api = API.getInstance();
		defaultSP = PreferenceManager.getDefaultSharedPreferences(getActivity());
		handler.sendEmptyMessage(0x125);
		return view;
	}

	private static Bitmap toRoundBitmap(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPx = width / 2;
			top = 0;
			bottom = width;
			left = 0;
			right = width;
			height = width;
			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;
			float clip = (width - height) / 2;
			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;
			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}

		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);

		paint.setAntiAlias(true);

		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint);
		return output;
	}
}

package com.example.tmappliction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

public class SelectPicPopupWindow extends PopupWindow {

	private TextView pickPhotoBtn, cancelBtn;
	private View mMenuView;

	@SuppressLint("InflateParams")
	public SelectPicPopupWindow(Context context, OnClickListener itemsOnClick) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.layout_dialog_pic, null);
		pickPhotoBtn = (TextView) mMenuView.findViewById(R.id.pickPhotoBtn);
		cancelBtn = (TextView) mMenuView.findViewById(R.id.cancelBtn);

		cancelBtn.setOnClickListener(itemsOnClick);
		pickPhotoBtn.setOnClickListener(itemsOnClick);

		this.setContentView(mMenuView);

		this.setWidth(LayoutParams.MATCH_PARENT);

		this.setHeight(LayoutParams.WRAP_CONTENT);

		this.setFocusable(true);

		this.setAnimationStyle(R.style.PopupAnimation);

		ColorDrawable dw = new ColorDrawable(0x80000000);

		this.setBackgroundDrawable(dw);

		mMenuView.setOnTouchListener(new OnTouchListener() {

			@Override
			@SuppressLint("ClickableViewAccessibility")
			public boolean onTouch(View v, MotionEvent event) {

				int height = mMenuView.findViewById(R.id.pop_layout).getTop();
				int y = (int) event.getY();
				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (y < height) {
						dismiss();
					}
				}
				return true;
			}
		});

	}

}

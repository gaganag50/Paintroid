package org.catrobat.paintroid.ui;

import android.animation.ObjectAnimator;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.catrobat.paintroid.MainActivity;
import org.catrobat.paintroid.NavigationDrawerMenuActivity;
import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.R;
import org.catrobat.paintroid.dialog.InfoDialog;
import org.catrobat.paintroid.tools.Tool;
import org.catrobat.paintroid.tools.ToolFactory;
import org.catrobat.paintroid.tools.ToolType;

public class BottomBar implements View.OnClickListener, View.OnLongClickListener {

	private static final int SWITCH_TOOL_TOAST_Y_OFFSET = (int) NavigationDrawerMenuActivity.ACTION_BAR_HEIGHT + 25;
	private static final boolean ENABLE_CENTER_SELECTED_TOOL = true;
	private static final boolean ENABLE_START_SCROLL_ANIMATION = true;

	private MainActivity mMainActivity;
	private LinearLayout mToolsLayout;
	private Tool mCurrentTool;
	private Toast mToolNameToast;

	private enum ActionType {
		BUTTON_CLICK, LONG_BUTTON_CLICK
	}

	public BottomBar(MainActivity mainActivity) {
		mMainActivity = mainActivity;
		mCurrentTool = ToolFactory.createTool(mainActivity, ToolType.BRUSH);
		PaintroidApplication.currentTool = mCurrentTool;
		mToolsLayout = (LinearLayout) mainActivity.findViewById(R.id.tools_layout);

		setBottomBarListener();

		if (ENABLE_START_SCROLL_ANIMATION) {
			startBottomBarAnimation();
		}
	}

	private void startBottomBarAnimation() {
		final HorizontalScrollView scrollView = (HorizontalScrollView) mMainActivity.findViewById(R.id.bottom_bar_scroll_view);
		scrollView.post(new Runnable() {
			public void run() {
				scrollView.setScrollX(scrollView.getChildAt(0).getRight());
				ObjectAnimator.ofInt(scrollView, "scrollX", 0).setDuration(1000).start();
			}
		});
	}

	private void setBottomBarListener() {
		for (int i = 0; i < mToolsLayout.getChildCount(); i++) {
			mToolsLayout.getChildAt(i).setOnClickListener(this);
			mToolsLayout.getChildAt(i).setOnLongClickListener(this);
		}

		setBottomBarScrollerListener();
	}

	private void setBottomBarScrollerListener() {
		final ImageView next = (ImageView) mMainActivity.findViewById(R.id.bottom_next);
		final ImageView previous = (ImageView) mMainActivity.findViewById(R.id.bottom_previous);

		((BottomBarHorizontalScrollView) mMainActivity.findViewById(R.id.bottom_bar_scroll_view))
				.setScrollStateListener(new BottomBarHorizontalScrollView.IScrollStateListener() {

			public void onScrollMostRight() {
				next.setVisibility(View.GONE);
			}

			public void onScrollMostLeft() {
				previous.setVisibility(View.GONE);
			}

			public void onScrollFromMostLeft() { previous.setVisibility(View.VISIBLE); }

			public void onScrollFromMostRight() { next.setVisibility(View.VISIBLE); }
		});
	}

	public void setTool(Tool tool) {
		mCurrentTool = tool;
		showToolChangeToast();
		resetActivatedButtons();
		getToolButtonByToolType(tool.getToolType()).setBackgroundResource(R.color.bottom_bar_button_activated);

		if (ENABLE_CENTER_SELECTED_TOOL) {
			scrollToSelectedTool(tool);
		}
	}

	private void scrollToSelectedTool(Tool tool) {
		HorizontalScrollView scrollView = (HorizontalScrollView) mMainActivity.findViewById(R.id.bottom_bar_scroll_view);
		scrollView.smoothScrollTo(
				(int) (getToolButtonByToolType(tool.getToolType()).getX() - scrollView.getWidth() / 2.0f
						+ mMainActivity.getResources().getDimension(R.dimen.bottom_bar_button_width) / 2.0f),
				(int) (getToolButtonByToolType(tool.getToolType()).getY()));
	}

	private void showToolChangeToast() {
		if (mToolNameToast != null) {
			mToolNameToast.cancel();
		}

		mToolNameToast = Toast.makeText(mMainActivity, mMainActivity.getString(mCurrentTool.getToolType().getNameResource()), Toast.LENGTH_SHORT);
		mToolNameToast.setGravity(Gravity.TOP | Gravity.RIGHT, 0, SWITCH_TOOL_TOAST_Y_OFFSET);
		mToolNameToast.show();
	}

	@Override
	public void onClick(View view) {
		performToolButtonAction(view, ActionType.BUTTON_CLICK);
	}

	@Override
	public boolean onLongClick(View view) {
		boolean longClickHandled = performToolButtonAction(view, ActionType.LONG_BUTTON_CLICK);
		return longClickHandled;
	}

	private boolean performToolButtonAction(View view, ActionType actionType) {
		ToolType toolType = null;

		for (ToolType type : ToolType.values()) {
			if (view.getId() == type.getToolButtonID()) {
				toolType = type;
				break;
			}
		}

		if (toolType == null) {
			return false;
		}
		else if (actionType == ActionType.BUTTON_CLICK) {
			if (PaintroidApplication.currentTool.getToolType() != toolType) {
				mMainActivity.switchTool(toolType);
			} else {
				PaintroidApplication.currentTool.toggleShowToolOptions();
			}
		}
		else if (actionType == ActionType.LONG_BUTTON_CLICK) {
			new InfoDialog(InfoDialog.DialogType.INFO, toolType.getHelpTextResource(),
					toolType.getNameResource()).show(
					mMainActivity.getSupportFragmentManager(),
					"helpdialogfragmenttag");
		}
		return true;
	}

	private ImageButton getToolButtonByToolType(ToolType toolType) {
		return (ImageButton) mMainActivity.findViewById(toolType.getToolButtonID());
	}

	private void resetActivatedButtons() {
		for (int i = 0; i < mToolsLayout.getChildCount(); i++) {
			mToolsLayout.getChildAt(i).setBackgroundResource(R.color.transparent);
		}
	}

}

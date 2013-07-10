package net.nexustools.jbitminingstats.util;

import net.nexustools.jbitminingstats.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

public class DialogHelper {
	private Context context;
	private boolean dialogDisplaying = false;
	
	public DialogHelper(Context context) {
		this.context = context;
	}
	
	public void create(int titleResId, int messageResId, boolean pos, boolean neu, boolean neg, int posResId, int neuResId, int negResId, final Runnable posRunnable, final Runnable neuRunnable, final Runnable negRunnable) {
		create(titleResId, context.getString(messageResId), pos, neu, neg, posResId, neuResId, negResId, posRunnable, neuRunnable, negRunnable);
	}
	
	public void create(int titleResId, String message, boolean pos, boolean neu, boolean neg, int posResId, int neuResId, int negResId, final Runnable posRunnable, final Runnable neuRunnable, final Runnable negRunnable) {
		dialogDisplaying = true;
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setTitle(titleResId);
		dialogBuilder.setMessage(message);
		dialogBuilder.setIcon(R.drawable.ic_launcher);
		DialogInterface.OnClickListener handler = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch(which) {
					case DialogInterface.BUTTON_POSITIVE:
						if(posRunnable != null)
							posRunnable.run();
						dialogDisplaying = false;
					break;
					case DialogInterface.BUTTON_NEUTRAL:
						if(neuRunnable != null)
							neuRunnable.run();
						dialogDisplaying = false;
					break;
					case DialogInterface.BUTTON_NEGATIVE:
						if(negRunnable != null)
							negRunnable.run();
						dialogDisplaying = false;
					break;
				}
			}
		};
		if(pos)
			dialogBuilder.setPositiveButton(posResId, handler);
		if(neu)
			dialogBuilder.setNeutralButton(neuResId, handler);
		if(neg)
			dialogBuilder.setNegativeButton(negResId, handler);
		dialogBuilder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialogDisplaying = false;
			}
		});
		dialogBuilder.create().show();
	}
	
	public boolean areAnyDialogsShowing() {
		return dialogDisplaying;
	}
}

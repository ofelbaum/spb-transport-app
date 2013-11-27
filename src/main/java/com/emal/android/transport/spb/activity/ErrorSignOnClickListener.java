package com.emal.android.transport.spb.activity;

import android.app.AlertDialog;
import android.os.Handler;
import android.view.View;
import com.emal.android.transport.spb.utils.UIHelper;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since 1.5
 */
public class ErrorSignOnClickListener implements View.OnClickListener{
    private View view;

    public ErrorSignOnClickListener(View view) {
        this.view = view;
    }

    @Override
    public void onClick(View v) {
        final AlertDialog dialog = UIHelper.getErrorDialog(view.getContext());
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                dialog.cancel();
                dialog.dismiss();
            }
        }, 5000);

        view.setVisibility(View.INVISIBLE);

    }
}

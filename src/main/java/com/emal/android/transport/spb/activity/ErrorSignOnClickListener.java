package com.emal.android.transport.spb.activity;

import android.app.AlertDialog;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

        builder.setTitle(com.emal.android.transport.spb.R.string.error_title);
        builder.setMessage(com.emal.android.transport.spb.R.string.server_error);
        builder.setPositiveButton(com.emal.android.transport.spb.R.string.ok, null);

        final AlertDialog dialog = builder.show();
        TextView messageText = (TextView) dialog.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);
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

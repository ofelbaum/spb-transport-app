package com.emal.android.transport.spb.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

/**
 * @author alexey.emelyanenko@gmail.com
 * @since: 1.5
 */
public class UIHelper {

    public static AlertDialog getErrorDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(com.emal.android.transport.spb.R.string.error_title);
        builder.setMessage(com.emal.android.transport.spb.R.string.server_error);
        builder.setPositiveButton(com.emal.android.transport.spb.R.string.ok, null);

        final AlertDialog dialog = builder.show();
        TextView messageText = (TextView) dialog.findViewById(android.R.id.message);
        messageText.setGravity(Gravity.CENTER);
        return dialog;
    }
}

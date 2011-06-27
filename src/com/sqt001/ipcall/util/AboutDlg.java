package com.sqt001.ipcall.util;

import android.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Show titled message dialg.
 * 
 * Usage:
 * 
 * AboutDlg about = new AboutDlg(Context);
 * about.show("Title", "Message');
 */
public class AboutDlg {
    private Context context;
    
    public AboutDlg(Context context) {
        this.context = context;
    }
    
    public void show(String title, String message) {
        show(title, message, null);
    }
    
    public void show(String title, String message, DialogInterface.OnClickListener listener) {
        final ScrollView view = getTextView(message);

        new AlertDialog.Builder(context)
        .setTitle(title)
        .setCancelable(true)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setPositiveButton(R.string.ok , listener)
        .setView(view)
        .create()
        .show();
    }

    private ScrollView getTextView(String message) {
        ScrollView svMessage = new ScrollView(context); 
        TextView tvMessage = new TextView(context);

        SpannableString spanText = new SpannableString(IOUtils.toUnixString(message));

        Linkify.addLinks(spanText, Linkify.ALL);
        tvMessage.setText(spanText);
        tvMessage.setMovementMethod(LinkMovementMethod.getInstance());

        svMessage.setPadding(14, 2, 10, 12);
        svMessage.addView(tvMessage);

        return svMessage;
    }
}

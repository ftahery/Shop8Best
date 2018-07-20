package company.shop8best.utils;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by dat9 on 18/02/18.
 */

public class ProgressDialogDisplay {

    public static ProgressDialog displayProgressDisplay(ProgressDialog progressDialog,Context context, String title, String message){
        if(progressDialog==null) {
            progressDialog = new ProgressDialog(context);
        }
        progressDialog.setMessage(message);
        progressDialog.setTitle(title);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
        return progressDialog;
    }

    public static void dismissProgressDialog(ProgressDialog progressDialog){
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}

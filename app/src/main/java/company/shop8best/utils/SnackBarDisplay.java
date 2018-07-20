package company.shop8best.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

/**
 * Created by dat9 on 15/02/18.
 */

public class SnackBarDisplay {

    public static void checkConnection(Context context, ConstraintLayout constraintLayout){
        boolean isConnected = ConnectivityReceiver.isConnected(context);
        showSnack(isConnected,constraintLayout);
    }

    public static void showSnack(boolean isConnected, ConstraintLayout constraintLayout){
        String message;
        int color;
        if(isConnected){
            message = "Good! Connected to internet";
            color = Color.WHITE;
        }
        else{
            message = "No internet connection. Please Retry";
            color = Color.RED;
        }

        Snackbar snackbar = Snackbar.make(constraintLayout,message,Snackbar.LENGTH_LONG)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        
                    }
                });

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

}

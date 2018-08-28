package company.shop8best;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import company.shop8best.model.UserAddresses;
import company.shop8best.utils.ConnectivityReceiver;
import company.shop8best.utils.HttpClientUtil;
import company.shop8best.constants.Constants;
import company.shop8best.utils.AccessTokenUtil;

/**
 * Created by dat9 on 03/02/18.
 */

public class AddressPage extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = "AddressPage";

    BootstrapEditText user_name;
    BootstrapEditText user_contact_number;
    BootstrapEditText user_area;
    BootstrapEditText user_block;
    BootstrapEditText user_street;
    BootstrapEditText user_jedda;
    BootstrapEditText user_house;
    BootstrapEditText user_floor;
    BootstrapEditText user_other_contact_info;
    BootstrapButton select_delivery_button;

    List<Integer> pincodes;

    UserAddresses userAddresses;
    SignInPage signInPage;
    AccessTokenUtil accessTokenUtil;
    String scope = "oauth2: profile email";
    String accessToken;
    String checkMethod = null;
    boolean isConnected;
    ConstraintLayout constraintLayout;
    boolean isNew = true;
    ConnectivityReceiver connectivityReceiver;
    Snackbar snackbar;

    @Override
    protected void onResume() {
        super.onResume();
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        connectivityReceiver = new ConnectivityReceiver();
        registerReceiver(connectivityReceiver, intentFilter);
        MyApplication.getInstance().setConnectivityListener(this);
        isConnected = ConnectivityReceiver.isConnected(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(connectivityReceiver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        checksBeforeBack();

    }

    private void checksBeforeBack() {
        if (SecurityCacheMapService.INSTANCE.exists("accessToken") && isConnected) {
            accessToken = SecurityCacheMapService.INSTANCE.get("accessToken");
            if (checkForAllEntries()) {
                if (!isNew) {
                    updateAddress(accessToken);
                }
                finish();
            }
        } else if (isConnected && !SecurityCacheMapService.INSTANCE.exists("accessToken")) {
            if (!isNew) {
                new GetTokenTask().execute("updateAddress");
            }
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                checksBeforeBack();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_address);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_edit_address);
        toolbar.setTitleTextColor(Color.rgb(228, 127, 49));
        setSupportActionBar(toolbar);

        setTitle("Edit address");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        constraintLayout = (ConstraintLayout) findViewById(R.id.edit_address_constraint_layout);

        userAddresses = new UserAddresses();

        userAddresses = (UserAddresses) getIntent().getSerializableExtra("USER_ADDRESS");

        user_name = (BootstrapEditText) findViewById(R.id.user_name);
        user_contact_number = (BootstrapEditText) findViewById(R.id.user_contact_number);
        user_area = (BootstrapEditText) findViewById(R.id.user_area);
        user_block = (BootstrapEditText) findViewById(R.id.user_block);
        user_street = (BootstrapEditText) findViewById(R.id.user_street);
        user_jedda = (BootstrapEditText) findViewById(R.id.user_jedda);
        user_house = (BootstrapEditText) findViewById(R.id.user_house);
        user_floor = (BootstrapEditText) findViewById(R.id.user_floor);
        user_other_contact_info = (BootstrapEditText) findViewById(R.id.user_other_contact_info);
        select_delivery_button = (BootstrapButton) findViewById(R.id.select_delivery_button);

        isConnected = ConnectivityReceiver.isConnected(getApplicationContext());

        if (userAddresses != null && isConnected) {
            setTextForAddressField();
            isNew = false;
        }

        select_delivery_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SecurityCacheMapService.INSTANCE.exists("accessToken") && isConnected) {
                    accessToken = SecurityCacheMapService.INSTANCE.get("accessToken");
                    if (checkForAllEntries()) {
                        updateAddress(accessToken);
                        Intent checkoutPage = new Intent(AddressPage.this, CheckoutPage.class);
                        checkoutPage.putExtra("USER_ADDRESS", userAddresses);
                        startActivity(checkoutPage);
                    }
                } else if (isConnected && !SecurityCacheMapService.INSTANCE.exists("accessToken")) {
                    new GetTokenTask().execute("updateAddress");
                }
            }
        });
    }

    private void setTextForAddressField() {
        user_name.setText(userAddresses.getUser_name());
        user_contact_number.setText(userAddresses.getUser_contact_number());
        user_area.setText(userAddresses.getUser_area());
        user_block.setText(userAddresses.getUser_block());
        user_street.setText(userAddresses.getUser_street());
        user_jedda.setText(userAddresses.getUser_jedda());
        user_area.setText(userAddresses.getUser_area());
        user_floor.setText(userAddresses.getUser_floor());
        user_other_contact_info.setText(userAddresses.getUser_other_contact_info());
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        this.isConnected = isConnected;

        if (!isConnected) {
            notConnectedToInternet();
        } else {
            select_delivery_button.setEnabled(true);
            if (snackbar != null && snackbar.isShown()) {
                snackbar.dismiss();
                startActivity(getIntent());
                finish();
            }
        }
    }

    private void notConnectedToInternet() {
        select_delivery_button.setEnabled(false);
        snackbar = Snackbar.make(constraintLayout, "No internet connection. Please retry", Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(getIntent());
                        finish();
                    }
                });
        snackbar.setActionTextColor(Color.RED);
        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    class GetTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            signInPage = new SignInPage();
            accessTokenUtil = new AccessTokenUtil();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                accessToken = GoogleAuthUtil.getToken(getApplicationContext(), signInPage.getAccount(), scope);
                SecurityCacheMapService.INSTANCE.putToCache("accessToken", accessToken, accessTokenUtil.getTokenExpiryTime(accessToken));
                checkMethod = params[0];
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String acxessToken) {
            super.onPostExecute(accessToken);
            runOnPostExecute(accessToken);
        }
    }

    private void runOnPostExecute(String accessToken) {

        switch (checkMethod) {
            case "updateAddress":
                if (checkForAllEntries()) {
                    updateAddress(accessToken);
                    Intent checkoutPage = new Intent(AddressPage.this, CheckoutPage.class);
                    checkoutPage.putExtra("USER_ADDRESS", userAddresses);
                    startActivity(checkoutPage);
                }
                break;

            default:
                break;
        }
    }

    private void updateAddress(String accessToken) {

        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", accessToken);
        headers.put("SIGNIN", SignInPage.getSignedInUsing());

        userAddresses.setUser_name(user_name.getText().toString());
        userAddresses.setUser_area(user_area.getText().toString());
        userAddresses.setUser_block(user_block.getText().toString());
        userAddresses.setUser_street(user_street.getText().toString());
        userAddresses.setUser_jedda(user_jedda.getText().toString());
        userAddresses.setUser_house(user_house.getText().toString());
        userAddresses.setUser_floor(user_floor.getText().toString());
        userAddresses.setUser_contact_number(user_contact_number.getText().toString());
        userAddresses.setUser_other_contact_info(user_other_contact_info.getText().toString());

        String responseBody = HttpClientUtil.updateAddress(Constants.SERVER_URL + Constants.UPDATE_USER_ADDRESS, headers, userAddresses);

        try {
            JSONObject response = new JSONObject(responseBody);
            userAddresses.setAddress_id(response.getInt("address_id"));
            Log.i(TAG, "THIS IS THE ADDRESS ID RECEIVED: " + userAddresses.getAddress_id());
            if ("Success".equals(response.getString("message"))) {
                Log.d(TAG, "Successfully updated the address");
                Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT);
            } else {
                Log.d(TAG, "Could not update the address");
            }
        } catch (JSONException e) {
            Log.d(TAG, "Error occured while parsing the json " + e.getMessage());
        }

    }

    public boolean checkForAllEntries() {

        if (user_jedda.getText().toString().isEmpty()) {
            user_jedda.setText("-");
        }

        if (user_floor.getText().toString().isEmpty()) {
            user_floor.setText("-");
        }

        if (user_other_contact_info.getText().toString().isEmpty()) {
            user_other_contact_info.setText("-");
        }

        if (user_name.getText().toString().isEmpty()) {
            user_name.setBootstrapBrand(DefaultBootstrapBrand.DANGER);
            Toast.makeText(getApplicationContext(), "Please fill the name", Toast.LENGTH_SHORT);
            return false;
        }

        if (user_contact_number.getText().toString().isEmpty()) {
            user_contact_number.setBootstrapBrand(DefaultBootstrapBrand.DANGER);
            Toast.makeText(getApplicationContext(), "Please provide contact number", Toast.LENGTH_SHORT);
            return false;
        }

        if (user_area.getText().toString().isEmpty()) {
            user_area.setBootstrapBrand(DefaultBootstrapBrand.DANGER);
            Toast.makeText(getApplicationContext(), "Please fill building details", Toast.LENGTH_SHORT);
            return false;
        }

        if (user_block.getText().toString().isEmpty()) {
            user_block.setBootstrapBrand(DefaultBootstrapBrand.DANGER);
            Toast.makeText(getApplicationContext(), "Please fill street details", Toast.LENGTH_SHORT);
            return false;
        }

        if (user_street.getText().toString().isEmpty()) {
            user_street.setBootstrapBrand(DefaultBootstrapBrand.DANGER);
            Toast.makeText(getApplicationContext(), "Please fill street details", Toast.LENGTH_SHORT);
            return false;
        }

        if (user_house.getText().toString().isEmpty()) {
            user_house.setBootstrapBrand(DefaultBootstrapBrand.DANGER);
            Toast.makeText(getApplicationContext(), "Please provide country", Toast.LENGTH_SHORT);
            return false;
        }

        if (userAddresses == null) {
            userAddresses = new UserAddresses();
        }

        userAddresses.setUser_name(user_name.getText().toString());
        userAddresses.setUser_contact_number(user_contact_number.getText().toString());
        userAddresses.setUser_area(user_area.getText().toString());
        userAddresses.setUser_block(user_block.getText().toString());
        userAddresses.setUser_street(user_street.getText().toString());
        userAddresses.setUser_jedda(user_jedda.getText().toString());
        userAddresses.setUser_house(user_house.getText().toString());
        userAddresses.setUser_floor(user_floor.getText().toString());
        userAddresses.setUser_other_contact_info(user_other_contact_info.getText().toString());


        return true;
    }
}

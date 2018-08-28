package company.shop8best;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import company.shop8best.utils.ConnectivityReceiver;
import company.shop8best.adapters.AddressSelectionAdapter;
import company.shop8best.constants.Constants;
import company.shop8best.model.UserAddresses;
import company.shop8best.utils.AccessTokenUtil;
import company.shop8best.utils.HttpClientUtil;
import company.shop8best.utils.ProgressDialogDisplay;

/**
 * Created by dat9 on 30/01/18.
 */

public class AddressSelection extends AppCompatActivity implements AbsListView.OnScrollListener, AbsListView.OnItemClickListener, ConnectivityReceiver.ConnectivityReceiverListener {

    BootstrapButton addNewAddress;
    public static final String TAG = "AddressSelection";
    AddressSelectionAdapter addressSelectionAdapter = null;

    ExpandableListView expandableListView;

    private boolean mHasRequestedMore;
    SignInPage signInPage;
    AccessTokenUtil accessTokenUtil;
    String scope = "oauth2: profile email";
    String accessToken;
    ProgressDialog progressDialog;
    UserAddresses[] userAddresses;
    BottomNavigationView bottomNavigationView;
    boolean isConnected;
    ConstraintLayout constraintLayout;
    ConnectivityReceiver connectivityReceiver;
    public static Context context;
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

        if (isConnected) {
            if (SecurityCacheMapService.INSTANCE.exists("accessToken")) {
                accessToken = SecurityCacheMapService.INSTANCE.get("accessToken");
                ArrayList<UserAddresses> addressList = generateData();
                getAddressList(addressList);

            } else {
                new GetTokenTask().execute();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(connectivityReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        Log.d(TAG, "HEYYYYY THIS IS THE ITEM ID :" + item.getItemId());
        switch (item.getItemId()) {

            case android.R.id.home:
                if (isConnected) {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProgressDialogDisplay.dismissProgressDialog(progressDialog);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.address_selection);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_address);
        setTitle("Address");
        toolbar.setTitleTextColor(Color.rgb(228, 127, 49));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        context = this;

        expandableListView = (ExpandableListView) findViewById(R.id.address_selection_elv);

        addNewAddress = (BootstrapButton) findViewById(R.id.add_new_address);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bot_nav_bar);
        constraintLayout = (ConstraintLayout) findViewById(R.id.address_selection_constraint_layout);

        bottomNavigationView.getMenu().findItem(R.id.home_bottom_nav).setCheckable(false);
        bottomNavigationView.getMenu().findItem(R.id.account_bottom_nav).setCheckable(false);
        isConnected = ConnectivityReceiver.isConnected(getApplicationContext());

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_bottom_nav:
                        if (isConnected) {
                            Intent goToProductListingPage = new Intent(AddressSelection.this, ProductListingPage.class);
                            goToProductListingPage.putExtra("SIGNEDIN", true);
                            startActivity(goToProductListingPage);
                        }
                        return true;

                    case R.id.account_bottom_nav:
                        if (isConnected) {
                            Intent goToAccountPage = new Intent(AddressSelection.this, AccountPage.class);
                            startActivity(goToAccountPage);
                        }
                        return true;
                }
                return true;
            }
        });

        addNewAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    startActivity(new Intent(AddressSelection.this, AddressPage.class));
                }
            }
        });

        if (isConnected) {
            new GetTokenTask().execute();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        Log.d(TAG, "onScrollStateChanged:" + scrollState);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Log.d(TAG, "onScroll firstVisibleItem:" + firstVisibleItem +
                " visibleItemCount:" + visibleItemCount +
                " totalItemCount:" + totalItemCount);
        // our handling
        if (!mHasRequestedMore) {
            int lastInScreen = firstVisibleItem + visibleItemCount;
            if (lastInScreen >= totalItemCount) {
                Log.d(TAG, "onScroll lastInScreen - so load more");
                mHasRequestedMore = true;

            }
        }
    }

    public ArrayList<UserAddresses> generateData() {
        ArrayList<UserAddresses> addressList = new ArrayList<>();
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", accessToken);
        headers.put("SIGNIN", SignInPage.getSignedInUsing());
        String userAddressesBody = HttpClientUtil.stringResponseForGetRequest(Constants.SERVER_URL + Constants.USER_ADDRESSES, headers);

        Gson gson = new Gson();
        userAddresses = gson.fromJson(userAddressesBody, UserAddresses[].class);

        for (UserAddresses userAddress : userAddresses) {
            addressList.add(userAddress);
        }
        return addressList;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        this.isConnected = isConnected;

        if (!isConnected) {
            notConnectedToInternet();
        } else {
            expandableListView.setEnabled(true);
            expandableListView.setVisibility(View.VISIBLE);
            addNewAddress.setEnabled(true);
            if (snackbar != null && snackbar.isShown()) {
                snackbar.dismiss();
                startActivity(getIntent());
                finish();
            }
        }

    }

    private void notConnectedToInternet() {
        expandableListView.setEnabled(false);
        expandableListView.setVisibility(View.GONE);
        addNewAddress.setEnabled(false);
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

    class GetTokenTask extends AsyncTask<String, Void, ArrayList<UserAddresses>> {

        ArrayList<UserAddresses> addressList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            signInPage = new SignInPage();
            accessTokenUtil = new AccessTokenUtil();
            progressDialog = ProgressDialogDisplay.displayProgressDisplay(progressDialog, AddressSelection.this, "Address", "Fetching addresses...");
        }

        @Override
        protected ArrayList<UserAddresses> doInBackground(String... params) {
            try {
                if (SecurityCacheMapService.INSTANCE.exists("accessToken")) {
                    accessToken = SecurityCacheMapService.INSTANCE.get("accessToken");
                } else {
                    accessToken = GoogleAuthUtil.getToken(getApplicationContext(), signInPage.getAccount(), scope);
                    SecurityCacheMapService.INSTANCE.putToCache("accessToken", accessToken, accessTokenUtil.getTokenExpiryTime(accessToken));
                }

                if (accessToken != null) {
                    addressList = new ArrayList<>();
                    addressList = generateData();
                    return addressList;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<UserAddresses> addressList) {
            super.onPostExecute(addressList);
            progressDialog.dismiss();
            getAddressList(addressList);
        }
    }

    private void getAddressList(ArrayList<UserAddresses> addressList) {
        addressSelectionAdapter = new AddressSelectionAdapter(getApplicationContext(), addressList);
        expandableListView.setAdapter(addressSelectionAdapter);

    }


}

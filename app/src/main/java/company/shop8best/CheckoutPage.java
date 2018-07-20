package company.shop8best;

import android.app.ProgressDialog;
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
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import company.shop8best.utils.ConnectivityReceiver;
import company.shop8best.adapters.CheckoutAdapter;
import company.shop8best.constants.Constants;
import company.shop8best.model.CartItem;
import company.shop8best.model.OrderRequest;
import company.shop8best.model.OrderedItemResponse;
import company.shop8best.model.UserAddresses;
import company.shop8best.utils.AccessTokenUtil;
import company.shop8best.utils.HttpClientUtil;
import company.shop8best.utils.ProgressDialogDisplay;

/**
 * Created by dat9 on 03/02/18.
 */

public class CheckoutPage extends AppCompatActivity implements AbsListView.OnScrollListener, AbsListView.OnItemClickListener, ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = "CheckoutPage";
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    GridView gridView;
    SignInPage signInPage;
    AccessTokenUtil accessTokenUtil;
    String scope = "oauth2: profile email";
    String accessToken;
    private boolean mHasRequestedMore;
    private ArrayList<CartItem> mData = null;
    String checkMethod = null;
    ConstraintLayout constraintLayout;
    OrderRequest orderRequest;

    CartItem[] cartItems;
    OrderedItemResponse[] orderedItemResponses;
    CheckoutAdapter checkoutAdapter;
    boolean isConnected;

    TextView delivery_address;
    TextView total_price;
    BootstrapButton place_order;
    UserAddresses userAddresses;
    ProgressDialog progressDialog;
    NumberFormat formatter = new DecimalFormat("#0.00");
    ConnectivityReceiver connectivityReceiver;
    BottomNavigationView bottomNavigationView;
    Snackbar snackbar;
    BootstrapEditText user_description;

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
    protected void onStop() {
        super.onStop();
        unregisterReceiver(connectivityReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProgressDialogDisplay.dismissProgressDialog(progressDialog);
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkout_page);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_checkout);
        setTitle("Checkout");
        toolbar.setTitleTextColor(Color.rgb(228, 127, 49));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        delivery_address = (TextView) findViewById(R.id.delivery_address);
        total_price = (TextView) findViewById(R.id.total_price);
        place_order = (BootstrapButton) findViewById(R.id.place_order);
        constraintLayout = (ConstraintLayout) findViewById(R.id.constraint_layout);
        gridView = (GridView) findViewById(R.id.grid_view_checkout);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bot_nav_bar);
        user_description = (BootstrapEditText) findViewById(R.id.user_description);

        userAddresses = (UserAddresses) getIntent().getSerializableExtra("USER_ADDRESS");

        String completeAddress = userAddresses.getUser_name() + ", House - " + userAddresses.getUser_house() + ", Floor - " + userAddresses.getUser_floor()
                + ", Block - " + userAddresses.getUser_block() + ", Street - " + userAddresses.getUser_street() + ", Jedda - " + userAddresses.getUser_jedda()
                + ", Area - " + userAddresses.getUser_area();

        delivery_address.setText(completeAddress);
        isConnected = ConnectivityReceiver.isConnected(getApplicationContext());

        if (isConnected) {
            new GetTokenTask().execute();
        }

        bottomNavigationView.getMenu().findItem(R.id.home_bottom_nav).setCheckable(false);
        bottomNavigationView.getMenu().findItem(R.id.account_bottom_nav).setCheckable(false);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_bottom_nav:
                        if (isConnected) {
                            Intent goToProductListingPage = new Intent(CheckoutPage.this, ProductListingPage.class);
                            goToProductListingPage.putExtra("SIGNEDIN", true);
                            startActivity(goToProductListingPage);
                        }
                        return true;

                    case R.id.account_bottom_nav:
                        if (isConnected) {
                            Intent goToAccountPage = new Intent(CheckoutPage.this, AccountPage.class);
                            startActivity(goToAccountPage);
                        }
                        return true;
                }
                return true;
            }
        });

        place_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected) {
                    orderRequest = new OrderRequest();
                    String order_date = ZonedDateTime.now().toString();
                    Log.d(TAG, "This is the order date ------> " + order_date);

                    if (user_description.getText().toString().isEmpty()) {
                        orderRequest.setUser_description("-");
                    } else {
                        orderRequest.setUser_description(user_description.getText().toString());
                    }

                    orderRequest.setAddress_id(userAddresses.getAddress_id());
                    orderRequest.setOrder_date(order_date);
                    Log.i(TAG, "THIS IS ADDRESS ID: " + orderRequest.getAddress_id());

                    if (SecurityCacheMapService.INSTANCE.exists("accessToken")) {
                        accessToken = SecurityCacheMapService.INSTANCE.get("accessToken");
                        placeOrder(orderRequest);
                    } else {
                        new GetTokenTaskForOrder().execute("placeOrder");
                    }
                }
            }
        });
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

    private ArrayList<CartItem> generateData() {
        ArrayList<CartItem> cartList = new ArrayList<>();
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", accessToken);
        String cartItemsBody = HttpClientUtil.stringResponseForGetRequest(Constants.SERVER_URL + Constants.CART_ITEMS_ENDPOINT, headers);

        Gson gson = new Gson();
        cartItems = gson.fromJson(cartItemsBody, CartItem[].class);

        for (CartItem cartItem : cartItems) {
            cartList.add(cartItem);
        }
        return cartList;
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
            gridView.setEnabled(true);
            place_order.setEnabled(true);
            if (snackbar != null && snackbar.isShown()) {
                snackbar.dismiss();
                startActivity(getIntent());
                finish();
            }
        }
    }

    private void notConnectedToInternet() {
        gridView.setEnabled(false);
        place_order.setEnabled(false);
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

    class GetTokenTask extends AsyncTask<Void, Void, ArrayList<CartItem>> {

        ArrayList<CartItem> cartList;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            signInPage = new SignInPage();
            accessTokenUtil = new AccessTokenUtil();
            progressDialog = ProgressDialogDisplay.displayProgressDisplay(progressDialog, CheckoutPage.this, "Checkout", "Please wait...");
        }

        @Override
        protected ArrayList<CartItem> doInBackground(Void... params) {
            try {
                if (SecurityCacheMapService.INSTANCE.exists("accessToken")) {
                    accessToken = SecurityCacheMapService.INSTANCE.get("accessToken");
                } else {
                    accessToken = GoogleAuthUtil.getToken(getApplicationContext(), signInPage.getAccount(), scope);
                    SecurityCacheMapService.INSTANCE.putToCache("accessToken", accessToken, accessTokenUtil.getTokenExpiryTime(accessToken));
                }

                if (accessToken != null) {
                    cartList = new ArrayList<>();
                    cartList = generateData();
                    Log.d(TAG, "Here is the length of cart and accessToken" + cartList.size() + " " + accessToken);
                    return cartList;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<CartItem> cartList) {
            super.onPostExecute(cartList);
            getCartList(cartList);
            progressDialog.dismiss();
        }
    }

    class GetTokenTaskForOrder extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ProgressDialogDisplay.displayProgressDisplay(progressDialog, CheckoutPage.this, "", "Loading, please wait ... ");
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                accessToken = GoogleAuthUtil.getToken(getApplicationContext(), signInPage.getAccount(), scope);
                SecurityCacheMapService.INSTANCE.putToCache("accessToken", accessToken, accessTokenUtil.getTokenExpiryTime(accessToken));
                checkMethod = params[0];
                if (accessToken != null) {
                    return accessToken;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String accessToken) {
            super.onPostExecute(accessToken);
            runOnPostExecute(accessToken);
            progressDialog.dismiss();
        }
    }

    private void runOnPostExecute(String accessToken) {

        switch (checkMethod) {
            case "placeOrder":
                placeOrder(orderRequest);
                break;

            default:
                break;
        }
    }

    private void getCartList(ArrayList<CartItem> cartList) {
        Log.d(TAG, "Size of the cart list -----> " + cartList.size());
        checkoutAdapter = new CheckoutAdapter(CheckoutPage.this, android.R.layout.simple_list_item_1, cartList);
        if (mData == null) {
            mData = new ArrayList<>();
            for (int i = 0; i < cartList.size(); i++) {
                mData.add(cartList.get(i));
            }
        }

        double totalPrice = 0;
        for (CartItem item : cartList) {
            totalPrice += (item.getItem_price() * item.getItem_quantity());
            Log.d(TAG, "TOTAL PRICE ===> " + totalPrice);
        }
        total_price.setText(formatter.format(totalPrice) + " KD");

        for (Iterator<CartItem> data = mData.iterator(); data.hasNext(); ) {
            checkoutAdapter.add(data.next());
        }

        gridView.setAdapter(checkoutAdapter);
        gridView.setOnScrollListener(CheckoutPage.this);
        gridView.setOnItemClickListener(CheckoutPage.this);
    }

    private void placeOrder(OrderRequest orderRequest) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", accessToken);

        String responseBody = HttpClientUtil.placeOrder(Constants.SERVER_URL + Constants.PLACE_ORDER, headers, orderRequest);

        try {
            JSONObject response = new JSONObject(responseBody);
            if (response.getBoolean("message")) {
                Log.d(TAG, "Successfully Order placed");
                Toast.makeText(this, "Your order is successfully placed", Toast.LENGTH_SHORT).show();
                /*Snackbar snackbar = Snackbar
                        .make(constraintLayout, "Your order is successfully placed", Snackbar.LENGTH_SHORT);
                snackbar.show();*/
                Intent intent = new Intent(this, ProductListingPage.class);
                intent.putExtra("SIGNEDIN", true);
                startActivity(intent);
            } else {
                Log.d(TAG, "Error occured while placing the order ");
                Toast.makeText(this, "Sorry, could not place your order. Please try again", Toast.LENGTH_SHORT).show();
                /*Snackbar snackbar = Snackbar
                        .make(constraintLayout, "Could not place your order", Snackbar.LENGTH_SHORT);
                snackbar.show();*/
            }
        } catch (JSONException e) {
            Log.d(TAG, "Error occured while parsing the json " + e.getMessage());
        }
    }
}

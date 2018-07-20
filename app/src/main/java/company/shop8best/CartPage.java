package company.shop8best;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import company.shop8best.adapters.CartAdapter;
import company.shop8best.utils.ConnectivityReceiver;
import company.shop8best.utils.HttpClientUtil;
import company.shop8best.constants.Constants;
import company.shop8best.fonts.FontManager;
import company.shop8best.model.CartItem;
import company.shop8best.utils.AccessTokenUtil;
import company.shop8best.utils.ProgressDialogDisplay;

/**
 * Created by dat9 on 01/12/17.
 */

public class CartPage extends AppCompatActivity implements AbsListView.OnScrollListener, AbsListView.OnItemClickListener, ConnectivityReceiver.ConnectivityReceiverListener {

    public static final String TAG = "CartPage";
    public static final String SAVED_DATA_KEY = "SAVED_DATA";

    GridView gridView;
    CartAdapter cartAdapter = null;
    private boolean mHasRequestedMore;
    private ArrayList<CartItem> mData = null;
    CartItem[] cartItems;
    SignInPage signInPage;
    AccessTokenUtil accessTokenUtil;
    String scope = "oauth2: profile email";
    String accessToken;
    ProgressDialog progressDialog;
    BottomNavigationView bottomNavigationView;

    public static TextView cartTotalPrice;
    BootstrapButton checkoutButton;
    ConstraintLayout constraintLayout;
    boolean isConnected;
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
    protected void onStop() {
        super.onStop();
        unregisterReceiver(connectivityReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProgressDialogDisplay.dismissProgressDialog(progressDialog);
        Log.d(TAG,"HELLO I'M ***********  onDestroy() ******* Cart Page  ******");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.checkout,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        Log.d(TAG,"HEYYYYY THIS IS THE ITEM ID :" +item.getItemId());
        switch(item.getItemId()){

            case android.R.id.home:
                if(isConnected) {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_cart);
        setTitle("My Cart");
        toolbar.setTitleTextColor(Color.rgb(228,127,49));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.cart_page_constraint_layout), iconFont);

        constraintLayout = (ConstraintLayout) findViewById(R.id.cart_page_constraint_layout);

        gridView = (GridView) findViewById(R.id.grid_view_cart);
        cartTotalPrice = (TextView) findViewById(R.id.cart_total_price);
        checkoutButton = (BootstrapButton) findViewById(R.id.cart_checkout_button);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bot_nav_bar);

        bottomNavigationView.getMenu().findItem(R.id.home_bottom_nav).setCheckable(false);
        bottomNavigationView.getMenu().findItem(R.id.account_bottom_nav).setCheckable(false);
        isConnected = ConnectivityReceiver.isConnected(getApplicationContext());

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.home_bottom_nav:
                        if(isConnected) {
                            Intent goToProductListingPage = new Intent(CartPage.this, ProductListingPage.class);
                            goToProductListingPage.putExtra("SIGNEDIN", true);
                            startActivity(goToProductListingPage);
                        }
                        return true;

                    case R.id.account_bottom_nav:
                        if(isConnected) {
                            Intent goToAccountPage = new Intent(CartPage.this, AccountPage.class);
                            startActivity(goToAccountPage);
                        }
                        return true;
                }
                return true;
            }
        });

        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(cartItems!=null && cartItems.length>0) {
                    startActivity(new Intent(CartPage.this, AddressSelection.class));
                }
            }
        });

        if(isConnected) {
            progressDialog = new ProgressDialog(this);
            new GetTokenTask().execute("getCartList");
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

    private ArrayList<CartItem> generateData() {
        ArrayList<CartItem> cartList = new ArrayList<>();
        HashMap<String,String> headers = new HashMap<>();
        headers.put("Authorization",accessToken);
        String cartItemsBody = HttpClientUtil.stringResponseForGetRequest(Constants.SERVER_URL+Constants.CART_ITEMS_ENDPOINT,headers);

        Gson gson = new Gson();
        cartItems = gson.fromJson(cartItemsBody,CartItem[].class);

        for(CartItem cartItem : cartItems) {
            cartList.add(cartItem);
        }
        return cartList;
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        this.isConnected = isConnected;

        if(!isConnected){
            notConnectedToInternet();
        }
        else{
            gridView.setEnabled(true);
            gridView.setVisibility(View.VISIBLE);
            checkoutButton.setEnabled(true);
            if(snackbar!=null && snackbar.isShown()){
                snackbar.dismiss();
                startActivity(getIntent());
                finish();
            }
        }
    }

    private void notConnectedToInternet() {
        gridView.setEnabled(false);
        gridView.setVisibility(View.GONE);
        checkoutButton.setEnabled(false);
        snackbar = Snackbar.make(constraintLayout,"No internet connection. Please retry",Snackbar.LENGTH_INDEFINITE)
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    class GetTokenTask extends AsyncTask<String,Void,ArrayList<CartItem>> {

        ArrayList<CartItem> cartList;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            signInPage = new SignInPage();
            accessTokenUtil = new AccessTokenUtil();
            ProgressDialogDisplay.displayProgressDisplay(progressDialog,CartPage.this,"Cart","Fetching cart...");
        }

        @Override
        protected ArrayList<CartItem> doInBackground(String... params) {
            try {
                if(SecurityCacheMapService.INSTANCE.exists("accessToken")){
                    accessToken = SecurityCacheMapService.INSTANCE.get("accessToken");
                }
                else {
                    accessToken = GoogleAuthUtil.getToken(getApplicationContext(), signInPage.getAccount(), scope);
                    SecurityCacheMapService.INSTANCE.putToCache("accessToken", accessToken, accessTokenUtil.getTokenExpiryTime(accessToken));
                }

                if(accessToken!=null) {
                    cartList = new ArrayList<>();
                    cartList = generateData();
                    Log.d(TAG,"Here is the length of cart and accessToken" + cartList.size() + " " + accessToken);
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
            progressDialog.dismiss();
            getCartList(cartList);
        }
    }

    private void getCartList(ArrayList<CartItem> cartList) {

        cartAdapter = new CartAdapter(CartPage.this,android.R.layout.simple_list_item_1,cartList);
        if (mData == null) {
            mData = new ArrayList<>();
            for(int i=0;i<cartList.size();i++){
                mData.add(cartList.get(i));
            }
        }

        for(Iterator<CartItem> data = mData.iterator(); data.hasNext();){
            cartAdapter.add(data.next());
        }

        gridView.setAdapter(cartAdapter);
        gridView.setOnScrollListener(CartPage.this);
        gridView.setOnItemClickListener(CartPage.this);
    }

}

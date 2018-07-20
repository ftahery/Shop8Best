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

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import company.shop8best.adapters.AccountPageAdapter;
import company.shop8best.constants.Constants;
import company.shop8best.model.OrderedItemResponse;
import company.shop8best.utils.AccessTokenUtil;
import company.shop8best.utils.ConnectivityReceiver;
import company.shop8best.utils.HttpClientUtil;
import company.shop8best.utils.ProgressDialogDisplay;

/**
 * Created by dat9 on 21/01/18.
 */

public class AccountPage extends AppCompatActivity implements View.OnClickListener ,AbsListView.OnScrollListener, AbsListView.OnItemClickListener, ConnectivityReceiver.ConnectivityReceiverListener{

    private static final String TAG  = "AccountPage";

    SignInPage signInPage;
    GoogleSignInAccount googleSignInAccount;
    GoogleSignInClient googleSignInClient;
    AccountPageAdapter accountPageAdapter = null;
    private boolean mHasRequestedMore;
    private ArrayList<OrderedItemResponse> mData = null;
    AccessTokenUtil accessTokenUtil;
    String scope = "oauth2: profile email";
    String accessToken;
    ProgressDialog progressDialog;
    BottomNavigationView bottomNavigationView;
    boolean isConnected;
    ConstraintLayout constraintLayout;

    GridView gridView;
    TextView user_name;
    TextView user_email;
    BootstrapButton signOut;
    OrderedItemResponse orders[];
    ConnectivityReceiver connectivityReceiver;
    Snackbar snackbar;

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
    protected void onDestroy() {
        super.onDestroy();
        ProgressDialogDisplay.dismissProgressDialog(progressDialog);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(connectivityReceiver);
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
        setContentView(R.layout.account_page);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_account);
        setTitle("Account");
        toolbar.setTitleTextColor(Color.rgb(228,127,49));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        signInPage = new SignInPage();
        googleSignInAccount = SignInPage.getGoogleSignInAccount();
        googleSignInClient = SignInPage.getGoogleSignInClient();
        constraintLayout = (ConstraintLayout) findViewById(R.id.account_page_constraint_layout) ;

        gridView = (GridView) findViewById(R.id.account_page_grid_view);
        user_name = (TextView) findViewById(R.id.user_name);
        user_email = (TextView) findViewById(R.id.user_email);
        signOut = (BootstrapButton) findViewById(R.id.sign_out_button);

        signInPage = new SignInPage();

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bot_nav_bar);
        isConnected = ConnectivityReceiver.isConnected(getApplicationContext());

        bottomNavigationView.getMenu().findItem(R.id.home_bottom_nav).setCheckable(false);
        bottomNavigationView.getMenu().findItem(R.id.account_bottom_nav).setChecked(true);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.home_bottom_nav:
                        if(isConnected) {
                            Intent goToProductListingPage = new Intent(AccountPage.this, ProductListingPage.class);
                            goToProductListingPage.putExtra("SIGNEDIN", true);
                            startActivity(goToProductListingPage);
                        }
                        return true;

                    case R.id.account_bottom_nav:
                        return true;
                }
                return true;
            }
        });



        user_name.setText(googleSignInAccount.getDisplayName());
        user_email.setText(googleSignInAccount.getEmail());

        if(isConnected) {
            progressDialog = new ProgressDialog(this);
            signOut.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.sign_out_button:
                if(isConnected) {
                    signOut();
                    signInPage.setGoogleSignInClient(googleSignInClient);
                    Intent intent = new Intent(this, SignInPage.class);
                    startActivity(intent);
                }
        }
    }

    private void signOut() {
        googleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });
    }

    private ArrayList<OrderedItemResponse> generateData() {
        ArrayList<OrderedItemResponse> orderList = new ArrayList<>();
        HashMap<String,String> headers = new HashMap<>();
        headers.put("Authorization",accessToken);

        String responseBody = HttpClientUtil.stringResponseForGetRequest(Constants.SERVER_URL+Constants.GET_PAST_ORDERS,headers);

        Gson gson = new Gson();
        orders = gson.fromJson(responseBody,OrderedItemResponse[].class);

        for(OrderedItemResponse order : orders) {
            orderList.add(order);
        }

        if(orderList!=null){
            Collections.sort(orderList, new Comparator<OrderedItemResponse>() {
                @Override
                public int compare(OrderedItemResponse o1, OrderedItemResponse o2) {
                    ZonedDateTime date1 = ZonedDateTime.parse(o1.getOrder_date());
                    ZonedDateTime date2 = ZonedDateTime.parse(o2.getOrder_date());
                    return date2.compareTo(date1);
                }
            });
        }

        return orderList;

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        this.isConnected = isConnected;

        if(!isConnected){
            notConnectedToInternet();
        }
        else{
            gridView.setEnabled(true);
            signOut.setEnabled(true);
            if(snackbar!=null && snackbar.isShown()){
                snackbar.dismiss();
                startActivity(getIntent());
                finish();
            }
        }
    }

    private void notConnectedToInternet() {
        gridView.setEnabled(false);
        signOut.setEnabled(false);
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

    class GetTokenTask extends AsyncTask<Void,Void,ArrayList<OrderedItemResponse>>{

        ArrayList<OrderedItemResponse> orderedItemsList;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            signInPage = new SignInPage();
            accessTokenUtil = new AccessTokenUtil();
            ProgressDialogDisplay.displayProgressDisplay(progressDialog,AccountPage.this,"Orders","Fetching orders...");
        }

        @Override
        protected ArrayList<OrderedItemResponse> doInBackground(Void... params) {
            try {
                if(SecurityCacheMapService.INSTANCE.exists("accessToken")){
                    accessToken = SecurityCacheMapService.INSTANCE.get("accessToken");
                }
                else {
                    accessToken = GoogleAuthUtil.getToken(getApplicationContext(), signInPage.getAccount(), scope);
                    SecurityCacheMapService.INSTANCE.putToCache("accessToken", accessToken, accessTokenUtil.getTokenExpiryTime(accessToken));
                }

                if(accessToken!=null) {
                    orderedItemsList = new ArrayList<>();
                    orderedItemsList = generateData();
                    Log.d(TAG,"Here is the length of orders and accessToken" + orderedItemsList.size() + " " + accessToken);
                    return orderedItemsList;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<OrderedItemResponse> ordersList) {
            super.onPostExecute(ordersList);
            getOrderList(ordersList);
            progressDialog.dismiss();
        }
    }

    private void getOrderList(ArrayList<OrderedItemResponse> ordersList) {
        accountPageAdapter = new AccountPageAdapter(AccountPage.this,android.R.layout.simple_list_item_1,ordersList);
        if (mData == null) {
            mData = new ArrayList<>();
            for(int i=0;i<ordersList.size();i++){
                mData.add(ordersList.get(i));
            }
        }

        Iterator<OrderedItemResponse> data = mData.iterator();
        while(data.hasNext()){
            accountPageAdapter.add(data.next());
        }


        for(OrderedItemResponse orderedItemResponse : ordersList){
            Log.d(TAG, "HERE ARE THE ITEMS-----> " + orderedItemResponse.getItem_name() + " " + orderedItemResponse.getOrder_date());
        }

        gridView.setAdapter(accountPageAdapter);
        gridView.setOnScrollListener(AccountPage.this);
        gridView.setOnItemClickListener(AccountPage.this);
    }

}

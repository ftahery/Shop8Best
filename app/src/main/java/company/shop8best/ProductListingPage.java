package company.shop8best;

/**
 * Created by dat9 on 05/11/17.
 */

import android.accounts.Account;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.gson.Gson;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import company.shop8best.adapters.ItemTypeAdapter;
import company.shop8best.adapters.ProductListingAdapter;
import company.shop8best.constants.Constants;
import company.shop8best.model.Item;
import company.shop8best.model.MenuModel;
import company.shop8best.utils.AccessTokenUtil;
import company.shop8best.utils.ConnectivityReceiver;
import company.shop8best.utils.HttpClientUtil;
import company.shop8best.utils.ProgressDialogDisplay;

import static android.view.View.GONE;

public class ProductListingPage extends AppCompatActivity implements AbsListView.OnScrollListener,
        AbsListView.OnItemClickListener, ConnectivityReceiver.ConnectivityReceiverListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "ProductListingPage";
    private static final String SAVED_DATA_KEY = "SAVED_DATA";
    private GridView mGridView;
    private boolean mHasRequestedMore;
    private ProductListingAdapter mAdapter = null;
    BottomNavigationView bottomNavigationView;
    MenuItem menuItem;
    Account account;
    SignInPage signInPage = null;
    GoogleSignInClient googleSignInClient;
    private Context context = this;
    ConstraintLayout constraintLayout;

    private ArrayList<Item> mData = null;
    int i = 0;
    private int count = 0;
    MultiMap multiImageUrl;

    Item[] items;
    String accessToken;
    String email;
    private PopupWindow popUpWindow;
    ArrayList<Item> listData = new ArrayList<>();
    ProgressDialog progressDialog;
    String scope = "oauth2: profile email";
    ConnectivityReceiver connectivityReceiver;
    boolean isConnected;
    boolean allProductsSelected;

    AccessTokenUtil accessTokenUtil;
    String checkMethod = null;
    boolean isSignedIn;
    Snackbar snackbar;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private View navHeader;
    List<MenuModel> headerList = new ArrayList<>();
    HashMap<MenuModel, List<MenuModel>> childList = new HashMap<>();
    private ExpandableListView expandableListView;
    private ItemTypeAdapter itemTypeAdapter;
    TextView nav_header_user_name;

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.d(TAG, "HELLO I'M ***********  onRestart() *************");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "HELLO I'M ***********  onStart() *************");
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(connectivityReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        connectivityReceiver = new ConnectivityReceiver();
        registerReceiver(connectivityReceiver, intentFilter);

        MyApplication.getInstance().setConnectivityListener(this);
        isConnected = ConnectivityReceiver.isConnected(getApplicationContext());

        if (bottomNavigationView != null) {
            bottomNavigationView.getMenu().findItem(R.id.home_bottom_nav).setChecked(true);
        }

        if (isSignedIn && isConnected) {
            if (SecurityCacheMapService.INSTANCE.exists("accessToken")) {
                accessToken = SecurityCacheMapService.INSTANCE.get("accessToken");
                Log.d(TAG, "ASYNC TASK IS NOT CALLED !!!!!!");
                if (menuItem != null) {
                    getCartCount(accessToken);
                }
            } else {
                Log.d(TAG, "I'M ASYNC TASK  FOR GETTING CartCount in onResume !!!!!!");
                new GetTokenTask().execute("getCartCount");
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProgressDialogDisplay.dismissProgressDialog(progressDialog);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Log.d(TAG, "HELLO I'M ***********  onCreateOptionsMenu() *************");

        getMenuInflater().inflate(R.menu.product_listing_page, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_icon_listing_page).getActionView();
        //searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        menuItem = menu.findItem(R.id.cart_button_listing_page);
        menuItem.setIcon(buildCounterDrawable(count, R.drawable.cart_icon));


        if (isSignedIn && isConnected) {
            if (SecurityCacheMapService.INSTANCE.exists("accessToken")) {
                accessToken = SecurityCacheMapService.INSTANCE.get("accessToken");
                Log.d(TAG, "ASYNC TASK IS NOT CALLED !!!!!!!");
                getCartCount(accessToken);
            } else {
                new GetTokenTask().execute("getCartCount");
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {

            case R.id.nav_home:
                return true;

            case R.id.nav_past_orders:
                Intent intent = new Intent(this, AccountPage.class);
                startActivity(intent);
                return true;

            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;

            case R.id.cart_button_listing_page:

                if (isSignedIn && isConnected) {
                    Intent goToCartPage = new Intent(this, CartPage.class);
                    startActivity(goToCartPage);
                } else if (isConnected) {
                    Intent goToSignInPage = new Intent(this, SignInPage.class);
                    goToSignInPage.putExtra("GOBACK", "CartPage");
                    startActivity(goToSignInPage);
                }
                return true;

            case R.id.search_icon_listing_page:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.navigation_drawer);

        Log.d(TAG, "HELLO I'M ***********  onCreate() *************");

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        implementToolbar();

        navigationView = (NavigationView) findViewById(R.id.navigation_drawer_view);
        expandableListView = (ExpandableListView) findViewById(R.id.nav_expandableList);

        prepareMenuData();
        populateExpandableList();

        navigationView.setNavigationItemSelectedListener(this);
        constraintLayout = (ConstraintLayout) findViewById(R.id.productListingPage);

        signInPage = new SignInPage();
        isSignedIn = getIntent().getBooleanExtra("SIGNEDIN", false);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bot_nav_bar);

        bottomNavigationView.getMenu().findItem(R.id.home_bottom_nav).setChecked(true);
        bottomNavigationView.getMenu().findItem(R.id.account_bottom_nav).setCheckable(false);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_bottom_nav:
                        return true;

                    case R.id.account_bottom_nav:
                        if (isSignedIn) {
                            Intent intent = new Intent(ProductListingPage.this, AccountPage.class);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(ProductListingPage.this, SignInPage.class);
                            intent.putExtra("GOBACK", "AccountPage");
                            startActivity(intent);
                        }
                        return true;
                }
                return true;
            }
        });


        setTitle("Products");
        mGridView = (GridView) findViewById(R.id.grid_view);

        isConnected = ConnectivityReceiver.isConnected(getApplicationContext());
        if (!isConnected) {
            notConnectedToInternet();
        } else {
            getAllItems();
        }


        navHeader = navigationView.getHeaderView(0);
        nav_header_user_name = navHeader.findViewById(R.id.nav_header_user_name);
        nav_header_user_name.setText("Hello, " + WordUtils.capitalizeFully(signInPage.getGoogleSignInAccount().getDisplayName()));

    }

    private void implementToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_listing_page);
        toolbar.setTitleTextColor(Color.rgb(228, 127, 49));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        /*
        for(HashMap<String,String> data: mData) {
            for(Map.Entry<String,String> entrySet : data.entrySet()){
                outState.putString(SAVED_DATA_KEY, entrySet.getValue());
            }

        }
        */
        //outState.putStringArrayList(SAVED_DATA_KEY, mData);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.navigation_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onScrollStateChanged(final AbsListView view, final int scrollState) {
        Log.d(TAG, "onScrollStateChanged:" + scrollState);
    }


    @Override
    public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
        Log.d(TAG, "onScroll firstVisibleItem:" + firstVisibleItem +
                " visibleItemCount:" + visibleItemCount +
                " totalItemCount:" + totalItemCount);
        // our handling
        if (!mHasRequestedMore) {
            int lastInScreen = firstVisibleItem + visibleItemCount;
            if (lastInScreen >= totalItemCount) {
                Log.d(TAG, "onScroll lastInScreen - so load more");
                mHasRequestedMore = true;
                //onLoadMoreItems();
            }
        }
    }

    private ArrayList<Item> generateData() {
        multiImageUrl = new MultiValueMap();

        try {
            String image_url_body = HttpClientUtil.stringResponseForGetRequest(Constants.SERVER_URL + Constants.IMAGE_URL_ENDPOINT, null);
            JSONArray itemImagesJson = new JSONArray(image_url_body);

            for (int i = 0; i < itemImagesJson.length(); i++) {
                JSONObject itemImage = itemImagesJson.getJSONObject(i);
                int item_id = itemImage.getInt("item");
                String item_image_url = itemImage.getString("item_image");
                item_image_url = Constants.SERVER_URL + item_image_url;
                multiImageUrl.put(Integer.toString(item_id), item_image_url);
            }

            String items_body = HttpClientUtil.stringResponseForGetRequest(Constants.SERVER_URL + Constants.ITEMS_ENDPOINT, null);

            Gson gson = new Gson();
            items = gson.fromJson(items_body, Item[].class);

            listData.clear();

            if (items.length > 0) {
                for (Item item : items) {
                    listData.add(item);
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "Error occurred while parsing the json in generateData : " + e.getMessage());
        }
        return listData;
    }

    private ArrayList<Item> generateDataForItemByType(String itemType) {
        multiImageUrl = new MultiValueMap();

        String items_body = HttpClientUtil.stringResponseForGetRequest(Constants.SERVER_URL + Constants.ITEM_BY_TYPE + itemType, null);

        Gson gson = new Gson();
        items = gson.fromJson(items_body, Item[].class);

        listData.clear();

        if (items.length > 0) {
            for (Item item : items) {
                listData.add(item);
            }
        }
        return listData;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Item item = (Item) adapterView.getItemAtPosition(position);

        ArrayList<String> imageUrls;

        imageUrls = (ArrayList) multiImageUrl.get(Integer.toString(item.getItem_id()));

        //Toast.makeText(this, "Item Clicked: " + item.getItem_id(), Toast.LENGTH_LONG).show();
        Intent productDescription = new Intent(ProductListingPage.this, ProductDescription.class);

        productDescription.putExtra("ITEM", item);
        productDescription.putStringArrayListExtra("ITEM_IMAGE_URLS", imageUrls);
        productDescription.putExtra("CART_COUNT", count);
        productDescription.putExtra("SIGNEDIN", isSignedIn);
        startActivity(productDescription);
    }

    private Drawable buildCounterDrawable(int count, int backgroundImageId) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.counter_menu_item, null);
        view.setBackgroundResource(backgroundImageId);

        if (count == 0) {
            View counterTextPanel = view.findViewById(R.id.counterValuePanel);
            counterTextPanel.setVisibility(GONE);
        } else {
            TextView textView = (TextView) view.findViewById(R.id.count);
            textView.setText("" + count);
        }

        view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);

        return new BitmapDrawable(getResources(), bitmap);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

        Log.d(TAG, "Current status of network ------> " + isConnected);
        this.isConnected = isConnected;
        if (!isConnected) {
            notConnectedToInternet();
        } else {
            mGridView.setEnabled(true);
            if (snackbar != null && snackbar.isShown()) {
                snackbar.dismiss();
                startActivity(getIntent());
                finish();
            }
        }
    }

    private void notConnectedToInternet() {
        mGridView.setEnabled(false);
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

    private void prepareMenuData() {

        MenuModel menuModel = new MenuModel("Home", true, false);
        headerList.add(menuModel);

        if (!menuModel.hasChildren) {
            childList.put(menuModel, null);
        }

        menuModel = new MenuModel("Type", true, true);
        headerList.add(menuModel);
        List<MenuModel> childModelsList = new ArrayList<>();

        MenuModel childModel = new MenuModel("Ring", false, false);
        childModelsList.add(childModel);

        childModel = new MenuModel("Bracelet", false, false);
        childModelsList.add(childModel);

        childModel = new MenuModel("Necklace", false, false);
        childModelsList.add(childModel);

        childModel = new MenuModel("Chain", false, false);
        childModelsList.add(childModel);

        childModel = new MenuModel("Earring", false, false);
        childModelsList.add(childModel);

        childModel = new MenuModel("Anklet", false, false);
        childModelsList.add(childModel);


        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }

        menuModel = new MenuModel("Your orders", true, false);
        headerList.add(menuModel);


        if (!menuModel.hasChildren) {
            childList.put(menuModel, null);
        }
    }

    private void populateExpandableList() {

        itemTypeAdapter = new ItemTypeAdapter(this, headerList, childList);
        expandableListView.setAdapter(itemTypeAdapter);

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                if (headerList.get(groupPosition).isGroup) {
                    switch (headerList.get(groupPosition).menuName) {
                        case "Home":
                            drawerLayout.closeDrawers();
                            if (!allProductsSelected) {
                                getAllItems();
                            }
                            return true;

                        case "Your orders":
                            drawerLayout.closeDrawers();
                            Intent accountPage = new Intent(getApplicationContext(), AccountPage.class);
                            startActivity(accountPage);
                            return true;
                    }
                }

                return false;
            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if (childList.get(headerList.get(groupPosition)) != null) {
                    MenuModel model = childList.get(headerList.get(groupPosition)).get(childPosition);

                    if ("Type".equals(headerList.get(groupPosition).menuName)) {
                        getItemsByType(model.menuName.toLowerCase());
                    }
                    onBackPressed();
                }

                return false;
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.navigation_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
                checkMethod = params[0];
                return checkMethod;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(String checkMethod) {
            super.onPostExecute(checkMethod);
            SecurityCacheMapService.INSTANCE.putToCache("accessToken", accessToken, accessTokenUtil.getTokenExpiryTime(accessToken));
            if (SecurityCacheMapService.INSTANCE.exists("accessToken")) {
                runPostExecuteMethod(accessToken);
            }
        }
    }

    private void runPostExecuteMethod(String accessToken) {

        switch (checkMethod) {

            case "getCartCount":
                getCartCount(accessToken);
                break;

            default:
                break;

        }
    }

    private void getProductTaskOnPost(ArrayList<Item> items) {
        if (mData == null) {
            mData = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                mData.add(items.get(i));
            }
        }

        for (Iterator<Item> data = mData.iterator(); data.hasNext(); ) {
            mAdapter.add(data.next());
        }
        mGridView.setAdapter(mAdapter);
        mGridView.setOnScrollListener(ProductListingPage.this);
        mGridView.setOnItemClickListener(ProductListingPage.this);
    }

    private void getAllItems() {
        allProductsSelected = true;
        new AsyncTask<Void, Void, ArrayList<Item>>() {

            @Override
            protected void onPreExecute() {
                progressDialog = ProgressDialogDisplay.displayProgressDisplay(progressDialog, ProductListingPage.this, "Products", "Fetching products...");
            }

            @Override
            protected ArrayList<Item> doInBackground(Void... params) {
                listData = generateData();
                mAdapter = new ProductListingAdapter(ProductListingPage.this, android.R.layout.simple_list_item_1, listData);
                mAdapter.setNotifyOnChange(true);
                return listData;

            }

            @Override
            protected void onPostExecute(ArrayList<Item> items) {

                if (ProductListingPage.this.isDestroyed()) {
                    return;
                }
                Log.d(TAG, "I'M ASYNC TASK FOR JUST INITIATING EVERYTHING ....... !!!");
                getProductTaskOnPost(items);
                progressDialog.dismiss();
            }
        }.execute();
    }

    private void getItemsByType(String itemType) {
        allProductsSelected = false;
        new AsyncTask<Void, Void, ArrayList<Item>>() {

            @Override
            protected void onPreExecute() {
                progressDialog = ProgressDialogDisplay.displayProgressDisplay(progressDialog, ProductListingPage.this, "Products", "Fetching products...");
            }

            @Override
            protected ArrayList<Item> doInBackground(Void... params) {
                listData = generateDataForItemByType(itemType);
                mAdapter = new ProductListingAdapter(ProductListingPage.this, android.R.layout.simple_list_item_1, listData);
                mAdapter.setNotifyOnChange(true);
                return listData;

            }

            @Override
            protected void onPostExecute(ArrayList<Item> items) {

                if (ProductListingPage.this.isDestroyed()) {
                    return;
                }
                Log.d(TAG, "I'M ASYNC TASK FOR JUST INITIATING EVERYTHING ....... !!!");
                getProductTaskOnPost(items);
                progressDialog.dismiss();
            }
        }.execute();
    }

    private void getCartCount(String accessToken) {
        final HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", accessToken);

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                String cartCountBody = HttpClientUtil.stringResponseForGetRequest(Constants.SERVER_URL + Constants.CART_COUNT, headers);
                Log.d(TAG, "here is the count cart body " + cartCountBody);
                return cartCountBody;
            }

            @Override
            protected void onPostExecute(String cartCountBody) {
                super.onPostExecute(cartCountBody);
                try {
                    JSONObject cartCount = new JSONObject(cartCountBody);

                    count = cartCount.getInt(signInPage.getAccount().name);

                    if (menuItem != null) {
                        menuItem.setIcon(buildCounterDrawable(count, R.drawable.cart_icon));
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Error occurred while json parsing in getting the cart count: " + e.getMessage());
                }
            }
        }.execute();
    }
}

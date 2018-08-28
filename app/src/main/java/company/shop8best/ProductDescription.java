package company.shop8best;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.api.defaults.DefaultBootstrapBrand;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;
import com.weiwangcn.betterspinner.library.BetterSpinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import company.shop8best.constants.Constants;
import company.shop8best.model.AddToCartRequest;
import company.shop8best.model.Item;
import company.shop8best.utils.AccessTokenUtil;
import company.shop8best.utils.ConnectivityReceiver;
import company.shop8best.utils.HttpClientUtil;
import company.shop8best.utils.ProgressDialogDisplay;

import static android.view.View.GONE;

/**
 * Created by dat9 on 19/11/17.
 */

public class ProductDescription extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = "ProductDescriptionPage";

    TextView item_name_tv;
    TextView item_color_tv;
    TextView item_carat_tv;
    TextView item_price_tv;
    TextView item_weight;
    TextView item_size_tag;
    BootstrapButton addToCart;
    BottomNavigationView bottomNavigationView;
    CarouselView carouselView;
    private Spinner itemSizeTypeSpinner;
    private Spinner itemSizeSpinner;
    private boolean doubleBackToExitPressedOnce = false;

    private ArrayList<String> item_image_urls;
    Item item;
    MenuItem menuItem;
    String accessToken;
    ProgressDialog progressDialog;
    NumberFormat formatter = new DecimalFormat("#0.00");
    NumberFormat weightFormatter = new DecimalFormat("#0.0000");

    private ScaleGestureDetector scaleGestureDetector;
    private Matrix matrix = new Matrix();
    public static ImageView imageView;
    public SecurityService securityService;
    public AccessTokenUtil accessTokenUtil;

    private int count = 0;
    private boolean addedToCart;
    SignInPage signInPage;
    String scope = "oauth2: profile email";
    String checkMethod = null;
    boolean isSignedIn;
    ConnectivityReceiver connectivityReceiver;
    boolean isConnected;
    ConstraintLayout constraintLayout;
    Snackbar snackbar;
    double item_size;
    String item_size_type;

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "HELLO I'M ***********  onRestart() ******* Product Description  ******");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "HELLO I'M ***********  onStart() ******* Product Description  ******");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "HELLO I'M ***********  onResume() ******* Product Description  ******");
        signInPage = new SignInPage();

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        connectivityReceiver = new ConnectivityReceiver();
        registerReceiver(connectivityReceiver, intentFilter);

        MyApplication.getInstance().setConnectivityListener(this);

        isConnected = ConnectivityReceiver.isConnected(getApplicationContext());

        if (isSignedIn && isConnected) {
            if (SecurityCacheMapService.INSTANCE.exists("accessToken")) {
                accessToken = SecurityCacheMapService.INSTANCE.get("accessToken");
                Log.d(TAG, "ASYNC TASK IS NOT CALLED !!!!!!!");
                getCartCount(accessToken);
                isCartItem(accessToken);
            } else if (isConnected && !isSignedIn) {
                //This also calls isCartItem after getCartCount is over
                new GetTokenTask().execute("getCartCount");
            }
        }
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
        Log.d(TAG, "HELLO I'M ***********  onDestroy() ******* Product Description  ******");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Log.d(TAG, "HELLO I'M ***********  onCreateOptionsMenu() ******* Product Description  ******");
        getMenuInflater().inflate(R.menu.product_desription, menu);
        menuItem = menu.findItem(R.id.cart_button_product_description);
        count = getIntent().getIntExtra("CART_COUNT", 0);
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

        Log.d(TAG, "HEYYYYY THIS IS THE ITEM ID :" + item.getItemId());
        switch (item.getItemId()) {

            case R.id.cart_button_product_description:
                if (isSignedIn && isConnected) {
                    Intent goToCartPage = new Intent(this, CartPage.class);
                    startActivity(goToCartPage);
                } else if (isConnected && !isSignedIn) {
                    Intent goToSignInPage = new Intent(this, SignInPage.class);
                    goToSignInPage.putExtra("GOBACK", "CartPage");
                    startActivity(goToSignInPage);
                }
                return true;

            case android.R.id.home:
                if (isConnected) {
                    finish();
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_description_page);

        Log.d(TAG, "HELLO I'M ***********  onCreate() ******* Product Description  ******");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_description_page);
        toolbar.setTitleTextColor(Color.rgb(228, 127, 49));
        setSupportActionBar(toolbar);

        setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        constraintLayout = (ConstraintLayout) findViewById(R.id.product_description_constraint_layout);

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        isSignedIn = getIntent().getBooleanExtra("SIGNEDIN", false);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bot_nav_bar);
        isConnected = ConnectivityReceiver.isConnected(getApplicationContext());

        item_size_tag = (TextView) findViewById(R.id.item_size_tag);

        bottomNavigationView.getMenu().findItem(R.id.home_bottom_nav).setCheckable(false);
        bottomNavigationView.getMenu().findItem(R.id.account_bottom_nav).setCheckable(false);

        itemSizeTypeSpinner = (Spinner) findViewById(R.id.item_size_type);
        itemSizeSpinner = (Spinner)
                findViewById(R.id.item_size);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_bottom_nav:
                        if (isConnected) {
                            finish();
                        }
                        return true;

                    case R.id.account_bottom_nav:
                        if (isSignedIn && isConnected) {
                            Intent intent = new Intent(ProductDescription.this, AccountPage.class);
                            startActivity(intent);
                        } else if (isConnected && !isSignedIn) {
                            Intent intent = new Intent(ProductDescription.this, SignInPage.class);
                            intent.putExtra("GOBACK", "AccountPage");
                            startActivity(intent);
                        }
                        return true;
                }
                return true;
            }
        });

        item_name_tv = (TextView) findViewById(R.id.item_name);
        item_color_tv = (TextView) findViewById(R.id.item_color);
        item_carat_tv = (TextView) findViewById(R.id.item_carat);
        item_price_tv = (TextView) findViewById(R.id.item_price);
        item_weight = (TextView) findViewById(R.id.item_weight);

        addToCart = (BootstrapButton) findViewById(R.id.addToCart);
        carouselView = (CarouselView) findViewById(R.id.carouselView);

        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isSignedIn && isConnected) {
                    accessToken = SecurityCacheMapService.INSTANCE.get("accessToken");

                    if (accessToken != null) {
                        addToCart.setText("Added To Cart");
                        addToCart.setBootstrapBrand(DefaultBootstrapBrand.WARNING);
                        doIncrease();
                        addedToCart = true;
                        addToCart.setEnabled(false);
                        getCartItemStatus(accessToken);
                    } else {
                        new GetTokenTask().execute("getCartItemStatus");
                    }
                } else if (isConnected && !isSignedIn) {
                    Intent goToSignInPage = new Intent(ProductDescription.this, SignInPage.class);
                    goToSignInPage.putExtra("GOBACK", "ProductDescription");
                    startActivity(goToSignInPage);
                }
            }
        });

        item = (Item) (getIntent().getSerializableExtra("ITEM"));

        item_image_urls = getIntent().getStringArrayListExtra("ITEM_IMAGE_URLS");

        item_name_tv.setText(item.getItem_name());
        item_color_tv.setText(item.getItem_color());
        item_carat_tv.setText("" + item.getItem_carat());
        item_price_tv.setText(formatter.format(item.getItem_price()) + " KD");
        item_weight.setText(weightFormatter.format(item.getItem_weight()) + " g");

        if (item_image_urls != null) {
            carouselView.setPageCount(item_image_urls.size() + 1);
        } else {
            carouselView.setPageCount(1);
        }
        carouselView.setImageListener(imageListener);

        Log.i(TAG, "This is the item type: " + item.getItem_type());

        if (!"ring".equals(item.getItem_type()) && !"chain".equals(item.getItem_type())) {
            itemSizeSpinner.setVisibility(GONE);
            itemSizeTypeSpinner.setVisibility(GONE);
            item_size_tag.setVisibility(GONE);
            item_size_type = "";
            item_size = 0;
        }

        List<Double> asiaRingSizes = DoubleStream.iterate(1, d -> d + 1).limit(36).boxed().collect(Collectors.toList());
        List<Double> usRingSizes = DoubleStream.iterate(1, d -> d + 0.5).limit(28).boxed().collect(Collectors.toList());
        List<String> itemSizeType = Arrays.asList("US", "ASIA");

        ArrayAdapter<String> itemSizeTypeAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, itemSizeType);
        ArrayAdapter<Double> usSizeItemAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, usRingSizes);
        ArrayAdapter<Double> asiaSizeItemAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, asiaRingSizes);

        itemSizeTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        usSizeItemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        asiaSizeItemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        itemSizeTypeSpinner.setAdapter(itemSizeTypeAdapter);
        itemSizeSpinner.setAdapter(usSizeItemAdapter);

        //item.setItem_size(itemSizeType.get(0) + "-" + usRingSizes.get(0));

        itemSizeTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if ("US".equals(itemSizeType.get(position))) {
                    itemSizeSpinner.setAdapter(usSizeItemAdapter);

                } else if ("ASIA".equals(itemSizeType.get(position))) {
                    itemSizeSpinner.setAdapter(asiaSizeItemAdapter);
                }

                item_size_type = itemSizeType.get(position);
                itemSizeSpinner.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        itemSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                item_size = (double) parent.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        scaleGestureDetector.onTouchEvent(ev);
        return true;
    }


    ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ProductDescription.imageView = imageView;
            if (position == 0) {
                Glide.with(getApplicationContext()).load(Constants.SERVER_URL + item.getItem_image()).into(imageView);
            } else {
                Glide.with(getApplicationContext()).load(item_image_urls.get(position - 1)).into(imageView);
            }
        }
    };


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


    private void doIncrease() {
        count++;
        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        return;
    }


    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        this.isConnected = isConnected;

        if (!isConnected) {
            notConnectedToInternet();
        } else {
            carouselView.setEnabled(true);
            if (snackbar != null && snackbar.isShown()) {
                snackbar.dismiss();
                startActivity(getIntent());
                finish();
            }
        }
    }


    private void notConnectedToInternet() {
        carouselView.setEnabled(false);
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

    private class ScaleListener extends ScaleGestureDetector.
            SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
            matrix.setScale(scaleFactor, scaleFactor);
            imageView.setImageMatrix(matrix);
            return true;
        }
    }

    class GetTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            signInPage = new SignInPage();
            accessTokenUtil = new AccessTokenUtil();
            progressDialog = ProgressDialogDisplay.displayProgressDisplay(progressDialog, ProductDescription.this, "Item", "Fetching item...");
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                accessToken = GoogleAuthUtil.getToken(getApplicationContext(), signInPage.getAccount(), scope);
                SecurityCacheMapService.INSTANCE.putToCache("accessToken", accessToken, accessTokenUtil.getTokenExpiryTime(accessToken));
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
            Log.d(TAG, "I'M ASYNC TASK !!!!!!... ");

            if (SecurityCacheMapService.INSTANCE.exists("accessToken")) {
                runPostExecuteMethod(accessToken);
                progressDialog.dismiss();
            }
        }
    }

    private void runPostExecuteMethod(String accessToken) {

        switch (checkMethod) {

            case "getCartCount":
                getCartCount(accessToken);
                isCartItem(accessToken);
                break;

            case "getCartItemStatus":
                getCartItemStatus(accessToken);
                break;

            case "isCartItem":
                isCartItem(accessToken);
                break;

            default:
                break;

        }
    }

    private void getCartCount(String accessToken) {
        final HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", accessToken);
        headers.put("SIGNIN", SignInPage.getSignedInUsing());

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                String cartCountBody = HttpClientUtil.stringResponseForGetRequest(Constants.SERVER_URL + Constants.CART_COUNT, headers);
                return cartCountBody;
            }

            @Override
            protected void onPostExecute(String cartCountBody) {
                super.onPostExecute(cartCountBody);
                Log.d(TAG, "here is the count cart body " + cartCountBody);
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

    private void getCartItemStatus(String accessToken) {

        final HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", accessToken);
        headers.put("SIGNIN", SignInPage.getSignedInUsing());

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                AddToCartRequest addToCartRequest = new AddToCartRequest();
                addToCartRequest.setItem_id(item.getItem_id());
                addToCartRequest.setItem_size(item_size);
                addToCartRequest.setItem_size_type(item_size_type);
                String addItemToCartBody = HttpClientUtil.postRequestForAddToCart(Constants.SERVER_URL + Constants.ADD_ITEM_TO_CART, headers, addToCartRequest);
                return addItemToCartBody;
            }

            @Override
            protected void onPostExecute(String addItemToCartBody) {
                super.onPostExecute(addItemToCartBody);
                try {
                    JSONObject addItemToCart = new JSONObject(addItemToCartBody);
                    String success = addItemToCart.getString("message");

                    if ("Success".equals(success)) {
                        Toast.makeText(ProductDescription.this, "Successfully added to cart", Toast.LENGTH_SHORT);
                    } else {
                        Toast.makeText(getApplicationContext(), "Sorry, could not add the item", Toast.LENGTH_SHORT);
                        count--;
                        invalidateOptionsMenu();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void isCartItem(String accessToken) {
        final HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", accessToken);
        headers.put("SIGNIN", SignInPage.getSignedInUsing());

        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = ProgressDialogDisplay.displayProgressDisplay(progressDialog, ProductDescription.this, "", "Loading...please wait...");
            }

            @Override
            protected String doInBackground(Void... params) {
                String isCartItem = HttpClientUtil.stringResponseForGetRequest(Constants.SERVER_URL + Constants.IS_CART_ITEM + item.getItem_id(), headers);
                return isCartItem;
            }

            @Override
            protected void onPostExecute(String isCartItem) {
                super.onPostExecute(isCartItem);
                try {
                    JSONObject isCartItemBody = new JSONObject(isCartItem);
                    addedToCart = isCartItemBody.getBoolean("message");
                    if (addedToCart) {
                        addToCart.setText("Added To Cart");
                        addToCart.setBootstrapBrand(DefaultBootstrapBrand.WARNING);
                        addToCart.setEnabled(false);
                    } else {
                        addToCart.setText("Add to cart");
                        addToCart.setBootstrapBrand(DefaultBootstrapBrand.SUCCESS);
                        addToCart.setEnabled(true);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error occurred while json parsing in getting the is cart item: " + e.getMessage());
                }
                progressDialog.dismiss();
            }
        }.execute();
    }

}

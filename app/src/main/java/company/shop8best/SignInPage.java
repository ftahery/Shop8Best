package company.shop8best;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.http.HttpAuthHeader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

import org.apache.http.HttpHeaders;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import company.shop8best.constants.Constants;
import company.shop8best.utils.AccessTokenUtil;
import company.shop8best.utils.ConnectivityReceiver;
import company.shop8best.utils.HttpClientUtil;
import company.shop8best.utils.ProgressDialogDisplay;

/**
 * Created by dat9 on 07/01/18.
 */

public class SignInPage extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = "SignInPage";
    public static GoogleSignInClient googleSignInClient;
    GoogleSignInButton signInButton;
    //Button skipButton;
    static Context context;
    static GoogleSignInAccount googleSignInAccount;
    static Account account;
    String scope = "oauth2: profile email";
    long cutOffTime = 120;
    ProgressDialog progressDialog = null;
    AccessTokenUtil accessTokenUtil;
    public static boolean isSignedIn = false;
    String goToClass = "ProductListingPage";
    ConnectivityReceiver connectivityReceiver;
    boolean isConnected;
    ConstraintLayout constraintLayout;
    String accessToken;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        connectivityReceiver = new ConnectivityReceiver();
        registerReceiver(connectivityReceiver, intentFilter);

        MyApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProgressDialogDisplay.dismissProgressDialog(progressDialog);
        unregisterReceiver(connectivityReceiver);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_sign_in);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_sign_in_page);
        toolbar.setTitle("Sign In");
        setSupportActionBar(toolbar);

        context = getApplicationContext();

        signInButton = (GoogleSignInButton) findViewById(R.id.sign_in_button);
        //skipButton = (Button) findViewById(R.id.skipButton);
        constraintLayout = (ConstraintLayout) findViewById(R.id.sign_in_page_constraint_layout);

        isConnected = ConnectivityReceiver.isConnected(getApplicationContext());

        if (!isConnected) {
            notConnectedToInternet();
        } else {
            GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.server_client_id)).requestEmail().build();
            googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
            signInButton.setOnClickListener(this);

            if (getIntent().getStringExtra("GOBACK") != null) {
                goToClass = getIntent().getStringExtra("GOBACK");
            }

            googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);

            if (googleSignInAccount != null) {
                Log.d(TAG, "Started the signIn activity");
                this.account = googleSignInAccount.getAccount();
                isSignedIn = true;
                new GetTokenTask().execute();
            }

            /*skipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    classToGo();
                }
            });*/
        }
    }

    public static void setGoogleSignInClient(GoogleSignInClient googleSignInClient) {
        SignInPage.googleSignInClient = googleSignInClient;
    }

    public static GoogleSignInClient getGoogleSignInClient() {
        return googleSignInClient;
    }

    public static GoogleSignInAccount getGoogleSignInAccount() {
        return googleSignInAccount;
    }

    public static void setGoogleSignInAccount(GoogleSignInAccount googleSignInAccount) {
        SignInPage.googleSignInAccount = googleSignInAccount;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    public static Context getContext() {
        return context;
    }

    public Account getAccount() {
        return account;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 9001);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 9001) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            handleSignInResult(task);

        }
    }


    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            googleSignInAccount = task.getResult(ApiException.class);
            if (googleSignInAccount != null) {
                account = googleSignInAccount.getAccount();
                isSignedIn = true;
                new GetTokenTask().execute();
            }


        } catch (ApiException e) {
            Log.d(TAG, "Login details were not fetched",e);
            updateUI(null);
        }
    }

    private void startProductListingPage() {
        Intent intent = new Intent(this, ProductListingPage.class);
        Log.d(TAG, "HERE ARE THE DETAILS FOR THE ACCOUNT EXISTING " + account.toString());
        intent.putExtra("ACCOUNT", account);
        intent.putExtra("SIGNEDIN", isSignedIn);
        startActivity(intent);

    }

    private void updateUI(Account account) {
        if (account != null) {
            signInButton.setVisibility(View.GONE);
        } else {
            signInButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        this.isConnected = isConnected;
        if (!isConnected) {
            notConnectedToInternet();
        }
    }

    private void notConnectedToInternet() {
        Snackbar snackbar = Snackbar.make(constraintLayout, "No internet connection. Please retry", Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recreate();
                    }
                });
        snackbar.setActionTextColor(Color.RED);
        // Changing action button text color
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    class GetTokenTask extends AsyncTask<Void, Void, String> {
        long expiryTime = 0;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(SignInPage.this);
            accessTokenUtil = new AccessTokenUtil();
            progressDialog.setMessage("Signing In...");
            progressDialog.setTitle("Google Sign In");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {


            try {
                Log.d(TAG, "This is the application context " + getApplicationContext());
                accessToken = GoogleAuthUtil.getToken(getApplicationContext(), account, scope);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            }
            if (accessToken != null) {
                expiryTime = accessTokenUtil.getTokenExpiryTime(accessToken);
                SecurityCacheMapService.INSTANCE.putToCache("accessToken", accessToken, expiryTime);
                Log.d(TAG, "ACCESS TOKEN RECEIVED HERE " + accessToken);
                return accessToken;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String accessToken) {
            createUser();
        }
    }

    private void createUser() {

        HashMap<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.AUTHORIZATION, accessToken);

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {

                String createUser = HttpClientUtil.stringResponseForGetRequest(Constants.SERVER_URL + Constants.CREATE_USER, headers);
                try {
                    JSONObject responseMessage = new JSONObject(createUser);
                    return responseMessage.getBoolean("message");
                } catch (JSONException e) {
                    Log.e(TAG, "Json parsing failed for the response while creating user",e);
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean isUserCreated) {
                super.onPostExecute(isUserCreated);
                if (isUserCreated) {
                    updateUI(account);
                    classToGo();
                    finish();
                }

            }
        }.execute();
    }

    private void classToGo() {

        switch (goToClass) {

            case "CartPage":
                startActivity(new Intent(this, CartPage.class));
                break;

            case "AccountPage":
                startActivity(new Intent(this, AccountPage.class));
                break;

            case "ProductDescription":
                Intent goToProductDescriptionPage = new Intent(this, ProductDescription.class);
                goToProductDescriptionPage.putExtra("SIGNEDIN", isSignedIn);
                startActivity(goToProductDescriptionPage);
                break;

            case "ProductListingPage":
                startProductListingPage();
                break;

            default:
                break;

        }
    }
}

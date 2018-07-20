package company.shop8best;

import android.accounts.Account;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import company.shop8best.utils.HttpClientUtil;
import company.shop8best.model.Oauth2TokenValidationResponse;

/**
 * Created by dat9 on 18/01/18.
 */

public class SecurityService {

    public static final String TAG= "SignInPage";
    SignInPage signInPage;
    String scope = "oauth2: email";
    Context currentContext = null;
    String token = null;

    public String getAccessToken(Context context) {
        if(SecurityCacheMapService.INSTANCE.exists("accessToken")){
            return SecurityCacheMapService.INSTANCE.get("accessToken");
        }
        return generateAccessToken(context);
    }

    public String generateAccessToken(Context context){
        signInPage = new SignInPage();
        currentContext = context;
        Log.d(TAG,"HELLO .... ACCOUNT DETAILS " + signInPage.getAccount());

        AsyncTask<Void,Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                Account account = signInPage.getAccount();
                try {
                    Log.d(TAG,"This is the application context "+ currentContext.getApplicationContext());
                    token = GoogleAuthUtil.getToken(currentContext, account,scope);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (GoogleAuthException e) {
                    e.printStackTrace();
                }
                if(token!=null) {
                    long expiryTime = getTokenExpiryTime(token);
                    long cutOffTimeForTokenExpiration = 120;
                    SecurityCacheMapService.INSTANCE.putToCache("accessToken",token,expiryTime - cutOffTimeForTokenExpiration);
                    Log.d(TAG,"ACCESS TOKEN RECEIVED HERE " + token);
                    return token;
                }
                return null;
            }
        };
        try {
            return task.get(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return null;
    }

    private long getTokenExpiryTime(String token) {
        String oauthResponse = HttpClientUtil.stringResponseForGetRequest("https://www.googleapis.com/oauth2/v1/tokeninfo?access_token="+token,null);
        Gson gson = new Gson();
        Oauth2TokenValidationResponse response = gson.fromJson(oauthResponse,Oauth2TokenValidationResponse.class);
        return response.getExpires_in()!=0 ? response.getExpires_in() : 0;
    }
}

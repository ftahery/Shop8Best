package company.shop8best;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.FacebookSdk;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by dat9 on 05/12/17.
 */

public class FacebookLogin extends AppCompatActivity{

    LoginButton loginButton;
    TextView textView;
    CallbackManager callbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.facebook_login);
        loginButton = (LoginButton) findViewById(R.id.fb_login_button);
        textView = (TextView) findViewById(R.id.profile_display);
        callbackManager = CallbackManager.Factory.create();

        loginButton.setReadPermissions("email","public_profile");

        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String userId = loginResult.getAccessToken().getUserId();
                //textView.setText("Login Success \n"+ userId + "\n Token: \n" + loginResult.getAccessToken().getToken());

                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                            try{
                                String email = response.getJSONObject().getString("email");
                                textView.setText(email);
                            }
                            catch(JSONException e)
                            {
                                Log.d("FacebookLogin",e.getMessage());
                            }
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,email,first_name,last_name,gender");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();
            }

            @Override
            public void onCancel() {


            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}

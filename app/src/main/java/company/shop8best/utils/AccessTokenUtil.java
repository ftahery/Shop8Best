package company.shop8best.utils;

import com.google.gson.Gson;

import company.shop8best.model.Oauth2TokenValidationResponse;

/**
 * Created by dat9 on 23/01/18.
 */

public class AccessTokenUtil {

    private long cutOffTime = 180;

    public long getTokenExpiryTime(String token) {
        String oauthResponse = HttpClientUtil.stringResponseForGetRequest("https://www.googleapis.com/oauth2/v1/tokeninfo?access_token="+token,null);
        Gson gson = new Gson();
        Oauth2TokenValidationResponse response = gson.fromJson(oauthResponse,Oauth2TokenValidationResponse.class);
        return response.getExpires_in()!=0 ? (response.getExpires_in() - cutOffTime) : 0;
    }
}

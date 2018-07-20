package company.shop8best;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import company.shop8best.constants.Constants;
import company.shop8best.model.CartItem;
import company.shop8best.utils.AccessTokenUtil;
import company.shop8best.utils.HttpClientUtil;

/**
 * Created by dat9 on 25/01/18.
 */

public class CartService {

    public static final String TAG = "CartService";

    SignInPage signInPage;
    String accessToken;
    AccessTokenUtil accessTokenUtil;
    String scope = "oauth2: profile email";

    public void removeItemFromCart(CartItem itemToRemove, String accessToken) {
        HashMap<String,String> headers = new HashMap<>();
        headers.put("Authorization",accessToken);
        String removeItemBody = HttpClientUtil.stringResponseForGetRequest(Constants.SERVER_URL+Constants.REMOVE_ITEM_FROM_CART+itemToRemove.getItem_id(),headers);

        try {
            JSONObject removeItem = new JSONObject(removeItemBody);
            Log.d(TAG,"Status of removing the item: " + removeItem.getString("message"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean addQuantityToCartItem(CartItem cartItem, String accessToken,String itemQuantity){
        HashMap<String,String> headers = new HashMap<>();
        headers.put("Authorization",accessToken);
        String addQuantityBody = HttpClientUtil.stringResponseForGetRequest(Constants.SERVER_URL+ Constants.ADD_QUANTITY_TO_CART_ITEM+cartItem.getItem_id()+"/quantity/"+itemQuantity,headers);

        try {
            JSONObject minusItem = new JSONObject(addQuantityBody);
            return minusItem.getBoolean("message");
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean minusItemFromCartItem(CartItem cartItem, String accessToken){
        HashMap<String,String> headers = new HashMap<>();
        headers.put("Authorization",accessToken);
        String minusItemBody = HttpClientUtil.stringResponseForGetRequest(Constants.SERVER_URL+Constants.MINUS_QUANTITY_FROM_CART_ITEM+cartItem.getItem_id(),headers);

        try {
            JSONObject minusItem = new JSONObject(minusItemBody);
            return minusItem.getBoolean("message");
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
}

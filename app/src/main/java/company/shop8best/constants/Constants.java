package company.shop8best.constants;

/**
 * Created by dat9 on 15/01/18.
 */

public class Constants {

    public static final String SERVER_URL = "http://192.168.0.7:8000";
    public static final String SHOP8BEST = "/shop8best";
    public static final String CART_ITEMS_ENDPOINT = SHOP8BEST + "/getCartItems";
    public static final String ITEMS_ENDPOINT = SHOP8BEST + "/getItems";
    public static final String IMAGE_URL_ENDPOINT = SHOP8BEST + "/getImages";
    public static final String CART_COUNT = SHOP8BEST + "/getCartCount";
    public static final String IS_CART_ITEM = SHOP8BEST + "/isCartItem/item_id/";
    public static final String ADD_ITEM_TO_CART = SHOP8BEST + "/addItemToCart/";
    public static final String REMOVE_ITEM_FROM_CART = SHOP8BEST + "/removeItemFromCart/item_id/";
    public static final String ADD_QUANTITY_TO_CART_ITEM = SHOP8BEST + "/addQuantityToCartItem/item_id/";
    public static final String MINUS_QUANTITY_FROM_CART_ITEM = SHOP8BEST + "/minusQuantityFromCartItem/item_id/";
    public static final String USER_ADDRESSES = SHOP8BEST + "/getUserAddresses/";
    public static final String DELETE_USER_ADDRESS = SHOP8BEST + "/deleteUserAddress/";
    public static final String UPDATE_USER_ADDRESS = SHOP8BEST + "/updateUserAddress/";
    public static final String PLACE_ORDER = SHOP8BEST + "/placeOrder/";
    public static final String GET_PAST_ORDERS = SHOP8BEST + "/getPastOrders/";
    public static final String CREATE_USER = SHOP8BEST + "/createUser/";
    public static final String ITEM_BY_TYPE = SHOP8BEST + "/getItemByType/item_type/";
}

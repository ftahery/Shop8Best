package company.shop8best.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import company.shop8best.CartPage;
import company.shop8best.CartService;

import company.shop8best.R;
import company.shop8best.SecurityCacheMapService;
import company.shop8best.SignInPage;
import company.shop8best.constants.Constants;
import company.shop8best.model.CartItem;
import company.shop8best.utils.AccessTokenUtil;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by dat9 on 01/12/17.
 */

public class CartAdapter extends ArrayAdapter<CartItem> {

    private static final String TAG = "CartAdapter";
    private final ArrayList<CartItem> values;

    private final LayoutInflater mLayoutInflater;
    CartService cartService;
    SignInPage signInPage;
    String checkMethod = null;
    AccessTokenUtil accessTokenUtil;
    String accessToken;
    String scope = "oauth2: profile email";
    CartItem currentCartItem;
    CartPage cartPage;
    NumberFormat formatter = new DecimalFormat("#0.00");
    NumberFormat weightFormatter = new DecimalFormat("#0.0000");

    public CartAdapter(Context context, int resource, ArrayList<CartItem> objects) {
        super(context, resource, objects);
        this.mLayoutInflater = LayoutInflater.from(context);
        this.values = new ArrayList<CartItem>(objects);
        cartService = new CartService();
        cartPage = new CartPage();
        updatePrice();
    }

    @Override
    public int getCount() {
        Log.d(TAG, "****** getCount() *****");
        return values.size();
    }

    @Override
    public View getView(final int position, View convertView,
                        final ViewGroup parent) {

        Log.d(TAG, "***** getView() *****");

        final ViewHolder vh;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.cart_row_grid,
                    parent, false);
            vh = new CartAdapter.ViewHolder();
            vh.cart_imageView = (ImageView) convertView
                    .findViewById(R.id.cart_imageView);
            vh.cart_itemName = (TextView) convertView.findViewById(R.id.cart_itemName);
            vh.cart_color = (TextView) convertView.findViewById(R.id.cart_color);
            vh.cart_carat = (TextView) convertView.findViewById(R.id.cart_carat);
            vh.cart_item_price = (TextView) convertView.findViewById(R.id.cart_item_price);
            vh.cart_item_weight = (TextView) convertView.findViewById(R.id.cart_item_weight);
            vh.cart_itemQuantity_minus = (Button) convertView.findViewById(R.id.cart_itemQuantity_minus);
            vh.cart_itemQuantity_plus = (Button) convertView.findViewById(R.id.cart_itemQuantity_plus);
            vh.cart_item_quantity = (TextView) convertView.findViewById(R.id.cart_item_quantity);
            vh.cart_item_remove = (Button) convertView.findViewById(R.id.cart_remove_item);
            vh.cart_item_size = (TextView) convertView.findViewById(R.id.cart_item_size);
            convertView.setTag(vh);
        } else {
            vh = (CartAdapter.ViewHolder) convertView.getTag();
        }


        Log.d(TAG, "VALUES : " + values.get(position).getItem_name());


        vh.cart_itemQuantity_plus.setTag(values.get(position));
        vh.cart_itemQuantity_minus.setTag(values.get(position));
        vh.cart_item_remove.setTag(values.get(position));

        vh.cart_color.setText(values.get(position).getItem_color());
        vh.cart_carat.setText(values.get(position).getItem_carat() + " carat");
        vh.cart_itemName.setText(values.get(position).getItem_name());
        vh.cart_item_price.setText(formatter.format(values.get(position).getItem_price()) + " KD");
        vh.cart_item_quantity.setText("" + values.get(position).getItem_quantity());
        vh.cart_item_weight.setText(weightFormatter.format(values.get(position).getItem_weight()) + "");

        if ("ring".equals(values.get(position).getItem_type()) || "chain".equals(values.get(position).getItem_type())) {
            vh.cart_item_size.setText(values.get(position).getItem_size_type() + "-" + values.get(position).getItem_size());
        } else {
            vh.cart_item_size.setText(" - ");
        }
        Glide
                .with(getContext())
                .load(Constants.SERVER_URL + values.get(position).getItem_image())
                .into(vh.cart_imageView);

        vh.cart_itemQuantity_plus.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                CartItem tag = (CartItem) vh.cart_itemQuantity_plus.getTag();
                int quantity = Integer.parseInt(vh.cart_item_quantity.getText().toString());

                if (SecurityCacheMapService.INSTANCE.exists("accessToken")) {
                    accessToken = SecurityCacheMapService.INSTANCE.get("accessToken");
                    boolean success = cartService.addQuantityToCartItem(tag, accessToken, vh.cart_item_quantity.getText().toString());
                    if (success) {
                        addQuantityDataSetChange(tag, quantity, vh);
                        updatePrice();
                    } else {
                        popUpForQuantityLimit();
                    }
                } else {
                    currentCartItem = tag;
                    new GetTokenTask(vh).execute("addQuantityToCartItem", vh.toString());
                }
            }
        });

        vh.cart_itemQuantity_minus.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                CartItem tag = (CartItem) vh.cart_itemQuantity_minus.getTag();
                int quantity = Integer.parseInt(vh.cart_item_quantity.getText().toString());
                quantity -= 1;

                if (quantity == 0) {
                    popUpForRemovingItem(vh);
                } else {
                    vh.cart_item_quantity.setText(quantity + "");
                    if (SecurityCacheMapService.INSTANCE.exists("accessToken")) {
                        accessToken = SecurityCacheMapService.INSTANCE.get("accessToken");
                        cartService.minusItemFromCartItem(tag, accessToken);
                        minusQuantityDataSetChange(tag);
                        updatePrice();
                    } else {
                        currentCartItem = tag;
                        new GetTokenTask(vh).execute("minusItemFromCartItem");
                    }
                }
            }
        });

        vh.cart_item_remove.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                popUpForRemovingItem(vh);
            }
        });
        return convertView;
    }

    private void addQuantityDataSetChange(CartItem tag, int quantity, ViewHolder vh) {
        quantity += 1;
        Log.d(TAG, "This is the quantity of item " + quantity);
        vh.cart_item_quantity.setText(quantity + "");
        CartItem item = values.get(values.indexOf(tag));
        item.setItem_quantity(item.getItem_quantity() + 1);
        values.set(values.indexOf(tag), item);
        notifyDataSetChanged();
    }

    private void minusQuantityDataSetChange(CartItem tag) {
        CartItem item = values.get(values.indexOf(tag));
        item.setItem_quantity(item.getItem_quantity() - 1);
        values.set(values.indexOf(tag), item);
        notifyDataSetChanged();
    }

    private void popUpForQuantityLimit() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Sorry, stock not available.")
                .setPositiveButton("Okay", dialogClickListener)
                .show();
    }

    private void popUpForRemovingItem(final ViewHolder vh) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        CartItem tag = (CartItem) vh.cart_item_remove.getTag();
                        Log.d(TAG, "CURRENTLY THE TAG IS THIS : " + tag.getItem_name() + " " + values.indexOf(tag));
                        if (SecurityCacheMapService.INSTANCE.exists("accessToken")) {
                            accessToken = SecurityCacheMapService.INSTANCE.get("accessToken");
                            cartService.removeItemFromCart(tag, accessToken);
                        } else {
                            currentCartItem = tag;
                            Log.d(TAG, "CURRENTLY THE TAG IS THIS : " + tag.getItem_name());
                            new GetTokenTask(vh).execute("removeItemFromCart");
                        }

                        values.remove(values.indexOf(tag));
                        notifyDataSetChanged();
                        updatePrice();
                        dialog.dismiss();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Are you sure you want to remove this item?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

    private void updatePrice() {

        double totalPrice = 0;
        for (CartItem cartItem : values) {
            totalPrice += (cartItem.getItem_price() * cartItem.getItem_quantity());
        }

        cartPage.cartTotalPrice.setText(formatter.format(totalPrice) + " KD");
    }

    static class ViewHolder {
        ImageView cart_imageView;
        TextView cart_itemName;
        TextView cart_color;
        TextView cart_carat;
        TextView cart_item_price;
        TextView cart_item_weight;
        TextView cart_item_size;
        Button cart_itemQuantity_minus;
        Button cart_itemQuantity_plus;
        Button cart_item_remove;
        TextView cart_item_quantity;
    }

    class GetTokenTask extends AsyncTask<String, Void, String> {

        ViewHolder vh;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            signInPage = new SignInPage();
            accessTokenUtil = new AccessTokenUtil();
        }

        GetTokenTask(ViewHolder vh) {
            this.vh = vh;
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

            if (SecurityCacheMapService.INSTANCE.exists("accessToken")) {
                runPostExecuteMethod(accessToken, vh);
            }
        }
    }

    private void runPostExecuteMethod(String accessToken, ViewHolder vh) {

        switch (checkMethod) {

            case "removeItemFromCart":
                cartService.removeItemFromCart(currentCartItem, accessToken);
                break;

            case "addQuantityToCartItem":
                boolean success = cartService.addQuantityToCartItem(currentCartItem, accessToken, vh.cart_item_quantity.getText().toString());
                int quantity = Integer.parseInt(vh.cart_item_quantity.getText().toString());
                if (success) {
                    addQuantityDataSetChange(currentCartItem, quantity, vh);
                    updatePrice();
                } else {
                    popUpForQuantityLimit();
                }
                break;

            case "minusItemFromCartItem":
                cartService.minusItemFromCartItem(currentCartItem, accessToken);
                minusQuantityDataSetChange(currentCartItem);
                updatePrice();
                break;

            default:
                break;

        }
    }
}

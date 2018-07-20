package company.shop8best.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import company.shop8best.R;
import company.shop8best.constants.Constants;
import company.shop8best.model.CartItem;

/**
 * Created by dat9 on 04/02/18.
 */

public class CheckoutAdapter extends ArrayAdapter<CartItem> {

    private static final String TAG = "CheckoutAdapter";
    private final ArrayList<CartItem> values;

    private final LayoutInflater mLayoutInflater;
    NumberFormat formatter = new DecimalFormat("#0.00");
    NumberFormat weightFormatter = new DecimalFormat("#0.0000");

    public CheckoutAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<CartItem> objects) {
        super(context, resource, objects);
        this.mLayoutInflater = LayoutInflater.from(context);
        this.values = new ArrayList<CartItem>(objects);
    }

    @Override
    public int getCount() {
        return values.size();
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder vh;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.checkout_page_row_grid,
                    parent, false);
            vh = new ViewHolder();
            vh.item_imageView = (ImageView) convertView.findViewById(R.id.item_image);
            vh.item_name = (TextView) convertView.findViewById(R.id.item_name);
            vh.item_color = (TextView) convertView.findViewById(R.id.item_color);
            vh.item_carat = (TextView) convertView.findViewById(R.id.item_carat);
            vh.item_price = (TextView) convertView.findViewById(R.id.item_price);
            vh.item_weight = (TextView) convertView.findViewById(R.id.item_weight);
            vh.item_quantity = (TextView) convertView.findViewById(R.id.item_quantity);
            vh.item_size = (TextView) convertView.findViewById(R.id.item_size);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        CartItem cartItem = values.get(position);
        vh.item_name.setText(cartItem.getItem_name());
        vh.item_color.setText(cartItem.getItem_color());
        vh.item_carat.setText(cartItem.getItem_carat() + " carat");
        vh.item_price.setText(formatter.format(cartItem.getItem_price()) + " KD");
        vh.item_weight.setText(weightFormatter.format(cartItem.getItem_weight()) + " g");
        vh.item_quantity.setText(cartItem.getItem_quantity() + "");

        if ("ring".equals(values.get(position).getItem_type()) || "chain".equals(values.get(position).getItem_type())) {
            vh.item_size.setText(values.get(position).getItem_size_type() + "-" + values.get(position).getItem_size());
        } else {
            vh.item_size.setText(" - ");
        }

        Glide
                .with(getContext())
                .load(Constants.SERVER_URL + values.get(position).getItem_image())
                .into(vh.item_imageView);

        return convertView;
    }


    static class ViewHolder {
        ImageView item_imageView;
        TextView item_name;
        TextView item_color;
        TextView item_carat;
        TextView item_price;
        TextView item_weight;
        TextView item_size;
        TextView item_quantity;
    }
}

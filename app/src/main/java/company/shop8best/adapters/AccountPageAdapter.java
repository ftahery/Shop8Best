package company.shop8best.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


import company.shop8best.constants.Constants;
import company.shop8best.model.OrderedItemResponse;
import company.shop8best.R;


/**
 * Created by dat9 on 05/02/18.
 */

public class AccountPageAdapter extends ArrayAdapter<OrderedItemResponse> {

    private final ArrayList<OrderedItemResponse> values;
    private final LayoutInflater mLayoutInflater;
    NumberFormat formatter = new DecimalFormat("#0.00");
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public AccountPageAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<OrderedItemResponse> objects) {
        super(context, resource, objects);
        this.mLayoutInflater = LayoutInflater.from(context);
        this.values = new ArrayList<OrderedItemResponse>(objects);
    }

    @Override
    public int getCount() {
        for (OrderedItemResponse orderedItemResponse : values) {
            Log.d("CLASS NAME", "HERE ARE THE ITEMS-----> " + orderedItemResponse.getItem_name() + " " + orderedItemResponse.getOrder_date());
        }
        return values.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder vh;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.account_page_orders_row_grid,
                    parent, false);
            vh = new ViewHolder();
            vh.item_name = (TextView) convertView.findViewById(R.id.item_name);
            vh.item_quantity = (TextView) convertView.findViewById(R.id.item_quantity);
            vh.item_size = (TextView) convertView.findViewById(R.id.item_size);
            vh.item_price = (TextView) convertView.findViewById(R.id.total_price);
            vh.order_status = (TextView) convertView.findViewById(R.id.order_status);
            vh.order_date = (TextView) convertView.findViewById(R.id.order_date);
            vh.item_image = (ImageView) convertView.findViewById(R.id.item_image);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        OrderedItemResponse orderedItemResponse = values.get(position);
        vh.item_name.setText(orderedItemResponse.getItem_name());
        vh.item_quantity.setText(orderedItemResponse.getItem_quantity() + "");
        vh.item_price.setText(formatter.format(orderedItemResponse.getItem_price()) + " KD");
        vh.order_status.setText(orderedItemResponse.getOrder_status());

        if ("ring".equals(values.get(position).getItem_type()) || "chain".equals(values.get(position).getItem_type())) {
            vh.item_size.setText(values.get(position).getItem_size_type() + "-" + values.get(position).getItem_size());
        } else {
            vh.item_size.setText(" - ");
        }

        ZonedDateTime date = ZonedDateTime.parse(orderedItemResponse.getOrder_date());
        vh.order_date.setText(dateTimeFormatter.format(date));

        Glide
                .with(getContext())
                .load(Constants.SERVER_URL + values.get(position).getItem_image())
                .into(vh.item_image);

        return convertView;
    }

    static class ViewHolder {
        TextView item_name;
        TextView item_quantity;
        TextView item_size;
        TextView item_price;
        TextView order_status;
        TextView order_date;
        ImageView item_image;
    }
}

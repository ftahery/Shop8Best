package company.shop8best.adapters;

import android.content.Context;
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
import company.shop8best.model.Item;
import company.shop8best.constants.Constants;
import company.shop8best.imageUtils.ImageLoader;

/**
 * Created by dat9 on 30/10/17.
 */

public class ProductListingAdapter extends ArrayAdapter<Item> {

    private static final String TAG = "ProductListingAdapter";

    private final LayoutInflater mLayoutInflater;

    public ImageLoader imageLoader;

    private final ArrayList<Item> values;
    private int i=0;
    NumberFormat formatter = new DecimalFormat("#0.00");


    public ProductListingAdapter(Context context, int textViewResourceId,
                                 ArrayList<Item> objects) {
        super(context, textViewResourceId, objects);

        this.mLayoutInflater = LayoutInflater.from(context);
        this.values = new ArrayList<Item>(objects);
        imageLoader = new ImageLoader(context);
    }

    @Override
    public int getCount()
    {
       return values.size();
    }

    @Override
    public View getView(final int position, View convertView,
                        final ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.row_grid_item,
                    parent, false);
            vh = new ViewHolder();

            vh.item_image = (ImageView) convertView
                    .findViewById(R.id.item_image);
            vh.item_description = (TextView) convertView.findViewById(R.id.item_description);
            vh.item_price = (TextView) convertView.findViewById(R.id.item_price);

            convertView.setTag(vh);

        } else {

            vh = (ViewHolder) convertView.getTag();
        }


        vh.item_description.setText(getItem(position).getItem_name().toUpperCase());
        vh.item_price.setText( formatter.format(getItem(position).getItem_price())+ " KD" );

        Glide.with(getContext()).load(Constants.SERVER_URL+getItem(position).getItem_image()).into(vh.item_image);
        //imageLoader.DisplayImage(Constants.SERVERL_URL_FOR_MEDIA+values.get(position).getItem_image(),vh.item_image);
        //ImageLoader.getInstance().displayImage(Constants.SERVERL_URL_FOR_MEDIA+getItem(position).getItem_image(), vh.item_image);

        return convertView;
    }

    static class ViewHolder {
        ImageView item_image;
        TextView item_description;
        TextView item_price;
    }




}

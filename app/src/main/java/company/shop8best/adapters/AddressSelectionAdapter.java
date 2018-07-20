package company.shop8best.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import company.shop8best.AddressSelection;
import company.shop8best.CheckoutPage;
import company.shop8best.AddressPage;

import company.shop8best.R;
import company.shop8best.SecurityCacheMapService;
import company.shop8best.SignInPage;
import company.shop8best.constants.Constants;
import company.shop8best.model.UserAddresses;
import company.shop8best.utils.AccessTokenUtil;
import company.shop8best.utils.HttpClientUtil;

import static com.facebook.FacebookSdk.getApplicationContext;
import static company.shop8best.SignInPage.getContext;

/**
 * Created by dat9 on 12/02/18.
 */

public class AddressSelectionAdapter extends BaseExpandableListAdapter {

    private static final String TAG = "AddressSelectionAdapter";
    private Context context;
    private ArrayList<UserAddresses> values;
    String checkMethod = null;
    String accessToken;
    SignInPage signInPage;
    AccessTokenUtil accessTokenUtil;
    String scope = "oauth2: profile email";
    UserAddresses userAddresses;

    public AddressSelectionAdapter(Context context, ArrayList<UserAddresses> objects) {
        this.context = context;
        this.values = new ArrayList<UserAddresses>(objects);
    }

    @Override
    public int getGroupCount() {
        return values.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return values.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.values.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        UserAddresses userAddresses = (UserAddresses) getGroup(groupPosition);
        this.userAddresses = userAddresses;

        if (convertView == null) {
            LayoutInflater inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inf.inflate(R.layout.address_selection_group, null);
        }

        TextView user_name = (TextView) convertView.findViewById(R.id.user_name);
        user_name.setText(userAddresses.getUser_name());

        TextView user_area = (TextView) convertView.findViewById(R.id.user_area);
        user_area.setText(userAddresses.getUser_area());

        if (isExpanded)
            convertView.setPadding(0, 0, 0, 0);
        else
            convertView.setPadding(0, 0, 0, 20);

        return convertView;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        UserAddresses userAddresses = (UserAddresses) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.address_selection_list, null);
        }

        TextView user_block_street_details = (TextView) convertView.findViewById(R.id.user_block_and_street);
        user_block_street_details.setText("Block - " + userAddresses.getUser_block() + " Street - " + userAddresses.getUser_street());

        TextView user_jedda_and_area = (TextView) convertView.findViewById(R.id.user_jedda_and_area);
        user_jedda_and_area.setText("Jedda - " + userAddresses.getUser_jedda() + " Area - " + userAddresses.getUser_area());

        TextView user_house_and_floor = (TextView) convertView.findViewById(R.id.user_house_and_floor);
        user_house_and_floor.setText("House Number - " + userAddresses.getUser_house() + ", Floor - " + userAddresses.getUser_floor());

        TextView user_other_contact_info = (TextView) convertView.findViewById(R.id.user_other_contact_info);
        user_other_contact_info.setText("Other contact info - " + userAddresses.getUser_other_contact_info());

        BootstrapButton selectDeliveryButton = (BootstrapButton) convertView.findViewById(R.id.selectDeliveryButton);
        BootstrapButton edit_delivery_button = (BootstrapButton) convertView.findViewById(R.id.edit_delivery_button);
        BootstrapButton delete_delivery_button = (BootstrapButton) convertView.findViewById(R.id.delete_delivery_button);

        selectDeliveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SecurityCacheMapService.INSTANCE.exists("accessToken")) {
                    accessToken = SecurityCacheMapService.INSTANCE.get("accessToken");
                    finalAddressForDelivery(userAddresses);
                } else {
                    new GetTokenTask().execute("finalAddressForDelivery");
                }

            }
        });

        delete_delivery_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                if (SecurityCacheMapService.INSTANCE.exists("accessToken")) {
                                    accessToken = SecurityCacheMapService.INSTANCE.get("accessToken");
                                    deleteAddress(accessToken);
                                } else {
                                    new GetTokenTask().execute("deleteAddress");
                                }
                                values.remove(values.indexOf(userAddresses));
                                notifyDataSetChanged();
                                dialog.dismiss();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                dialog.dismiss();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(AddressSelection.context);
                builder.setMessage("Are you sure you want to delete the address?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener)
                        .show();
            }
        });

        edit_delivery_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddressPage.class);
                intent.putExtra("USER_ADDRESS", userAddresses);
                getContext().startActivity(intent);
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    class GetTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            signInPage = new SignInPage();
            accessTokenUtil = new AccessTokenUtil();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                accessToken = GoogleAuthUtil.getToken(getApplicationContext(), signInPage.getAccount(), scope);
                SecurityCacheMapService.INSTANCE.putToCache("accessToken", accessToken, accessTokenUtil.getTokenExpiryTime(accessToken));
                checkMethod = params[0];
                if (accessToken != null) {
                    return accessToken;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String accessToken) {
            super.onPostExecute(accessToken);
            runOnPostExecute(accessToken);
        }
    }

    private void runOnPostExecute(String accessToken) {

        switch (checkMethod) {

            case "finalAddressForDelivery":
                finalAddressForDelivery(userAddresses);
                break;

            case "deleteAddress":
                deleteAddress(accessToken);
                break;

            default:
                break;
        }
    }

    private void finalAddressForDelivery(UserAddresses userAddresses) {
        Log.d(TAG, "USER ADDRESS ------> " + userAddresses.getUser_name() + " " + userAddresses.getUser_area());
        Intent intent = new Intent(getContext(), CheckoutPage.class);
        intent.putExtra("USER_ADDRESS", userAddresses);
        getContext().startActivity(intent);
    }

    private void deleteAddress(String accessToken) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", accessToken);

        String responseBody = HttpClientUtil.stringResponseForGetRequest(Constants.SERVER_URL + Constants.DELETE_USER_ADDRESS + userAddresses.getAddress_id(), headers);
        try {
            JSONObject response = new JSONObject(responseBody);
            if ("Success".equals(response.getString("message"))) {
                Log.d(TAG, "Successfully removed the address");
                Toast.makeText(getContext(), "Address Removed", Toast.LENGTH_SHORT);
            } else {
                Log.d(TAG, "Could not remove the address due to some issue");
            }

        } catch (JSONException e) {
            Log.d(TAG, "Could not remove the address due to parse error in json " + e.getMessage());
        }

    }
}

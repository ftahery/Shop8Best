package company.shop8best.utils;

import android.util.Log;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;

import company.shop8best.model.AddToCartRequest;
import company.shop8best.model.CartItem;
import company.shop8best.model.Item;
import company.shop8best.model.UserAddresses;
import company.shop8best.model.OrderRequest;

/**
 * Created by dat9 on 15/01/18.
 */

public class HttpClientUtil {

    public static final String TAG = "HttpClientUtil";

    static int connectionTimeOut = 10000;
    static int socketTimeOut = 10000;

    public static String stringResponseForGetRequest(String url, HashMap<String, String> headers) {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeOut);
        HttpConnectionParams.setSoTimeout(params, socketTimeOut);

        HttpClient client = new DefaultHttpClient(params);
        HttpGet urlRequest = new HttpGet(url);

        if (headers != null) {
            for (Map.Entry<String, String> keyset : headers.entrySet()) {
                urlRequest.addHeader(keyset.getKey(), keyset.getValue());
            }
        }
        HttpResponse response = null;
        try {
            response = client.execute(urlRequest);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            response.getEntity().getContent()));

            String inputLine;
            String responseBody = "";
            while ((inputLine = in.readLine()) != null)
                responseBody += inputLine;
            return responseBody;

        } catch (ConnectException e) {
            Log.e(TAG, "Connection timeout occurred " + e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Error occurred while fetching the data from server for url: " + url + " : " + e.getMessage());
            return null;
        }

    }

    public static String updateAddress(String url, HashMap<String, String> headers, UserAddresses userAddresses) {

        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeOut);
        HttpConnectionParams.setSoTimeout(params, socketTimeOut);
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost urlRequest = new HttpPost(url);

        Gson gson = new Gson();

        if (headers != null) {
            for (Map.Entry<String, String> keyset : headers.entrySet()) {
                urlRequest.addHeader(keyset.getKey(), keyset.getValue());
            }
        }

        try {
            StringEntity body = new StringEntity(gson.toJson(userAddresses));
            Log.d(TAG, "This is the address" + gson.toJson(userAddresses.toString()));
            urlRequest.setEntity(body);
            urlRequest.setHeader("Content-type", "application/json");
            HttpResponse response = client.execute(urlRequest);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            response.getEntity().getContent()));

            String inputLine;
            String responseBody = "";
            while ((inputLine = in.readLine()) != null)
                responseBody += inputLine;
            return responseBody;

        } catch (IOException e) {
            Log.e(TAG, "Error occurred while fetching the data from server for url: " + url + " : " + e.getMessage());
            return null;
        }

    }

    public static String placeOrder(String url, HashMap<String, String> headers, OrderRequest orderRequest) {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeOut);
        HttpConnectionParams.setSoTimeout(params, socketTimeOut);
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost urlRequest = new HttpPost(url);

        Gson gson = new Gson();

        if (headers != null) {
            for (Map.Entry<String, String> keyset : headers.entrySet()) {
                urlRequest.addHeader(keyset.getKey(), keyset.getValue());
            }
        }


        try {
            StringEntity body = new StringEntity(gson.toJson(orderRequest));
            urlRequest.setEntity(body);
            HttpResponse response = client.execute(urlRequest);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            response.getEntity().getContent()));

            String inputLine;
            String responseBody = "";
            while ((inputLine = in.readLine()) != null)
                responseBody += inputLine;
            return responseBody;

        } catch (IOException e) {
            Log.e(TAG, "Error occurred while fetching the data from server for url: " + url + " : " + e.getMessage());
            return null;
        }
    }

    public static String postRequestForAddToCart(String url, HashMap<String, String> headers, AddToCartRequest addToCartRequest) {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeOut);
        HttpConnectionParams.setSoTimeout(params, socketTimeOut);
        HttpClient client = new DefaultHttpClient();
        HttpPost urlRequest = new HttpPost(url);

        Gson gson = new Gson();

        if (headers != null) {
            for (Map.Entry<String, String> keyset : headers.entrySet()) {
                urlRequest.addHeader(keyset.getKey(), keyset.getValue());
            }
        }

        try {
            StringEntity body = new StringEntity(gson.toJson(addToCartRequest));
            urlRequest.setEntity(body);
            HttpResponse response = client.execute(urlRequest);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            response.getEntity().getContent()));

            String inputLine;
            String responseBody = "";
            while ((inputLine = in.readLine()) != null)
                responseBody += inputLine;
            return responseBody;

        } catch (IOException e) {
            Log.e(TAG, "Error occurred while fetching the data from server for url: " + url + " : " + e.getMessage());
            return null;
        }
    }

    public static String postRequestForFinalDeliveryAddress(String url, HashMap<String, String> headers, UserAddresses userAddresses) {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeOut);
        HttpConnectionParams.setSoTimeout(params, socketTimeOut);
        HttpClient client = new DefaultHttpClient();
        HttpPost urlRequest = new HttpPost(url);

        if (headers != null) {
            for (Map.Entry<String, String> keyset : headers.entrySet()) {
                urlRequest.addHeader(keyset.getKey(), keyset.getValue());
            }
        }

        try {
            HttpResponse response = client.execute(urlRequest);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            response.getEntity().getContent()));

            String inputLine;
            String responseBody = "";
            while ((inputLine = in.readLine()) != null)
                responseBody += inputLine;
            return responseBody;

        } catch (IOException e) {
            Log.e(TAG, "Error occurred while fetching the data from server for url: " + url + " : " + e.getMessage());
            return null;
        }


    }
}

package company.shop8best.model;

/**
 * Created by dat9 on 01/12/17.
 */

public class CartItem {

    int item_id;
    String item_image;
    String item_name;
    String item_color;
    double item_price;
    double item_weight;
    double item_size;
    String item_size_type;
    String item_type;
    int item_carat;
    int item_quantity;
    String user_email;

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public int getItem_id() {
        return item_id;
    }

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    public String getItem_image() {
        return item_image;
    }

    public void setItem_image(String item_image) {
        this.item_image = item_image;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public String getItem_color() {
        return item_color;
    }

    public void setItem_color(String item_color) {
        this.item_color = item_color;
    }

    public double getItem_price() {
        return item_price;
    }

    public void setItem_price(double item_price) {
        this.item_price = item_price;
    }

    public double getItem_weight() {
        return item_weight;
    }

    public void setItem_weight(double item_weight) {
        this.item_weight = item_weight;
    }

    public double getItem_size() {
        return item_size;
    }

    public void setItem_size(double item_size) {
        this.item_size = item_size;
    }

    public String getItem_size_type() {
        return item_size_type;
    }

    public void setItem_size_type(String item_size_type) {
        this.item_size_type = item_size_type;
    }

    public String getItem_type() {
        return item_type;
    }

    public void setItem_type(String item_type) {
        this.item_type = item_type;
    }

    public int getItem_carat() {
        return item_carat;
    }

    public void setItem_carat(int item_carat) {
        this.item_carat = item_carat;
    }

    public int getItem_quantity() {
        return item_quantity;
    }

    public void setItem_quantity(int item_quantity) {
        this.item_quantity = item_quantity;
    }


}

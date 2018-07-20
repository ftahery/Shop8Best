package company.shop8best.model;

/**
 * Created by dat9 on 01/07/18.
 */

public class AddToCartRequest {

    int item_id;
    double item_size;
    String item_size_type;


    public int getItem_id() {
        return item_id;
    }

    public void setItem_id(int item_id) {
        this.item_id = item_id;
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
}

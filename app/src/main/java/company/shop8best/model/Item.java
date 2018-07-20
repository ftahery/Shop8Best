package company.shop8best.model;

import java.io.Serializable;

/**
 * Created by dat9 on 03/01/18.
 */

public class Item implements Serializable {
    int item_id;
    String item_color;
    String item_name;
    int item_carat;
    double item_price;
    double item_weight;
    String item_type;
    int item_quantity;
    String item_image;

    public int getItem_id() {
        return item_id;
    }

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    public String getItem_color() {
        return item_color;
    }

    public void setItem_color(String item_color) {
        this.item_color = item_color;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public int getItem_carat() {
        return item_carat;
    }

    public void setItem_carat(int item_carat) {
        this.item_carat = item_carat;
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

    public String getItem_type() {
        return item_type;
    }

    public void setItem_type(String item_type) {
        this.item_type = item_type;
    }

    public int getItem_quantity() {
        return item_quantity;
    }

    public void setItem_quantity(int item_quantity) {
        this.item_quantity = item_quantity;
    }

    public String getItem_image() {
        return item_image;
    }

    public void setItem_image(String item_image) {
        this.item_image = item_image;
    }

    @Override
    public String toString() {
        return "Item{" +
                "item_id=" + item_id +
                ", item_color='" + item_color + '\'' +
                ", item_name='" + item_name + '\'' +
                ", item_carat=" + item_carat +
                ", item_price=" + item_price +
                ", item_quantity=" + item_quantity +
                ", item_image='" + item_image + '\'' +
                '}';
    }
}

package company.shop8best.model;

import java.io.Serializable;

/**
 * Created by dat9 on 30/01/18.
 */

public class UserAddresses implements Serializable {

    int address_id;
    String user_name;
    String user_contact_number;
    String user_area;
    String user_street;
    String user_block;
    String user_jedda;
    String user_house;
    String user_floor;
    String user_other_contact_info;

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    String user_email;

    public int getAddress_id() {
        return address_id;
    }

    public void setAddress_id(int address_id) {
        this.address_id = address_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_contact_number() {
        return user_contact_number;
    }

    public void setUser_contact_number(String user_contact_number) {
        this.user_contact_number = user_contact_number;
    }

    public String getUser_street() {
        return user_street;
    }

    public void setUser_street(String user_street) {
        this.user_street = user_street;
    }

    public String getUser_block() {
        return user_block;
    }

    public void setUser_block(String user_block) {
        this.user_block = user_block;
    }

    public String getUser_jedda() {
        return user_jedda;
    }

    public void setUser_jedda(String user_jedda) {
        this.user_jedda = user_jedda;
    }

    public String getUser_house() {
        return user_house;
    }

    public void setUser_house(String user_house) {
        this.user_house = user_house;
    }

    public String getUser_floor() {
        return user_floor;
    }

    public void setUser_floor(String user_floor) {
        this.user_floor = user_floor;
    }

    public String getUser_area() {
        return user_area;
    }

    public void setUser_area(String user_area) {
        this.user_area = user_area;
    }

    public String getUser_other_contact_info() {
        return user_other_contact_info;
    }

    public void setUser_other_contact_info(String user_other_contact_info) {
        this.user_other_contact_info = user_other_contact_info;
    }

}

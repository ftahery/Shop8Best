package company.shop8best.model;

import java.util.List;

/**
 * Created by dat9 on 01/02/18.
 */

public class CompleteOrder {

    String user_contact_number;
    String user_building_details;
    String user_street_details;
    String user_country;
    String user_area;
    List<OrderedItemResponse> orderedItemResponses;

    public List<OrderedItemResponse> getOrderedItemResponses() {
        return orderedItemResponses;
    }

    public void setOrderedItemResponses(List<OrderedItemResponse> orderedItemResponses) {
        this.orderedItemResponses = orderedItemResponses;
    }

    public String getUser_contact_number() {
        return user_contact_number;
    }

    public void setUser_contact_number(String user_contact_number) {
        this.user_contact_number = user_contact_number;
    }

    public String getUser_building_details() {
        return user_building_details;
    }

    public void setUser_building_details(String user_building_details) {
        this.user_building_details = user_building_details;
    }

    public String getUser_street_details() {
        return user_street_details;
    }

    public void setUser_street_details(String user_street_details) {
        this.user_street_details = user_street_details;
    }

    public String getUser_country() {
        return user_country;
    }

    public void setUser_country(String user_country) {
        this.user_country = user_country;
    }

    public String getUser_area() {
        return user_area;
    }

    public void setUser_area(String user_area) {
        this.user_area = user_area;
    }
}

package company.shop8best.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import company.shop8best.R;
import company.shop8best.model.MenuModel;
import company.shop8best.model.UserAddresses;

/**
 * Created by dat9 on 08/07/18.
 */

public class ItemTypeAdapter extends BaseExpandableListAdapter {

    private static final String TAG = "AddressSelectionAdapter";
    private Context context;
    private ArrayList<String> values;
    private List<MenuModel> listDataHeader;
    private HashMap<MenuModel, List<MenuModel>> listDataChild;

    public ItemTypeAdapter(Context context, List<MenuModel> listDataHeader,
                           HashMap<MenuModel, List<MenuModel>> listChildData) {
        this.context = context;
        this.listDataHeader = new ArrayList<>(listDataHeader);
        this.listDataChild = new HashMap<>(listChildData);
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (this.listDataChild.get(this.listDataHeader.get(groupPosition)) == null) {
            return 0;
        } else {
            return this.listDataChild.get(this.listDataHeader.get(groupPosition)).size();
        }
    }

    @Override
    public MenuModel getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public MenuModel getChild(int groupPosition, int childPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition)).get(childPosition);
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
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        String headerTitle = getGroup(groupPosition).menuName;
        if (convertView == null) {
            LayoutInflater inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inf.inflate(R.layout.navigation_list_group_header, null);
        }

        TextView listHeader = (TextView) convertView.findViewById(R.id.nav_list_header);
        listHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        String childTitle = getChild(groupPosition, childPosition).menuName;

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.navigation_list_group_child, null);
        }

        TextView childHeader = (TextView) convertView.findViewById(R.id.nav_list_child);
        childHeader.setText(childTitle);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}

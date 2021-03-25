package com.example.test;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends BaseAdapter {
    private ArrayList<AlertDTO> mItems = new ArrayList<>();
    List<Address> address=null;
    Geocoder g =null;
    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public AlertDTO getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_custom, parent, false);
        }

        //ImageView iv_img = (ImageView) convertView.findViewById(R.id.iv_img) ;
        TextView tv_knh_date = (TextView) convertView.findViewById(R.id.tv_knh_date);
        TextView tv_knh_titleLST = (TextView) convertView.findViewById(R.id.tv_knh_titleLST) ;
        TextView tv_knh_bodyLST = (TextView) convertView.findViewById(R.id.tv_knh_bodyLST) ;

        AlertDTO myItem = getItem(position);

        //iv_img.setImageDrawable(myItem.getIcon());
        g = new Geocoder(convertView.getContext());

        try {
            address = g.getFromLocation(myItem.getLatitude(),myItem.getLongitude(),10);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(address!=null){
            if(address.size()==0){
                tv_knh_titleLST.setText("실패");
            }else{
                tv_knh_titleLST.setText(address.get(0).getAddressLine(0).replace("대한민국",""));
            }
        }

        tv_knh_date.setText(myItem.getTime().substring(5));
        tv_knh_bodyLST.setText(myItem.getComment());

        return convertView;
    }

    // 아이템 데이터 추가를 위한 함수.
    public void addItem(AlertDTO alertDTO) {
        AlertDTO mItem = alertDTO;
        mItems.add(mItem);
    }
    public void removeItem(int deleteId) {
        mItems.remove(deleteId);
    }
}
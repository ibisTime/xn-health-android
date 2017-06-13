package com.chengdai.ehealthproject.model.tabsurrounding.adapters;

import android.content.Context;
import android.widget.CheckBox;
import android.widget.TextView;

import com.chengdai.ehealthproject.R;
import com.chengdai.ehealthproject.model.tabsurrounding.model.HotelListModel;
import com.chengdai.ehealthproject.model.tabsurrounding.model.StoreListModel;
import com.zhy.adapter.abslistview.CommonAdapter;
import com.zhy.adapter.abslistview.ViewHolder;

import java.util.List;

/**
 * Created by 李先俊 on 2017/6/13.
 */

public class HotelSelectListAdapter extends CommonAdapter{

    private int mSelectPosition=-1;

    public void setSelectPosition(int mSelectPosition) {
        this.mSelectPosition = mSelectPosition;
        notifyDataSetChanged();
    }

    public HotelListModel.ListBean getSelectItem(){
        return (HotelListModel.ListBean) mDatas.get(mSelectPosition);
    }

    public HotelSelectListAdapter(Context context, List<HotelListModel.ListBean> datas) {
        super(context, R.layout.item_hetel_selet_list, datas);
    }

    public void setData(List<HotelListModel.ListBean> datas){
        this.mDatas=datas;
        notifyDataSetChanged();
    }

    public void addData(List<HotelListModel.ListBean> datas){
        if(datas!=null){
            this.mDatas.addAll(datas);
            notifyDataSetChanged();
        }
    }

    @Override
    protected void convert(ViewHolder viewHolder, Object item, int position) {

        HotelListModel.ListBean model= (HotelListModel.ListBean) item;

        TextView tvName=viewHolder.getView(R.id.tv_hotel_name);
        TextView tvPrice=viewHolder.getView(R.id.tv_price);
        TextView tvHotelNum=viewHolder.getView(R.id.tv_hotel_num);
        TextView tvHotelSlogan=viewHolder.getView(R.id.tv_hotel_slogan);
        CheckBox checkBox=viewHolder.getView(R.id.checkbox_hotel_select);

        if(mSelectPosition == position){
            checkBox.setChecked(true);
        }else{
            checkBox.setChecked(false);
        }

        if( model!=null ){
            tvName.setText(model.getName());
            tvPrice.setText(model.getPrice().doubleValue()+"");
            tvHotelNum.setText(model.getRemainNum ()+"");
            tvHotelSlogan.setText(model.getSlogan ());

        }


    }



}

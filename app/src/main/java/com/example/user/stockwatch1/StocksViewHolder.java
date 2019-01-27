package com.example.user.stockwatch1;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by user on 01-03-2018.
 */

//Sets references to the template xml file
public class StocksViewHolder extends RecyclerView.ViewHolder {

    //Android framework needs ViewHolder data members to be public
    public TextView tvStockSymbol;
    public TextView tvCompanyName;
    public TextView tvStockPrice;
    public TextView tvPriceChange;
    public TextView tvPercentageChange;

    public StocksViewHolder(View itemView) {
        super(itemView);

        tvStockSymbol = (TextView) itemView.findViewById(R.id.tv_stock_symb);
        tvCompanyName = (TextView) itemView.findViewById(R.id.tv_comp_name);
        tvStockPrice = (TextView) itemView.findViewById(R.id.tv_stock_price);
        tvPriceChange = (TextView) itemView.findViewById(R.id.tv_price_change);
        tvPercentageChange = (TextView) itemView.findViewById(R.id.tv_per_change);
    }
}

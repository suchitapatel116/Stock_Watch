package com.example.user.stockwatch1;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by user on 01-03-2018.
 */

//This class populates the list view in the main activity class by taking its reference and data in UI through ViewHolder
public class StocksAdapter extends RecyclerView.Adapter<StocksViewHolder> {

    private static final String TAG = "StocksAdapter";
    //This two objects are needed, so that the adapter knows that it is working with main activity and stocks view holder
    private MainActivity mainActivity;
    private ArrayList<Stock> list_of_stocks;

    private final String mWatch_url = "https://www.marketwatch.com/investing/stock/";
    //private static DecimalFormat df = new DecimalFormat(".##");

    public StocksAdapter(MainActivity mainActi, ArrayList<Stock> list) {
        this.mainActivity = mainActi;
        this.list_of_stocks = list;
    }

    @Override
    public StocksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");

        //Create a view of the template with the filled data
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_stock, parent, false);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: ");

                //Opens the stock watch website in the browser
                int pos = mainActivity.getRecyclerView().getChildAdapterPosition(view);
                String url = mWatch_url + list_of_stocks.get(pos).getStock_symbol();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                mainActivity.startActivity(i);
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "onLongClick: ");

                int pos = mainActivity.getRecyclerView().getChildAdapterPosition(view);
                mainActivity.deleteStock(view, pos);

                return true;
            }
        });

        //-- The data is filled now create object and display
        return new StocksViewHolder(itemView);
    }

    //Sets the data in the UI from list in backend (Stocks class)
    @Override
    public void onBindViewHolder(StocksViewHolder holder, int position) {

        Stock stockItem = list_of_stocks.get(position);
        if(stockItem.getPrice_change() >= 0) {
            holder.tvStockSymbol.setTextColor(Color.GREEN);
            holder.tvStockSymbol.setText(stockItem.getStock_symbol());

            holder.tvCompanyName.setTextColor(Color.GREEN);
            holder.tvCompanyName.setText(stockItem.getCompany_name());

            holder.tvStockPrice.setTextColor(Color.GREEN);
            holder.tvStockPrice.setText("" + stockItem.getStock_price());

            holder.tvPriceChange.setTextColor(Color.GREEN);
            //holder.tvPriceChange.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_up_48dp, 0,0,0);
            holder.tvPriceChange.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_up_24, 0,0,0);
            holder.tvPriceChange.setText("" + stockItem.getPrice_change());

            holder.tvPercentageChange.setTextColor(Color.GREEN);
            holder.tvPercentageChange.setText("(" + String.format("%.2f", (stockItem.getChange_percentage() * 100)) + "%)");
        }
        else {
            holder.tvStockSymbol.setTextColor(Color.RED);
            holder.tvStockSymbol.setText(stockItem.getStock_symbol());

            holder.tvCompanyName.setTextColor(Color.RED);
            holder.tvCompanyName.setText(stockItem.getCompany_name());

            holder.tvStockPrice.setTextColor(Color.RED);
            holder.tvStockPrice.setText("" + stockItem.getStock_price());

            holder.tvPriceChange.setTextColor(Color.RED);
            holder.tvPriceChange.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_down_24, 0,0,0);
            holder.tvPriceChange.setText("" + stockItem.getPrice_change());

            holder.tvPercentageChange.setTextColor(Color.RED);
            holder.tvPercentageChange.setText("(" + String.format("%.2f", (stockItem.getChange_percentage() * 100)) + "%)");
        }
    }

    @Override
    public int getItemCount() {
        return list_of_stocks.size();
    }

    public ArrayList<Stock> sortList()
    {
        Collections.sort(list_of_stocks, new Comparator<Stock>() {
            @Override
            public int compare(Stock stock, Stock t1) {
                return stock.compare(stock.getStock_symbol(), t1.getStock_symbol());
            }
        });
        return list_of_stocks;
    }
}

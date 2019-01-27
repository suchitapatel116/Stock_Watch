package com.example.user.stockwatch1;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ArrayList<Stock> list_stocks = new ArrayList<>();
    private RecyclerView recyclerView;
    private StocksAdapter stocksAdapter;
    private DatabaseHandler dbHandler;
    private SwipeRefreshLayout swiper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: ");

        recyclerView = (RecyclerView) findViewById(R.id.rv_list_stocks);
        //3.c) link the adapter to main activity
        stocksAdapter = new StocksAdapter(this, list_stocks);
        recyclerView.setAdapter(stocksAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));   //Can do operations on items view in the UI

        dbHandler = new DatabaseHandler(this);

        //9) Add swipeRefreshLayout in the xml file and it needs to be written manual text as item does not appear on the platte
        //Note: When adding items in the swipeRefreshLayout it takes up the whole space as it specifies Linear Layout,
        //So add ConstraintLayout first to the swipeRefresh and then add other items, here its ok for the recycler view as it is a single item
        swiper = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
                //Set this to false else when the view is refreshed even if its task its done it shows the refreshing symbol
                swiper.setRefreshing(false);
            }
        });
    }

    private void doRefresh() {
        Log.d(TAG, "doRefresh: ");

        if(amConnected()) {
            //Load the data from the database
            ArrayList<String[]> temp_list = dbHandler.loadStocks();
            //Get the financial data from the internet by async task
            ArrayList<Stock> stocks_lst = new ArrayList<>();
            for (int i = 0; i < temp_list.size(); i++) {
                new AsyncTaskLoadFinancialData(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, temp_list.get(i)[0]);
            }
            //All this done in getFromAsyncTaskStockFinancialData() method do clear here (in onResume)
            //as when app loads when in pause state the data is already there and other appends
            //The list_stocks gets filled while loading the data from AsyncTaskLoadFinancialData, So no need here
            list_stocks.clear();
            Log.d(TAG, "doRefresh: " + temp_list);
            stocksAdapter.notifyDataSetChanged();
        }
        else
            createAckDialog(getString(R.string.no_network), "Please connect to network to watch the stocks", 0);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        dbHandler.dumpDbToLog();
        doRefresh();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        dbHandler.shutDown();
        super.onDestroy();
    }

    //1) Add menu - create menu folder, add menu layout in that, add menu items, override 2 menu methods
    //Based on action_menu_stock.xml
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu_stock, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId())
        {
            case R.id.menu_add:
                Log.d(TAG, "onOptionsItemSelected: Add menu selected");
                //Call add stock popup by checking the network connectivity first

                if(amConnected())
                {
                    AlertDialog.Builder diag_builder = new AlertDialog.Builder(this);

                    final EditText ed = new EditText(this);
                    ed.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);   //set uppercase characters
                    ed.setGravity(Gravity.CENTER_HORIZONTAL);
                    ed.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
                    diag_builder.setView(ed);
                    //diag_builder.setIcon(R.drawable.ic_add_circle_outline_white_48dp);

                    diag_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Now find the stock according to the name specified

                            String inputSymbol = ed.getText().toString();
                            //asyncTask_stockSymb.execute(inputSymbol);
                            new AsyncTaskStockSymbol(MainActivity.this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, inputSymbol);
                        }
                    });
                    diag_builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Cancelled
                        }
                    });

                    diag_builder.setMessage(R.string.stock_sel_msg);
                    diag_builder.setTitle(R.string.stock_sel);

                    AlertDialog dialog = diag_builder.create();
                    dialog.show();
                }
                else
                {
                    //Not connected to network so just display an alert dialog
                    createAckDialog(getString(R.string.no_network), getString(R.string.add_stock_without_network), 0);
                }

                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    //2) Create customized template for the list view

    //3) Create list view to display stocks
    //3.a) Create data object class
    //3.b) Add recyclerView in the main acti xml, Create view holder class
    //3.c) Create Adapter class, link the adapter to main activity

    //4) Check the connectivity of the network and then display add new stock popup
    //Add ACCESS_NETWORK_STATE permission in the manifest file(Public function can be used by anyone)
    public boolean amConnected()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnectedOrConnecting())
            return true;
        else
            return false;
    }

    //5) Adding new stock
    //Create asynTask to search the stock from url and load

    public void getFromAsyncTaskStockSymbolData(HashMap stock_symb, String searchKey) {
        processNewStock(stock_symb, searchKey);
    }

    public void processNewStock(HashMap stock_symb, String searchKey)
    {
        if (stock_symb.size() == 0) {
            createAckDialog("Symbol Not Found: " + searchKey, "Data for stock symbol not found", 0);
        } else if (stock_symb.size() == 1) {
            //Direct display in the list view
            //Toast.makeText(MainActivity.this,"Selected item: "+stock_symb, Toast.LENGTH_SHORT).show();

            //get the stock symbol obtained from the data
            String stockName = "";
            Map<String, String> map = stock_symb;
            for(Map.Entry<String, String> entry : map.entrySet())
                stockName = entry.getKey();

            getStockFinancialDetails(stockName);
        } else {
            //Call multiple list dialog
            displayMultipleStockListDialog(stock_symb);
            //getStockFinancialDetails(val);
        }
    }

    public void displayMultipleStockListDialog(HashMap stock_symb)
    {
        Log.d(TAG, "displayMultipleStockListDialog: ");
        //selectedStock = "";
        int k=0;
        final CharSequence[] charArray = new CharSequence[stock_symb.size()];
        Map<String, String> map = stock_symb;

        for(Map.Entry<String, String> entry : map.entrySet())
        {
            String line = entry.getKey() + " - " + entry.getValue();
            Log.d(TAG, "displayMultipleStockListDialog: map= " +line);
            charArray[k++] = line;
        }

        AlertDialog.Builder diag_builder = new AlertDialog.Builder(this);
        diag_builder.setTitle(R.string.make_sel);
        diag_builder.setItems(charArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Stock symbol selected, now get the financial data
                //Toast.makeText(MainActivity.this,"Selected item: "+charArray[i], Toast.LENGTH_SHORT).show();
                String stockName = charArray[i].toString().substring(0, (charArray[i].toString().indexOf("-") - 1));
                getStockFinancialDetails(stockName);
            }
        });

        diag_builder.setNegativeButton("NEVERMIND", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Do nothing
            }
        });

        AlertDialog dialog = diag_builder.create();
        dialog.show();
    }

    //6) Create new Async task class, Get the financial data of the stock selected
    public void getStockFinancialDetails(String stock_sym)
    {
        Log.d(TAG, "getStockFinancialDetails: selected stock symbol = " + stock_sym);

        //If the selected stock already exists in the list then don't add again and display a warning
        if (dbHandler.isStockExist(stock_sym))
            createAckDialog("Duplicate Stock", "Stock symbol " + stock_sym + " is already added", R.drawable.ic_warning_black_48dp);
        else
            new AsyncTaskLoadFinancialData(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, stock_sym);
    }

    public void getFromAsyncTaskStockFinancialData(HashMap stockDataMap) {
        addNewStock(stockDataMap);
    }

    private void addNewStock(HashMap stockDataMap)
    {
        if(stockDataMap == null)
        {
            //Toast.makeText(MainActivity.this,"No Financial Data found!", Toast.LENGTH_SHORT).show();
            //Show dialog tha no such data found
            createAckDialog("No Data Found", "No Financial Stock Data found for the selected item!", 0);
            return;
        }
        //Now display the details in the list view
        Stock st = new Stock();
        st.setStock_symbol(stockDataMap.get(R.string.map_sym).toString());
        st.setCompany_name(stockDataMap.get(R.string.map_cname).toString());

        try {
            Double val = Double.parseDouble(stockDataMap.get(R.string.map_price).toString());
            st.setStock_price(val);

            val = Double.parseDouble(stockDataMap.get(R.string.map_ch).toString());
            st.setPrice_change(val);

            val = Double.parseDouble(stockDataMap.get(R.string.map_chPer).toString());
            st.setChange_percentage(val);
        }
        catch (Exception e)
        {
            Log.d(TAG, "getFromAsyncTaskStockFinancialData: Number Format Exception");
        }

        list_stocks.add(st);
        stocksAdapter.sortList();
        stocksAdapter.notifyDataSetChanged();

        //Add the stock in database
        dbHandler.addStock(st);
    }

    //General dialog without any buttons
    public void createAckDialog(String title, String msg, int iconId)
    {
        AlertDialog.Builder diag_builder = new AlertDialog.Builder(this);
        diag_builder.setMessage(msg);
        diag_builder.setTitle(title);
        if(iconId != 0)
            diag_builder.setIcon(iconId);

        AlertDialog dialog = diag_builder.create();
        dialog.show();
    }

    //7) Create dbHandler class to store the stock symbol and company name
    //When user returns get the names of the stock from the db class and fetch the stock data from online by async task

    //8) Delete item on long click from the database and the list
    public void deleteStock(View v, final int index)
    {
        AlertDialog.Builder dialog_builder = new AlertDialog.Builder(this);
        dialog_builder.setTitle("Delete Stock");
        dialog_builder.setMessage("Delete Stock Symbol "+ list_stocks.get(index).getStock_symbol() +"?");
        dialog_builder.setIcon(R.drawable.ic_delete_black_48dp);

        dialog_builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Delete from db and list
                dbHandler.deleteStock(list_stocks.get(index).getStock_symbol());

                list_stocks.remove(index);
                stocksAdapter.notifyDataSetChanged();

                Toast.makeText(MainActivity.this,"Stock Deleted Successfully", Toast.LENGTH_SHORT).show();
            }
        });

        dialog_builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //do nothing
            }
        });

        AlertDialog dialog = dialog_builder.create();
        dialog.show();
    }

    //To get a reference in the NoteAdapter class when onclick method is used
    public RecyclerView getRecyclerView() {
        return recyclerView;
    }
}

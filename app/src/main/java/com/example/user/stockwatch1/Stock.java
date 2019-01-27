package com.example.user.stockwatch1;

import android.util.Log;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by user on 01-03-2018.
 */

//Holds all stock data
public class Stock implements Serializable, Comparator<String> {

    private String stock_symbol;
    private String company_name;
    private double stock_price;
    private double price_change;
    private double change_percentage;

    public String getStock_symbol() {
        return stock_symbol;
    }

    public void setStock_symbol(String stock_symbol) {
        this.stock_symbol = stock_symbol;
    }

    public String getCompany_name() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name = company_name;
    }

    public double getStock_price() {
        return stock_price;
    }

    public void setStock_price(double stock_price) {
        this.stock_price = stock_price;
    }

    public double getPrice_change() {
        return price_change;
    }

    public void setPrice_change(double price_change) {
        this.price_change = price_change;
    }

    public double getChange_percentage() {
        return change_percentage;
    }

    public void setChange_percentage(double change_percentage) {
        this.change_percentage = change_percentage;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "stock_symbol='" + stock_symbol + '\'' +
                ", company_name='" + company_name + '\'' +
                ", stock_price=" + stock_price +
                ", price_change=" + price_change +
                ", change_percentage=" + change_percentage +
                '}';
    }

    @Override
    public int compare(String s, String t1) {
        return s.compareTo(t1);
    }
}
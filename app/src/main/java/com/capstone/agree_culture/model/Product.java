package com.capstone.agree_culture.model;

import java.io.Serializable;

public class Product implements Serializable {


    private String collection_id;

    private String user_id;
    private String product_name;
    private Double product_price;
    private Integer product_quantity;
    private Integer product_minimum;


    public Product(){

    }

    public Product(String user_id, String product_name, Double product_price, Integer product_quantity, Integer product_minimum){
        this.user_id = user_id;
        this.product_name = product_name;
        this.product_price = product_price;
        this.product_quantity = product_quantity;
        this.product_minimum = product_minimum;
    }


    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public Double getProduct_price() {
        return product_price;
    }

    public void setProduct_price(Double product_price) {
        this.product_price = product_price;
    }

    public Integer getProduct_quantity() {
        return product_quantity;
    }

    public void setProduct_quantity(Integer product_quantity) {
        this.product_quantity = product_quantity;
    }

    public Integer getProduct_minimum() {
        return product_minimum;
    }

    public void setProduct_minimum(Integer product_minimum) {
        this.product_minimum = product_minimum;
    }

    public String getCollection_id() {
        return collection_id;
    }

    public void setCollection_id(String collection_id) {
        this.collection_id = collection_id;
    }
}
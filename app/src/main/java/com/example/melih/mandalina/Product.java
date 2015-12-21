package com.example.melih.mandalina;

/**
 * Created by Melih on 21.11.2015.
 */
public class Product {

    String name = "unknown" , price = "" , url ="" , imgUrl ="" ;

    public Product(){

    }

    public  Product(String name , String price , String imgUrl , String url){

        this.name = name;
        this.price = price;
        this.url = url;
        this.imgUrl=imgUrl;

    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getUrl() {
        return url;
    }

    public String getImgUrl() {
        return imgUrl;
    }
}

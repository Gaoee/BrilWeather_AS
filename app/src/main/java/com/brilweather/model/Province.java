package com.brilweather.model;

/**
 * Created by Administrator on 2016/6/10.
 */
public class Province {
    private int id;
    private String pro_name;

    public void setId(int id){
        this.id = id;
    }

    public int getId(){
        return this.id;
    }

    public void setProName(String name){
        this.pro_name = name;
    }

    public String getProName(){
        return this.pro_name;
    }
}

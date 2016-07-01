package com.brilweather.model;

/**
 * Created by Administrator on 2016/6/30.
 */
public class Index {
    private String clothIndex;
    private String clothSug;
    private String coldIndex;
    private String coldSug;
    private String traffIndex;
    private String traffSug;

    public void setClothIndex(String clothIndex) {
        this.clothIndex = clothIndex;
    }
    public String getClothIndex(){
        return this.clothIndex;
    }

    public void setClothSug(String name) {
        this.clothSug = name;
    }
    public String getClothSug() {
        return this.clothSug;
    }

    public void setColdIndex(String code){
        this.coldIndex = code;
    }
    public String getColdIndex() {
        return this.coldIndex;
    }

    public void setColdSug(String temp) {
        this.coldSug = temp;
    }
    public String getColdSug(){
        return this.coldSug;
    }

    public void setTraffIndex(String temp) {
        this.traffIndex = temp;
    }
    public String getTraffIndex(){
        return this.traffIndex;
    }

    public void setTraffSug(String traffSug) {
        this.traffSug = traffSug;
    }
    public String getTraffSug(){
        return this.traffSug;
    }

}

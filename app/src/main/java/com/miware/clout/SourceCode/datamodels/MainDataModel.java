package com.miware.clout.SourceCode.datamodels;

public class MainDataModel {

    public MainDataModel(){
    }

    private String date, description, location, Transacting_Users, current_date;
    private double amount;
    private MainDataModel(String date,
                          String description,
                          String location,
                          String amount,
                          String Transacting_Users,
                          String current_date){
        this.current_date = current_date;
        this.date = date;
        this.description = description;
        this.location = location;
        this.amount = Integer.parseInt(amount);
        this.Transacting_Users = Transacting_Users;
    }

    public String getCurrent_date(){return current_date;}
    public void setCurrent_date(){this.current_date = current_date;}

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }

    public double getAmount() {
        return amount;
    }
    public void setAmount(String amount) {
        this.amount = Double.parseDouble(amount);
    }

    public String getTransacting_Users() {
        return Transacting_Users;
    }
    public void setTransacting_Users(String transacting_Users) {
        this.Transacting_Users = transacting_Users;
    }
}

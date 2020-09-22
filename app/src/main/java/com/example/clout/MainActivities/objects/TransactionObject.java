package com.example.clout.MainActivities.objects;

import java.util.Date;

// This class is an object class which will hold the data for the Transaction report feed (list view)
public class TransactionObject {
    private int Id;
    private String Receiver;
    private Double Amount;
    private Date CurrentDate; // date received
    private Date ReturnDate; // date to return loan

    public void setId(int id) {
        Id = id;
    }

    public int getId(){
        System.out.println("Id : " + Id);
        return Id;
    }

    public void setReceiver( String receiver ) {
        Receiver = receiver;
    }

    public String getReceiver( ) {
        System.out.println("Receiver : " + Receiver );
        return Receiver;
    }

    public void setAmount(Double amount) {
        Amount = amount;
    }

    public Double getAmount( ) {
        System.out.println("Amount : " + Amount );
        return Amount;
    }

    public void setCurrentDate( Date currentDate ) {
        CurrentDate = currentDate;
    }

    public Date getCurrentDate( ) {
        System.out.println("Sender : " + CurrentDate );
        return CurrentDate;
    }

    public void setReturnDate( Date returnDate ) {
        ReturnDate = returnDate;
    }

    public Date getReturnDate( ) {
        System.out.println("Return Date : " + ReturnDate );
        return ReturnDate;
    }
}

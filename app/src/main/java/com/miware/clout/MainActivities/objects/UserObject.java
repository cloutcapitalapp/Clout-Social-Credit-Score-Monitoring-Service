package com.miware.clout.MainActivities.objects;

public class UserObject {

    String Username;
    String Email;
    String BirthDate;
    String AccountKey;
    double Score;
    int funds;
    Long PhoneNumber;

    public void setScore(double score) {Score = score;}
    public double getScore() {return Score;}
    public void setFunds(int funds) {this.funds = funds;}
    public int getFunds() {return funds;}
    public void setPhoneNumber(Long phoneNumber){this.PhoneNumber = phoneNumber;}
    public Long getPhoneNumber() {return PhoneNumber;}
    public void setUsername(String username) {
        this.Username = username;
    }
    public String getUsername() {
        return Username;
    }
    public void setEmail(String email) {
        this.Email = email;
    }
    public String getEmail() {
        return Username;
    }
    public void setBirthDate(String birthDate) {
        this.BirthDate = birthDate;
    }
    public String getBirthDate() {
        return BirthDate;
    }
    public void setAccountKey(String accountKey) {
        this.AccountKey = accountKey;
    }
    public String getAccountKey() {
        return AccountKey;
    }
}

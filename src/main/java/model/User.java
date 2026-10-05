package model;

public class User {
    int id;
    String firstName;
    String lastName;
    String email;
    String passportData;
    String passwordHash;


    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getFirstName() {
        return firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getLastName() {
        return lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    public String getEmail() {
        return email;
    }

    public void setPassportData(String passportData) {
        this.passportData = passportData;
    }
    public String getPassportData() {
        return passportData;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
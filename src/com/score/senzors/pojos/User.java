package com.score.senzors.pojos;

/**
 * POJO class to hold user attributes
 */
public class User {
    String id;
    String username;
    String email;
    String password;

    public User(String id, String username, String email, String password) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof User) {
            User toCompare = (User) obj;
            return (this.username.equalsIgnoreCase(toCompare.getUsername()));
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (this.getUsername()).hashCode();
    }
}

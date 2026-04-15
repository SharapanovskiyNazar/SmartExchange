package com.futurecoders.smartexchange.data.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "users",
        indices = {
                @Index(value = {"phoneNumber"}, unique = true)
        }
)
public class UserEntity {

    @PrimaryKey(autoGenerate = true)
    private int userId;

    private String idnp;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String password;

    public UserEntity(String idnp,
                      String firstName,
                      String lastName,
                      String phoneNumber,
                      String password) {
        this.idnp = idnp;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    // ===== GETTERS / SETTERS =====

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getIdnp() {
        return idnp;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPassword() {
        return password;
    }
}

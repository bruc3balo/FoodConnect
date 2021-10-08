package com.victoria.foodconnect.globals.userDb;



import static com.victoria.foodconnect.globals.GlobalVariables.USERS;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.domain.Domain.AppUser;


@Dao
public interface UserDao {

    String GET_USER = "SELECT * FROM " + USERS;
    String CLEAR_USER = "DELETE FROM " + USERS;


    @Insert
    void insert(AppUser user);

    @Update
    void update(AppUser user);

    @Delete
    void delete(AppUser user);

    @Query(CLEAR_USER)
    void clear();

    @Query(GET_USER)
    AppUser getUserObject();

    @Query(GET_USER)
    LiveData<AppUser> getUserLiveData ();



}

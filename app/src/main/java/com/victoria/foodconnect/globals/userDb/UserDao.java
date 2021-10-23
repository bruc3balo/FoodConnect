package com.victoria.foodconnect.globals.userDb;



import static com.victoria.foodconnect.globals.GlobalVariables.USER_COLLECTION;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;


import com.victoria.foodconnect.domain.Domain;

import java.util.List;
import java.util.Optional;

@Dao
public interface UserDao {

    String GET_ALL_USER = "SELECT * FROM " + USER_COLLECTION +" LIMIT 1";
    String CLEAR_USER = "DELETE FROM " + USER_COLLECTION;


    @Insert
    void insert(Domain.AppUser user);

    @Update
    void update(Domain.AppUser user);

    @Delete
    void delete(Domain.AppUser user);

    @Query(CLEAR_USER)
    @Transaction
    void clear();

    @Query(GET_ALL_USER)
    List<Domain.AppUser> getUserObject();

    @Query(GET_ALL_USER)
    LiveData<Optional<Domain.AppUser>> getUserLiveData ();


}

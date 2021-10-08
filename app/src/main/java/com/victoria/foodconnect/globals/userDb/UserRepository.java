package com.victoria.foodconnect.globals.userDb;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.domain.Domain.AppUser;


public class UserRepository {
    private final UserDao userDao;


    //methods are to store just one users data, not many
    //to be used to cache

    public UserRepository(Application application) {
        UserDB database = UserDB.getInstance(application);
        userDao = database.userDao();

    }


    //Abstraction layer for encapsulation

    private void insertUser(AppUser user) {
        new Thread(() -> {
            try {
                userDao.clear();
                userDao.insert(user);
                System.out.println(user.getName() + " inserted");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateUser(AppUser user) {
        new Thread(() -> {
            try {
                userDao.update(user);
                System.out.println(user.getName() + " updated");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void deleteUser(AppUser user) {
        new Thread(() -> {
            userDao.clear();
            System.out.println(user.getName() + " deleted");
        }).start();
    }

    private void clearUser() {
        userDao.clear();
    }


    //Used Methods
    public void insert(AppUser user) {
        insertUser(user);
    }

    public void update(AppUser user) {
        updateUser(user);
    }

    public void delete(AppUser user) {
        deleteUser(user);
    }

    public void deleteUserDb() {
        clearUser();
    }

    public AppUser getUser() {
        return userDao.getUserObject();
    }

    public LiveData<AppUser> getUserLive() {
        return userDao.getUserLiveData();
    }


}

package com.victoria.foodconnect.globals.userDb;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.room.Transaction;

import com.victoria.foodconnect.domain.Domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;


public class UserRepository {
    private final UserDao userDao;
    public static Domain.AppUser appUser;

    //methods are to store just one AppUsers data, not many
    //to be used to cache

    public UserRepository(Application application) {
        UserDB database = UserDB.getInstance(application);
        userDao = database.userDao();

    }

    //Abstraction layer for encapsulation

    @Transaction
    private void insertUser(Domain.AppUser appUser) {
        new Thread(() -> {
            try {
                clearAppUser();
                Thread.sleep(1500);

            } catch (Exception e) {
                System.out.println(appUser.getUsername() + " failed insert");
            } finally {
                userDao.insert(appUser);
                System.out.println(appUser.getUsername() + " inserted");
            }
        }).start();
    }

    @Transaction
    private Domain.AppUser updateUser(Domain.AppUser appUser) {
        new Thread(() -> {
            try {
                userDao.update(appUser);
                System.out.println(appUser.getUsername() + " updated");
            } catch (Exception e) {
                System.out.println(appUser.getUsername() + " inserted instead");
                insertUser(appUser);
            }
        }).start();
        return appUser;
    }

    @Transaction
    private void deleteUser(Domain.AppUser appUser) {
        new Thread(() -> {
            userDao.delete(appUser);
            System.out.println(appUser.getUsername() + " deleted");
        }).start();
    }

    @Transaction
    private void clearAppUser() {
        new Thread(() -> {
            userDao.clear();
            System.out.println("CLEARING AppUser DB");
        }).start();
    }


    //Used Methods
    public void insert(Domain.AppUser AppUser) {
        insertUser(AppUser);
    }

    public void update(Domain.AppUser AppUser) {
        updateUser(AppUser);
    }

    public void delete(Domain.AppUser AppUser) {
        deleteUser(AppUser);
    }

    public void deleteAppUserDb() {
        clearAppUser();
    }

    public Optional<Domain.AppUser> getUser() {
        List<Domain.AppUser> user = userDao.getUserObject();
        if (user == null || user.isEmpty()) {
            return Optional.empty();
        } else {
            appUser = user.get(0);
            return Optional.of(user.get(0));
        }
    }

    public LiveData<Optional<Domain.AppUser>> getUserLive() {
        return userDao.getUserLiveData();
    }


}

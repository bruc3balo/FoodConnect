package com.victoria.foodconnect.domain;

import static com.victoria.foodconnect.globals.GlobalVariables.USERS;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

public class Domain {

    @Entity(tableName = USERS)
    public static class AppUser {

        @PrimaryKey
        @NotNull
        private String id;
        private String name;
        private String username;
        private String email_address;
        private String password;
        private String phoneNumber;
        private String created_at;
        private String updated_at;
        private Boolean is_deleted;
        private Boolean is_disabled;
        private String role;

        public AppUser() {

        }

        public AppUser(@NonNull String id, String username) {
            this.id = id;
            this.username = username;
        }

        public AppUser(String name) {
            this.name = name;
        }


        public AppUser(String name, String username, String email_address, String password) {
            this.name = name;
            this.username = username;
            this.email_address = email_address;
            this.password = password;
        }

        public AppUser(String name, String username, String email_address, String password, String phoneNumber,Boolean is_deleted, Boolean is_disabled,String role) {
            this.name = name;
            this.username = username;
            this.email_address = email_address;
            this.password = password;
            this.role = role;
            this.phoneNumber = phoneNumber;
            this.is_disabled = is_disabled;
            this.is_deleted = is_deleted;
        }

        public AppUser(String name, String username, String email_address, String password, String phoneNumber, String created_at, String updated_at, Boolean is_deleted, Boolean is_disabled, String role) {
            this.name = name;
            this.username = username;
            this.email_address = email_address;
            this.password = password;
            this.phoneNumber = phoneNumber;
            this.created_at = created_at;
            this.updated_at = updated_at;
            this.is_deleted = is_deleted;
            this.is_disabled = is_disabled;
            this.role = role;
        }

        @NonNull
        public String getId() {
            return id;
        }

        public void setId(@NonNull String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail_address() {
            return email_address;
        }

        public void setEmail_address(String email_address) {
            this.email_address = email_address;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getCreated_at() {
            return created_at;
        }

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }

        public String getUpdated_at() {
            return updated_at;
        }

        public void setUpdated_at(String updated_at) {
            this.updated_at = updated_at;
        }

        public Boolean getIs_deleted() {
            return is_deleted;
        }

        public void setIs_deleted(Boolean is_deleted) {
            this.is_deleted = is_deleted;
        }

        public Boolean getIs_disabled() {
            return is_disabled;
        }

        public void setIs_disabled(Boolean is_disabled) {
            this.is_disabled = is_disabled;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    public static class AppRole {

        private Long id;
        private String name;
        private Set<Permissions> permissions = new LinkedHashSet<>();

        public AppRole() {

        }

        public AppRole(String name) {
            this.name = name;
        }

        public AppRole(String name, Set<Permissions> permissions) {
            this.name = name;
            this.permissions = permissions;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Set<Permissions> getPermissions() {
            return permissions;
        }

        public void setPermissions(Set<Permissions> permissions) {
            this.permissions = permissions;
        }
    }

    public static class Permissions {

        private Long id;
        private String name;

        public Permissions() {
        }

        public Permissions(String name) {
            this.name = name;
        }

        public Permissions(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}

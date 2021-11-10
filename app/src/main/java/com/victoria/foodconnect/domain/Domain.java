package com.victoria.foodconnect.domain;

import static com.victoria.foodconnect.globals.GlobalVariables.USER_COLLECTION;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

public class Domain {

    @Entity(tableName = USER_COLLECTION)
    public static class AppUser implements Serializable {

        @PrimaryKey
        @NotNull
        private String uid;
        private String id_number;
        private String phone_number;
        private String bio;
        private String email_address;
        private String names;
        private String username;
        private String role;
        private String created_at;
        private String updated_at;
        private boolean deleted;
        private boolean disabled;
        private boolean tutorial;
        private boolean verified;
        private String last_known_location;
        private String password;
        private String profile_picture;


        public AppUser() {

        }

        public AppUser(@NotNull String uid, String id_number, String phone_number, String bio, String email_address, String names, String username, String role, String createdAt, String updatedAt, boolean deleted, boolean disabled, boolean tutorial, boolean verified,String last_known_location, String password,String profile_picture) {
            this.uid = uid;
            this.id_number = id_number;
            this.phone_number = phone_number;
            this.bio = bio;
            this.email_address = email_address;
            this.names = names;
            this.username = username;
            this.role = role;
            this.created_at = createdAt;
            this.updated_at = updatedAt;
            this.deleted = deleted;
            this.verified = verified;
            this.disabled = disabled;
            this.tutorial = tutorial;
            this.last_known_location = last_known_location;
            this.password = password;
            this.profile_picture = profile_picture;
        }


        @NonNull
        public String getUid() {
            return uid;
        }

        public void setUid(@NonNull String uid) {
            this.uid = uid;
        }

        public String getProfile_picture() {
            return profile_picture;
        }

        public void setProfile_picture(String profile_picture) {
            this.profile_picture = profile_picture;
        }

        public String getId_number() {
            return id_number;
        }

        public void setId_number(String id_number) {
            this.id_number = id_number;
        }

        public String getPhone_number() {
            return phone_number;
        }

        public void setPhone_number(String phone_number) {
            this.phone_number = phone_number;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public String getEmail_address() {
            return email_address;
        }

        public void setEmail_address(String email_address) {
            this.email_address = email_address;
        }

        public String getNames() {
            return names;
        }

        public void setNames(String names) {
            this.names = names;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
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

        public boolean isDeleted() {
            return deleted;
        }

        public void setDeleted(boolean deleted) {
            this.deleted = deleted;
        }

        public boolean isDisabled() {
            return disabled;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }

        public boolean isTutorial() {
            return tutorial;
        }

        public void setTutorial(boolean tutorial) {
            this.tutorial = tutorial;
        }

        public String getLast_known_location() {
            return last_known_location;
        }

        public void setLast_known_location(String last_known_location) {
            this.last_known_location = last_known_location;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isVerified() {
            return verified;
        }

        public void setVerified(boolean verified) {
            this.verified = verified;
        }
    }


}

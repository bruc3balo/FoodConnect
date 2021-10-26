package com.victoria.foodconnect.models;


import static com.victoria.foodconnect.globals.GlobalVariables.HY;
import static com.victoria.foodconnect.globals.GlobalVariables.IMAGE;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_CATEGORY_NAME;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_DESCRIPTION;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_NAME;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_PRICE;
import static com.victoria.foodconnect.globals.GlobalVariables.UNIT;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.victoria.foodconnect.domain.Domain;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;


public class Models {

    public static class NewUserForm {
        private String name;
        private String username;
        private String email_address;
        private String password;
        private String phone_number = HY;
        private String id_number = HY;
        private String bio = HY;
        private String role;


        public NewUserForm() {

        }


        public NewUserForm(String name, String username, String email_address, String password, String phone_number, String id_number, String bio, String role) {
            this.name = name;
            this.username = username;
            this.email_address = email_address;
            this.password = password;
            this.phone_number = phone_number;
            this.id_number = id_number;
            this.bio = bio;
            this.role = role;
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

        public String getPhone_number() {
            return phone_number;
        }

        public void setPhone_number(String phone_number) {
            this.phone_number = phone_number;
        }

        public String getId_number() {
            return id_number;
        }

        public void setId_number(String id_number) {
            this.id_number = id_number;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    public static class RoleCreationForm {

        private String name;

        private Set<String> permissions = new LinkedHashSet<>();

        public RoleCreationForm() {

        }

        public RoleCreationForm(String name, Set<String> permissions) {
            this.name = name;
            this.permissions = permissions;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Set<String> getPermissions() {
            return permissions;
        }

        public void setPermissions(Set<String> permissions) {
            this.permissions = permissions;
        }
    }

    public static class RoleToUserForm {

        private String username;
        private String role_name;

        public RoleToUserForm() {

        }

        public RoleToUserForm(String username, String role_name) {
            this.username = username;
            this.role_name = role_name;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRole_name() {
            return role_name;
        }

        public void setRole_name(String role_name) {
            this.role_name = role_name;
        }
    }

    public static class UsernameAndPasswordAuthenticationRequest {
        private String username;
        private String password;

        public UsernameAndPasswordAuthenticationRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public UsernameAndPasswordAuthenticationRequest() {
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class UserUpdateForm {

        private String names;
        private String role;
        private String phone_number;
        private String id_number;
        private String bio;
        private Boolean tutorial;


        public UserUpdateForm() {

        }


        public UserUpdateForm(String id_number, String bio, Boolean tutorial) {
            this.id_number = id_number;
            this.bio = bio;

            this.tutorial = tutorial;
        }

        public UserUpdateForm(String id_number, String bio) {
            this.id_number = id_number;
            this.bio = bio;

        }

        public UserUpdateForm(String names, String role, String phone_number, String id_number, String bio, Boolean tutorial) {
            this.names = names;
            this.role = role;
            this.phone_number = phone_number;
            this.id_number = id_number;
            this.bio = bio;
            this.tutorial = tutorial;
        }

        public UserUpdateForm(Boolean tutorial) {
            this.tutorial = tutorial;
        }

        public Boolean getTutorial() {
            return tutorial;
        }

        public void setTutorial(Boolean tutorial) {
            this.tutorial = tutorial;
        }

        public String getNames() {
            return names;
        }

        public void setNames(String names) {
            this.names = names;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getPhone_number() {
            return phone_number;
        }

        public void setPhone_number(String phone_number) {
            this.phone_number = phone_number;
        }

        public String getId_number() {
            return id_number;
        }

        public void setId_number(String id_number) {
            this.id_number = id_number;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

    }

    public static class LoginResponse implements Serializable {
        private String access_token;
        private String refresh_token;
        private String auth_type;

        public LoginResponse() {
        }

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        public String getRefresh_token() {
            return refresh_token;
        }

        public void setRefresh_token(String refresh_token) {
            this.refresh_token = refresh_token;
        }

        public String getAuth_type() {
            return auth_type;
        }

        public void setAuth_type(String auth_type) {
            this.auth_type = auth_type;
        }
    }

    public static class AppUser implements Serializable {

        private String uid;
        private String names;
        private String username;
        private String id_number;
        private String email_address;
        private String phone_number;
        private String password;
        private String bio;
        private String last_known_location;
        private String created_at;
        private String updated_at;
        private AppRole role;
        private Boolean disabled;
        private Boolean deleted;
        private Boolean tutorial;
        private Boolean verified;

        public AppUser() {

        }

        public AppUser(String names, String username, String id_number, String email_address, String phone_number, String created_at, String updated_at, Boolean disabled, Boolean deleted, AppRole role, Boolean tutorial) {

        }


        public AppUser(String uid, String names, String username, String id_number, String email_address, String phone_number, String password, String bio, String last_known_location, String created_at, String updated_at, AppRole role, Boolean disabled, Boolean deleted, Boolean tutorial, Boolean verified) {
            this.uid = uid;
            this.names = names;
            this.username = username;
            this.id_number = id_number;
            this.email_address = email_address;
            this.phone_number = phone_number;
            this.password = password;
            this.bio = bio;
            this.last_known_location = last_known_location;
            this.created_at = created_at;
            this.updated_at = updated_at;
            this.role = role;
            this.disabled = disabled;
            this.deleted = deleted;
            this.tutorial = tutorial;
            this.verified = verified;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public void setCreated_at(String created_at) {
            this.created_at = created_at;
        }

        public void setUpdated_at(String updated_at) {
            this.updated_at = updated_at;
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

        public String getId_number() {
            return id_number;
        }

        public void setId_number(String id_number) {
            this.id_number = id_number;
        }

        public String getEmail_address() {
            return email_address;
        }

        public void setEmail_address(String email_address) {
            this.email_address = email_address;
        }

        public Boolean getTutorial() {
            return tutorial;
        }

        public void setTutorial(Boolean tutorial) {
            this.tutorial = tutorial;
        }

        public String getPhone_number() {
            return phone_number;
        }

        public void setPhone_number(String phone_number) {
            this.phone_number = phone_number;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public String getLast_known_location() {
            return last_known_location;
        }

        public void setLast_known_location(String last_known_location) {
            this.last_known_location = last_known_location;
        }

        public AppRole getRole() {
            return role;
        }

        public void setRole(AppRole role) {
            this.role = role;
        }


        public Boolean getDisabled() {
            return disabled;
        }

        public void setDisabled(Boolean disabled) {
            this.disabled = disabled;
        }

        public Boolean getDeleted() {
            return deleted;
        }

        public void setDeleted(Boolean deleted) {
            this.deleted = deleted;
        }

        public String getCreated_at() {
            return created_at;
        }

        public String getUpdated_at() {
            return updated_at;
        }

        public Boolean getVerified() {
            return verified;
        }

        public void setVerified(Boolean verified) {
            this.verified = verified;
        }
    }

    public static class AppRole {

        private String id;
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

        public String getId() {
            return id;
        }

        public void setId(String id) {
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

        private String id;
        private String name;

        public Permissions() {
        }

        public Permissions(String name) {
            this.name = name;
        }

        public Permissions(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class TutorialModel implements Serializable {
        private String imageId;
        private String explanation;
        private String title;

        public TutorialModel(String imageId, String explanation, String title) {
            this.imageId = imageId;
            this.explanation = explanation;
            this.title = title;
        }

        public String getImageId() {
            return imageId;
        }

        public void setImageId(String imageId) {
            this.imageId = imageId;
        }

        public String getExplanation() {
            return explanation;
        }

        public void setExplanation(String explanation) {
            this.explanation = explanation;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public static class ProductCreationFrom {

        @JsonProperty(PRODUCT_NAME)
        private String product_name;

        @JsonProperty(PRODUCT_PRICE)
        private String product_price;

        @JsonProperty(PRODUCT_CATEGORY_NAME)
        private String product_category_name;

        @JsonProperty(IMAGE)
        private String image;

        @JsonProperty(UNIT)
        private String unit;

        @JsonProperty(PRODUCT_DESCRIPTION)
        private String productDescription;


        public ProductCreationFrom() {

        }

        public ProductCreationFrom(String product_name, String product_price, String product_category_name, String image) {
            this.product_name = product_name;
            this.product_price = product_price;
            this.product_category_name = product_category_name;
            this.image = image;
        }

        public String getProduct_name() {
            return product_name;
        }

        public void setProduct_name(String product_name) {
            this.product_name = product_name;
        }

        public String getProduct_price() {
            return product_price;
        }

        public void setProduct_price(String product_price) {
            this.product_price = product_price;
        }

        public String getProduct_category_name() {
            return product_category_name;
        }

        public void setProduct_category_name(String product_category_name) {
            this.product_category_name = product_category_name;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }

    public static class ProductCategory {

        private String id;

        private String name;

        private Boolean deleted;

        private Boolean disabled;

        private Date createdAt;

        private Date updatedAt;

        public ProductCategory() {

        }

        public ProductCategory(String name) {
            this.name = name;
        }



        public ProductCategory(String name, Boolean deleted, Boolean disabled, Date createdAt, Date updatedAt) {
            this.name = name;
            this.deleted = deleted;
            this.disabled = disabled;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public ProductCategory(String id, String name, Boolean deleted, Boolean disabled) {
            this.name = name;
            this.deleted = deleted;
            this.disabled = disabled;
            this.id = id;
        }

        public ProductCategory(String id, String name) {
            this.id =id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Boolean getDeleted() {
            return deleted;
        }

        public void setDeleted(Boolean deleted) {
            this.deleted = deleted;
        }

        public Boolean getDisabled() {
            return disabled;
        }

        public void setDisabled(Boolean disabled) {
            this.disabled = disabled;
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }

        public Date getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(Date updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

}
package com.victoria.foodconnect.models;


import static com.victoria.foodconnect.globals.GlobalVariables.HY;
import static com.victoria.foodconnect.globals.GlobalVariables.IMAGE;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_CATEGORY_NAME;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_DESCRIPTION;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_NAME;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_PRICE;
import static com.victoria.foodconnect.globals.GlobalVariables.UNIT;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class Models implements Serializable{

    public static class NewUserForm implements Serializable{
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

    public static class RoleCreationForm implements Serializable{

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

    public static class RoleToUserForm implements Serializable{

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

    public static class UsernameAndPasswordAuthenticationRequest implements Serializable{
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

    public static class UserUpdateForm implements Serializable{

        private String names;
        private String role;
        private String phone_number;
        private String id_number;
        private String bio;
        private Boolean tutorial;
        private String profile_picture;
        private Boolean verified;
        private Boolean deleted;
        private Boolean disabled;


        public UserUpdateForm() {

        }

        public UserUpdateForm(Boolean deleted, Boolean disabled) {
            this.deleted = deleted;
            this.disabled = disabled;
        }

        public UserUpdateForm(String profile_picture) {
            this.profile_picture = profile_picture;
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

        public Boolean getVerified() {
            return verified;
        }

        public void setVerified(Boolean verified) {
            this.verified = verified;
        }

        public String getProfile_picture() {
            return profile_picture;
        }

        public void setProfile_picture(String profile_picture) {
            this.profile_picture = profile_picture;
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
        private String auth_type;

        public LoginResponse() {
        }

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
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
        private String profile_picture;


        public AppUser() {

        }


        public AppUser(String uid, String names, String username, String id_number, String email_address, String phone_number, String password, String bio, String last_known_location, String created_at, String updated_at, AppRole role, Boolean disabled, Boolean deleted, Boolean tutorial, Boolean verified, String profile_picture) {
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
            this.profile_picture = profile_picture;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getProfile_picture() {
            return profile_picture;
        }

        public void setProfile_picture(String profile_picture) {
            this.profile_picture = profile_picture;
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

    public static class AppRole implements Serializable{

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

    public static class Permissions implements Serializable{

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

    public static class ProductCreationFrom implements Serializable{

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
        private String product_description;

        private String username;

        public ProductCreationFrom() {

        }

        public ProductCreationFrom(String product_name, String product_price, String product_category_name, String image) {
            this.product_name = product_name;
            this.product_price = product_price;
            this.product_category_name = product_category_name;
            this.image = image;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
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

        public String getProduct_description() {
            return product_description;
        }

        public void setProduct_description(String product_description) {
            this.product_description = product_description;
        }
    }

    public static class Product implements Serializable{

        private String id;

        private String name;

        private ProductCategory product_category;

        private BigDecimal price;

        private String image;

        private List<ProductAmount> productAmount = new LinkedList<>();

        private String created_at;

        private String updated_at;

        private Boolean deleted;

        private Boolean disabled;

        private String unit;

        private String product_description;



        public Product() {

        }



        public Product(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public Product(String name, BigDecimal price, String image, String created_at, String updated_at, Boolean deleted, Boolean disabled) {
            this.name = name;
            this.price = price;
            this.image = image;
            this.created_at = created_at;
            this.updated_at = updated_at;
            this.deleted = deleted;
            this.disabled = disabled;
        }

        public Product(String id, String name, ProductCategory product_category, BigDecimal price, String image, String created_at, String updated_at, Boolean deleted, Boolean disabled, String unit, String product_description) {
            this.id = id;
            this.name = name;
            this.product_category = product_category;
            this.price = price;
            this.image = image;
            this.created_at = created_at;
            this.updated_at = updated_at;
            this.deleted = deleted;
            this.disabled = disabled;
            this.unit = unit;
            this.product_description = product_description;
        }

        public Product(String id, String name, Boolean deleted, Boolean disabled) {
            this.id = id;
            this.name = name;
            this.deleted = deleted;
            this.disabled = disabled;
        }

        public List<ProductAmount> getProductAmount() {
            return productAmount;
        }

        public void setProductAmount(List<ProductAmount> productAmount) {
            this.productAmount = productAmount;
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

        public Models.ProductCategory getProduct_category() {
            return product_category;
        }

        public void setProduct_category(Models.ProductCategory product_category) {
            this.product_category = product_category;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
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

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getProduct_description() {
            return product_description;
        }

        public void setProduct_description(String product_description) {
            this.product_description = product_description;
        }
    }

    public static class ProductAmount implements Serializable{

        private String sellersId;

        private int unit;

        public ProductAmount() {
        }

        public ProductAmount(String sellersId, int unit) {
            this.sellersId = sellersId;
            this.unit = unit;
        }

        public String getSellersId() {
            return sellersId;
        }

        public void setSellersId(String sellersId) {
            this.sellersId = sellersId;
        }

        public int getUnit() {
            return unit;
        }

        public void setUnit(int unit) {
            this.unit = unit;
        }
    }

    public static class ProductCategory implements Serializable{

        private String id;

        private String name;

        private Boolean deleted;

        private Boolean disabled;

        private String created_at;

        private String updated_at;

        public ProductCategory() {

        }

        public ProductCategory(String name) {
            this.name = name;
        }


        public ProductCategory(String name, Boolean deleted, Boolean disabled, String created_at, String updated_at) {
            this.name = name;
            this.deleted = deleted;
            this.disabled = disabled;
            this.created_at = created_at;
            this.updated_at = updated_at;
        }

        public ProductCategory(String id, String name, Boolean deleted, Boolean disabled) {
            this.name = name;
            this.deleted = deleted;
            this.disabled = disabled;
            this.id = id;
        }

        public ProductCategory(String id, String name) {
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
    }

    public static class ProductCategoryUpdateForm implements Serializable{

        private String name;

        private Boolean deleted;

        private Boolean disabled;

        public ProductCategoryUpdateForm(String name, Boolean deleted, Boolean disabled) {
            this.name = name;
            this.deleted = deleted;
            this.disabled = disabled;
        }

        public ProductCategoryUpdateForm(Boolean deleted, Boolean disabled) {
            this.deleted = deleted;
            this.disabled = disabled;
        }

        public ProductCategoryUpdateForm() {
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
    }

    public static class ProductUpdateForm implements Serializable {

        private String id;

        private String product_name;

        private String product_price;

        private String product_category_name;

        private String image;

        private String unit;

        private String product_description;

        private Boolean disabled;

        private Boolean deleted;

        private String sellersId;

        private Integer unitsLeft;

        public ProductUpdateForm() {
        }

        public ProductUpdateForm(String id, String sellersId, Integer unitsLeft) {
            this.id = id;
            this.sellersId = sellersId;
            this.unitsLeft = unitsLeft;
        }

        public ProductUpdateForm(String id, String product_name, String product_price, String product_category_name, String image, String unit, String product_description, Boolean disabled, Boolean deleted) {
            this.id = id;
            this.product_name = product_name;
            this.product_price = product_price;
            this.product_category_name = product_category_name;
            this.image = image;
            this.unit = unit;
            this.product_description = product_description;
            this.disabled = disabled;
            this.deleted = deleted;
        }

        public String getSellersId() {
            return sellersId;
        }

        public void setSellersId(String sellersId) {
            this.sellersId = sellersId;
        }

        public Integer getUnitsLeft() {
            return unitsLeft;
        }

        public void setUnitsLeft(Integer unitsLeft) {
            this.unitsLeft = unitsLeft;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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

        public String getProduct_description() {
            return product_description;
        }

        public void setProduct_description(String product_description) {
            this.product_description = product_description;
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
    }

}
package com.victoria.foodconnect.globals.productDb;

import static com.victoria.foodconnect.globals.GlobalVariables.AUTHORIZATION;
import static com.victoria.foodconnect.globals.GlobalVariables.CONTENT_TYPE_ME;
import static com.victoria.foodconnect.globals.GlobalVariables.ID;
import static com.victoria.foodconnect.globals.GlobalVariables.NAME;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_CATEGORY_NAME;
import static com.victoria.foodconnect.globals.GlobalVariables.USERNAME;

import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.JsonResponse;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface ProductApi {

    String baseProduct = "product";
    String baseProductCategory = baseProduct + "/category";

    @GET(baseProduct + "/all")
    Call<JsonResponse> getAllProducts(@Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);

    @GET(baseProduct + "/allSeller")
    Call<JsonResponse> getMySellingProducts(@Header(AUTHORIZATION) String token, @Query(USERNAME) String username,@Header(CONTENT_TYPE_ME) String contentType);

    @GET(baseProductCategory + "/all")
    Call<JsonResponse> getAllProductCategories(@Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);

    @GET(baseProductCategory)
    Call<JsonResponse> getProductListWithCategory(@Query(PRODUCT_CATEGORY_NAME) String categoryName, @Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);

    @POST(baseProductCategory + "/new")
    Call<JsonResponse> createNewProductCategory(@Query(NAME) String categoryName, @Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);

    @GET(baseProductCategory + "/specific")
    Call<JsonResponse> getProductCategory(@QueryMap HashMap<String,String> params, @Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType);

    @PUT(baseProductCategory + "/update")
    Call<JsonResponse> updateProductCategory(@Query(PRODUCT_CATEGORY_NAME) String categoryName, @Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType, @Body Models.ProductCategoryUpdateForm form);

    @POST(baseProduct +"/new")
    Call<JsonResponse> createNewProduct(@Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType, @Body Models.ProductCreationFrom from);

    @POST(baseProduct +"/specific")
    Call<JsonResponse> getSpecificProduct(@Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType, @Query(ID) String id);

    @PUT(baseProduct + " /update")
    Call<JsonResponse> updateProduct(@Header(AUTHORIZATION) String token, @Header(CONTENT_TYPE_ME) String contentType,@Query(ID) String id ,@Body Models.ProductUpdateForm form);
}

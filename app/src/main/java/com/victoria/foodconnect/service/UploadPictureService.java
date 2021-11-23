package com.victoria.foodconnect.service;


import static com.victoria.foodconnect.globals.GlobalRepository.application;
import static com.victoria.foodconnect.globals.GlobalRepository.userRepository;
import static com.victoria.foodconnect.globals.GlobalVariables.MEDIA_TYPE;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_COLLECTION;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_COLLECTION_UPDATE;
import static com.victoria.foodconnect.globals.GlobalVariables.PRODUCT_IMAGE;
import static com.victoria.foodconnect.globals.GlobalVariables.PROFILE_PICTURE;
import static com.victoria.foodconnect.utils.DataOpts.getDomainUserFromModelUser;
import static com.victoria.foodconnect.utils.DataOpts.getObjectMapper;
import static com.victoria.foodconnect.utils.NotificationChannelClass.SYNCH_NOTIFICATION_CHANNEL;
import static com.victoria.foodconnect.utils.NotificationChannelClass.getPopUri;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.HasDefaultViewModelProviderFactory;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.victoria.foodconnect.R;
import com.victoria.foodconnect.domain.Domain;
import com.victoria.foodconnect.globals.productDb.ProductViewModel;
import com.victoria.foodconnect.globals.userDb.UserViewModel;
import com.victoria.foodconnect.models.Models;
import com.victoria.foodconnect.utils.JsonResponse;

import java.util.Objects;

import io.vertx.core.json.JsonObject;


public class UploadPictureService extends LifecycleService implements ViewModelStoreOwner, HasDefaultViewModelProviderFactory {

    private static boolean imageUploaded;
    private Models.ProductCreationFrom productCreationFrom;
    private Models.ProductUpdateForm productUpdateForm;
    private Models.UserUpdateForm userUpdateForm;
    private Domain.AppUser user;

    final ViewModelStore mViewModelStore = new ViewModelStore();
    private ViewModelProvider.Factory mFactory;
    private NotificationCompat.Builder notification;

    @Override
    public void onCreate() {
        super.onCreate();
        imageUploaded = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        imageUploaded = false;

        userRepository.getUserLive().observe(this, appUser -> {
            if (!appUser.isPresent()) {
                Toast.makeText(UploadPictureService.this, "Failed to get user data", Toast.LENGTH_SHORT).show();
                stopSelf();
                return;
            }

            user = appUser.get();

            switch (intent.getExtras().getString(MEDIA_TYPE)) {
                case PRODUCT_COLLECTION:
                    productCreationFrom = (Models.ProductCreationFrom) intent.getExtras().getSerializable(PRODUCT_COLLECTION);
                    productCreationFrom.setUsername(user.getUsername());
                    System.out.println("data is " + intent.getExtras().getString(MEDIA_TYPE));
                    uploadProductImage(productCreationFrom.getImage());
                    Toast.makeText(getApplication(), "Your product will be uploaded soon", Toast.LENGTH_SHORT).show();
                    break;
                case PRODUCT_COLLECTION_UPDATE:
                    productUpdateForm = (Models.ProductUpdateForm) intent.getExtras().getSerializable(PRODUCT_COLLECTION_UPDATE);
                    System.out.println("data is " + intent.getExtras().getString(MEDIA_TYPE));
                    productUpdateForm.setSellersId(user.getUsername());


                    if (productUpdateForm.getImage() == null) {
                        updateProductDetails();
                    } else {

                        uploadProductImageUpdate(productUpdateForm.getImage());
                    }

                    Toast.makeText(getApplication(), "Your product will be updated soon", Toast.LENGTH_SHORT).show();
                    break;
                case PROFILE_PICTURE:
                    Toast.makeText(getApplication(), "Your picture will be uploaded soon", Toast.LENGTH_SHORT).show();
                    userUpdateForm = (Models.UserUpdateForm) intent.getExtras().getSerializable(PROFILE_PICTURE);
                    uploadProfileImage(userUpdateForm.getProfile_picture());
                    break;
            }
        });


        System.out.println("Image upload started");


        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(@NonNull Intent intent) {
        super.onBind(intent);
        return null;
    }

    private void uploadProductImage(String data) {
        StorageReference profileBucket = FirebaseStorage.getInstance().getReference().child(PRODUCT_IMAGE).child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        profileBucket.putFile(Uri.parse(data)).addOnProgressListener(snapshot -> showSyncNotification(getPercentageProgress(snapshot.getBytesTransferred(), snapshot.getTotalByteCount()), productCreationFrom.getProduct_name(), productCreationFrom.getProduct_description())).addOnSuccessListener(taskSnapshot -> profileBucket.getDownloadUrl().addOnSuccessListener(uri -> {
            productCreationFrom.setImage(uri.toString());
            startForeground(1, notification.setProgress(100, 100, true).build());
            imageUploaded = true;
            sendProductDetails();
            System.out.println("Stored link uri : " + uri.toString());
        }).addOnFailureListener(e -> {
            imageUploaded = false;
            stopSelf();
        }));
    }


    private void uploadProductImageUpdate(String data) {
        StorageReference profileBucket = FirebaseStorage.getInstance().getReference().child(PRODUCT_IMAGE).child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        profileBucket.putFile(Uri.parse(data)).addOnProgressListener(snapshot -> showSyncNotification(getPercentageProgress(snapshot.getBytesTransferred(), snapshot.getTotalByteCount()), productUpdateForm.getProduct_name(), productUpdateForm.getProduct_description())).addOnSuccessListener(taskSnapshot -> profileBucket.getDownloadUrl().addOnSuccessListener(uri -> {
            productUpdateForm.setImage(uri.toString());

            startForeground(1, notification.setProgress(100, 100, true).build());
            imageUploaded = true;
            updateProductDetails();
            System.out.println("Stored link uri : " + uri.toString());
        }).addOnFailureListener(e -> {
            imageUploaded = false;
            stopSelf();
        }));
    }

    private void uploadProfileImage(String data) {
        StorageReference profileBucket = FirebaseStorage.getInstance().getReference().child(PROFILE_PICTURE).child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        profileBucket.putFile(Uri.parse(data)).addOnProgressListener(snapshot -> showSyncNotification(getPercentageProgress(snapshot.getBytesTransferred(), snapshot.getTotalByteCount()), "profile picture", "uploading profile picture")).addOnSuccessListener(taskSnapshot -> profileBucket.getDownloadUrl().addOnSuccessListener(uri -> {
            imageUploaded = true;
            userUpdateForm.setProfile_picture(uri.toString());
            startForeground(1, notification.setProgress(100, 100, true).build());
            updateProfileImage(userUpdateForm);
            System.out.println("Stored link uri : " + uri.toString());
        }).addOnFailureListener(e -> {
            imageUploaded = false;
            stopSelf();
        }));
    }

    private void updateProfileImage(Models.UserUpdateForm form) {
        getDefaultViewModelProviderFactory().create(UserViewModel.class).updateAUser(FirebaseAuth.getInstance().getUid(), form).observe(this, jsonResponseResponse -> {
            if (!jsonResponseResponse.isPresent()) {
                Toast.makeText(application, "Failed to send update request", Toast.LENGTH_SHORT).show();
                stopSelf();
                return;
            }

            JsonResponse response = jsonResponseResponse.get().body();

            if (response == null) {
                Toast.makeText(application, "Something went wrong", Toast.LENGTH_SHORT).show();
                stopSelf();
                return;
            }

            if (response.isHas_error()) {
                Toast.makeText(application, response.getApi_code_description(), Toast.LENGTH_SHORT).show();
                stopSelf();
                return;
            }

            if (response.getData() == null) {
                Toast.makeText(application, "No user data", Toast.LENGTH_SHORT).show();
                stopSelf();
                return;
            }

            ObjectMapper mapper = getObjectMapper();

            try {
                JsonObject userJson = new JsonObject(mapper.writeValueAsString(response.getData()));

                //save user to offline db
                Models.AppUser firebaseDbUser = mapper.readValue(userJson.toString(), Models.AppUser.class);

                System.out.println(mapper.writeValueAsString(firebaseDbUser));

                if (firebaseDbUser == null) {
                    Toast.makeText(application, "Failed to get user data", Toast.LENGTH_SHORT).show();
                    stopSelf();
                    return;
                }

                userRepository.insert(getDomainUserFromModelUser(firebaseDbUser));

                Thread.sleep(2000);

                Toast.makeText(application, "Profile picture updated", Toast.LENGTH_SHORT).show();
                stopSelf();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(application, e.getMessage(), Toast.LENGTH_SHORT).show();
                stopSelf();
            }
        });
    }

    private void showSyncNotification(Integer currentProgress, String title, String description) {
        Intent notificationIntent = new Intent(this, UploadPictureService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification = new NotificationCompat.Builder(this, SYNCH_NOTIFICATION_CHANNEL)
                .setContentTitle("Uploading image for " + title)
                .setContentText(description)
                .setSmallIcon(R.drawable.ic_give_food)
                .setAutoCancel(true)
                .setGroup(SYNCH_NOTIFICATION_CHANNEL)
                .setOnlyAlertOnce(true)
                .setProgress(100, 0, true)
                .setSubText("This won't take long")
                .setContentIntent(pendingIntent)
                .setProgress(100, currentProgress, false)
                .setSound(getPopUri(getApplicationContext()).getKey(0));

        startForeground(1, notification.build());
    }

    private int getPercentageProgress(Long bytesTransferred, Long totalByteCount) {
        return (int) ((bytesTransferred * 100) / totalByteCount);
    }

    private void sendProductDetails() {

        try {
            System.out.println("Product " + getObjectMapper().writeValueAsString(productCreationFrom));

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        getDefaultViewModelProviderFactory().create(ProductViewModel.class).createNewProductLive(productCreationFrom).observe(this, jsonResponse -> {
            if (!jsonResponse.isPresent()) {
                Toast.makeText(getApplication(), "Failed to save details", Toast.LENGTH_SHORT).show();
                stopSelf();
                return;
            }

            Toast.makeText(getApplication(), "Product successfully uploaded", Toast.LENGTH_SHORT).show();
            stopSelf();
        });
    }

    private void updateProductDetails() {

        try {
            System.out.println("Product " + getObjectMapper().writeValueAsString(productUpdateForm));

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        getDefaultViewModelProviderFactory().create(ProductViewModel.class).updateProductLive(productUpdateForm).observe(this, jsonResponse -> {
            if (!jsonResponse.isPresent()) {
                Toast.makeText(getApplication(), "Failed to update product", Toast.LENGTH_SHORT).show();
                stopSelf();
                return;
            }

            Toast.makeText(getApplication(), "Product successfully updated", Toast.LENGTH_SHORT).show();
            stopSelf();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageUploaded) {
            System.out.println("Image successfully uploaded");
        } else {
            System.out.println("Image failed to uploaded");
        }
    }

    @NonNull
    @Override
    public ViewModelProvider.Factory getDefaultViewModelProviderFactory() {
        return mFactory != null ? mFactory : (mFactory = new ViewModelProvider.AndroidViewModelFactory(getApplication()));
    }

    @NonNull
    @Override
    public ViewModelStore getViewModelStore() {
        return mViewModelStore;
    }
}

package com.adrnc_g02.appcaycanh;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Class to manage user role and session information
 * This will be used to control access to different parts of the app
 */
public class AccessControl {
    private static final String TAG = "AccessControl";
    private static final String PREF_NAME = "UserSessionPrefs";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    public static final String ROLE_ADMIN = "Admin";
    public static final String ROLE_USER = "User";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public AccessControl(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }
    public void saveUserSession(String email, String role) {
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_ROLE, role);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void clearUserSession() {
        Log.d(TAG, "clearUserSession: Clearing user session");
        editor.clear();
        editor.apply();
    }
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserRole() {
        return preferences.getString(KEY_USER_ROLE, ROLE_USER);
    }

    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, "");
    }

    public boolean isAdmin() {
        String role = getUserRole();
        Log.d(TAG, "isAdmin: Current user role: " + role);
        return ROLE_ADMIN.equals(role);
    }
    public boolean checkAccessForActivity(Class<?> activityClass) {
        boolean isAdminUser = isAdmin();
        Log.d(TAG, "checkAccessForActivity: Checking access for " + activityClass.getSimpleName() + ", isAdmin: " + isAdminUser);

        if (isAdminUser) {
            return activityClass == Admin.class ||
                    activityClass == CategoryOrders.class ||
                    activityClass == ManagerStore.class ||
                    activityClass == ProductAdmin.class ||
                    activityClass == Login.class;
        }
        else {
            return activityClass != Admin.class&&
                    activityClass != CategoryOrders.class&&
                    activityClass != ManagerStore.class&&
                    activityClass != ProductAdmin.class;
        }
    }

    /**
     * Redirect user to the appropriate home page based on role
     */
    public void redirectToHomePage(Context context) {
        if (isAdmin()) {
            Log.d(TAG, "redirectToHomePage: Redirecting admin to AdminActivity");
            Intent intent = new Intent(context, Admin.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        } else {
            Log.d(TAG, "redirectToHomePage: Redirecting user to MainActivity");
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
    }

    public void redirectUnauthorizedAccess(Context context) {
        Log.d(TAG, "redirectUnauthorizedAccess: Redirecting unauthorized access");
        if (isLoggedIn()) {
            redirectToHomePage(context);
        } else {
            Intent intent = new Intent(context, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
    }
}
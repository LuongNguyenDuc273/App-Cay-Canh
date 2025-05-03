package com.adrnc_g02.appcaycanh.Menu;

import android.content.Context;
import android.content.Intent;

import com.adrnc_g02.appcaycanh.Bloggg.Blog;
import com.adrnc_g02.appcaycanh.Home.MainActivity;
import com.adrnc_g02.appcaycanh.Profile.Profile;
import com.adrnc_g02.appcaycanh.R;
import com.adrnc_g02.appcaycanh.ProductType.Shopping;
import com.adrnc_g02.appcaycanh.Cart.ShoppingCart;

public class MenuNavigation {

    private Context context;

    public MenuNavigation(Context context) {
        this.context = context;
    }

    public void navigateTo(int destinationID) {
        // Thực hiện chuyển hướng đến destinationID
        if (destinationID == R.id.navHome){
            Intent intent = new Intent(context, MainActivity.class); // Replace LoginActivity
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        else if (destinationID == R.id.navCart) {
            Intent intent = new Intent(context, Shopping.class); // Replace LoginActivity
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        else if (destinationID == R.id.navExplore) {
            Intent intent = new Intent(context, ShoppingCart.class); // Replace LoginActivity
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        else if (destinationID == R.id.navProfile) {
            Intent intent = new Intent(context, Profile.class); // Replace LoginActivity
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        else if (destinationID == R.id.navBlog) {
            Intent intent = new Intent(context, Blog.class); // Replace LoginActivity
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

    }
}

package com.android.montelongoworldwide.pages;

import android.view.View;
import android.widget.TextView;
import com.android.R;
import com.android.montelongoworldwide.PackageSelectionActivity;

import java.util.ArrayList;
import java.util.List;

public class User extends Page<User.Model> {
    public class Model {

        public final String name, email, phone;

        public Model(String name, String email, String phone)
        {
            this.name = name;
            this.email = email;
            this.phone = phone;
        }
    }

    public User(PackageSelectionActivity mainActivity)
    {
        super(mainActivity, R.layout.pages_user, R.id.userModelContainer);
    }

    public void load()
    {
        this.models = new ArrayList<Model>() {{
            add(new Model("Ethan Parker", "ethan.parker@example.com", "+1 (555) 123-4567"));
            add(new Model("Olivia Adams", "olivia.adams@example.com", "+1 (555) 234-5678"));
            add(new Model("Benjamin Smith", "benjamin.smith@example.com", "+1 (555) 345-6789"));
            add(new Model("Ava Johnson", "ava.johnson@example.com", "+1 (555) 456-7890"));
            add(new Model("Noah Thompson", "noah.thompson@example.com", "+1 (555) 567-8901"));
            add(new Model("Emma Wilson", "emma.wilson@example.com", "+1 (555) 678-9012"));
            add(new Model("Liam Davis", "liam.davis@example.com", "+1 (555) 789-0123"));
            add(new Model("Isabella Martinez", "isabella.martinez@example.com", "+1 (555) 890-1234"));
            add(new Model("Lucas Anderson", "lucas.anderson@example.com", "+1 (555) 901-2345"));
            add(new Model("Sophia Taylor", "sophia.taylor@example.com", "+1 (555) 012-3456"));
        }};
    }

    @Override
    public boolean onFilter(String searchKeyword, Model user) {
        return user.name.toLowerCase().contains(searchKeyword) ||
                user.email.toLowerCase().contains(searchKeyword) ||
                user.phone.toLowerCase().contains(searchKeyword);
    }

    @Override
    public View onRender(Model user, int index)
    {
        View cardView = this.mainActivity.getLayout(R.layout.components_user_card, null);
        TextView fullNameTextView = cardView.findViewById(R.id.fullNameTextView);
        TextView emailPhoneTextView = cardView.findViewById(R.id.emailPhoneTextView);

        fullNameTextView.setText(user.name);
        emailPhoneTextView.setText(String.format("%s | %s", user.email, user.phone));

        // Set an OnClickListener for the cardContainer
        cardView.setOnClickListener(v -> {
            // Handle the cardContainer click event here
            mainActivity.setUser(user);
        });

        return cardView;
    }
}

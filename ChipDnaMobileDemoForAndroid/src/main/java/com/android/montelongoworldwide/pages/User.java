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

    protected List<Model> users;

    public User(PackageSelectionActivity mainActivity)
    {
        super(mainActivity, R.layout.pages_users, R.id.userModelContainer);
    }

    public void load()
    {
        this.users = new ArrayList<Model>() {{
            add(new Model("Chrys Ugwu", "foo@bar.com", "832 332 1930"));
            add(new Model("Lucky Ugwu", "lucky@bar.com", "832 332 1930"));
            add(new Model("Austin Ugwu", "austine@bar.com", "832 332 1930"));
            add(new Model("Stephine Ugwu", "stephine@bar.com", "832 332 1930"));
        }};
    }

    @Override
    public List<Model> getModels() {
        return this.users;
    }

    @Override
    public boolean onFilter(String searchKeyword, Model user) {
        return user.name.toLowerCase().contains(searchKeyword) ||
                user.email.toLowerCase().contains(searchKeyword) ||
                user.phone.toLowerCase().contains(searchKeyword);
    }

    @Override
    public View onRender(Model user)
    {
        View cardView = this.mainActivity.getLayout(R.layout.components_user_card_component, null);
        TextView fullNameTextView = cardView.findViewById(R.id.fullNameTextView);
        TextView emailPhoneTextView = cardView.findViewById(R.id.emailPhoneTextView);

        fullNameTextView.setText(user.name);
        emailPhoneTextView.setText(String.format("%s | %s", user.email, user.phone));

        return cardView;
    }
}

package com.android.montelongoworldwide.pages;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.R;
import com.android.montelongoworldwide.PackageSelectionActivity;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;

public class User extends Page<User.Model> {
    public class Model {

        public final int id;
        public final String name, email, phone, avatar;

        public Model(int id, String name, String email, String phone, String avatar)
        {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.avatar = avatar;
        }
    }

    public User(PackageSelectionActivity mainActivity)
    {
        super(mainActivity, R.layout.pages_user, R.id.userModelContainer);
    }

    @Override
    public boolean onFilter(String searchKeyword, Model user) {
        return user.name.toLowerCase().contains(searchKeyword) ||
                user.email.toLowerCase().contains(searchKeyword) ||
                user.phone.toLowerCase().contains(searchKeyword);
    }

    @Override
    public Model buildModelFromJson(JSONObject student) throws JSONException {
        return new User.Model(
            student.getInt("id"),
            String.format("%s %s", student.getString("first_name"), student.getString("last_name")),
            student.getString("email"),
            student.getString("phone"),
            student.getString("avatar").replace("null", "")
        );
    }

    @Override
    public View onRender(Model user, int index)
    {
        View cardView = this.mainActivity.getLayout(R.layout.components_user_card, null);
        TextView fullNameTextView = cardView.findViewById(R.id.fullNameTextView);
        ImageView profileImageView = cardView.findViewById(R.id.profileImage);
        TextView emailPhoneTextView = cardView.findViewById(R.id.emailPhoneTextView);

        fullNameTextView.setText(user.name);
        emailPhoneTextView.setText(String.format("%s | %s", user.email, user.phone));

        if (!user.avatar.isEmpty()) {
            Picasso.get()
                    .load("https://montelongoworldwide.net" + user.avatar)
                    .into(profileImageView);
        }

        cardView.setOnClickListener(v -> mainActivity.setUser(user));

        return cardView;
    }
}

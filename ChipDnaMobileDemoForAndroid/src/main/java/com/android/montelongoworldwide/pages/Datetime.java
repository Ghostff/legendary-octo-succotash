package com.android.montelongoworldwide.pages;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.android.R;
import com.android.montelongoworldwide.PackageSelectionActivity;
import com.android.montelongoworldwide.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.List;

public class Datetime extends Page<Datetime.Model> {
    public class Model {

        public final int id;
        public final String datetime, zip, address;

        public JSONArray users;

        public Model(int id, String datetime, String zip, String address, JSONArray users)
        {
            this.id = id;
            this.datetime = Utils.formatDate(datetime);
            this.zip = zip;
            this.address = address;
            this.users = users;
        }
    }

    public Datetime(PackageSelectionActivity mainActivity)
    {
        super(mainActivity, R.layout.pages_datetime, R.id.dateTimeModelContainer);
    }

    @Override
    public boolean onFilter(String searchKeyword, Model user) {
        return user.datetime.toLowerCase().contains(searchKeyword) ||
                user.zip.toLowerCase().contains(searchKeyword) ||
                user.address.toLowerCase().contains(searchKeyword);
    }

    @Override
    public Model buildModelFromJson(JSONObject datetime) throws JSONException {
        return new Model(
            datetime.getInt("id"),
            datetime.getString("start_at"),
            datetime.getString("zip"),
            datetime.getString("address"),
            datetime.getJSONArray("checkedInUsers")
        );
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onRender(Model datetime, int index)
    {
        View cardView = this.mainActivity.getLayout(R.layout.components_datetime_card, null);
        TextView textViewTitle = cardView.findViewById(R.id.textViewTitle);
        TextView textViewDescription = cardView.findViewById(R.id.textViewDescription);

        textViewTitle.setText(datetime.datetime);
        textViewDescription.setText("Address: " + datetime.address);

        cardView.setOnClickListener(v -> mainActivity.setDatetime(datetime));

        return PackageSelectionActivity.addVerticalMargin(cardView, 15);
    }
}

package com.android.montelongoworldwide.pages;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.android.R;
import com.android.montelongoworldwide.PackageSelectionActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Event extends Page<Event.Model> {
    public class Model {

        public final int id;
        public final String name, shortName, color;
        public JSONArray datetimes;

        public Model(int id, String name, JSONArray datetimes)
        {
            this.id = id;
            this.name = name;
            this.datetimes = datetimes;

            if (name.equals("Front End") || name.equals("New Leads")) {
                this.shortName = "FE";
                this.color = "#6c9512";
            } else if (name.equals("Middle End")) {
                this.shortName = "ME";
                this.color = "#e63757";
            } else if (name.equals("Bus Tour")) {
                this.shortName = "BE";
                this.color = "#821aa8";
            } else {
                this.color = "#1b457b";
                this.shortName = "MM";
            }
        }
    }

    public Event(PackageSelectionActivity mainActivity)
    {
        super(mainActivity, R.layout.pages_event, R.id.eventModelContainer);
    }

    @Override
    public boolean onFilter(String searchKeyword, Model user) {
        return user.name.toLowerCase().contains(searchKeyword);
    }

    @Override
    public Model buildModelFromJson(JSONObject event) throws JSONException {
        return new Model(event.getInt("id"), event.getString("name"), event.getJSONArray("startDates"));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onRender(Model event, int index)
    {
        View cardView = this.mainActivity.getLayout(R.layout.components_event_card, null);
        CardView imageContainer = cardView.findViewById(R.id.imageContainer);
        TextView eventAbbreviation = cardView.findViewById(R.id.eventAbbreviation);
        TextView textViewTitle = cardView.findViewById(R.id.textViewTitle);

        textViewTitle.setText(event.name);
        imageContainer.setCardBackgroundColor(Color.parseColor(event.color));
        eventAbbreviation.setText(event.shortName);

        cardView.setOnClickListener(v -> mainActivity.setEvent(event));

        return PackageSelectionActivity.addVerticalMargin(cardView, 15);
    }
}

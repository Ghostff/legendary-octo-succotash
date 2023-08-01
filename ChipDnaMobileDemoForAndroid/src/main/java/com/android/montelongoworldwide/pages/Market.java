package com.android.montelongoworldwide.pages;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.R;
import com.android.montelongoworldwide.PackageSelectionActivity;
import com.android.montelongoworldwide.Utils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Market extends Page<Market.Model> {
    public class Model
    {
        public final int id;
        public final String city, state, time, type;

        public JSONArray events;

        public Model(int id, String city, String state, String time, String type, JSONArray events) {
            this.id = id;
            this.city = city;
            this.state = state;
            this.time = time;
            this.type = type;
            this.events = events;
        }

        @NotNull
        @Override
        public String toString() {
            return String.format("%s, %s", this.city, this.state);
        }
    }

    public Market(PackageSelectionActivity mainActivity)
    {
        super(mainActivity, R.layout.pages_market, R.id.marketModelContainer);
    }

    public Model buildModelFromJson(JSONObject market) throws JSONException {
        return new Model(
                market.getInt("id"),
                market.getString("city"),
                market.getString("state"),
                Utils.formatDate(market.getJSONArray("dates").getJSONObject(0).getString("start_at")),
                market.getString("type"),
                market.getJSONArray("events")
        );
    }

    @Override
    public boolean onFilter(String searchKeyword, Model market)
    {
        return market.city.toLowerCase().contains(searchKeyword) ||
                market.state.toLowerCase().contains(searchKeyword) ||
                market.time.toLowerCase().contains(searchKeyword);
    }

    @Override
    public View onRender(Model market, int index)
    {
        View cardView = this.mainActivity.getLayout(R.layout.components_market_card, null);

        TextView titleTextView = cardView.findViewById(R.id.textViewTitle);
        ImageView iconView = cardView.findViewById(R.id.icon);
        TextView descriptionTextView = cardView.findViewById(R.id.textViewDescription);

        titleTextView.setText(String.format("%s, %s", market.city, market.state));
        descriptionTextView.setText(market.time);
        iconView.setImageResource(market.type.equals("cannabis") ? R.drawable.cannabis : R.drawable.real_estate);

        // Set an OnClickListener for the cardContainer
        cardView.setOnClickListener(v -> {
            // Handle the cardContainer click event here
            mainActivity.setMarket(market);
        });

        return PackageSelectionActivity.addVerticalMargin(cardView, 15);
    }

}

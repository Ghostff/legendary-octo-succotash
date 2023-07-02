package com.android.montelongoworldwide.pages;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.R;
import com.android.montelongoworldwide.PackageSelectionActivity;

import java.util.ArrayList;
import java.util.List;

public class Market extends Page<Market.Model> {
    public class Model
    {
        public final String city, state, time, type;

        public Model(String city, String state, String time, String type) {
            this.city = city;
            this.state = state;
            this.time = time;
            this.type = type;
        }
    }


    protected List<Model> markets;


    public Market(PackageSelectionActivity mainActivity)
    {
        super(mainActivity, R.layout.pages_market, R.id.marketModelContainer);
        this.load();
    }

    public void load()
    {
        this.markets = new ArrayList<Model>() {{
            add(new Model("Houston", "TX", "10/12/2023 09:30 PM", "real-estate"));
            add(new Model("San Antonio", "TX", "10/22/2023 08:00 PM", "real-estate"));
            add(new Model("Montgomery", "AL", "02/12/2023 05:30 PM", "cannabis"));
            add(new Model("Little Rock", "AK", "12/01/2023 07:00 PM", "real-estate"));
            add(new Model("Tallahassee", "FL", "04/22/2023 09:30 PM", "real-estate"));
        }};
    }

    @Override
    public List<Model> getModels() {
        return this.markets;
    }

    @Override
    public boolean onFilter(String searchKeyword, Model market)
    {
        return market.city.toLowerCase().contains(searchKeyword) ||
                market.state.toLowerCase().contains(searchKeyword) ||
                market.time.toLowerCase().contains(searchKeyword);
    }

    @Override
    public View onRender(Model market)
    {
        View cardView = this.mainActivity.getLayout(R.layout.components_market_card_component, null);

        TextView titleTextView = cardView.findViewById(R.id.textViewTitle);
        ImageView iconView = cardView.findViewById(R.id.icon);
        TextView descriptionTextView = cardView.findViewById(R.id.textViewDescription);

        titleTextView.setText(String.format("%s, %s", market.city, market.state));
        descriptionTextView.setText(market.time);
        iconView.setImageResource(market.type.equals("cannabis") ? R.drawable.cannabis : R.drawable.real_estate);

        // Set an OnClickListener for the cardContainer
        cardView.setOnClickListener(v -> {
            // Handle the cardContainer click event here
            mainActivity.selectMarket(market);
        });

        return PackageSelectionActivity.addVerticalMargin(cardView, 15);
    }

}

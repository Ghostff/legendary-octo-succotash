package com.android.montelongoworldwide.pages;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.android.R;
import com.android.montelongoworldwide.PackageSelectionActivity;
import com.android.montelongoworldwide.Utils;
import org.json.JSONException;
import org.json.JSONObject;

public class Package extends Page<Package.Model>
{
    public class Model
    {
        public final String id, name, description;
        public final int price;

        public Model(String id, String name, String description, int price) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
        }
    }

    protected boolean showPrice = false;

    public Package setShowPrice(boolean showPrice) {
        this.showPrice = showPrice;

        return this;
    }

    public Package(PackageSelectionActivity mainActivity)
    {
        super(mainActivity, R.layout.pages_package, R.id.packageModelContainer);
    }

    @Override
    public boolean onFilter(String searchKeyword, Model model) {
        return model.description.toLowerCase().contains(searchKeyword) ||
                String.valueOf(model.price).toLowerCase().contains(searchKeyword) ||
                model.name.toLowerCase().contains(searchKeyword);
    }

    @Override
    public Model buildModelFromJson(JSONObject pkg) throws JSONException {
        return new Model(
            pkg.getString("Id"),
            pkg.getString("Name"),
            pkg.getString("Description"),
            Integer.parseInt(pkg.getString("UnitPrice")) * 100
        );
    }

    @Override
    public View onRender(Model pkg, int index) {
        View cardView = this.mainActivity.getLayout(R.layout.components_package_card, null);

        ImageView bgImageView = cardView.findViewById(R.id.packageBackgroundImage);
        TextView nameView = cardView.findViewById(R.id.packageNameView);
        TextView priceView = cardView.findViewById(R.id.packagePriceView);
        TextView titleView = cardView.findViewById(R.id.packageTitleView);
        CardView planInterCardView = cardView.findViewById(R.id.planInterCardView);

        nameView.setText(pkg.name);
        priceView.setText(this.showPrice ? Utils.formatAmount(pkg.price) : "");
        titleView.setText(pkg.description);

        if (pkg.price == 30_000 * 100) {
            bgImageView.setImageResource(R.drawable.platinum);
        } else if (pkg.price == 17_000 * 100) {
            bgImageView.setImageResource(R.drawable.gold);
        } else {
            bgImageView.setImageResource(R.drawable.silver);
        }

        // Set an OnClickListener for the cardContainer
        cardView.setOnClickListener(v -> {
            // Handle the cardContainer click event here
            mainActivity.setPackage(pkg);
        });

        if (index > 0) {
            int textColor = Color.parseColor("#f3f3f3");
            planInterCardView.setCardBackgroundColor(Color.parseColor("#1a1a1a"));
            nameView.setTextColor(textColor);
            priceView.setTextColor(textColor);
            titleView.setTextColor(textColor);
        }

        return PackageSelectionActivity.addVerticalMargin(cardView, 15);
    }
}

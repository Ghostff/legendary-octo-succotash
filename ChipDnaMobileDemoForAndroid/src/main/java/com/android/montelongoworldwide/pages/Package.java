package com.android.montelongoworldwide.pages;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.android.R;
import com.android.montelongoworldwide.PackageSelectionActivity;

import java.util.ArrayList;

public class Package extends Page<Package.Model>
{
    public class Model
    {
        public final String title, name;
        public final int price;

        public Model(String name, int price, String title) {
            this.price = price;
            this.name = name;
            this.title = title;
        }

        public String getHumanPrice()
        {
            return String.format("$%,.2f", this.price / 100.0);
        }
    }

    public Package(PackageSelectionActivity mainActivity)
    {
        super(mainActivity, R.layout.pages_package, R.id.packageModelContainer);
        this.load();
    }

    @Override
    public void load() {
        this.models = new ArrayList<Model>() {{
            add(new Model("Platinum", 30_000 * 100, "3 Days Workshop"));
            add(new Model("Gold", 17_000 * 100, "Advanced Bus Tour"));
            add(new Model("Silver", 10_000 * 100, "Mentoring"));
        }};
    }

    @Override
    public boolean onFilter(String searchKeyword, Model model) {
        return model.title.toLowerCase().contains(searchKeyword) ||
                String.valueOf(model.price).toLowerCase().contains(searchKeyword) ||
                model.name.toLowerCase().contains(searchKeyword);
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
        priceView.setText(pkg.getHumanPrice());
        titleView.setText(pkg.title);

        if (pkg.name.equalsIgnoreCase("platinum")) {
            bgImageView.setImageResource(R.drawable.platinum);
        } else if (pkg.name.equalsIgnoreCase("gold")) {
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

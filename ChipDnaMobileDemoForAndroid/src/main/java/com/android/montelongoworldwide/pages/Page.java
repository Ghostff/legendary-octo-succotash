package com.android.montelongoworldwide.pages;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.R;
import com.android.montelongoworldwide.PackageSelectionActivity;
import com.android.montelongoworldwide.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Page<T> extends AbstractToggleable
{
    protected final PackageSelectionActivity mainActivity;
    protected final LinearLayout cardContainer;
    protected final View noResultTextView;
    protected List<T> models = new ArrayList<>();

    public Page(PackageSelectionActivity mainActivity, int pageLayout, int cardContainer) {
        ViewGroup parentLayout = mainActivity.findViewById(R.id.mainPageContainer);
        this.layout = mainActivity.getLayout(pageLayout, parentLayout, false);

        // Add layout1 to the parent layout
        parentLayout.addView(this.layout);
        this.noResultTextView = mainActivity.getLayout(R.layout.components_no_result_found, null);
        this.cardContainer = this.layout.findViewById(cardContainer);
        this.mainActivity = mainActivity;
        this.setVisibility(false);
    }

    public abstract boolean onFilter(String searchKeyword, T model);
    public abstract T buildModelFromJson(JSONObject jsonObject) throws JSONException;
    public abstract View onRender(T model, int index);

    public void render(String searchKeyword)
    {
        // Remove all existing card components from the cardContainer
        this.cardContainer.removeAllViews();

        List<T> filteredList = this.models;
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            filteredList = PackageSelectionActivity.filterList(filteredList, model -> onFilter(searchKeyword, model));
        }

        if (filteredList.size() == 0) {
            this.cardContainer.addView(this.noResultTextView);
            return;
        }

        int index = 0;
        // Create clones of the card component and set dynamic data
        for (T model : filteredList) {
            int finalIndex = index++;
            this.mainActivity.runOnUiThread(() -> {
                this.cardContainer.addView(this.onRender(model, finalIndex));
            });
        }
    }

    public Page<T> setModels(JSONArray jsonArray)
    {
        if (jsonArray != null) {
            this.models.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    this.models.add(this.buildModelFromJson(jsonArray.getJSONObject(i)));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        this.render(null);

        return this;
    }
}

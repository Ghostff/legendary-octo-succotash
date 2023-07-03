package com.android.montelongoworldwide.pages;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.R;
import com.android.montelongoworldwide.PackageSelectionActivity;

import java.util.List;

public abstract class Page<T>
{
    protected PackageSelectionActivity mainActivity;
    protected View layout;
    protected LinearLayout cardContainer;
    protected View noResultTextView;
    protected List<T> models;

    public Page(PackageSelectionActivity mainActivity, int pageLayout, int cardContainer) {
        ViewGroup parentLayout = mainActivity.findViewById(R.id.mainPageContainer);
        this.layout = mainActivity.getLayout(pageLayout, parentLayout, false);

        // Add layout1 to the parent layout
        parentLayout.addView(this.layout);
        this.noResultTextView = mainActivity.getLayout(R.layout.components_no_result_found, null);
        this.cardContainer = this.layout.findViewById(cardContainer);
        this.mainActivity = mainActivity;

    }

    public abstract void load();
    public abstract boolean onFilter(String searchKeyword, T model);
    public abstract View onRender(T model);
    public void setVisibility(boolean visible)
    {
        this.layout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

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

        // Create clones of the card component and set dynamic data
        for (T model : filteredList) {
            this.cardContainer.addView(this.onRender(model));
        }
    }
}

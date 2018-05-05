package hn.cqf.com.lib;

import android.view.View;
import android.view.ViewGroup;

/**
 * @desc ${TODD}
 */

public interface CustomAdapter {
    int getCount();

    Object getItem(int position);

    View getView(int position, ViewGroup parent);

    int getItemViewType(int position);

    int getViewTypeCount();
}

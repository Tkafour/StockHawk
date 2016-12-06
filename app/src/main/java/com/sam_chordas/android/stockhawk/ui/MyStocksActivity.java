package com.sam_chordas.android.stockhawk.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.widget.StockRemoteViewsFactory;

public class MyStocksActivity extends AppCompatActivity implements StockListFragment.Callbacks {

    private final String STOCK_TO_FRAGMENT = "stock";

    protected Fragment createFragment() {
        return new StockListFragment();
    }

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction().add(R.id.fragment_container, fragment).addToBackStack(null).commit();
        }
    }

    public void onStockSelected(String stockName) {

        Fragment stockDetail = new StockDetailFragment();

        Bundle bundle = new Bundle();
        bundle.putString(STOCK_TO_FRAGMENT, stockName);
        stockDetail.setArguments(bundle);

        if (findViewById(R.id.stock_detail_fragment) == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, stockDetail)
                    .addToBackStack(null)
                    .commit();
        } else {

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.stock_detail_fragment, stockDetail)
                    .addToBackStack(null)
                    .commit();
        }
    }


}

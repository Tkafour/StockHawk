package com.sam_chordas.android.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.HistoryQuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


public class StockDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String STOCK_TO_FRAGMENT = "stock";
    private static final int CURSOR_LOADER_ID = 0;
    private static final int HISTORY_CURSOR_LOADER_ID = 1;

    private String mStockName;

    private TextView mStockText;
    private TextView mStockPercentChange;
    private TextView mStockChange;
    private TextView mStockPrice;
    private TextView mStockCreated;
    private TextView mStockSelected;

    private LineChart mStockDynamic;

    private List<String> mDateList = new ArrayList<>();


    public StockDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stock_detail, container, false);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mStockName = bundle.getString(STOCK_TO_FRAGMENT);
        }

        mStockDynamic = (LineChart) v.findViewById(R.id.stock_chart);
        mStockDynamic.setViewPortOffsets(0, 0, 0, 0);

        mStockDynamic.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                Float yValue = e.getY();
                Float xValue = e.getX();

                String stockValue = getResources().getString(R.string.stock_price) + yValue.toString() + " " + getResources().getString(R.string.date) + mDateList.get(xValue.intValue());

                mStockSelected.setText(stockValue);
            }

            @Override
            public void onNothingSelected() {

            }
        });

        XAxis x = mStockDynamic.getXAxis();
        x.setEnabled(true);

        YAxis y = mStockDynamic.getAxisLeft();
        y.setLabelCount(10, false);
        y.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        y.setDrawGridLines(false);
        y.setAxisLineColor(Color.WHITE);
        y.setTextColor(Color.WHITE);

        mStockDynamic.setDragEnabled(true);
        mStockDynamic.setScaleEnabled(true);
        mStockDynamic.setPinchZoom(true);

        mStockDynamic.getAxisRight().setEnabled(true);

        mStockPercentChange = (TextView) v.findViewById(R.id.stock_percent_change);
        mStockChange = (TextView) v.findViewById(R.id.stock_change);
        mStockCreated = (TextView) v.findViewById(R.id.stock_created);
        mStockPrice = (TextView) v.findViewById(R.id.stock_bid_price);

        mStockText = (TextView) v.findViewById(R.id.stock_name);
        mStockText.setText(mStockName);

        mStockSelected = (TextView) v.findViewById(R.id.stock_selected);

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        getLoaderManager().initLoader(HISTORY_CURSOR_LOADER_ID, null, this);
        return v;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        switch (id) {
            case CURSOR_LOADER_ID:
                return new CursorLoader(getActivity(), QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{QuoteColumns.BIDPRICE, QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.CREATED, QuoteColumns.ISUP},
                        QuoteColumns.SYMBOL + " = ?",
                        new String[]{mStockName},
                        null);
            case HISTORY_CURSOR_LOADER_ID:
                return new CursorLoader(getActivity(), QuoteProvider.HistoryQuotes.CONTENT_URI,
                        new String[]{HistoryQuoteColumns._ID, HistoryQuoteColumns.SYMBOL, HistoryQuoteColumns.DATE, HistoryQuoteColumns.OPENPRICE},
                        QuoteColumns.SYMBOL + " = ?",
                        new String[]{mStockName},
                        HistoryQuoteColumns.DATE + " ASC");
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case CURSOR_LOADER_ID:
                if (data != null && data.moveToFirst()) {
                    mStockPrice.setText(data.getString((data.getColumnIndex(QuoteColumns.BIDPRICE))));
                    mStockPercentChange.setText(data.getString((data.getColumnIndex(QuoteColumns.PERCENT_CHANGE))));
                    mStockChange.setText(data.getString((data.getColumnIndex(QuoteColumns.CHANGE))));
                    mStockCreated.setText(data.getString((data.getColumnIndex(QuoteColumns.CREATED))));
                }
                data.close();

            case HISTORY_CURSOR_LOADER_ID:
                try {
                    if (data != null && data.moveToFirst()) {
                        List<Entry> graphicData = new ArrayList<Entry>();
                        int day = 0;
                        while (data.moveToNext()) {
                            String price = data.getString(data.getColumnIndex(HistoryQuoteColumns.OPENPRICE));
                            mDateList.add(data.getString(data.getColumnIndex(HistoryQuoteColumns.DATE)));
                            Float entryPrice = Float.parseFloat(price);
                            graphicData.add(new Entry(day, entryPrice));
                            ++day;
                        }
                        LineDataSet dataSet = new LineDataSet(graphicData, mStockName);
                        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                        dataSet.setValueTextColor(Color.WHITE);
                        dataSet.setFillColor(Color.WHITE);
                        dataSet.setCubicIntensity(0.2f);


                        LineData lineData = new LineData(dataSet);


                        mStockDynamic.setData(lineData);
                        mStockDynamic.invalidate();
                        data.close();
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}

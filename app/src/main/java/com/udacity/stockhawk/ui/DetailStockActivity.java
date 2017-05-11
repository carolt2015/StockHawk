package com.udacity.stockhawk.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailStockActivity extends AppCompatActivity implements
        SwipeRefreshLayout.OnRefreshListener, LoaderManager.LoaderCallbacks<Cursor> {

//    @BindView(R.id.toolbar)
//    Toolbar toolbar;
    @BindView(R.id.swipe_refresh_detail1)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.error_detail)
    TextView error;
    @BindView(R.id.text_view_price)
    TextView priceView;
    @BindView(R.id.text_view_percent_change)
    TextView percentView;
    @BindView(R.id.text_view_change)
    TextView changeView;
    @BindView(R.id.text_current_date)
    TextView current_date;
    @BindView(R.id.text_view_symbol)
    TextView symbolView;
    @BindView(R.id.line_chart)
    LineChart lineChart;


    private static final int STOCKS_LOADER = 1;
    public static final String STOCK_SYMBOL = "STOCK_SYMBOL";
    private String stock_symbol;

    private boolean chartAnimated = false;
    private DecimalFormat percentageFormat;
    private DecimalFormat dollarFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail_stock);
        ButterKnife.bind(this);

        stock_symbol = getIntent().getExtras().getString(STOCK_SYMBOL);

        setTitle(stock_symbol);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();

        getSupportLoaderManager().initLoader(STOCKS_LOADER, null, this);

        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormat.setMaximumFractionDigits(2);
        dollarFormat.setMinimumFractionDigits(2);

        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);

        String currentDate = DateFormat.getDateTimeInstance().format(new Date());
        String[] currDate = currentDate.split(",");
        String []currTime = currentDate.split(" ");


        current_date.setText(currDate[0] +", "+ currTime[3] +" " + currTime[4]);
      }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                Contract.Quote.COLUMN_SYMBOL + "=?",
                new String[]{stock_symbol},
                Contract.Quote.COLUMN_SYMBOL);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swipeRefreshLayout.setRefreshing(false);
        if (data != null) {
            data.moveToFirst();

            priceView.setText(dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE)) +
                    " " + getString(R.string.currency_label));

            symbolView.setText("(" + data.getString(Contract.Quote.POSITION_SYMBOL)+ ")");


            float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            changeView.setText(data.getString((Contract.Quote.POSITION_ABSOLUTE_CHANGE)));

            float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
            String percentage = percentageFormat.format(percentageChange / 100);
            percentView.setText("("+(percentage)+ ")");

            if (rawAbsoluteChange > 0) {
                changeView.setTextColor(getColor(R.color.material_green_700));
                percentView.setTextColor(getColor(R.color.material_green_700));
            } else {
                changeView.setTextColor(getColor(R.color.material_red_700));
                percentView.setTextColor(getColor(R.color.material_red_700));
            }

                String history = data.getString(Contract.Quote.POSITION_HISTORY);
                fillChart(history);

        }
  }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void fillChart(String historyBuilder) {

        List<Entry> entries = new ArrayList<>();

        String[] lines = historyBuilder.split("\\n");
        int linesLength = lines.length;
        final String[] dates = new String[linesLength];

        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy", Locale.US);
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < linesLength; i++){

            String[] dateAndPrice = lines[linesLength - i - 1].split(", ");
            calendar.setTimeInMillis(Long.valueOf(dateAndPrice[0]));
            dates[i] = formatter.format(calendar.getTime());
            entries.add(new Entry(i, Float.valueOf(dateAndPrice[1])));

        }
        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.chart_label));
        XAxis xAxis = lineChart.getXAxis();

        // Set Colors
        dataSet.setValueTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        LineData lineData = new LineData(dataSet);
        xAxis.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        lineChart.getAxisLeft().setTextColor(
                ContextCompat.getColor(getApplicationContext(), R.color.white));
        lineChart.getAxisRight().setTextColor(
                ContextCompat.getColor(getApplicationContext(), R.color.white));
        lineChart.getLegend().setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));

        // Set date labels for x-axis
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                return dates[(int) value];
            }

          });

        Description description = new Description();
        description.setText(getString(R.string.chart_description));
        description.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        lineChart.setDescription(description);
        lineChart.setData(lineData);

        if (!chartAnimated) {
            lineChart.animateX(2000);
            chartAnimated = true;
        }
    }


    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onRefresh() {


        if (!networkUp()) {
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_network));
            error.setVisibility(View.VISIBLE);
        } else if (!networkUp()) {
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();

        } else if (PrefUtils.getStocks(this).size() == 0) {
            swipeRefreshLayout.setRefreshing(false);
            error.setText(getString(R.string.error_no_stocks));
            error.setVisibility(View.VISIBLE);


        } else {
            error.setVisibility(View.GONE);


        }
    }
}




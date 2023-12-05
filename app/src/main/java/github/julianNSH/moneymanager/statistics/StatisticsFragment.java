package github.julianNSH.moneymanager.statistics;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import github.julianNSH.moneymanager.R;
import github.julianNSH.moneymanager.database.DatabaseClass;
import github.julianNSH.moneymanager.statistics.SortData.SortMode;

public class StatisticsFragment extends Fragment {

    private final String[] sortParameters = {"Perioada Desc.", "Perioada Asc.", "Suma Desc.", "Suma Asc.", "A-Z", "Z-A"};
    private OnPositionChangedListener onPositionChangedListener;
    private Calendar date;
    private View root;
    private int currentMonth;
    private int currentYear;
    private String sortParamKey;
    private Button statisticsDateButton;
    private GestureDetector gestureDetector;
    private RecyclerView recyclerView;
    private StatisticsAdapter statisticsAdapter;
    private DatabaseClass databaseClass;
    private ArrayList<StatisticsModelClass> statisticsModelClasses;
    private LinearLayout[] llArr;
    private float totalSpending;
    private DatePickerDialog datePicker;
    private TextView infoText;
    private PieChart pieChart;
    private ArrayList<Integer> pieColors;
    private ArrayList<PieEntry> pieEntries;
    private int position;
    public StatisticsFragment() {
        // Required empty public constructor
    }


    public static StatisticsFragment getInstance(int position) {
        StatisticsFragment fragment = new StatisticsFragment();
        fragment.setPosition(position);
        return fragment;
    }

    public void setPosition(int position) {
        this.position = position;
        handlePositionChanged();
    }

    private void handlePositionChanged() {
        if (onPositionChangedListener != null) {
            onPositionChangedListener.onPositionChanged(position); // position là vị trí mới
        }
    }

    public void setOnPositionChangedListener(OnPositionChangedListener listener) {
        this.onPositionChangedListener = listener;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"ResourceType", "SetTextI18n", "DefaultLocale"})
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_statistics, container, false);;

        date = Calendar.getInstance();
        sortParamKey = "Perioada Desc.";
        currentMonth = date.get(Calendar.MONTH) + 1;
        currentYear = date.get(Calendar.YEAR);

        statisticsDateButton = root.findViewById(R.id.btn_date);
        statisticsDateButton.setText("Tháng" + position);


        llArr = new LinearLayout[6];
        for (int i = 0; i < 6; i++) {
            int resID = getResources().getIdentifier("ll" + (i + 1), "id", root.getContext().getPackageName());
            llArr[i] = root.findViewById(resID);
            llArr[i].setVisibility(LinearLayout.GONE);
        }

        Spinner spinner = root.findViewById(R.id.spinnerSort);
        ArrayAdapter arrayAdapter = new ArrayAdapter(root.getContext(), android.R.layout.simple_spinner_dropdown_item, sortParameters);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortParamKey = sortParameters[position];
                showStatisticsData(root, currentMonth, currentYear, sortParamKey);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        showStatisticsData(root, currentMonth, currentYear, sortParamKey);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Update UI based on the initial position
        updateUI(position);
    }

    public void updateUI(int newPosition) {
        // Example: Update a TextView based on the new position
        TextView positionTextView = requireView().findViewById(R.id.btn_date);
        positionTextView.setText("Tháng " + newPosition);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint({"ResourceType", "SetTextI18n", "DefaultLocale"})
    public void showStatisticsData(View view, int month, int year, String sortParam) {
        @SuppressLint("DefaultLocale") String date = String.format("%04d-%02d", year, month);

        databaseClass = new DatabaseClass(getContext());
        infoText = view.findViewById(R.id.infoText);
        pieChart = view.findViewById(R.id.pieChart);
        recyclerView = view.findViewById(R.id.rv_statistics_list);

        ArrayList<StatisticsModelClass> distinctMC = databaseClass.getDistinctOutgoingsAmountByDate(date);
        statisticsModelClasses = databaseClass.getOutgoingDataByMonthYear(date);
        statisticsAdapter = new StatisticsAdapter(view.getContext(), statisticsModelClasses);

        if (statisticsModelClasses.size() == 0) {
            infoText.setVisibility(LinearLayout.VISIBLE);
            pieChart.setVisibility(LinearLayout.GONE);

        } else {
            pieChart.setVisibility(LinearLayout.VISIBLE);
            infoText.setVisibility(LinearLayout.GONE);
        }

        totalSpending = 0;
        float[] biggestCateg = new float[6];
        float[] pbValues = new float[6];
        biggestCateg[5] = 0;

        Collections.sort(distinctMC, new SortMode((byte) 0));
        if (sortParam.equals("Perioada Asc."))
            Collections.sort(statisticsModelClasses, new SortMode((byte) 2));
        if (sortParam.equals("Perioada Desc."))
            Collections.sort(statisticsModelClasses, new SortMode((byte) 3));
        if (sortParam.equals("Suma Desc."))
            Collections.sort(statisticsModelClasses, new SortMode((byte) 0));
        if (sortParam.equals("Suma Asc."))
            Collections.sort(statisticsModelClasses, new SortMode((byte) 1));
        if (sortParam.equals("A-Z"))
            Collections.sort(statisticsModelClasses, new SortMode((byte) 4));
        if (sortParam.equals("Z-A"))
            Collections.sort(statisticsModelClasses, new SortMode((byte) 5));

        for (int i = 0; i < distinctMC.size(); i++) {
            totalSpending += distinctMC.get(i).getTvAmount();
            if (i >= 5) biggestCateg[5] += distinctMC.get(i).getTvAmount();
            if (i <= 4) biggestCateg[i] = distinctMC.get(i).getTvAmount();
        }

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(statisticsAdapter);

        pieEntries = new ArrayList<>();
        pieColors = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            pbValues[i] = (biggestCateg[i] * 100) / totalSpending;
            if (i == distinctMC.size()) break;
        }

        for (int i = 0; i < 6; i++) {
            llArr[i].setVisibility(LinearLayout.GONE);
        }

        for (int i = 0; i < Math.min(distinctMC.size(), 5); i++) {
            TextView cat = view.findViewById(getResources().getIdentifier("cat" + (i + 1), "id", view.getContext().getPackageName()));
            TextView catVal = view.findViewById(getResources().getIdentifier("catval" + (i + 1), "id", view.getContext().getPackageName()));

            cat.setText(distinctMC.get(i).getTvType());
            catVal.setText(biggestCateg[i] + " " + getResources().getString(R.string.currency));

            pieEntries.add(new PieEntry(pbValues[i], String.format("%3.1f", pbValues[i]) + "%"));
            pieColors.add(view.getContext().getColor(getResources().getIdentifier("stat_elem" + (i + 1), "color", view.getContext().getPackageName())));

            llArr[i].setVisibility(LinearLayout.VISIBLE);
            llArr[i].setGravity(Gravity.CENTER_HORIZONTAL);
        }

        TextView catVal6 = view.findViewById(R.id.catval6);
        if (distinctMC.size() >= 6) {
            llArr[5].setVisibility(LinearLayout.VISIBLE);
            llArr[5].setGravity(Gravity.CENTER_HORIZONTAL);
            catVal6.setText(biggestCateg[5] + " " + getResources().getString(R.string.currency));
            pieEntries.add(new PieEntry(pbValues[5], String.format("%3.1f", pbValues[5]) + "%"));
            pieColors.add(view.getContext().getColor(R.color.stat_elem6));
        }

        pieChart = view.findViewById(R.id.pieChart);
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
        pieDataSet.setColors(pieColors);
        PieData pieData = new PieData(pieDataSet);

        pieDataSet.setDrawValues(false);

        pieChart.setUsePercentValues(true);
        pieChart.setDescription(null);
        pieChart.getLegend().setEnabled(false);

        pieChart.setCenterTextSize(18);
        pieChart.setEntryLabelColor(view.getResources().getColor(R.color.white));
        pieChart.setCenterText("Tổng chi tiêu \n" + totalSpending + " " + getResources().getString(R.string.currency));
        pieChart.setHoleRadius(60);
        pieChart.setTransparentCircleRadius(65);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    //    private ArrayList<String> createMonthList() {
//        ArrayList<String> monthList = new ArrayList<>();
//        return monthList;
//    }
//    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
//    }

}


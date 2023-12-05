package github.julianNSH.moneymanager.overview;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.*;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.*;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.*;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import github.julianNSH.moneymanager.CustomDateParser;
import github.julianNSH.moneymanager.R;
import github.julianNSH.moneymanager.database.DatabaseClass;
import github.julianNSH.moneymanager.overview.SortData.SortMode;

public class OverviewFragment extends Fragment {
    ////////////////////////////////////////////////////////////////Chart Elements
    private final int MAX_X_VALUE = 7;
    private final int GROUPS = 2;
    private static String GROUP_1_LABEL;
    private static String GROUP_2_LABEL;
    private static final float BAR_SPACE = 0.05f;
    private static final float BAR_WIDTH = 0.2f;
    private BarChart chart;

    ////////////////////////////////////////////////////////////////Recycler Elements
    private ArrayList<OverviewModelClass> overviewModelClasses;
    private RecyclerView recyclerView;
    private OverviewAdapter overviewAdapter;

    //////////////////////////
    private DatabaseClass databaseClass;
    private TextView incomeOverview, outgoingOverview, inOutView;
    private DatePickerDialog datePicker;
    private Calendar date;
//    private int  currentMonth, currentYear;

    ///////////////////////////////////
    private String[] sortParameters = {"Mới nhất", "Cũ nhất" , "Số tiền giảm dần", "Số tiền tăng dần","A-Z","Z-A"};
    private String sortParamKey;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"SetTextI18n", "NonConstantResourceId", "DefaultLocale"})
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState){
        View root = inflater.inflate(R.layout.fragment_overview, container, false);


        //////////////DATE PICKER
        date = Calendar.getInstance();


        sortParamKey = "Mới nhất";
        showOverviewData(root, sortParamKey); //on first run

        ////On sort spinner click
        Spinner spinner = root.findViewById(R.id.spinnerSort);
        ArrayAdapter arrayAdapter = new ArrayAdapter(root.getContext(), android.R.layout.simple_spinner_dropdown_item, sortParameters);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortParamKey = sortParameters[position];
                showOverviewData(root,sortParamKey);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return root;
    }

    @SuppressLint({"ResourceType", "SetTextI18n", "DefaultLocale"})
    public void showOverviewData(View view, String sortParam){
        databaseClass = new DatabaseClass(getContext());
        overviewModelClasses = databaseClass.getOverviewDataAll();
        float totalIncome=databaseClass.getTotalIncomeAll();
        float totalOutgoing=databaseClass.getTotalOutgoingAll();
        float inOutRatio;

        LinearLayout linearLayoutChart = view.findViewById(R.id.barChartLayout);
        LinearLayout linearLayoutOverview = view.findViewById(R.id.overviewLayout);
        LinearLayout parentLayout = view.findViewById(R.id.parentLayout);

        if(overviewModelClasses.size()==0){
            parentLayout.setGravity(Gravity.CENTER_VERTICAL);
            linearLayoutChart.setVisibility(LinearLayout.GONE);
            linearLayoutOverview.setVisibility(LinearLayout.GONE);
        } else {
            linearLayoutChart.setVisibility(LinearLayout.VISIBLE);
            linearLayoutOverview.setVisibility(LinearLayout.VISIBLE);
        }

        //TOP LAYOUT 
        if(overviewModelClasses!=null) {
            incomeOverview = view.findViewById(R.id.incomeOverview);
            incomeOverview.setText("+"+totalIncome +" "+ getResources().getString(R.string.currency));
            outgoingOverview = view.findViewById(R.id.outgoingOverview);
            outgoingOverview.setText("-"+totalOutgoing +" "+ getResources().getString(R.string.currency));

            //Exclude possibility of divide by zero
            if(totalIncome==0) {
                inOutRatio = totalOutgoing * 100;
            } else {inOutRatio = totalOutgoing*100/totalIncome;}

            inOutView = view.findViewById(R.id.in_out_percent);
            inOutView.setText(String.format("%3.1f",inOutRatio)+"%");
            if(inOutRatio<=50){
                inOutView.setTextColor(getResources().getColor(R.color.lvl1));
                inOutView.getBackground().setColorFilter(getResources().getColor(R.color.lvl1),
                        PorterDuff.Mode.SRC_ATOP);
            }
            if(inOutRatio>50 && inOutRatio<=80){
                inOutView.setTextColor(getResources().getColor(R.color.lvl2));
                inOutView.getBackground().setColorFilter(getResources().getColor(R.color.lvl2),
                        PorterDuff.Mode.SRC_ATOP);
            }
            if(inOutRatio>80){
                inOutView.setTextColor(getResources().getColor(R.color.lvl3));
                inOutView.getBackground().setColorFilter(getResources().getColor(R.color.lvl3),
                        PorterDuff.Mode.SRC_ATOP);
            }
            if (inOutRatio>100)inOutView.setText(">100%");
        }

        ////RECICLERVIEW
        recyclerView = view.findViewById(R.id.rvTransaction);

        if(overviewModelClasses!=null) {

            if(sortParam.equals("Mới nhất")) Collections.sort(overviewModelClasses, new SortMode((byte) 2));
            if(sortParam.equals("Cũ nhất")) Collections.sort(overviewModelClasses, new SortMode((byte)3));
            if(sortParam.equals("Số tiền giảm dần")) Collections.sort(overviewModelClasses, new SortMode((byte) 0));
            if(sortParam.equals("Số tiền tăng dần")) Collections.sort(overviewModelClasses, new SortMode((byte) 1));
            if(sortParam.equals("A-Z")) Collections.sort(overviewModelClasses, new SortMode((byte)4));
            if(sortParam.equals("Z-A")) Collections.sort(overviewModelClasses, new SortMode((byte)5));

            for (int i = 0; i <overviewModelClasses.size(); i++) {
                switch (overviewModelClasses.get(i).getTvDomain()){
                    case "income":
                        overviewModelClasses.get(i).setTvDomain(getResources().getString(R.string.income));
                        overviewModelClasses.get(i).setIvFigure(R.drawable.ic_up);
                        break;
                    case "outgoing":
                        overviewModelClasses.get(i).setTvDomain(getResources().getString(R.string.outgoings));
                        overviewModelClasses.get(i).setIvFigure(R.drawable.ic_down);
                        break;
                    default:
                        overviewModelClasses.get(i).setTvDomain("Scop");
                        overviewModelClasses.get(i).setIvFigure(R.drawable.ic_flat);
                }
            }
        }
        overviewAdapter = new OverviewAdapter(view.getContext(),overviewModelClasses);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(overviewAdapter);



        /////CHART
        chart = view.findViewById(R.id.fragment_groupedbarchart_chart);
        BarData data = createChartData();
        configureChartAppearance();
        prepareChartData(data);
    }

    @SuppressLint("DefaultLocale")
    void configureChartAppearance() {
        chart.setPinchZoom(false);
        chart.setDrawBarShadow(false);
        chart.setDrawGridBackground(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.getDescription().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setSpaceTop(35f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(new LargeValueFormatter());

        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setAxisMinimum(0);
        chart.getXAxis().setAxisMaximum(MAX_X_VALUE);
        chart.setDrawBorders(true);
        chart.setDrawGridBackground(true);
    }

    BarData createChartData() {
        ArrayList<BarEntry> values1 = new ArrayList<>();
        ArrayList<BarEntry> values2 = new ArrayList<>();

        values1.add(new BarEntry(0, databaseClass.getTotalIncomeAll()));
        values2.add(new BarEntry(0, databaseClass.getTotalOutgoingAll()));

        GROUP_1_LABEL = getResources().getString(R.string.income);
        GROUP_2_LABEL = getResources().getString(R.string.outgoings);
        BarDataSet set1 = new BarDataSet(values1, GROUP_1_LABEL);
        BarDataSet set2 = new BarDataSet(values2, GROUP_2_LABEL);
        set1.setColor(ColorTemplate.MATERIAL_COLORS[0]);
        set2.setColor(ColorTemplate.MATERIAL_COLORS[2]);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        dataSets.add(set2);

        BarData data = new BarData(dataSets);
        data.setValueFormatter(new LargeValueFormatter());

        ArrayList<String> months = new ArrayList<>();
        months.add(""); // Chỉ để có một giá trị trên trục x

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new MyAxysValueFormatter(months));
        return data;
    }

    void prepareChartData(BarData data) {
        chart.setData(data);

        chart.getBarData().setBarWidth(BAR_WIDTH);

        float groupSpace = 1f - ((BAR_SPACE + BAR_WIDTH) * GROUPS);
        chart.groupBars(0, groupSpace, BAR_SPACE);

        chart.invalidate();
    }
    public static class MyAxysValueFormatter extends IndexAxisValueFormatter {
        private List labels;

        public MyAxysValueFormatter(List<String> labels){
            this.labels=labels;
        }
        @Override
        public String getFormattedValue(float value) {
            try {
                int index = (int) value;
                return String.valueOf(labels.get(index));
            } catch (Exception e) {
                return "";
            }
        }
    }
}

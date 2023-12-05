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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import github.julianNSH.moneymanager.CustomDateParser;
import github.julianNSH.moneymanager.R;
import github.julianNSH.moneymanager.database.DatabaseClass;
import github.julianNSH.moneymanager.statistics.SortData.SortMode;

public class MonthFragment extends Fragment {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private View rootView;
    private Calendar date;
    private int currentMonth;
    private int currentYear;

    public MonthFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint({"ResourceType", "SetTextI18n", "DefaultLocale"})
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_list_trans, container, false);
        View root = rootView;

        // Tìm ViewPager2 và TabLayout trong layout
        viewPager = rootView.findViewById(R.id.viewPager);
        tabLayout = rootView.findViewById(R.id.tabLayout);
        date = Calendar.getInstance();
        currentMonth = date.get(Calendar.MONTH) + 1;
        currentYear = date.get(Calendar.YEAR);

        // Khởi tạo ViewPager2 và Adapter
        MonthPagerAdapter monthPagerAdapter = new MonthPagerAdapter(this);
        viewPager.setAdapter(monthPagerAdapter);

        // Thêm TabLayoutMediator
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText("Tháng " + (position + 1))
        ).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                handlePageSelected(position);
            }
        });
        return root;
    }

    private void handlePageSelected(int selectedMonth) {
        currentMonth = selectedMonth + 1;
    }

}


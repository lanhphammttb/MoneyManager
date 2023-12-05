package github.julianNSH.moneymanager.statistics;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import github.julianNSH.moneymanager.statistics.StatisticsFragment;

public class MonthPagerAdapter extends FragmentStateAdapter {

    private OnPositionChangedListener onPositionChangedListener;

    public MonthPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
        this.onPositionChangedListener = null;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        StatisticsFragment fragment = StatisticsFragment.getInstance(position);

        // Truyền giá trị position từ MonthPagerAdapter sang StatisticsFragment
        fragment.setPosition(position + 1);

        // Set the listener for the fragment
        fragment.setOnPositionChangedListener(onPositionChangedListener);

        return fragment;
    }

    @Override
    public int getItemCount() {
        return 12;
    }
}

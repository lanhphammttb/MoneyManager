package github.julianNSH.moneymanager.ui.statistics;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import github.julianNSH.moneymanager.R;

public class StatisticsAdapter extends RecyclerView.Adapter<StatisticsAdapter.MyViewHolder>{
    StatisticsFragment context;

    private List<StatisticsModelClass> list;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvType,tvPrice;
        ImageView ivMain;

        public MyViewHolder(View view) {
            super(view);

            tvType = (TextView) view.findViewById(R.id.tvType);
            tvPrice = (TextView) view.findViewById(R.id.tvPrice);
            ivMain = (ImageView) view.findViewById(R.id.ivMain);

        }

    }

    public StatisticsAdapter(StatisticsFragment context, List<StatisticsModelClass> moviesList) {
        this.list = moviesList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_element, parent, false);
        return new MyViewHolder(itemView);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(final MyViewHolder holder,final int position) {
        StatisticsModelClass movie = list.get(position);
        holder.tvType.setText(movie.getTvType());
        holder.tvPrice.setText(movie.getTvPrice());
        holder.ivMain.setImageResource(movie.getIvMain());
    }
    @Override
    public int getItemCount() {
        return list.size();
    }


}

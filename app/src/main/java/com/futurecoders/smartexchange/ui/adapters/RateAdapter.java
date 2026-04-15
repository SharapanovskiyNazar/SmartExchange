package com.futurecoders.smartexchange.ui.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.futurecoders.smartexchange.R;
import com.futurecoders.smartexchange.data.entities.CurrencyRateEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RateAdapter extends RecyclerView.Adapter<RateAdapter.Holder> {

    private final List<CurrencyRateEntity> list = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<CurrencyRateEntity> data) {
        list.clear();
        if (data != null) list.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_rate, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        CurrencyRateEntity r = list.get(position);

        h.tvCurrency.setText(r.getCurrency());

        int resId = getFlagRes(h.itemView.getContext(), r.getCurrency());

        if (resId != 0) {
            h.imgFlag.setImageResource(resId);
        } else {
            h.imgFlag.setImageResource(R.drawable.flag_unknown);
        }

        // ровные значения, без "Buy/Sell", без зелёного/красного
        h.tvBuy.setText(String.format(Locale.US, "%.2f", r.getBuyRate()));
        h.tvSell.setText(String.format(Locale.US, "%.2f", r.getSellRate()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {

        TextView tvCurrency, tvBuy, tvSell;
        ImageView imgFlag;
        public Holder(@NonNull View itemView) {
            super(itemView);
            imgFlag = itemView.findViewById(R.id.imgFlag);
            tvCurrency = itemView.findViewById(R.id.tvCurrency);
            tvBuy = itemView.findViewById(R.id.tvBuy);
            tvSell = itemView.findViewById(R.id.tvSell);
        }
    }

    private int getFlagRes(Context context, String currency) {
        String name = currency.toLowerCase(); // "EUR" -> "eur"
        return context.getResources().getIdentifier(
                name,
                "drawable",
                context.getPackageName()
        );
    }
}

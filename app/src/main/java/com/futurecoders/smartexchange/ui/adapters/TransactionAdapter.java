package com.futurecoders.smartexchange.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.futurecoders.smartexchange.R;
import com.futurecoders.smartexchange.data.entities.TransactionEntity;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter
        extends RecyclerView.Adapter<TransactionAdapter.Holder> {

    private List<TransactionEntity> data = new ArrayList<>();

    public void setData(List<TransactionEntity> list) {
        data = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        TransactionEntity t = data.get(position);
        h.tvCurrency.setText(t.getCurrency());
        h.tvType.setText(t.getType());
        h.tvAmount.setText(String.format("%.2f", t.getAmount()));
        h.tvTotal.setText(String.format("%.2f MDL", t.getTotal()));
        h.tvDate.setText(t.getTransactionDate());

        if ("BUY".equalsIgnoreCase(t.getType())) {

            h.tvTotal.setText(
                    String.format("%.2f MDL", t.getTotal())
            );
            h.tvTotal.setTextColor(
                    h.itemView.getContext().getColor(R.color.maib_red)
            );

        } else if ("SELL".equalsIgnoreCase(t.getType())) {

            h.tvTotal.setText(
                    String.format("+%.2f MDL", t.getTotal())
            );
            h.tvTotal.setTextColor(
                    h.itemView.getContext().getColor(R.color.maib_green_dark)
            );
        }
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    static class Holder extends RecyclerView.ViewHolder {

        TextView tvCurrency, tvType, tvAmount, tvTotal, tvDate;
        View rootCard;

        Holder(View v) {
            super(v);
            rootCard = v.findViewById(R.id.rootCard);
            tvCurrency = v.findViewById(R.id.tvCurrency);
            tvType = v.findViewById(R.id.tvType);
            tvAmount = v.findViewById(R.id.tvAmount);
            tvTotal = v.findViewById(R.id.tvTotal);
            tvDate = v.findViewById(R.id.tvDate);
        }
    }

    private void setTextColor(Holder h, int colorRes) {
        int color = h.itemView.getContext().getColor(colorRes);
        h.tvCurrency.setTextColor(color);
        h.tvType.setTextColor(color);
        h.tvAmount.setTextColor(color);
        h.tvTotal.setTextColor(color);
        h.tvDate.setTextColor(color);
    }
}

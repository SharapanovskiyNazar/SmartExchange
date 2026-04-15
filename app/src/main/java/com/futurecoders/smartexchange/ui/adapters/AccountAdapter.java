package com.futurecoders.smartexchange.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.futurecoders.smartexchange.R;
import com.futurecoders.smartexchange.data.entities.AccountEntity;
import com.futurecoders.smartexchange.ui.BuySellActivity;
import com.futurecoders.smartexchange.ui.TransactionHistoryActivity;

import java.util.ArrayList;
import java.util.List;

public class AccountAdapter
        extends RecyclerView.Adapter<AccountAdapter.ViewHolder> {

    private List<AccountEntity> accounts = new ArrayList<>();

    public void setData(List<AccountEntity> list) {
        accounts = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_account, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        AccountEntity account = accounts.get(position);

        holder.tvCurrency.setText(account.getCurrency());
        holder.tvBalance.setText(String.format("%.2f", account.getBalance()));
        int resId = getFlagRes(holder.itemView.getContext(), account.getCurrency());

        if (resId != 0) {
            holder.imgFlag.setImageResource(resId);
        } else {
            holder.imgFlag.setImageResource(R.drawable.flag_unknown);
        }


        if ("MDL".equals(account.getCurrency())) {

            holder.cardAccount.setBackgroundResource(
                    R.drawable.bg_card_green
            );
            holder.btnBuySell.setVisibility(View.GONE);
            holder.btnHistory.setVisibility(View.VISIBLE);

            holder.btnHistory.setOnClickListener(v -> {
                Context ctx = v.getContext();
                ctx.startActivity(
                        new Intent(ctx, TransactionHistoryActivity.class)
                );
            });


        } else {
            holder.btnHistory.setVisibility(View.GONE);

            holder.cardAccount.setBackgroundResource(
                    R.drawable.bg_card_dark
            );
            holder.btnBuySell.setVisibility(View.VISIBLE);

            holder.btnBuySell.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), BuySellActivity.class);
                intent.putExtra("currency", account.getCurrency());
                v.getContext().startActivity(intent);
            });
        }
    }



    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View btnHistory;
        TextView tvCurrency, tvBalance;
        Button btnBuySell;
        CardView cardAccount;
        ImageView imgFlag;

        ViewHolder(View itemView) {
            super(itemView);
            imgFlag = itemView.findViewById(R.id.imgFlag);
            tvCurrency = itemView.findViewById(R.id.tvCurrency);
            tvBalance = itemView.findViewById(R.id.tvBalance);
            btnBuySell = itemView.findViewById(R.id.btnBuySell);
            cardAccount = itemView.findViewById(R.id.cardAccount);
            btnHistory = itemView.findViewById(R.id.btnHistory);
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

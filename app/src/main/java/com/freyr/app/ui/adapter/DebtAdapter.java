package com.freyr.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.freyr.app.R;
import com.freyr.app.data.model.Debt;
import com.freyr.app.data.model.FamilyGroup;
import com.freyr.app.data.model.Payment;
import com.freyr.app.data.repository.FreyrRepository;
import java.util.ArrayList;
import java.util.List;

public class DebtAdapter extends RecyclerView.Adapter<DebtAdapter.DebtViewHolder> {

    public interface OnDebtClickListener {
        void onDebtClick(Debt debt);
        void onPayClick(Debt debt);
    }

    private final Context context;
    private List<Debt> debts = new ArrayList<>();
    private List<Payment> allPayments = new ArrayList<>();
    private List<FamilyGroup> allGroups = new ArrayList<>();
    private final OnDebtClickListener listener;

    public DebtAdapter(Context context, OnDebtClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setDebts(List<Debt> debts) {
        this.debts = debts != null ? debts : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setPayments(List<Payment> payments) {
        this.allPayments = payments != null ? payments : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setGroups(List<FamilyGroup> groups) {
        this.allGroups = groups != null ? groups : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DebtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_debt, parent, false);
        return new DebtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DebtViewHolder holder, int position) {
        Debt debt = debts.get(position);
        holder.tvName.setText(debt.getName());

        if (debt.getDescription() != null && !debt.getDescription().trim().isEmpty()) {
            holder.tvDesc.setText(debt.getDescription());
            holder.tvDesc.setVisibility(View.VISIBLE);
        } else {
            holder.tvDesc.setVisibility(View.GONE);
        }

        // Scope
        if (debt.getFamilyGroupId() != null) {
            String groupName = "Family Group";
            for (FamilyGroup g : allGroups) {
                if (g.getId().equals(debt.getFamilyGroupId())) {
                    groupName = g.getName();
                    break;
                }
            }
            holder.tvScope.setText("Family · " + groupName);
        } else {
            holder.tvScope.setText("Personal Debt");
        }

        // Calculate paid amount
        double paid = 0.0;
        for (Payment p : allPayments) {
            if (p.getDebtId().equals(debt.getId())) {
                paid += p.getAmount();
            }
        }
        double remaining = Math.max(0.0, debt.getAmount() - paid);
        boolean isPaid = remaining == 0.0 || "paid".equalsIgnoreCase(debt.getStatus());

        holder.tvTotal.setText(FreyrRepository.formatCurrency(debt.getAmount()));
        holder.tvPaid.setText(FreyrRepository.formatCurrency(paid));
        holder.tvRemaining.setText(FreyrRepository.formatCurrency(remaining));

        if (isPaid) {
            holder.tvStatus.setText("Paid");
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.freyr_status_paid));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_paid);
            holder.btnPay.setVisibility(View.GONE);
            holder.pbProgress.setProgress(100);
        } else {
            holder.tvStatus.setText("Pending");
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.freyr_primary));
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
            holder.btnPay.setVisibility(View.VISIBLE);
            int percent = debt.getAmount() > 0 ? (int) Math.min(100, (paid / debt.getAmount()) * 100) : 0;
            holder.pbProgress.setProgress(percent);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDebtClick(debt);
        });

        holder.btnHistory.setOnClickListener(v -> {
            if (listener != null) listener.onDebtClick(debt);
        });

        holder.btnPay.setOnClickListener(v -> {
            if (listener != null) listener.onPayClick(debt);
        });
    }

    @Override
    public int getItemCount() {
        return debts.size();
    }

    static class DebtViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvScope, tvDesc, tvStatus;
        TextView tvTotal, tvPaid, tvRemaining;
        ProgressBar pbProgress;
        Button btnHistory, btnPay;

        public DebtViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_debt_name);
            tvScope = itemView.findViewById(R.id.tv_debt_scope);
            tvDesc = itemView.findViewById(R.id.tv_debt_desc);
            tvStatus = itemView.findViewById(R.id.tv_debt_status);
            tvTotal = itemView.findViewById(R.id.tv_debt_total);
            tvPaid = itemView.findViewById(R.id.tv_debt_paid);
            tvRemaining = itemView.findViewById(R.id.tv_debt_remaining);
            pbProgress = itemView.findViewById(R.id.pb_debt_progress);
            btnHistory = itemView.findViewById(R.id.btn_debt_history);
            btnPay = itemView.findViewById(R.id.btn_debt_pay);
        }
    }
}

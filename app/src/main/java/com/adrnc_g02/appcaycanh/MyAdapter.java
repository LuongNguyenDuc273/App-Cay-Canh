package com.adrnc_g02.appcaycanh;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.adrnc_g02.appcaycanh.R;

import Model.Line;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    Context context;
    List<Line> lines;
    int selectedPosition = -1;
    private final OnLineClickListener onLineClickListener;

    Button btnAll;

    public interface OnLineClickListener {
        void onLineClick(int position, Line line);
    }

    public void setAllButton(Button btnAll)
    {
        this.btnAll = btnAll;
    }
    public MyAdapter(Context context, List<Line> lines, OnLineClickListener onLineClickListener) {
        this.context = context;
        this.lines = lines;
        this.onLineClickListener = onLineClickListener;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.line, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final Line line = lines.get(position);
        holder.btnName.setText(line.getNameLine());
        if (position ==selectedPosition)
        {
            holder.btnName.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_button_green));
            holder.btnName.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        }else
        {
            holder.btnName.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_button_outline));
            holder.btnName.setTextColor(ContextCompat.getColor(context, android.R.color.black));
        }

        holder.btnName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int previousPosition = selectedPosition;
                selectedPosition = position;
                if(btnAll != null)
                {
                    btnAll.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_button_outline));
                    btnAll.setTextColor(ContextCompat.getColor(context, android.R.color.black));
                }
                if (previousPosition != -1) {
                    notifyItemChanged(previousPosition);
                }
                notifyItemChanged(selectedPosition);

                if (onLineClickListener != null) {
                    onLineClickListener.onLineClick(position, line);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return lines == null ? 0 : lines.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        Button btnName;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            btnName = itemView.findViewById(R.id.btnLine);

        }
    }
    public void selectAllButton(){
        int previousPosition = selectedPosition;
        selectedPosition = -1;
        if(previousPosition!=1){
            notifyItemChanged(previousPosition);
        }
        notifyDataSetChanged();
    }


}
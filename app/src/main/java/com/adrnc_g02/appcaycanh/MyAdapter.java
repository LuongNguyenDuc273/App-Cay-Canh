package com.adrnc_g02.appcaycanh;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.adrnc_g02.appcaycanh.R;

import Model.Line;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    Context context;
    List<Line> lines;
    private final OnLineClickListener onLineClickListener;

    public interface OnLineClickListener {
        void onLineClick(int position, Line line);
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
        holder.btnName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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


}
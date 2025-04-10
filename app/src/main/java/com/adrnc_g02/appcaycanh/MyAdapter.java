package com.adrnc_g02.appcaycanh;

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
    public MyAdapter(Context context, List<Line> lines) {
        this.context = context;
        this.lines = lines;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.line, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.btnName.setText(lines.get(position).getNameLine());
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
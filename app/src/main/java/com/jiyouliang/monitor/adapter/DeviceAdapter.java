package com.jiyouliang.monitor.adapter;

import android.annotation.SuppressLint;
import android.content.res.loader.ResourcesLoader;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiyouliang.monitor.R;
import com.jiyouliang.monitor.bean.Device;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private List<Device> devices = new ArrayList<>();

    private OnItemCLickListener onItemCLickListener;
    private OnItemLongClickListener onItemLongClickListener;


    public void setOnItemCLickListener(OnItemCLickListener onItemCLickListener) {
        this.onItemCLickListener = onItemCLickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setDevices(List<Device> devices){
        this.devices = devices;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.device_item,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, @SuppressLint("RecyclerView") final int i) {
        final Device device = devices.get(i);
        viewHolder.robotName.setText(device.getName());
        viewHolder.electricity.setText("65%");
        viewHolder.imageView.setImageDrawable(viewHolder.itemView.getResources().getDrawable(R.drawable.ic_robot));
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemCLickListener != null){
                    onItemCLickListener.onItemClick(device,viewHolder,i);
                }
            }
        });

        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onItemLongClickListener.onItemLongClick(device,viewHolder,i);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices == null ? 0 : devices.size();
    }

    public interface OnItemCLickListener {
        void onItemClick(Device device, ViewHolder holder, int position);
    }

    public interface OnItemLongClickListener{
        void onItemLongClick(Device device, ViewHolder holder, int position);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView robotName;
        TextView electricity;
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            robotName = itemView.findViewById(R.id.robot_name);
            electricity = itemView.findViewById(R.id.electricity);
            imageView = itemView.findViewById(R.id.robotimageView);
        }
    }
}

package com.example.bluetoothserial;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder> {

    private static final String TAG = "BluetoothDeviceAdapter";

    private List<BluetoothDevice> mDeviceList = new ArrayList<>();

    private Listener mListener;

    // ViewHolder的构造函数
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView cardView;
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceState;
        Button operateButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;

            deviceName = (TextView) cardView.findViewById(R.id.bluetooth_device_name);
            deviceAddress = (TextView) cardView.findViewById(R.id.bluetooth_device_address);
            deviceState = (TextView) cardView.findViewById(R.id.bluetooth_device_state);
            operateButton = (Button) cardView.findViewById(R.id.operate_button);
            operateButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.operate_button) {
                int pos = getAdapterPosition();
                Log.d(TAG, "onClick, getAdapterPosition " + pos);
                if (pos >= 0 && pos < mDeviceList.size())
                    mListener.onItemClick(mDeviceList.get(pos));
            }
        }
    }

    public BluetoothDeviceAdapter(Listener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    public void add(BluetoothDevice device) {
        if (mDeviceList.contains(device))
            return;
        mDeviceList.add(device);
        notifyDataSetChanged();
    }

    public void clearAll() {
        mDeviceList.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice device = mDeviceList.get(position);
        holder.deviceName.setText(device.getName());
        holder.deviceAddress.setText(device.getAddress());

        String state = null;
        if (device.getBondState() == BluetoothDevice.BOND_NONE) {
            state = "未绑定";
        } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            state = "已绑定";
        }
        holder.deviceState.setText(state);
        holder.operateButton.setText("连接");
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }

    interface Listener {
        void onItemClick(BluetoothDevice bluetoothDevice);
    }
}



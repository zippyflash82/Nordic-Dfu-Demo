package com.example.dfudemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DevicesAdapter(
    private val devicesList: ArrayList<Device>,
    private val listener: ItemClickListener
) :
    RecyclerView.Adapter<DevicesAdapter.DeviceViewHolder>() {

    interface ItemClickListener {
        fun onItemClickListener(device: Device)
    }

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDevice: TextView = itemView.findViewById(R.id.tvDevice)
        val tvDeviceName: TextView = itemView.findViewById(R.id.tvDeviceName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devicesList[position]
        holder.apply {
            tvDevice.text = device.macAddress
            tvDeviceName.text = device.name
            itemView.setOnClickListener {
                listener.onItemClickListener(device)
            }
        }
    }

    override fun getItemCount(): Int {
        return devicesList.size
    }
}
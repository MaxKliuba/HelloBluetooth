package com.maxclub.android.hellobluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class ConnectionFragment : Fragment() {
    private lateinit var bluetoothPermissionView: View
    private lateinit var turnOnBluetoothView: View
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var deviceRecyclerViewPlaceholder: View
    private lateinit var deviceRecyclerView: RecyclerView
    private lateinit var deviceAdapter: DeviceAdapter

    private val bluetoothRepository: BluetoothRepository = BluetoothRepository.get()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            bluetoothPermissionView.visibility = View.GONE
            updateDeviceRecyclerView()
        } else {
            bluetoothPermissionView.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_connection, container, false)

        bluetoothPermissionView = view.findViewById(R.id.bluetoothPermissionView)
        view.findViewById<Button>(R.id.turnOnBluetoothButton).apply {
            setOnClickListener {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    data = Uri.fromParts("package", activity?.packageName, null)
                }
                startActivity(intent)
            }
        }

        turnOnBluetoothView = view.findViewById(R.id.turnOnBluetoothView)
        view.findViewById<Button>(R.id.turnOnBluetoothButton).apply {
            setOnClickListener {
                if (!checkBluetoothPermission()) return@setOnClickListener

                bluetoothRepository.bluetoothAdapter.enable()
            }
        }

        swipeRefreshLayout = view.findViewById<SwipeRefreshLayout?>(R.id.swipeRefreshLayout).apply {
            setOnRefreshListener {
                updateDeviceRecyclerView()
                isRefreshing = false
            }
        }

        deviceRecyclerViewPlaceholder = view.findViewById(R.id.deviceRecyclerViewPlaceholder)
        deviceAdapter = DeviceAdapter()
        deviceRecyclerView = view.findViewById<RecyclerView>(R.id.deviceRecyclerView).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = deviceAdapter
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bluetoothRepository.state.observe(viewLifecycleOwner) { state ->
            (activity as? AppCompatActivity)?.supportActionBar?.subtitle = when (state) {
                BluetoothAdapter.STATE_OFF -> getString(R.string.state_off)
                BluetoothAdapter.STATE_ON -> getString(R.string.state_on)
                BluetoothAdapter.STATE_TURNING_OFF -> getString(R.string.state_turning_off)
                BluetoothAdapter.STATE_TURNING_ON -> getString(R.string.state_turning_on)
                BluetoothAdapter.STATE_DISCONNECTED -> getString(R.string.state_disconnected)
                BluetoothAdapter.STATE_CONNECTED -> getString(R.string.state_connected)
                BluetoothAdapter.STATE_DISCONNECTING -> getString(R.string.state_disconnecting)
                BluetoothAdapter.STATE_CONNECTING -> getString(R.string.state_connecting)
                else -> getString(R.string.state_error)
            }

            if (state == BluetoothAdapter.STATE_OFF) {
                turnOnBluetoothView.visibility = View.VISIBLE
                swipeRefreshLayout.visibility = View.GONE
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivity(enableBluetoothIntent)
            } else {
                turnOnBluetoothView.visibility = View.GONE
                swipeRefreshLayout.visibility = View.VISIBLE
                updateDeviceRecyclerView()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        (activity as? AppCompatActivity)?.supportActionBar?.subtitle = null
    }

    private fun checkBluetoothPermission(): Boolean =
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        ) {
            bluetoothPermissionView.visibility = View.VISIBLE
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            false
        } else true

    private fun getBondedDevices(): List<BluetoothDevice> {
        if (!checkBluetoothPermission()) return emptyList()

        return bluetoothRepository.bluetoothAdapter
            .bondedDevices
            .toList()
    }

    private fun updateDeviceRecyclerView() {
        val bondedDevices = getBondedDevices()

        deviceAdapter.apply {
            submitList(bondedDevices)
        }

        deviceRecyclerViewPlaceholder.visibility = if (bondedDevices.isEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private abstract inner class DeviceHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        protected lateinit var bluetoothDevice: BluetoothDevice

        private val deviceNameTextView: TextView =
            itemView.findViewById(R.id.deviceNameTextView)
        private val deviceMacAddressTextView: TextView =
            itemView.findViewById(R.id.deviceMacAddressTextView)

        fun bind(bluetoothDevice: BluetoothDevice) {
            this.bluetoothDevice = bluetoothDevice

            if (!checkBluetoothPermission()) return

            deviceNameTextView.text = bluetoothDevice.name
            deviceMacAddressTextView.text = bluetoothDevice.address
        }
    }

    private inner class DisconnectedDeviceHolder(itemView: View) : DeviceHolder(itemView) {
        init {
            itemView.setOnClickListener {
                bluetoothRepository.bluetoothDevice = bluetoothDevice
                updateDeviceRecyclerView()
                // TODO
            }
        }
    }

    private inner class StateChangingDeviceHolder(itemView: View) : DeviceHolder(itemView)

    private inner class ConnectedDeviceHolder(itemView: View) : DeviceHolder(itemView) {
        init {
            itemView.setOnClickListener {
                // TODO
            }
        }
    }

    private inner class DeviceAdapter : RecyclerView.Adapter<DeviceHolder>() {
        private val devices: SortedList<BluetoothDevice> = SortedList(
            BluetoothDevice::class.java,
            object : SortedList.Callback<BluetoothDevice>() {
                override fun compare(
                    item1: BluetoothDevice,
                    item2: BluetoothDevice
                ): Int {
                    val bluetoothDevice = bluetoothRepository.bluetoothDevice
                    val state = bluetoothRepository.state.value
                    return when {
                        item1 == bluetoothDevice && state == BluetoothAdapter.STATE_CONNECTED -> -1
                        item2 == bluetoothDevice && state == BluetoothAdapter.STATE_CONNECTED -> 1
                        else -> 0
                    }
                }

                override fun onInserted(position: Int, count: Int) {
                    notifyItemRangeInserted(position, count)
                }

                override fun onRemoved(position: Int, count: Int) {
                    notifyItemRangeRemoved(position, count)
                }

                override fun onMoved(fromPosition: Int, toPosition: Int) {
                    notifyItemMoved(fromPosition, toPosition)
                }

                override fun onChanged(position: Int, count: Int) {
                    notifyItemRangeChanged(position, count)
                }

                override fun areContentsTheSame(
                    oldItem: BluetoothDevice,
                    newItem: BluetoothDevice
                ): Boolean = false

                override fun areItemsTheSame(
                    item1: BluetoothDevice,
                    item2: BluetoothDevice
                ): Boolean = item1.address == item2.address
            })

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): DeviceHolder {
            return when (viewType) {
                BluetoothAdapter.STATE_DISCONNECTED -> {
                    val view = layoutInflater.inflate(
                        R.layout.list_item_bluetooth_device,
                        parent,
                        false
                    )
                    DisconnectedDeviceHolder(view)
                }
                BluetoothAdapter.STATE_CONNECTED -> {
                    val view = layoutInflater.inflate(
                        R.layout.list_item_bluetooth_device_connected,
                        parent,
                        false
                    )
                    ConnectedDeviceHolder(view)
                }
                else -> {
                    val view =
                        layoutInflater.inflate(
                            R.layout.list_item_bluetooth_device_state_changing,
                            parent,
                            false
                        )
                    StateChangingDeviceHolder(view)
                }
            }
        }

        override fun onBindViewHolder(holder: DeviceHolder, position: Int) {
            holder.bind(devices[position])
        }

        override fun getItemCount(): Int = devices.size()

        override fun getItemViewType(position: Int): Int =
            if (devices[position] == bluetoothRepository.bluetoothDevice) {
                bluetoothRepository.state.value ?: BluetoothAdapter.STATE_DISCONNECTED
            } else {
                BluetoothAdapter.STATE_DISCONNECTED
            }

        fun submitList(items: List<BluetoothDevice>) {
            devices.replaceAll(items)
        }
    }
}
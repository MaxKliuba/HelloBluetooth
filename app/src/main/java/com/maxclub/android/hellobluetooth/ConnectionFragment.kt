package com.maxclub.android.hellobluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator

private const val LOG_TAG = "ConnectionFragment"

class ConnectionFragment : Fragment() {
    private val connectionViewModel by lazy {
        ViewModelProvider(this)[ConnectionViewModel::class.java]
    }

    private lateinit var bluetoothPermissionView: View
    private lateinit var turnOnBluetoothView: View
    private lateinit var refreshFloatingActionButton: FloatingActionButton
    private lateinit var pairedDevicesProgressIndicator: LinearProgressIndicator
    private lateinit var pairedDevicesPlaceholder: View
    private lateinit var pairedDevicesRecyclerView: RecyclerView
    private lateinit var pairedDevicesAdapter: PairedDevicesAdapter
    private lateinit var availableDevicesProgressIndicator: LinearProgressIndicator
    private lateinit var availableDevicesPlaceholder: View
    private lateinit var availableDevicesRecyclerView: RecyclerView
    private lateinit var availableDevicesAdapter: AvailableDevicesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }
        activity?.registerReceiver(deviceDiscoveryReceiver, filter)
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_connection, container, false)

        bluetoothPermissionView = view.findViewById(R.id.bluetoothPermissionView)
        view.findViewById<Button>(R.id.bluetoothPermissionSettingsButton).apply {
            setOnClickListener {
                val intent = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    data = Uri.fromParts("package", activity?.packageName, null)
                }
                startActivity(intent)
            }
        }

        turnOnBluetoothView = view.findViewById(R.id.turnOnBluetoothView)
        view.findViewById<Button>(R.id.turnOnBluetoothButton).apply {
            setOnClickListener {
                if (checkHasBluetoothPermission()) {
                    connectionViewModel.bluetoothAdapter.enable()
                }
            }
        }

        refreshFloatingActionButton =
            view.findViewById<FloatingActionButton?>(R.id.refreshFloatingActionButton).apply {
                setOnClickListener {
                    if (checkHasBluetoothPermission()) {
                        connectionViewModel.bluetoothAdapter.startDiscovery()
                    }
                    updatePairedDevicesRecyclerView()
                }
            }

        pairedDevicesProgressIndicator =
            view.findViewById(R.id.pairedDevicesProgressIndicator)
        pairedDevicesPlaceholder = view.findViewById(R.id.pairedDevicesPlaceholder)
        pairedDevicesAdapter = PairedDevicesAdapter()
        pairedDevicesRecyclerView =
            view.findViewById<RecyclerView>(R.id.pairedDevicesRecyclerView).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = pairedDevicesAdapter
            }

        availableDevicesProgressIndicator =
            view.findViewById(R.id.availableDevicesProgressIndicator)
        availableDevicesPlaceholder = view.findViewById(R.id.availableDevicesPlaceholder)
        availableDevicesAdapter = AvailableDevicesAdapter()
        availableDevicesRecyclerView =
            view.findViewById<RecyclerView>(R.id.availableDevicesRecyclerView).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = availableDevicesAdapter
            }

        checkHasBluetoothPermission(true)

        return view
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connectionViewModel.connectionState.observe(viewLifecycleOwner) {
            updateUIbyConnectionState()
        }
    }

    override fun onStart() {
        super.onStart()
        updateUIbyConnectionState()
    }

    @SuppressLint("MissingPermission")
    override fun onStop() {
        super.onStop()
        if (checkHasBluetoothPermission()) {
            connectionViewModel.bluetoothAdapter.cancelDiscovery()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(deviceDiscoveryReceiver)
    }

    private val bluetoothEnableResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            connectionViewModel.isBluetoothEnableIntentLaunched = false
        }

    private val deviceDiscoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val bluetoothDevice: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    bluetoothDevice?.let { device ->
                        Log.i(LOG_TAG, "ACTION_FOUND -> ${device.address}")
                        connectionViewModel.availableDevices += device
                        updateAvailableDevicesRecyclerView()
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.i(LOG_TAG, "ACTION_DISCOVERY_STARTED")
                    connectionViewModel.availableDevices.clear()
                    updateAvailableDevicesRecyclerView()
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.i(LOG_TAG, "ACTION_DISCOVERY_FINISHED")
                    updateAvailableDevicesRecyclerView()
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val bondState =
                        intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                    val bluetoothDevice: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    bluetoothDevice?.let { device ->
                        Log.i(
                            LOG_TAG,
                            "ACTION_BOND_STATE_CHANGED -> ${device.address} ($bondState)"
                        )
                        if (bondState == BluetoothDevice.BOND_BONDED) {
                            updatePairedDevicesRecyclerView()
                            connectionViewModel.availableDevices -= device
                        }
                        updateAvailableDevicesRecyclerView()
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isAllGranted = permissions.entries.all { it.value }
        if (isAllGranted) {
            bluetoothPermissionView.visibility = View.GONE
            refreshFloatingActionButton.show()
            if (checkHasBluetoothPermission()) {
                connectionViewModel.bluetoothAdapter.startDiscovery()
            }
            updatePairedDevicesRecyclerView()

        } else {
            bluetoothPermissionView.visibility = View.VISIBLE
            refreshFloatingActionButton.hide()
        }
    }

    private fun checkHasBluetoothPermission(launch: Boolean = false): Boolean {
        val requiredPermissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            listOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        }

        val missingPermissions = requiredPermissions.filter { permission ->
            ActivityCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }

        return if (missingPermissions.isNotEmpty()) {
            bluetoothPermissionView.visibility = View.VISIBLE
            refreshFloatingActionButton.hide()
            if (launch) {
                requestPermissionLauncher.launch(requiredPermissions.toTypedArray())
            }
            false
        } else {
            true
        }
    }

    private fun updateUIbyConnectionState() {
        connectionViewModel.connectionState.value?.let { state ->
            when (state) {
                BluetoothAdapter.STATE_ON,
                BluetoothAdapter.STATE_DISCONNECTED,
                BluetoothAdapter.STATE_CONNECTED,
                BluetoothAdapter.STATE_DISCONNECTING,
                BluetoothAdapter.STATE_CONNECTING -> {
                    turnOnBluetoothView.visibility = View.GONE
                    refreshFloatingActionButton.show()
                    updatePairedDevicesRecyclerView()
                    updateAvailableDevicesRecyclerView()

//                    if (checkHasBluetoothPermission()) {
//                        if (state == BluetoothAdapter.STATE_ON ||
//                            state == BluetoothAdapter.STATE_DISCONNECTED
//                        ) {
//                            connectionViewModel.bluetoothAdapter.startDiscovery()
//                        } else {
//                            connectionViewModel.bluetoothAdapter.cancelDiscovery()
//                        }
//                    }
                }
                else -> {
                    turnOnBluetoothView.visibility = View.VISIBLE
                    refreshFloatingActionButton.hide()
                    if (!connectionViewModel.isBluetoothEnableIntentLaunched &&
                        (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.ERROR)
                    ) {
                        val bluetoothEnableIntent =
                            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        bluetoothEnableResultLauncher.launch(bluetoothEnableIntent)
                        connectionViewModel.isBluetoothEnableIntentLaunched = true
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getBondedDevices(): List<BluetoothDevice> {
        if (!checkHasBluetoothPermission()) {
            return emptyList()
        }

        return connectionViewModel.bluetoothAdapter
            .bondedDevices
            .toList()
    }

    private fun updatePairedDevicesRecyclerView() {
        val devices = getBondedDevices()
        pairedDevicesAdapter.submitList(devices)
        pairedDevicesPlaceholder.visibility = if (devices.isEmpty()) View.VISIBLE else View.GONE
        connectionViewModel.connectionState.value?.let { connectionState ->
            pairedDevicesProgressIndicator.visibility =
                if (connectionState == BluetoothAdapter.STATE_CONNECTING ||
                    connectionState == BluetoothAdapter.STATE_DISCONNECTING
                ) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
    }

    private fun updateAvailableDevicesRecyclerView() {
        val devices = connectionViewModel.availableDevices
        availableDevicesAdapter.submitList(devices)
        availableDevicesPlaceholder.visibility =
            if (devices.isEmpty() && !connectionViewModel.bluetoothAdapter.isDiscovering) {
                View.VISIBLE
            } else {
                View.GONE
            }
        availableDevicesProgressIndicator.visibility =
            if (checkHasBluetoothPermission() &&
                (connectionViewModel.bluetoothAdapter.isDiscovering || connectionViewModel.isBonding)
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private abstract inner class PairedDeviceHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        protected lateinit var bluetoothDevice: BluetoothDevice

        private val deviceNameTextView: TextView =
            itemView.findViewById(R.id.deviceNameTextView)
        private val deviceMacAddressTextView: TextView =
            itemView.findViewById(R.id.deviceMacAddressTextView)

        @SuppressLint("MissingPermission")
        fun bind(bluetoothDevice: BluetoothDevice) {
            this.bluetoothDevice = bluetoothDevice
            if (!checkHasBluetoothPermission()) {
                return
            }
            deviceNameTextView.text = bluetoothDevice.name
            deviceMacAddressTextView.text = bluetoothDevice.address
        }
    }

    private inner class DisconnectedPairedDeviceHolder(itemView: View) :
        PairedDeviceHolder(itemView) {
        init {
            itemView.setOnClickListener {
                connectionViewModel.connect(bluetoothDevice)
            }
        }
    }

    private inner class ConnectingPairedDeviceHolder(itemView: View) : PairedDeviceHolder(itemView)

    private inner class ConnectedPairedDeviceHolder(itemView: View) : PairedDeviceHolder(itemView) {
        init {
            itemView.setOnClickListener {
                connectionViewModel.disconnect()
            }
        }
    }

    private inner class PairedDevicesAdapter : RecyclerView.Adapter<PairedDeviceHolder>() {
        private val devices: SortedList<BluetoothDevice> = SortedList(
            BluetoothDevice::class.java,
            object : SortedList.Callback<BluetoothDevice>() {
                override fun compare(
                    item1: BluetoothDevice,
                    item2: BluetoothDevice
                ): Int {
                    val bluetoothDevice = connectionViewModel.bluetoothDevice
                    val state = connectionViewModel.connectionState.value
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
        ): PairedDeviceHolder {
            return when (viewType) {
                BluetoothAdapter.STATE_CONNECTING, BluetoothAdapter.STATE_DISCONNECTING -> {
                    val view =
                        layoutInflater.inflate(
                            R.layout.list_item_bluetooth_device_connecting,
                            parent,
                            false
                        )
                    ConnectingPairedDeviceHolder(view)
                }
                BluetoothAdapter.STATE_CONNECTED -> {
                    val view = layoutInflater.inflate(
                        R.layout.list_item_bluetooth_device_connected,
                        parent,
                        false
                    )
                    ConnectedPairedDeviceHolder(view)
                }
                else -> {
                    val view = layoutInflater.inflate(
                        R.layout.list_item_bluetooth_device,
                        parent,
                        false
                    )
                    DisconnectedPairedDeviceHolder(view)
                }
            }
        }

        override fun onBindViewHolder(holderPaired: PairedDeviceHolder, position: Int) {
            holderPaired.bind(devices[position])
        }

        override fun getItemCount(): Int = devices.size()

        override fun getItemViewType(position: Int): Int =
            if (devices[position] == connectionViewModel.bluetoothDevice) {
                connectionViewModel.connectionState.value ?: BluetoothAdapter.STATE_DISCONNECTED
            } else {
                BluetoothAdapter.STATE_DISCONNECTED
            }

        fun submitList(items: List<BluetoothDevice>) {
            devices.replaceAll(items)
        }
    }

    private abstract inner class AvailableDeviceHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        protected lateinit var bluetoothDevice: BluetoothDevice

        private val deviceNameTextView: TextView =
            itemView.findViewById(R.id.deviceNameTextView)
        private val deviceMacAddressTextView: TextView =
            itemView.findViewById(R.id.deviceMacAddressTextView)

        @SuppressLint("MissingPermission")
        fun bind(bluetoothDevice: BluetoothDevice) {
            this.bluetoothDevice = bluetoothDevice
            if (!checkHasBluetoothPermission()) {
                return
            }
            deviceNameTextView.text = bluetoothDevice.name
            deviceMacAddressTextView.text = bluetoothDevice.address
        }
    }

    @SuppressLint("MissingPermission")
    private inner class NotPairedAvailableDeviceHolder(itemView: View) :
        AvailableDeviceHolder(itemView) {
        init {
            itemView.setOnClickListener {
                if (checkHasBluetoothPermission()) {
                    bluetoothDevice.createBond()
                }
            }
        }
    }

    private inner class PairingAvailableDeviceHolder(itemView: View) :
        AvailableDeviceHolder(itemView)

    private inner class AvailableDevicesAdapter : RecyclerView.Adapter<AvailableDeviceHolder>() {
        private val devices: SortedList<BluetoothDevice> = SortedList(
            BluetoothDevice::class.java,
            object : SortedList.Callback<BluetoothDevice>() {
                override fun compare(
                    item1: BluetoothDevice,
                    item2: BluetoothDevice
                ): Int = 0

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
        ): AvailableDeviceHolder {
            return when (viewType) {
                BluetoothDevice.BOND_BONDING -> {
                    val view = layoutInflater.inflate(
                        R.layout.list_item_bluetooth_device_pairing,
                        parent,
                        false
                    )
                    PairingAvailableDeviceHolder(view)
                }
                else -> {
                    val view =
                        layoutInflater.inflate(
                            R.layout.list_item_bluetooth_device,
                            parent,
                            false
                        )
                    NotPairedAvailableDeviceHolder(view)
                }
            }
        }

        override fun onBindViewHolder(holderPaired: AvailableDeviceHolder, position: Int) {
            holderPaired.bind(devices[position])
        }

        override fun getItemCount(): Int = devices.size()

        @SuppressLint("MissingPermission")
        override fun getItemViewType(position: Int): Int =
            if (!checkHasBluetoothPermission()) {
                BluetoothDevice.BOND_NONE
            } else {
                devices[position].bondState
            }

        fun submitList(items: List<BluetoothDevice>) {
            devices.replaceAll(items)
        }
    }
}
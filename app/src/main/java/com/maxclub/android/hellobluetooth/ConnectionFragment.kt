package com.maxclub.android.hellobluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator

class ConnectionFragment : Fragment(), BluetoothPairingReceiver.Callbacks {
    interface Callbacks {
        fun onConnect(device: BluetoothDevice)

        fun onDisconnect()

        fun getState(): LiveData<Int>

        fun getDevice(): BluetoothDevice?
    }

    private var callbacks: Callbacks? = null

    private val connectionViewModel by lazy {
        ViewModelProvider(this)[ConnectionViewModel::class.java]
    }

    private lateinit var bluetoothPermissionView: View
    private lateinit var turnOnBluetoothView: View
    private lateinit var locationPermissionView: View
    private lateinit var turnOnLocationView: View
    private lateinit var refreshFloatingActionButton: FloatingActionButton
    private lateinit var pairedDevicesProgressIndicator: LinearProgressIndicator
    private lateinit var pairedDevicesPlaceholder: View
    private lateinit var pairedDevicesRecyclerView: RecyclerView
    private lateinit var pairedDevicesAdapter: PairedDevicesAdapter
    private lateinit var availableDevicesProgressIndicator: LinearProgressIndicator
    private lateinit var availableDevicesView: View
    private lateinit var availableDevicesPlaceholder: View
    private lateinit var availableDevicesRecyclerView: RecyclerView
    private lateinit var availableDevicesAdapter: AvailableDevicesAdapter

    private val bluetoothPairingReceiver: BluetoothPairingReceiver = BluetoothPairingReceiver()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bluetoothPairingReceiver.register(requireContext(), this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_connection, container, false)

        bluetoothPermissionView = view.findViewById(R.id.bluetoothPermissionView)
        view.findViewById<Button>(R.id.bluetoothPermissionSettingsButton).apply {
            setOnClickListener {
                launchPermissionSettingsActivity()
            }
        }

        turnOnBluetoothView = view.findViewById(R.id.turnOnBluetoothView)
        view.findViewById<Button>(R.id.turnOnBluetoothButton).apply {
            setOnClickListener {
                if (checkHasBluetoothPermission(true)) {
                    connectionViewModel.bluetoothAdapter.enable()
                }
            }
        }

        locationPermissionView = view.findViewById(R.id.locationPermissionView)
        view.findViewById<Button>(R.id.locationPermissionSettingsButton).apply {
            setOnClickListener {
                launchPermissionSettingsActivity()
            }
        }

        turnOnLocationView = view.findViewById(R.id.turnOnLocationView)
        view.findViewById<Button>(R.id.turnOnLocationButton).apply {
            setOnClickListener {
                val intent = Intent().apply {
                    action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            }
        }

        refreshFloatingActionButton =
            view.findViewById<FloatingActionButton?>(R.id.refreshFloatingActionButton).apply {
                setOnClickListener {
                    if (checkHasScanPermission(true)) {
                        connectionViewModel.bluetoothAdapter.startDiscovery()
                    }
                    updatePairedDevicesRecyclerView()
                    updateAvailableDevicesRecyclerView()
                }
            }

        pairedDevicesProgressIndicator = view.findViewById(R.id.pairedDevicesProgressIndicator)
        pairedDevicesPlaceholder = view.findViewById(R.id.pairedDevicesPlaceholder)
        pairedDevicesRecyclerView =
            view.findViewById<RecyclerView>(R.id.pairedDevicesRecyclerView).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = PairedDevicesAdapter().apply {
                    pairedDevicesAdapter = this
                }
            }

        availableDevicesView = view.findViewById(R.id.availableDevicesView)
        availableDevicesProgressIndicator =
            view.findViewById(R.id.availableDevicesProgressIndicator)
        availableDevicesPlaceholder = view.findViewById(R.id.availableDevicesPlaceholder)
        availableDevicesRecyclerView =
            view.findViewById<RecyclerView>(R.id.availableDevicesRecyclerView).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = AvailableDevicesAdapter().apply {
                    availableDevicesAdapter = this
                }
            }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkHasBluetoothPermission(true)
        callbacks?.getState()?.observe(viewLifecycleOwner) {
            updateUIbyConnectionState()
        }
    }

    override fun onStart() {
        super.onStart()
        updateUIbyConnectionState()
    }

    override fun onStop() {
        super.onStop()
        if (checkHasScanPermission()) {
            connectionViewModel.bluetoothAdapter.cancelDiscovery()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothPairingReceiver.unregister(requireContext())
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    /*
     * BluetoothPairingReceiver.Callbacks
     */
    override fun onDiscoveryStarted() {
        connectionViewModel.availableDevices.clear()
        updateAvailableDevicesRecyclerView()
    }

    override fun onDiscoveryFinished() {
        updateAvailableDevicesRecyclerView()
    }

    override fun onDeviceFound(device: BluetoothDevice) {
        connectionViewModel.availableDevices += device
        updateAvailableDevicesRecyclerView()
    }

    override fun onBoundStateChanged(state: Int, device: BluetoothDevice) {
        if (state == BluetoothDevice.BOND_BONDED) {
            updatePairedDevicesRecyclerView()
            connectionViewModel.availableDevices -= device
        }
        updateAvailableDevicesRecyclerView()
    }

    private val requestBluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isAllGranted = permissions.entries.all { it.value }
        if (isAllGranted) {
            bluetoothPermissionView.visibility = View.GONE
            updatePairedDevicesRecyclerView()
            updateAvailableDevicesRecyclerView()

        } else {
            bluetoothPermissionView.visibility = View.VISIBLE
        }
    }

    private val requestScanPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isAllGranted = permissions.entries.all { it.value }
        if (isAllGranted) {
            locationPermissionView.visibility = View.GONE
            updateAvailableDevicesRecyclerView()

        } else {
            locationPermissionView.visibility = View.VISIBLE
        }
    }

    private fun checkHasBluetoothPermission(launch: Boolean = false): Boolean {
        val requiredPermissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            emptyList()
        } else {
            listOf(Manifest.permission.BLUETOOTH_CONNECT)
        }

        val missingPermissions = requiredPermissions.filter { permission ->
            ActivityCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }

        return if (missingPermissions.isNotEmpty()) {
            bluetoothPermissionView.visibility = View.VISIBLE
            if (launch) {
                requestBluetoothPermissionLauncher.launch(requiredPermissions.toTypedArray())
            }
            false
        } else {
            bluetoothPermissionView.visibility = View.GONE
            true
        }
    }

    private fun checkHasScanPermission(launch: Boolean = false): Boolean {
        val requiredPermissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        } else {
            listOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        }

        val missingPermissions = requiredPermissions.filter { permission ->
            ActivityCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }

        return if (missingPermissions.isNotEmpty()) {
            locationPermissionView.visibility = View.VISIBLE
            if (launch) {
                requestScanPermissionLauncher.launch(requiredPermissions.toTypedArray())
            }
            false
        } else {
            locationPermissionView.visibility = View.GONE
            true
        }
    }

    private fun isLocationEnabled(): Boolean =
        (context?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager)?.let { locationManager ->
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } ?: false

    private fun isDiscoveryAvailable(): Boolean =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            isLocationEnabled()
        } else {
            true
        }

    private fun launchPermissionSettingsActivity() {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.fromParts("package", activity?.packageName, null)
        }
        startActivity(intent)
    }

    private fun updateUIbyConnectionState() {
        callbacks?.getState()?.value?.let { state ->
            when (state) {
                BluetoothAdapter.STATE_ON,
                BluetoothAdapter.STATE_DISCONNECTED,
                BluetoothAdapter.STATE_CONNECTED,
                BluetoothAdapter.STATE_DISCONNECTING,
                BluetoothAdapter.STATE_CONNECTING -> {
                    turnOnBluetoothView.visibility = View.GONE
                    updatePairedDevicesRecyclerView()
                    updateAvailableDevicesRecyclerView()
                }
                else -> {
                    turnOnBluetoothView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun getBondedDevices(): List<BluetoothDevice> {
        return if (checkHasBluetoothPermission()) {
            connectionViewModel.bluetoothAdapter
                .bondedDevices
                .toList()
        } else {
            return emptyList()
        }
    }

    private fun updatePairedDevicesRecyclerView() {
        val devices = getBondedDevices()
        pairedDevicesAdapter.submitList(devices)
        pairedDevicesPlaceholder.visibility = if (devices.isEmpty()) View.VISIBLE else View.GONE
        callbacks?.getState()?.value?.let { state ->
            pairedDevicesProgressIndicator.visibility =
                if (state == BluetoothAdapter.STATE_CONNECTING ||
                    state == BluetoothAdapter.STATE_DISCONNECTING
                ) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
    }

    private fun isBonding(): Boolean =
        checkHasScanPermission() && connectionViewModel.availableDevices.any {
            it.bondState == BluetoothDevice.BOND_BONDING
        }

    private fun updateAvailableDevicesRecyclerView() {
        val isDiscovering =
            checkHasScanPermission() && connectionViewModel.bluetoothAdapter.isDiscovering
        val devices = connectionViewModel.availableDevices
        availableDevicesAdapter.submitList(devices)
        availableDevicesPlaceholder.visibility =
            if (devices.isEmpty() && !isDiscovering) {
                View.VISIBLE
            } else {
                View.GONE
            }
        availableDevicesProgressIndicator.visibility =
            if (isDiscovering || isBonding()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        turnOnLocationView.visibility = if (!isDiscoveryAvailable()) {
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

        fun bind(bluetoothDevice: BluetoothDevice) {
            this.bluetoothDevice = bluetoothDevice
            if (checkHasBluetoothPermission()) {
                deviceNameTextView.text = bluetoothDevice.name
            }
            deviceMacAddressTextView.text = bluetoothDevice.address
        }
    }

    private inner class DisconnectedPairedDeviceHolder(itemView: View) :
        PairedDeviceHolder(itemView) {
        init {
            itemView.setOnClickListener {
                callbacks?.onConnect(bluetoothDevice)
            }
        }

        constructor(parent: ViewGroup) : this(
            layoutInflater.inflate(
                R.layout.list_item_bluetooth_device,
                parent,
                false
            )
        )
    }

    private inner class ConnectingPairedDeviceHolder(itemView: View) :
        PairedDeviceHolder(itemView) {
        constructor(parent: ViewGroup) : this(
            layoutInflater.inflate(
                R.layout.list_item_bluetooth_device_connecting,
                parent,
                false
            )
        )
    }

    private inner class ConnectedPairedDeviceHolder(itemView: View) : PairedDeviceHolder(itemView) {
        init {
            itemView.setOnClickListener {
                callbacks?.onDisconnect()
            }
        }

        constructor(parent: ViewGroup) : this(
            layoutInflater.inflate(
                R.layout.list_item_bluetooth_device_connected,
                parent,
                false
            )
        )
    }

    private inner class PairedDevicesAdapter : RecyclerView.Adapter<PairedDeviceHolder>() {
        private val devices: SortedList<BluetoothDevice> = SortedList(
            BluetoothDevice::class.java,
            object : SortedList.Callback<BluetoothDevice>() {
                override fun compare(item1: BluetoothDevice, item2: BluetoothDevice): Int {
                    val device = callbacks?.getDevice()
                    val state = callbacks?.getState()?.value
                    return when {
                        item1 == device && state == BluetoothAdapter.STATE_CONNECTED -> -1
                        item2 == device && state == BluetoothAdapter.STATE_CONNECTED -> 1
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
                BluetoothAdapter.STATE_CONNECTING,
                BluetoothAdapter.STATE_DISCONNECTING -> ConnectingPairedDeviceHolder(parent)
                BluetoothAdapter.STATE_CONNECTED -> ConnectedPairedDeviceHolder(parent)
                else -> DisconnectedPairedDeviceHolder(parent)
            }
        }

        override fun onBindViewHolder(holderPaired: PairedDeviceHolder, position: Int) {
            holderPaired.bind(devices[position])
        }

        override fun getItemCount(): Int = devices.size()

        override fun getItemViewType(position: Int): Int =
            if (devices[position] == callbacks?.getDevice()) {
                callbacks?.getState()?.value ?: BluetoothAdapter.STATE_DISCONNECTED
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

        fun bind(bluetoothDevice: BluetoothDevice) {
            this.bluetoothDevice = bluetoothDevice
            if (checkHasBluetoothPermission()) {
                deviceNameTextView.text = bluetoothDevice.name
            }
            deviceMacAddressTextView.text = bluetoothDevice.address
        }
    }

    private inner class NotPairedAvailableDeviceHolder(itemView: View) :
        AvailableDeviceHolder(itemView) {
        init {
            itemView.setOnClickListener {
                if (checkHasScanPermission()) {
                    bluetoothDevice.createBond()
                }
            }
        }

        constructor(parent: ViewGroup) : this(
            layoutInflater.inflate(
                R.layout.list_item_bluetooth_device,
                parent,
                false
            )
        )
    }

    private inner class PairingAvailableDeviceHolder(itemView: View) :
        AvailableDeviceHolder(itemView) {
        constructor(parent: ViewGroup) : this(
            layoutInflater.inflate(
                R.layout.list_item_bluetooth_device_pairing,
                parent,
                false
            )
        )
    }

    private inner class AvailableDevicesAdapter : RecyclerView.Adapter<AvailableDeviceHolder>() {
        private val devices: SortedList<BluetoothDevice> = SortedList(
            BluetoothDevice::class.java,
            object : SortedList.Callback<BluetoothDevice>() {
                override fun compare(item1: BluetoothDevice, item2: BluetoothDevice): Int = 0

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
                BluetoothDevice.BOND_BONDING -> PairingAvailableDeviceHolder(parent)
                else -> NotPairedAvailableDeviceHolder(parent)
            }
        }

        override fun onBindViewHolder(holderPaired: AvailableDeviceHolder, position: Int) {
            holderPaired.bind(devices[position])
        }

        override fun getItemCount(): Int = devices.size()

        override fun getItemViewType(position: Int): Int =
            if (!checkHasScanPermission()) {
                BluetoothDevice.BOND_NONE
            } else {
                devices[position].bondState
            }

        fun submitList(items: List<BluetoothDevice>) {
            devices.replaceAll(items)
        }
    }
}
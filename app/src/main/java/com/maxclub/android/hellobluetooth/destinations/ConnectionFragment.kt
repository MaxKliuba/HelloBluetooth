package com.maxclub.android.hellobluetooth.destinations

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.maxclub.android.hellobluetooth.viewmodel.ConnectionViewModel
import com.maxclub.android.hellobluetooth.R
import com.maxclub.android.hellobluetooth.bluetooth.IBluetoothConnectionCallbacks
import com.maxclub.android.hellobluetooth.model.BluetoothDeviceWithState
import com.maxclub.android.hellobluetooth.receivers.BluetoothPairingReceiver

class ConnectionFragment : Fragment(), BluetoothPairingReceiver.Callbacks {
    private var callbacks: IBluetoothConnectionCallbacks? = null

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
        callbacks = context as? IBluetoothConnectionCallbacks?
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_connection, container, false)

        bluetoothPermissionView = view.findViewById(R.id.bluetooth_permission_view)
        view.findViewById<Button>(R.id.bluetooth_permission_settings_button).apply {
            setOnClickListener {
                launchPermissionSettingsActivity()
            }
        }

        turnOnBluetoothView = view.findViewById(R.id.turn_on_bluetooth_view)
        view.findViewById<Button>(R.id.turn_on_bluetooth_button).apply {
            setOnClickListener {
                if (checkHasBluetoothPermission(true)) {
                    connectionViewModel.bluetoothAdapter.enable()
                }
            }
        }

        locationPermissionView = view.findViewById(R.id.location_permission_view)
        view.findViewById<Button>(R.id.location_permission_settings_button).apply {
            setOnClickListener {
                launchPermissionSettingsActivity()
            }
        }

        turnOnLocationView = view.findViewById(R.id.turn_on_location_view)
        view.findViewById<Button>(R.id.turn_on_location_button).apply {
            setOnClickListener {
                val intent = Intent().apply {
                    action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            }
        }

        refreshFloatingActionButton =
            view.findViewById<FloatingActionButton>(R.id.refresh_floating_action_button).apply {
                setOnClickListener {
                    if (checkHasScanPermission(true)) {
                        connectionViewModel.bluetoothAdapter.startDiscovery()
                    }
                    updatePairedDevicesRecyclerView()
                    updateAvailableDevicesRecyclerView()
                }
            }

        pairedDevicesProgressIndicator = view.findViewById(R.id.paired_devices_progress_indicator)
        pairedDevicesPlaceholder = view.findViewById(R.id.paired_devices_placeholder)
        pairedDevicesRecyclerView =
            view.findViewById<RecyclerView>(R.id.paired_devices_recycler_view).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = PairedDevicesAdapter().apply {
                    pairedDevicesAdapter = this
                }
            }

        availableDevicesView = view.findViewById(R.id.available_devices_view)
        availableDevicesProgressIndicator =
            view.findViewById(R.id.available_devices_progress_indicator)
        availableDevicesPlaceholder = view.findViewById(R.id.available_devices_placeholder)
        availableDevicesRecyclerView =
            view.findViewById<RecyclerView>(R.id.available_devices_recycler_view).apply {
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
        bluetoothPairingReceiver.register(requireContext(), this)
    }

    override fun onStop() {
        super.onStop()
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

    @SuppressLint("MissingPermission")
    private val requestScanPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isAllGranted = permissions.entries.all { it.value }
        if (isAllGranted) {
            locationPermissionView.visibility = View.GONE
            connectionViewModel.bluetoothAdapter.startDiscovery()
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
                }
                else -> {
                    connectionViewModel.availableDevices.clear()
                    turnOnBluetoothView.visibility = View.VISIBLE
                }
            }
        }

        if (checkHasScanPermission(true) &&
            !connectionViewModel.bluetoothAdapter.isDiscovering &&
            connectionViewModel.availableDevices.isEmpty()
        ) {
            connectionViewModel.bluetoothAdapter.startDiscovery()
        }
        updatePairedDevicesRecyclerView()
        updateAvailableDevicesRecyclerView()
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
            .map { device ->
                BluetoothDeviceWithState(
                    device,
                    if (device == callbacks?.getDevice()) {
                        callbacks?.getState()?.value ?: BluetoothAdapter.STATE_DISCONNECTED
                    } else {
                        BluetoothAdapter.STATE_DISCONNECTED
                    }
                )
            }.distinct()
        pairedDevicesAdapter.submitSortedList(devices)
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
            .map { BluetoothDeviceWithState(it, it.bondState) }
            .distinct()
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
        protected lateinit var bluetoothDeviceWithState: BluetoothDeviceWithState

        private val deviceNameTextView: TextView =
            itemView.findViewById(R.id.device_name_text_view)
        private val deviceMacAddressTextView: TextView =
            itemView.findViewById(R.id.device_mac_address_text_view)

        fun bind(bluetoothDevice: BluetoothDeviceWithState) {
            this.bluetoothDeviceWithState = bluetoothDevice
            if (checkHasBluetoothPermission()) {
                deviceNameTextView.text = bluetoothDevice.device.name
            }
            deviceMacAddressTextView.text = bluetoothDevice.device.address
        }
    }

    private inner class DisconnectedPairedDeviceHolder(itemView: View) :
        PairedDeviceHolder(itemView) {
        init {
            itemView.setOnClickListener {
                callbacks?.onConnect(bluetoothDeviceWithState.device)
            }
        }
    }

    private inner class ConnectingPairedDeviceHolder(itemView: View) : PairedDeviceHolder(itemView)

    private inner class ConnectedPairedDeviceHolder(itemView: View) : PairedDeviceHolder(itemView) {
        init {
            itemView.setOnClickListener {
                callbacks?.onDisconnect()
            }
        }
    }

    private inner class PairedDevicesAdapter :
        ListAdapter<BluetoothDeviceWithState, PairedDeviceHolder>(DiffCallback()) {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): PairedDeviceHolder {
            return when (viewType) {
                BluetoothAdapter.STATE_CONNECTING,
                BluetoothAdapter.STATE_DISCONNECTING -> ConnectingPairedDeviceHolder(
                    layoutInflater.inflate(
                        R.layout.list_item_bluetooth_device_connecting,
                        parent,
                        false
                    )
                )
                BluetoothAdapter.STATE_CONNECTED -> ConnectedPairedDeviceHolder(
                    layoutInflater.inflate(
                        R.layout.list_item_bluetooth_device_connected,
                        parent,
                        false
                    )
                )
                else -> DisconnectedPairedDeviceHolder(
                    layoutInflater.inflate(
                        R.layout.list_item_bluetooth_device,
                        parent,
                        false
                    )
                )
            }
        }

        override fun onBindViewHolder(holderPaired: PairedDeviceHolder, position: Int) {
            holderPaired.bind(getItem(position))
        }

        override fun getItemViewType(position: Int): Int = getItem(position).state

        fun submitSortedList(list: List<BluetoothDeviceWithState>?) {
            submitList(
                list?.sortedBy {
                    when (it.state) {
                        BluetoothAdapter.STATE_CONNECTED -> -1
                        else -> 0
                    }
                }
            )
        }
    }

    private abstract inner class AvailableDeviceHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        protected lateinit var bluetoothDeviceWithState: BluetoothDeviceWithState

        private val deviceNameTextView: TextView =
            itemView.findViewById(R.id.device_name_text_view)
        private val deviceMacAddressTextView: TextView =
            itemView.findViewById(R.id.device_mac_address_text_view)

        fun bind(bluetoothDevice: BluetoothDeviceWithState) {
            this.bluetoothDeviceWithState = bluetoothDevice
            if (checkHasBluetoothPermission()) {
                deviceNameTextView.text = bluetoothDevice.device.name
            }
            deviceMacAddressTextView.text = bluetoothDevice.device.address
        }
    }

    private inner class NotPairedAvailableDeviceHolder(itemView: View) :
        AvailableDeviceHolder(itemView) {
        init {
            itemView.setOnClickListener {
                if (checkHasScanPermission()) {
                    bluetoothDeviceWithState.device.createBond()
                }
            }
        }
    }

    private inner class PairingAvailableDeviceHolder(itemView: View) :
        AvailableDeviceHolder(itemView)

    private inner class AvailableDevicesAdapter :
        ListAdapter<BluetoothDeviceWithState, AvailableDeviceHolder>(DiffCallback()) {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): AvailableDeviceHolder {
            return when (viewType) {
                BluetoothDevice.BOND_BONDING -> PairingAvailableDeviceHolder(
                    layoutInflater.inflate(
                        R.layout.list_item_bluetooth_device_pairing,
                        parent,
                        false
                    )
                )
                else -> NotPairedAvailableDeviceHolder(
                    layoutInflater.inflate(
                        R.layout.list_item_bluetooth_device,
                        parent,
                        false
                    )
                )
            }
        }

        override fun onBindViewHolder(holderPaired: AvailableDeviceHolder, position: Int) {
            holderPaired.bind(getItem(position))
        }

        override fun getItemViewType(position: Int): Int = getItem(position).state
    }

    private class DiffCallback : DiffUtil.ItemCallback<BluetoothDeviceWithState>() {
        override fun areItemsTheSame(
            oldItem: BluetoothDeviceWithState,
            newItem: BluetoothDeviceWithState
        ): Boolean = oldItem.device.address == newItem.device.address

        override fun areContentsTheSame(
            oldItem: BluetoothDeviceWithState,
            newItem: BluetoothDeviceWithState
        ): Boolean = oldItem == newItem
    }
}
package com.maxclub.android.hellobluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.maxclub.android.hellobluetooth.data.Command
import com.maxclub.android.hellobluetooth.destinations.ConnectionFragment
import com.maxclub.android.hellobluetooth.destinations.ControllerFragment
import com.maxclub.android.hellobluetooth.destinations.ExamplesPage
import com.maxclub.android.hellobluetooth.destinations.TerminalFragment
import com.maxclub.android.hellobluetooth.receivers.BluetoothStateReceiver
import com.maxclub.android.hellobluetooth.receivers.BluetoothTransferReceiver
import com.maxclub.android.hellobluetooth.viewmodel.MainViewModel
import java.util.*

class MainActivity : AppCompatActivity(),
    BluetoothStateReceiver.Callbacks,
    BluetoothTransferReceiver.Callbacks,
    ConnectionFragment.Callbacks,
    ControllerFragment.Callbacks,
    TerminalFragment.Callbacks {
    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var navHeaderSubtitleTextView: TextView
    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var destinationChangedListener: NavController.OnDestinationChangedListener
    private val bluetoothStateReceiver: BluetoothStateReceiver = BluetoothStateReceiver()
    private val bluetoothTransferReceiver: BluetoothTransferReceiver = BluetoothTransferReceiver()

    private val topLevelDestinationIds = setOf(
        R.id.connectionFragment,
        R.id.myControllersFragment,
        R.id.terminalFragment,
        R.id.settingsFragment,
    )
    private val destinationIdsWithConnectionState = setOf(
        R.id.connectionFragment,
        R.id.myControllersFragment,
        R.id.controllerFragment,
        R.id.terminalFragment,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById<NavigationView>(R.id.navigationView).apply {
            setupWithNavController(navController)
            menu.findItem(R.id.examplesPage)
                .setOnMenuItemClickListener {
                    ExamplesPage().launch(this@MainActivity)
                    false
                }
        }
        navHeaderSubtitleTextView = navigationView.getHeaderView(0)
            .findViewById(R.id.navHeaderSubtitleTextView)
        appBarConfiguration = AppBarConfiguration(topLevelDestinationIds, drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        destinationChangedListener = NavController.OnDestinationChangedListener { _, _, _ ->
            hideKeyboard()
            updateUIbyConnectionState()
        }
        navController.addOnDestinationChangedListener(destinationChangedListener)

        bluetoothStateReceiver.register(this, this)
        bluetoothTransferReceiver.register(this, this)

        mainViewModel.bluetoothService.state.observe(this) { state ->
            updateUIbyConnectionState()

            if (state == BluetoothAdapter.STATE_CONNECTED) {
                mainViewModel.startListening()
            } else if (state == BluetoothAdapter.STATE_DISCONNECTED ||
                state == BluetoothAdapter.STATE_OFF
            ) {
                mainViewModel.bluetoothService.closeConnection()
                mainViewModel.clearCommands()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        updateUIbyConnectionState()
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothStateReceiver.unregister(this)
        bluetoothTransferReceiver.unregister(this)
        navController.removeOnDestinationChangedListener(destinationChangedListener)
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.close()
        else super.onBackPressed()
    }

    /*
     * ConnectionFragment.Callbacks
     */
    override fun onConnect(device: BluetoothDevice) {
        mainViewModel.connect(device)
    }

    override fun onDisconnect() {
        mainViewModel.disconnect()
    }

    override fun getState(): LiveData<Int> = mainViewModel.bluetoothService.state

    override fun getDevice(): BluetoothDevice? = mainViewModel.bluetoothService.device

    /*
     * ControllerFragment.Callbacks
     * TerminalFragment.Callbacks
     */
    override fun onSend(data: String) {
        mainViewModel.bluetoothService.send(data)
    }

    override fun getCommands(): LiveData<List<Command>> = mainViewModel.getCommands()

    /*
     * BluetoothStateReceiver.Callbacks
     */
    override fun onStateChanged(state: Int) {
        mainViewModel.bluetoothService.updateState(state)
    }

    override fun onConnectionStateChanged(state: Int, device: BluetoothDevice) {
        mainViewModel.bluetoothService.device?.let {
            if (device == it) {
                mainViewModel.bluetoothService.updateState(state)
            }
        }
    }

    override fun onFailure(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /*
     * BluetoothTransferReceiver.Callbacks
     */
    override fun onSent(data: String) {
        val newCommand = Command(Command.OUTPUT_COMMAND, data, Date())
        mainViewModel.addCommand(newCommand)
    }

    override fun onReceived(data: String) {
        val newCommand = Command(Command.INPUT_COMMAND, data, Date())
        mainViewModel.addCommand(newCommand)
    }

    override fun onFailure(data: String, message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun hideKeyboard() {
        findViewById<View>(android.R.id.content)?.let {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun updateUIbyConnectionState() {
        mainViewModel.bluetoothService.state.value?.let { state ->
            val connectionState = connectionStateCodeToString(state)
            navHeaderSubtitleTextView.text = connectionState
            supportActionBar?.subtitle =
                if (destinationIdsWithConnectionState.contains(navController.currentDestination?.id)) {
                    connectionState
                } else {
                    null
                }
        }
    }

    private fun connectionStateCodeToString(code: Int): String =
        when (code) {
            BluetoothAdapter.STATE_OFF -> getString(R.string.state_off)
            BluetoothAdapter.STATE_ON -> getString(R.string.state_on)
            BluetoothAdapter.STATE_TURNING_OFF -> getString(R.string.state_turning_off)
            BluetoothAdapter.STATE_TURNING_ON -> getString(R.string.state_turning_on)
            BluetoothAdapter.STATE_DISCONNECTED -> getString(R.string.state_disconnected)
            BluetoothAdapter.STATE_CONNECTED -> getString(R.string.state_connected)
            BluetoothAdapter.STATE_DISCONNECTING -> getString(R.string.state_disconnecting)
            BluetoothAdapter.STATE_CONNECTING -> getString(R.string.state_connecting)
            else -> getString(R.string.state_none)
        }
}
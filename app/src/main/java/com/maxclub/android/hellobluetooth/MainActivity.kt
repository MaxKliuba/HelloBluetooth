package com.maxclub.android.hellobluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.maxclub.android.hellobluetooth.bluetooth.IBluetoothConnectionCallbacks
import com.maxclub.android.hellobluetooth.bluetooth.IBluetoothDataCallbacks
import com.maxclub.android.hellobluetooth.model.Command
import com.maxclub.android.hellobluetooth.utils.ExamplesPage
import com.maxclub.android.hellobluetooth.receivers.BluetoothStateReceiver
import com.maxclub.android.hellobluetooth.receivers.BluetoothTransferReceiver
import com.maxclub.android.hellobluetooth.viewmodel.MainViewModel
import java.util.*

class MainActivity : AppCompatActivity(),
    BluetoothStateReceiver.Callbacks,
    BluetoothTransferReceiver.Callbacks,
    IBluetoothConnectionCallbacks,
    IBluetoothDataCallbacks {
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

    private lateinit var sendDataAlertDialog: AlertDialog

    private val topLevelDestinationIds = setOf(
        R.id.connection_fragment,
        R.id.my_controllers_fragment,
        R.id.terminal_fragment,
        R.id.settings_fragment,
    )
    private val destinationIdsWithConnectionState = setOf(
        R.id.connection_fragment,
        R.id.my_controllers_fragment,
        R.id.controller_fragment,
        R.id.terminal_fragment,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container_view) as NavHostFragment
        navController = navHostFragment.navController
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById<NavigationView>(R.id.navigation_view).apply {
            setupWithNavController(navController)
            menu.findItem(R.id.examples_page)
                .setOnMenuItemClickListener {
                    ExamplesPage().launch(this@MainActivity)
                    false
                }
        }
        navHeaderSubtitleTextView = navigationView.getHeaderView(0)
            .findViewById(R.id.nav_header_subtitle_text_view)
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
            }
        }

        sendDataAlertDialog = AlertDialog.Builder(this)
            .apply {
                setIcon(R.drawable.ic_baseline_error_24)
                setTitle(R.string.connection_error_dialog_title)
                setMessage(R.string.connection_error_dialog_message)
                setPositiveButton(R.string.connect_dialog_button_text) { _, _ ->
                    navController.navigate(R.id.connection_fragment)
                }
                setCancelable(true)
            }.create()
            .apply {
                window?.setBackgroundDrawableResource(R.drawable.popup_menu_background)
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

    override fun onSupportNavigateUp(): Boolean {
        hideKeyboard()
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.close()
        else super.onBackPressed()
    }

    /*
     * IBluetoothConnectionCallbacks
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
     * IBluetoothDataCallbacks
     */
    override fun onSend(data: String) {
        mainViewModel.bluetoothService.send(data)
    }

    override fun onCommandListener(): LiveData<Command> = mainViewModel.getCommand()

    /*
     * BluetoothStateReceiver.Callbacks
     */
    override fun onStateChanged(state: Int) {
        if (state == BluetoothAdapter.STATE_ON) {
            mainViewModel.bluetoothService.updateState(BluetoothAdapter.STATE_DISCONNECTED)
        } else {
            mainViewModel.bluetoothService.updateState(state)
        }
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
        val newCommand = Command(Command.OUTPUT_COMMAND, data, Date(), true)
        mainViewModel.addCommand(newCommand)
    }

    override fun onReceived(data: String) {
        val newCommand = Command(Command.INPUT_COMMAND, data, Date(), true)
        mainViewModel.addCommand(newCommand)
    }

    override fun onFailure(data: String, message: String) {
        val newCommand = Command(Command.OUTPUT_COMMAND, data, Date(), false)
        mainViewModel.addCommand(newCommand)

        if (!sendDataAlertDialog.isShowing) {
            sendDataAlertDialog.show()
        }
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
package com.maxclub.android.hellobluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), BluetoothStateBroadcastReceiver.BluetoothStateListener {
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
    private val bluetoothStateBroadcastReceiver: BluetoothStateBroadcastReceiver =
        BluetoothStateBroadcastReceiver()

    private val topLevelDestinationIds = setOf(
        R.id.connectionFragment,
        R.id.myControllersFragment,
        R.id.terminalFragment,
        R.id.settingsFragment,
    )
    private val destinationIdsWithConnectionState = setOf(
        R.id.connectionFragment,
        R.id.myControllersFragment,
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
        }
        navHeaderSubtitleTextView = navigationView.getHeaderView(0)
            .findViewById(R.id.navHeaderSubtitleTextView)
        appBarConfiguration = AppBarConfiguration(topLevelDestinationIds, drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)

        destinationChangedListener =
            NavController.OnDestinationChangedListener { _, destination, _ ->
                mainViewModel.currentDestination = destination
                BluetoothService.state.value?.let { code ->
                    supportActionBar?.subtitle =
                        if (destinationIdsWithConnectionState.contains(mainViewModel.currentDestination.id)) {
                            connectionStateCodeToString(code)
                        } else {
                            null
                        }
                }
            }
        navController.addOnDestinationChangedListener(destinationChangedListener)

        bluetoothStateBroadcastReceiver.register(this, this)

        BluetoothService.state.observe(this) {
            updateUIbyConnectionState()
        }
    }

    override fun onStart() {
        super.onStart()
        updateUIbyConnectionState()
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothStateBroadcastReceiver.unregister(this)
        navController.removeOnDestinationChangedListener(destinationChangedListener)
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()

    override fun onStateChanged(state: Int) {
        BluetoothService.updateState(state)
    }

    override fun onConnectionStateChanged(state: Int, device: BluetoothDevice) {
        BluetoothService.processDevice?.let {
            if (device == it) {
                BluetoothService.updateState(state)
            }
        }
    }

    private fun updateUIbyConnectionState() {
        BluetoothService.state.value?.let { state ->
            val connectionState = connectionStateCodeToString(state)
            navHeaderSubtitleTextView.text = connectionState
            supportActionBar?.subtitle =
                if (destinationIdsWithConnectionState.contains(mainViewModel.currentDestination.id)) {
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
            else -> getString(R.string.state_error)
        }
}
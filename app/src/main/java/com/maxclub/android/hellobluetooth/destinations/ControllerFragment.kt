package com.maxclub.android.hellobluetooth.destinations

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.maxclub.android.hellobluetooth.R
import com.maxclub.android.hellobluetooth.bluetooth.IBluetoothDataCallbacks
import com.maxclub.android.hellobluetooth.viewmodel.ControllerViewModel

class ControllerFragment : Fragment() {
    private var callbacks: IBluetoothDataCallbacks? = null

    private val controllerViewModel: ControllerViewModel by lazy {
        ViewModelProvider(this)[ControllerViewModel::class.java]
    }

    private lateinit var navController: NavController
    private val args: ControllerFragmentArgs by navArgs()

    private lateinit var widgetsRecyclerView: RecyclerView
    private lateinit var addWidgetFloatingActionButton: FloatingActionButton
    private lateinit var applyChangesFloatingActionButton: FloatingActionButton

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as? IBluetoothDataCallbacks?
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_controller, container, false)
        navController = findNavController()

        (activity as? AppCompatActivity)?.supportActionBar?.title = args.controller.name

        widgetsRecyclerView = view.findViewById<RecyclerView?>(R.id.widgetsRecyclerView).apply {
            layoutManager = GridLayoutManager(context, 2)
            // adapter =
        }

        addWidgetFloatingActionButton =
            view.findViewById<FloatingActionButton>(R.id.addWidgetFloatingActionButton).apply {
                setOnClickListener {
                    val direction =
                        ControllerFragmentDirections.actionControllerFragmentToWidgetSettingsFragment(
                            args.controller, null
                        )
                    navController.navigate(direction)
                }
            }

        applyChangesFloatingActionButton =
            view.findViewById<FloatingActionButton>(R.id.applyChangesFloatingActionButton).apply {
                setOnClickListener {
                    // TODO
                    controllerViewModel.isDragging = false
                    updateFloatingActionButtonState()
                }
            }

        updateFloatingActionButtonState()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controllerViewModel.getWidgets(args.controller.id).observe(viewLifecycleOwner) {
            // TODO
        }

        callbacks?.getCommands()?.observe(viewLifecycleOwner) {
            // TODO
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun updateFloatingActionButtonState() {
        if (controllerViewModel.isDragging) {
            applyChangesFloatingActionButton.show()
        } else {
            applyChangesFloatingActionButton.hide()
        }
    }
}
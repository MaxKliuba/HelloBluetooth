package com.maxclub.android.hellobluetooth.destinations

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.PopupMenu
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.maxclub.android.hellobluetooth.R
import com.maxclub.android.hellobluetooth.data.Controller
import com.maxclub.android.hellobluetooth.data.ControllerWithWidgets
import com.maxclub.android.hellobluetooth.viewmodel.MyControllersViewModel
import java.lang.reflect.Method

class MyControllersFragment : Fragment() {
    private val myControllersViewModel: MyControllersViewModel by lazy {
        ViewModelProvider(this)[MyControllersViewModel::class.java]
    }

    private lateinit var navController: NavController

    private lateinit var controllersRecyclerView: RecyclerView
    private lateinit var controllersAdapter: ControllersAdapter
    private lateinit var addControllerFloatingActionButton: FloatingActionButton
    private lateinit var applyChangesFloatingActionButton: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_controllers, container, false)
        navController = findNavController()

        controllersRecyclerView =
            view.findViewById<RecyclerView>(R.id.controllersRecyclerView).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = ControllersAdapter().apply {
                    controllersAdapter = this
                }
            }

        addControllerFloatingActionButton =
            view.findViewById<FloatingActionButton>(R.id.addControllerFloatingActionButton).apply {
                setOnClickListener {
                    val direction =
                        MyControllersFragmentDirections.actionMyControllersFragmentToControllerSettingsFragment(
                            null
                        )
                    navController.navigate(direction)
                }
            }

        applyChangesFloatingActionButton =
            view.findViewById<FloatingActionButton>(R.id.applyChangesFloatingActionButton).apply {
                setOnClickListener {
                    // TODO
                    myControllersViewModel.isDragging = false
                    updateFloatingActionButtonState()
                }
            }

        updateFloatingActionButtonState()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myControllersViewModel.getControllersWithWidgets().observe(viewLifecycleOwner) {
            controllersAdapter.submitList(it)
        }
    }

    private fun updateFloatingActionButtonState() {
        if (myControllersViewModel.isDragging) {
            applyChangesFloatingActionButton.show()
        } else {
            applyChangesFloatingActionButton.hide()
        }
    }

    private fun showPopupMenu(anchor: View, controller: Controller) {
        PopupMenu(context, anchor).apply {
            menuInflater.inflate(R.menu.controller_popup, menu)

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.dragMenuItem -> {
                        myControllersViewModel.isDragging = true
                        updateFloatingActionButtonState()
                        // TODO
                        true
                    }
                    R.id.editMenuItem -> {
                        val direction =
                            MyControllersFragmentDirections.actionMyControllersFragmentToControllerSettingsFragment(
                                controller
                            )
                        navController.navigate(direction)
                        true
                    }
                    R.id.deleteMenuItem -> {
                        myControllersViewModel.deleteController(controller)
                        true
                    }
                    else -> false
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setForceShowIcon(true)
            } else {
                try {
                    val fields = this.javaClass.declaredFields
                    for (field in fields) {
                        if ("mPopup" == field.name) {
                            field.isAccessible = true
                            val menuPopupHelper = field[this]
                            val classPopupHelper =
                                Class.forName(menuPopupHelper.javaClass.name)
                            val setForceIcons: Method = classPopupHelper.getMethod(
                                "setForceShowIcon",
                                Boolean::class.javaPrimitiveType
                            )
                            setForceIcons.invoke(menuPopupHelper, true)
                            break
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            show()
        }
    }

    private inner class ControllerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var controllerWithWidgets: ControllerWithWidgets

        private val controllerNameTextView: TextView =
            itemView.findViewById(R.id.controllerNameTextView)
        private val widgetsAmountTextView: TextView =
            itemView.findViewById(R.id.widgetsAmountTextView)

        init {
            itemView.apply {
                setOnClickListener {
                    if (!myControllersViewModel.isDragging) {
                        val direction =
                            MyControllersFragmentDirections.actionMyControllersFragmentToControllerFragment(
                                controllerWithWidgets.controller
                            )
                        navController.navigate(direction)
                    }
                }
                setOnLongClickListener {
                    if (!myControllersViewModel.isDragging) {
                        showPopupMenu(it, controllerWithWidgets.controller)
                        true
                    } else {
                        false
                    }
                }
            }
        }

        fun bind(controllerWithWidgets: ControllerWithWidgets) {
            this.controllerWithWidgets = controllerWithWidgets
            controllerNameTextView.text = controllerWithWidgets.controller.name
            val widgetsAmount = controllerWithWidgets.widgets.size
            widgetsAmountTextView.text =
                resources.getQuantityString(R.plurals.widget_plural, widgetsAmount, widgetsAmount)
        }
    }

    private inner class ControllersAdapter : RecyclerView.Adapter<ControllerHolder>() {
        private val controllers: SortedList<ControllerWithWidgets> = SortedList(
            ControllerWithWidgets::class.java,
            object : SortedList.Callback<ControllerWithWidgets>() {
                override fun compare(
                    item1: ControllerWithWidgets,
                    item2: ControllerWithWidgets
                ): Int =
                    item1.controller.order.compareTo(item2.controller.order)

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
                    oldItem: ControllerWithWidgets,
                    newItem: ControllerWithWidgets
                ): Boolean =
                    oldItem.controller == newItem.controller

                override fun areItemsTheSame(
                    item1: ControllerWithWidgets,
                    item2: ControllerWithWidgets
                ): Boolean =
                    item1.controller.id == item2.controller.id
            })

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ControllerHolder =
            ControllerHolder(layoutInflater.inflate(R.layout.list_item_controller, parent, false))

        override fun onBindViewHolder(holder: ControllerHolder, position: Int) {
            holder.bind(controllers[position])
        }

        override fun getItemCount(): Int = controllers.size()

        fun submitList(items: List<ControllerWithWidgets>) {
            controllers.replaceAll(items)
        }
    }
}
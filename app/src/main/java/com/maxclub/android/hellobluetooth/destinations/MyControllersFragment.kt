package com.maxclub.android.hellobluetooth.destinations

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.PopupMenu
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
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
            view.findViewById<RecyclerView>(R.id.controllers_recycler_view).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = ControllersAdapter(DiffCallback()).apply {
                    controllersAdapter = this
                }
            }

        addControllerFloatingActionButton =
            view.findViewById<FloatingActionButton>(R.id.add_controller_floating_action_button)
                .apply {
                    setOnClickListener {
                        val direction =
                            MyControllersFragmentDirections.actionMyControllersFragmentToControllerSettingsFragment(
                                null
                            )
                        navController.navigate(direction)
                    }
                }

        applyChangesFloatingActionButton =
            view.findViewById<FloatingActionButton>(R.id.apply_changes_floating_action_button)
                .apply {
                    setOnClickListener {
                        // TODO
                        myControllersViewModel.isDragged = false
                        updateFloatingActionButtonState()
                        controllersAdapter.submitList()
                    }
                }

        updateFloatingActionButtonState()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myControllersViewModel.getControllersWithWidgets()
            .observe(viewLifecycleOwner) { controllersWithWidgets ->
                controllersAdapter.submitList(controllersWithWidgets.sortedBy { it.controller.order })
            }
    }

    private fun updateFloatingActionButtonState() {
        if (myControllersViewModel.isDragged) {
            applyChangesFloatingActionButton.show()
        } else {
            applyChangesFloatingActionButton.hide()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showPopupMenu(anchor: View, controller: Controller) {
        PopupMenu(context, anchor).apply {
            menuInflater.inflate(R.menu.controller_popup, menu)

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.drag -> {
                        myControllersViewModel.isDragged = true
                        updateFloatingActionButtonState()
                        controllersAdapter.submitList()
                        true
                    }
                    R.id.edit -> {
                        val direction =
                            MyControllersFragmentDirections.actionMyControllersFragmentToControllerSettingsFragment(
                                controller
                            )
                        navController.navigate(direction)
                        true
                    }
                    R.id.delete -> {
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

    private abstract inner class ControllerHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        protected lateinit var controllerWithWidgets: ControllerWithWidgets

        protected val controllerNameTextView: TextView =
            itemView.findViewById(R.id.controller_name_text_view)
        protected val widgetsAmountTextView: TextView =
            itemView.findViewById(R.id.widgets_amount_text_view)

        fun bind(controllerWithWidgets: ControllerWithWidgets) {
            this.controllerWithWidgets = controllerWithWidgets
            controllerNameTextView.text = controllerWithWidgets.controller.name
            val widgetsAmount = controllerWithWidgets.widgets.size
            widgetsAmountTextView.text =
                resources.getQuantityString(R.plurals.widget_plural, widgetsAmount, widgetsAmount)
        }
    }

    private inner class ClickableControllerHolder(itemView: View) : ControllerHolder(itemView) {
        init {
            itemView.apply {
                setOnClickListener {
                    val direction =
                        MyControllersFragmentDirections.actionMyControllersFragmentToControllerFragment(
                            controllerWithWidgets.controller
                        )
                    navController.navigate(direction)
                }
                setOnLongClickListener {
                    showPopupMenu(it, controllerWithWidgets.controller)
                    true
                }
            }
        }
    }

    private inner class DraggedControllerHolder(itemView: View) : ControllerHolder(itemView)

    private inner class ControllersAdapter(val diffCallback: DiffCallback) :
        ListAdapter<ControllerWithWidgets, ControllerHolder>(diffCallback) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ControllerHolder =
            when (viewType) {
                DRAGGED_TYPE -> DraggedControllerHolder(
                    layoutInflater.inflate(
                        R.layout.list_item_controller_drag,
                        parent,
                        false
                    )
                )
                else -> ClickableControllerHolder(
                    layoutInflater.inflate(
                        R.layout.list_item_controller,
                        parent,
                        false
                    )
                )
            }

        override fun onBindViewHolder(holder: ControllerHolder, position: Int) {
            holder.bind(getItem(position))
        }

        override fun getItemViewType(position: Int): Int =
            if (myControllersViewModel.isDragged) {
                DRAGGED_TYPE
            } else {
                CLICKABLE_TYPE
            }

        override fun onCurrentListChanged(
            previousList: MutableList<ControllerWithWidgets>,
            currentList: MutableList<ControllerWithWidgets>
        ) {
            super.onCurrentListChanged(previousList, currentList)
            diffCallback.isForceUpdate = false
        }

        fun submitList() {
            diffCallback.isForceUpdate = true
            submitList(currentList)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ControllerWithWidgets>() {
        var isForceUpdate: Boolean = false

        override fun areItemsTheSame(
            oldItem: ControllerWithWidgets,
            newItem: ControllerWithWidgets
        ): Boolean = oldItem.controller.id == newItem.controller.id

        override fun areContentsTheSame(
            oldItem: ControllerWithWidgets,
            newItem: ControllerWithWidgets
        ): Boolean = oldItem == newItem && !isForceUpdate
    }

    companion object {
        private const val CLICKABLE_TYPE = 0
        private const val DRAGGED_TYPE = 1
    }
}
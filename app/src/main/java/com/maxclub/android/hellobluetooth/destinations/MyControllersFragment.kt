package com.maxclub.android.hellobluetooth.destinations

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.maxclub.android.hellobluetooth.R
import com.maxclub.android.hellobluetooth.data.ControllerWithWidgets
import com.maxclub.android.hellobluetooth.utils.DeleteDialogBuilder
import com.maxclub.android.hellobluetooth.utils.PopupMenuBuilder
import com.maxclub.android.hellobluetooth.viewmodel.MyControllersViewModel

class MyControllersFragment : Fragment() {
    private val myControllersViewModel: MyControllersViewModel by lazy {
        ViewModelProvider(this)[MyControllersViewModel::class.java]
    }

    private lateinit var navController: NavController

    private lateinit var controllersPlaceholder: View
    private lateinit var controllersRecyclerView: RecyclerView
    private lateinit var controllersAdapter: ControllersAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var addControllerFloatingActionButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_controllers, container, false)
        navController = findNavController()

        controllersPlaceholder = view.findViewById(R.id.controllers_placeholder)
        controllersRecyclerView =
            view.findViewById<RecyclerView>(R.id.controllers_recycler_view).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = ControllersAdapter(DiffCallback()).apply {
                    controllersAdapter = this
                }
                itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback).also {
                    it.attachToRecyclerView(this)
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

        updateAddControllerButtonState()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myControllersViewModel.getControllersWithWidgets()
            .observe(viewLifecycleOwner) { controllersWithWidgets ->
                if (!myControllersViewModel.isDragged) {
                    myControllersViewModel.tempList = controllersWithWidgets
                }
                updateControllersRecyclerView(myControllersViewModel.tempList)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_my_controllers, menu)
        menu.findItem(R.id.apply).isVisible = myControllersViewModel.isDragged
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.apply -> {
                myControllersViewModel.isDragged = false
                updateAddControllerButtonState()
                activity?.invalidateOptionsMenu()
                controllersAdapter.forceUpdateSortedList()
                val controllers = myControllersViewModel.tempList.map { it.controller }
                    .toTypedArray()
                myControllersViewModel.updateControllers(*controllers)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun updateAddControllerButtonState() {
        if (myControllersViewModel.isDragged) {
            addControllerFloatingActionButton.hide()
        } else {
            addControllerFloatingActionButton.show()
        }
    }

    private fun updateControllersRecyclerView(controllers: List<ControllerWithWidgets>?) {
        controllersPlaceholder.isVisible = controllers.isNullOrEmpty()
        controllersAdapter.submitSortedList(controllers)
    }

    private fun showPopupMenu(anchor: View, controllerWithWidgets: ControllerWithWidgets) {
        PopupMenuBuilder.create(requireContext(), anchor, R.menu.controller_popup) {
            when (it.itemId) {
                R.id.share -> {
                    QrCodeDialogFragment.newInstance(controllerWithWidgets)
                        .show(childFragmentManager, QrCodeDialogFragment.TAG)
                    true
                }
                R.id.drag -> {
                    myControllersViewModel.isDragged = true
                    updateAddControllerButtonState()
                    activity?.invalidateOptionsMenu()
                    controllersAdapter.forceUpdateSortedList()
                    true
                }
                R.id.edit -> {
                    val direction =
                        MyControllersFragmentDirections.actionMyControllersFragmentToControllerSettingsFragment(
                            controllerWithWidgets.controller
                        )
                    navController.navigate(direction)
                    true
                }
                R.id.delete -> {
                    DeleteDialogBuilder
                        .create(requireContext(), controllerWithWidgets.controller.name) {
                            myControllersViewModel.deleteController(
                                controllerWithWidgets.controller
                            )
                        }
                        .show()
                    true
                }
                else -> false
            }
        }.show()
    }

    private abstract inner class ControllerHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        protected lateinit var controllerWithWidgets: ControllerWithWidgets

        protected val controllerNameTextView: TextView =
            itemView.findViewById(R.id.controller_name_text_view)
        protected val widgetsCountTextView: TextView =
            itemView.findViewById(R.id.widgets_count_text_view)

        fun bind(controllerWithWidgets: ControllerWithWidgets) {
            this.controllerWithWidgets = controllerWithWidgets
            controllerNameTextView.text = controllerWithWidgets.controller.name
            widgetsCountTextView.text =
                resources.getQuantityString(
                    R.plurals.widget_plural,
                    controllerWithWidgets.widgets.size,
                    controllerWithWidgets.widgets.size
                )
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
                    showPopupMenu(it, controllerWithWidgets)
                    true
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private inner class DraggedControllerHolder(itemView: View) : ControllerHolder(itemView) {
        init {
            itemView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(this)
                }
                false
            }
        }
    }

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

        fun submitSortedList(list: List<ControllerWithWidgets>?) {
            submitList(list?.sortedBy { it.controller.order })
        }

        fun forceUpdateSortedList() {
            diffCallback.isForceUpdate = true
            submitSortedList(currentList)
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
        ): Boolean =
            oldItem == newItem && !isForceUpdate
    }

    private val itemTouchHelperCallback = object :
        ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or
                    ItemTouchHelper.DOWN or
                    ItemTouchHelper.START or
                    ItemTouchHelper.END,
            0
        ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromController =
                controllersAdapter.currentList[viewHolder.adapterPosition].controller
            val toController = controllersAdapter.currentList[target.adapterPosition].controller
            val fromOrder = fromController.order
            val toOrder = toController.order
            myControllersViewModel.tempList = controllersAdapter.currentList.map {
                it.apply {
                    if (controller == fromController) {
                        controller.order = toOrder
                    } else if (controller == toController) {
                        controller.order = fromOrder
                    }
                }
            }
            updateControllersRecyclerView(myControllersViewModel.tempList)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }

        override fun isLongPressDragEnabled(): Boolean = myControllersViewModel.isDragged
    }

    companion object {
        private const val CLICKABLE_TYPE = 0
        private const val DRAGGED_TYPE = 1
    }
}
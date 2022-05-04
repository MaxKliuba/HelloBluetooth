package com.maxclub.android.hellobluetooth.destinations

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.maxclub.android.hellobluetooth.R
import com.maxclub.android.hellobluetooth.bluetooth.IBluetoothDataCallbacks
import com.maxclub.android.hellobluetooth.data.Widget
import com.maxclub.android.hellobluetooth.utils.CommandHelper
import com.maxclub.android.hellobluetooth.viewmodel.ControllerViewModel
import java.lang.reflect.Method

private const val LOG_TAG = "ControllerFragment"

class ControllerFragment : Fragment() {
    private var callbacks: IBluetoothDataCallbacks? = null

    private val controllerViewModel: ControllerViewModel by lazy {
        ViewModelProvider(this)[ControllerViewModel::class.java]
    }

    private lateinit var navController: NavController
    private val args: ControllerFragmentArgs by navArgs()

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var widgetsRecyclerView: RecyclerView
    private lateinit var widgetsAdapter: WidgetsAdapter
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

        swipeRefreshLayout =
            view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout).apply {
                setOnRefreshListener {
                    callbacks?.onSend(CommandHelper.SYNC)
                }
            }

        widgetsRecyclerView = view.findViewById<RecyclerView>(R.id.widgets_recycler_view).apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = WidgetsAdapter().apply {
                widgetsAdapter = this
            }
        }

        addWidgetFloatingActionButton =
            view.findViewById<FloatingActionButton>(R.id.add_widget_floating_action_button).apply {
                setOnClickListener {
                    val direction =
                        ControllerFragmentDirections.actionControllerFragmentToWidgetSettingsFragment(
                            args.controller, null
                        )
                    navController.navigate(direction)
                }
            }

        applyChangesFloatingActionButton =
            view.findViewById<FloatingActionButton>(R.id.apply_changes_floating_action_button)
                .apply {
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
        controllerViewModel.getWidgets(args.controller.id).observe(viewLifecycleOwner) { widgets ->
            val commands = controllerViewModel.getCommands()
            Log.d(LOG_TAG, commands.size.toString())
            if (commands.isNotEmpty()) {
                val tagsWithData = commands.filter { it.isSuccess }
                    .map { CommandHelper.parse(it.text) }
                    .groupBy({ it.first }) { it.second }
                widgets.forEachIndexed { index, widget ->
                    widgets[index].state = tagsWithData[widget.tag]?.last()
                }
            }
            widgetsAdapter.submitList(widgets)
        }

        callbacks?.onReceive()?.observe(viewLifecycleOwner) { command ->
            command?.let {
                if (command.text == CommandHelper.SYNC) {
                    swipeRefreshLayout.isRefreshing = false
                }

                val tagWithData = CommandHelper.parse(it.text)
                val widgets: MutableList<Widget> = mutableListOf()
                for (i in 0 until widgetsAdapter.widgets.size()) {
                    val widget = widgetsAdapter.widgets[i]
                    if (widget.tag == tagWithData.first && it.isSuccess) {
                        widget.state = tagWithData.second
                    }
                    widgets += widget
                }
                widgetsAdapter.submitList(widgets)
            }
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

    private fun showPopupMenu(anchor: View, widget: Widget) {
        PopupMenu(context, anchor).apply {
            menuInflater.inflate(R.menu.widget_popup, menu)

            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.drag -> {
                        controllerViewModel.isDragging = true
                        updateFloatingActionButtonState()
                        // TODO
                        true
                    }
                    R.id.edit -> {
                        val direction =
                            ControllerFragmentDirections.actionControllerFragmentToWidgetSettingsFragment(
                                args.controller, widget
                            )
                        navController.navigate(direction)
                        true
                    }
                    R.id.delete -> {
                        controllerViewModel.deleteWidget(widget)
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

    private abstract inner class WidgetHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        protected lateinit var widget: Widget

        protected val widgetNameTextView: TextView =
            itemView.findViewById(R.id.widget_name_text_view)

        init {
            itemView.setOnLongClickListener {
                if (!controllerViewModel.isDragging) {
                    showPopupMenu(it, widget)
                    true
                } else {
                    false
                }
            }
        }

        abstract fun bind(widget: Widget)
    }

    private inner class SwitchWidgetHolder(itemView: View) :
        WidgetHolder(itemView) {

        private val switchMaterial: SwitchMaterial =
            itemView.findViewById<SwitchMaterial>(R.id.widget_switch).apply {
                setOnCheckedChangeListener { button, isChecked ->
                    if (!button.isEnabled) return@setOnCheckedChangeListener

                    val data: String = if (isChecked) "1" else "0"
                    callbacks?.onSend(CommandHelper.create(widget.tag, data))
                    widget.desiredState = data
                }
            }

        constructor(parent: ViewGroup) : this(
            layoutInflater.inflate(
                R.layout.list_item_widget_switch,
                parent,
                false
            )
        )

        override fun bind(widget: Widget) {
            widgetNameTextView.text = widget.name
            widget.desiredState = widget.state
            switchMaterial.isEnabled = false
            switchMaterial.isChecked = widget.state == "1"
            if (!widget.isReadOnly) {
                switchMaterial.isEnabled = true
            }

            this.widget = widget
        }
    }

    private inner class ButtonWidgetHolder(itemView: View) :
        WidgetHolder(itemView) {

        @SuppressLint("ClickableViewAccessibility")
        private val materialButton: MaterialButton =
            itemView.findViewById<MaterialButton>(R.id.widget_button).apply {
                setOnTouchListener { button, motionEvent ->
                    if (!button.isEnabled) return@setOnTouchListener false

                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            val data = "1"
                            callbacks?.onSend(CommandHelper.create(widget.tag, data))
                            widget.desiredState = data
                        }
                        MotionEvent.ACTION_UP,
                        MotionEvent.ACTION_CANCEL -> {
                            val data = "0"
                            callbacks?.onSend(CommandHelper.create(widget.tag, data))
                            widget.desiredState = data
                        }
                    }
                    false
                }
            }

        constructor(parent: ViewGroup) : this(
            layoutInflater.inflate(
                R.layout.list_item_widget_button,
                parent,
                false
            )
        )

        override fun bind(widget: Widget) {
            widgetNameTextView.text = widget.name
            if (controllerViewModel.isWidgetIconResIdValid(widget.iconResId)) {
                materialButton.icon = ContextCompat.getDrawable(requireContext(), widget.iconResId)
            }
            widget.desiredState = widget.state
            materialButton.isEnabled = false
            materialButton.isPressed = widget.state == "1"
            if (!widget.isReadOnly) {
                materialButton.isEnabled = true
            }

            this.widget = widget
        }
    }

    private inner class WidgetsAdapter : RecyclerView.Adapter<WidgetHolder>() {
        val widgets: SortedList<Widget> = SortedList(
            Widget::class.java,
            object : SortedList.Callback<Widget>() {
                override fun compare(
                    item1: Widget,
                    item2: Widget
                ): Int =
                    item1.order.compareTo(item2.order)

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
                    oldItem: Widget,
                    newItem: Widget
                ): Boolean =
                    oldItem == newItem && oldItem.desiredState == newItem.state

                override fun areItemsTheSame(
                    item1: Widget,
                    item2: Widget
                ): Boolean =
                    item1.id == item2.id
            })

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetHolder =
            when (viewType) {
                Widget.Type.SWITCH.ordinal -> SwitchWidgetHolder(parent)
                else -> ButtonWidgetHolder(parent)
            }

        override fun onBindViewHolder(holder: WidgetHolder, position: Int) {
            holder.bind(widgets[position])
        }

        override fun getItemCount(): Int = widgets.size()

        override fun getItemViewType(position: Int): Int =
            widgets[position].type.ordinal

        fun submitList(items: List<Widget>) {
            widgets.replaceAll(items)
        }
    }
}
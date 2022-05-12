package com.maxclub.android.hellobluetooth.destinations

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import com.maxclub.android.hellobluetooth.R
import com.maxclub.android.hellobluetooth.bluetooth.IBluetoothDataCallbacks
import com.maxclub.android.hellobluetooth.data.Widget
import com.maxclub.android.hellobluetooth.utils.CommandHelper
import com.maxclub.android.hellobluetooth.utils.DeleteDialogBuilder
import com.maxclub.android.hellobluetooth.utils.PopupMenuBuilder
import com.maxclub.android.hellobluetooth.viewmodel.ControllerViewModel

class ControllerFragment : Fragment() {
    private var callbacks: IBluetoothDataCallbacks? = null

    private val controllerViewModel: ControllerViewModel by lazy {
        ViewModelProvider(this)[ControllerViewModel::class.java]
    }

    private lateinit var navController: NavController
    private val args: ControllerFragmentArgs by navArgs()

    private lateinit var widgetsPlaceholder: View
    private lateinit var widgetsRecyclerView: RecyclerView
    private lateinit var widgetsAdapter: WidgetsAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    private val speechRecognizerIntentResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { words ->
                    controllerViewModel.currentVoiceWidget?.let { widget ->
                        val data = words.joinToString(separator = " ")
                        callbacks?.onSend(CommandHelper.create(widget.tag, data))
                        widget.desiredState = data
                    }
                }
            }
            controllerViewModel.currentVoiceWidget = null
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as? IBluetoothDataCallbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_controller, container, false)
        navController = findNavController()

        (activity as? AppCompatActivity)?.supportActionBar?.title = args.controller.name

        widgetsPlaceholder = view.findViewById(R.id.widgets_placeholder)
        widgetsRecyclerView = view.findViewById<RecyclerView>(R.id.widgets_recycler_view).apply {
            layoutManager = GridLayoutManager(context, SPAN_COUNT)
            adapter = WidgetsAdapter(DiffCallback()).apply {
                widgetsAdapter = this
            }
            itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback).also {
                it.attachToRecyclerView(this)
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controllerViewModel.getWidgets(args.controller.id).observe(viewLifecycleOwner) { widgets ->
            if (!controllerViewModel.isDragged) {
                controllerViewModel.tempList = widgets
                val commands = controllerViewModel.getCommands()
                if (commands.isNotEmpty()) {
                    val tagsWithData = commands.filter { it.isSuccess }
                        .map { CommandHelper.parse(it.text) }
                        .groupBy({ it.first }) { it.second }
                    controllerViewModel.tempList.forEach {
                        it.state = tagsWithData[it.tag]?.last()
                    }
                }
            }
            updateWidgetsRecyclerView(controllerViewModel.tempList)
        }

        callbacks?.onCommandListener()?.observe(viewLifecycleOwner) {
            if (!controllerViewModel.isDragged && !controllerViewModel.tempList.isNullOrEmpty()) {
                it?.let { command ->
                    val tagWithData = CommandHelper.parse(it.text)
                    controllerViewModel.tempList = widgetsAdapter.currentList.map { widget ->
                        widget.apply {
                            if (tag == tagWithData.first && command.isSuccess) {
                                state = tagWithData.second
                            }
                        }
                    }
                    updateWidgetsRecyclerView(controllerViewModel.tempList)
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_controller, menu)
        menu.findItem(R.id.sync).isVisible = !controllerViewModel.isDragged
        menu.findItem(R.id.add).isVisible = !controllerViewModel.isDragged
        menu.findItem(R.id.apply).isVisible = controllerViewModel.isDragged
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.sync -> {
                val data = CommandHelper.HIGH_VALUE
                callbacks?.onSend(CommandHelper.create(CommandHelper.SYNC_TAG, data))
                true
            }
            R.id.add -> {
                val direction =
                    ControllerFragmentDirections.actionControllerFragmentToWidgetSettingsFragment(
                        args.controller, null
                    )
                navController.navigate(direction)
                true
            }
            R.id.apply -> {
                controllerViewModel.isDragged = false
                activity?.invalidateOptionsMenu()
                widgetsAdapter.forceUpdateSortedList()
                val widgets = controllerViewModel.tempList.toTypedArray()
                controllerViewModel.updateWidgets(*widgets)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun updateWidgetsRecyclerView(widgets: List<Widget>?) {
        widgetsPlaceholder.isVisible = widgets.isNullOrEmpty()
        widgetsAdapter.submitSortedList(widgets)
    }

    private fun showPopupMenu(anchor: View, widget: Widget) {
        PopupMenuBuilder.create(requireContext(), anchor, R.menu.widget_popup) {
            when (it.itemId) {
                R.id.drag -> {
                    controllerViewModel.isDragged = true
                    activity?.invalidateOptionsMenu()
                    widgetsAdapter.forceUpdateSortedList()
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
                    DeleteDialogBuilder
                        .create(requireContext(), widget.name) {
                            controllerViewModel.deleteWidget(widget)
                        }
                        .show()
                    true
                }
                else -> false
            }
        }.show()
    }

    private abstract inner class WidgetHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        protected lateinit var widget: Widget

        protected val widgetNameTextView: TextView =
            itemView.findViewById(R.id.widget_name_text_view)

        abstract fun bind(widget: Widget)

        abstract fun bindState(widget: Widget)
    }

    private abstract inner class ClickableWidgetHolder(itemView: View) : WidgetHolder(itemView) {
        protected val readonlyIndicatorView: View =
            itemView.findViewById(R.id.readonly_indicator_view)

        init {
            val onLongClickListener: (view: View) -> Boolean = {
                if (!controllerViewModel.isDragged) {
                    showPopupMenu(it, widget)
                    true
                } else {
                    false
                }
            }
            itemView.setOnLongClickListener(onLongClickListener)
            readonlyIndicatorView.setOnLongClickListener(onLongClickListener)
        }
    }

    private inner class ButtonWidgetHolder(itemView: View) : ClickableWidgetHolder(itemView) {
        @SuppressLint("ClickableViewAccessibility")
        private val button: MaterialButton =
            itemView.findViewById<MaterialButton>(R.id.widget_button).apply {
                setOnTouchListener { button, motionEvent ->
                    if (button.tag == true) {
                        when (motionEvent.action) {
                            MotionEvent.ACTION_DOWN -> {
                                button.isPressed = true
                                val data = CommandHelper.HIGH_VALUE
                                callbacks?.onSend(CommandHelper.create(widget.tag, data))
                                widget.desiredState = data
                            }
                            MotionEvent.ACTION_UP -> {
                                button.isPressed = false
                                val data = CommandHelper.LOW_VALUE
                                callbacks?.onSend(CommandHelper.create(widget.tag, data))
                                widget.desiredState = data
                            }
                        }
                        widgetsRecyclerView.requestDisallowInterceptTouchEvent(button.isPressed)
                    }
                    true
                }
            }

        override fun bind(widget: Widget) {
            this.widget = widget
            widgetNameTextView.text = widget.name
            bindState(widget)
            readonlyIndicatorView.isVisible = widget.isReadOnly
        }

        override fun bindState(widget: Widget) {
            widget.desiredState = widget.state
            button.apply {
                if (controllerViewModel.isWidgetIconResIdValid(widget.iconResId)) {
                    icon = ContextCompat.getDrawable(requireContext(), widget.iconResId)
                }
                tag = false
                isPressed = widget.state != null && widget.state != CommandHelper.LOW_VALUE
                tag = true
            }
        }
    }

    private inner class SwitchWidgetHolder(itemView: View) : ClickableWidgetHolder(itemView) {
        private val iconImageView: ImageView = itemView.findViewById(R.id.widget_icon_image_view)
        private val switchButton: SwitchMaterial =
            itemView.findViewById<SwitchMaterial>(R.id.widget_switch).apply {
                setOnCheckedChangeListener { switchButton, isChecked ->
                    if (switchButton.tag == true) {
                        val data: String =
                            if (isChecked) CommandHelper.HIGH_VALUE else CommandHelper.LOW_VALUE
                        callbacks?.onSend(CommandHelper.create(widget.tag, data))
                        widget.desiredState = data
                    }
                }
            }

        override fun bind(widget: Widget) {
            this.widget = widget
            widgetNameTextView.text = widget.name
            bindState(widget)
            if (controllerViewModel.isWidgetIconResIdValid(widget.iconResId)) {
                iconImageView.visibility = View.VISIBLE
                iconImageView.setImageResource(widget.iconResId)
            } else {
                iconImageView.visibility = View.GONE
            }
            readonlyIndicatorView.isVisible = widget.isReadOnly
        }

        override fun bindState(widget: Widget) {
            widget.desiredState = widget.state
            switchButton.apply {
                tag = false
                isChecked = widget.state != null && widget.state != CommandHelper.LOW_VALUE
                tag = true
            }
        }
    }

    private inner class SliderWidgetHolder(itemView: View) : ClickableWidgetHolder(itemView) {
        private val iconImageView: ImageView = itemView.findViewById(R.id.widget_icon_image_view)
        private val sliderValueTextView: TextView =
            itemView.findViewById(R.id.slider_value_text_view)
        private val slider: Slider = itemView.findViewById<Slider>(R.id.widget_slider).apply {
            addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                @SuppressLint("RestrictedApi")
                override fun onStartTrackingTouch(slider: Slider) {
                }

                @SuppressLint("RestrictedApi")
                override fun onStopTrackingTouch(slider: Slider) {
                    sendData()
                }
            })
            addOnChangeListener { _, value, _ ->
                sliderValueTextView.text = value.toInt().toString()
            }
            sliderValueTextView.text = value.toInt().toString()
        }
        private val decreaseButton: MaterialButton =
            itemView.findViewById<MaterialButton>(R.id.decrease_button).apply {
                setOnClickListener {
                    val newValue = slider.value - slider.stepSize
                    if (newValue >= slider.valueFrom) {
                        slider.value = newValue
                        sendData()
                    }
                }
            }

        private val increaseButton: MaterialButton =
            itemView.findViewById<MaterialButton>(R.id.increase_button).apply {
                setOnClickListener {
                    val newValue = slider.value + slider.stepSize
                    if (newValue <= slider.valueTo) {
                        slider.value = newValue
                        sendData()
                    }
                }
            }

        override fun bind(widget: Widget) {
            this.widget = widget
            widgetNameTextView.text = widget.name
            bindState(widget)
            if (controllerViewModel.isWidgetIconResIdValid(widget.iconResId)) {
                iconImageView.visibility = View.VISIBLE
                iconImageView.setImageResource(widget.iconResId)
            } else {
                iconImageView.visibility = View.GONE
            }
            decreaseButton.isEnabled = !widget.isReadOnly
            increaseButton.isEnabled = !widget.isReadOnly
            readonlyIndicatorView.isVisible = widget.isReadOnly
        }

        override fun bindState(widget: Widget) {
            widget.desiredState = widget.state
            slider.apply {
                tag = false
                value = widget.state?.toFloatOrNull()?.let { newValue ->
                    if (newValue >= slider.valueFrom && newValue <= slider.valueTo) {
                        newValue
                    } else {
                        value
                    }
                } ?: 0.0f
                tag = true
            }
        }

        private fun sendData() {
            val data: String = slider.value.toInt().toString()
            callbacks?.onSend(CommandHelper.create(widget.tag, data))
            widget.desiredState = data
        }
    }

    private inner class TextFieldWidgetHolder(itemView: View) : ClickableWidgetHolder(itemView) {
        private val textField: TextInputLayout =
            itemView.findViewById<TextInputLayout>(R.id.widget_input_field).apply {
                setEndIconOnClickListener {
                    editText?.let {
                        val data = it.text.toString().trim()
                        if (data.isNotEmpty()) {
                            callbacks?.onSend(CommandHelper.create(widget.tag, data))
                            widget.desiredState = data
                        }
                    }
                }
            }

        override fun bind(widget: Widget) {
            this.widget = widget
            widgetNameTextView.text = widget.name
            bindState(widget)
            textField.endIconDrawable =
                if (controllerViewModel.isWidgetIconResIdValid(widget.iconResId)) {
                    ContextCompat.getDrawable(requireContext(), widget.iconResId)
                } else if (!widget.isReadOnly) {
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_baseline_send_24)
                } else {
                    null
                }
            readonlyIndicatorView.isVisible = widget.isReadOnly
        }

        override fun bindState(widget: Widget) {
            widget.desiredState = widget.state
            textField.editText?.apply {
                text = null
                widget.state?.let {
                    append(it)
                }
            }
        }
    }

    private inner class VoiceButtonWidgetHolder(itemView: View) : ClickableWidgetHolder(itemView) {
        init {
            itemView.findViewById<MaterialButton>(R.id.widget_voice_button).apply {
                setOnClickListener {
                    controllerViewModel.currentVoiceWidget = widget
                    val speechRecognizerIntent = Intent().apply {
                        action = RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                        putExtra(
                            RecognizerIntent.EXTRA_PROMPT,
                            getString(R.string.speech_to_text_hint)
                        )
                    }
                    speechRecognizerIntentResultLauncher.launch(speechRecognizerIntent)
                }
            }
        }

        override fun bind(widget: Widget) {
            this.widget = widget
            widgetNameTextView.text = widget.name
            bindState(widget)
        }

        override fun bindState(widget: Widget) {
            widget.desiredState = widget.state
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private inner class DraggedWidgetHolder(itemView: View) : WidgetHolder(itemView) {
        init {
            itemView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    itemTouchHelper.startDrag(this)
                }
                false
            }
        }

        override fun bind(widget: Widget) {
            this.widget = widget
            widgetNameTextView.text = widget.name
            bindState(widget)
        }

        override fun bindState(widget: Widget) {
        }
    }

    private inner class WidgetsAdapter(private val diffCallback: DiffCallback) :
        ListAdapter<Widget, WidgetHolder>(diffCallback) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WidgetHolder =
            when (viewType) {
                Widget.Type.BUTTON.ordinal -> ButtonWidgetHolder(
                    layoutInflater.inflate(
                        R.layout.list_item_widget_button,
                        parent,
                        false
                    )
                )
                Widget.Type.SWITCH.ordinal -> SwitchWidgetHolder(
                    layoutInflater.inflate(
                        R.layout.list_item_widget_switch,
                        parent,
                        false
                    )
                )
                Widget.Type.SLIDER.ordinal -> SliderWidgetHolder(
                    layoutInflater.inflate(
                        R.layout.list_item_widget_slider,
                        parent,
                        false
                    )
                )
                Widget.Type.TEXT_FIELD.ordinal -> TextFieldWidgetHolder(
                    layoutInflater.inflate(
                        R.layout.list_item_widget_text_field,
                        parent,
                        false
                    )
                )
                Widget.Type.VOICE_BUTTON.ordinal -> VoiceButtonWidgetHolder(
                    layoutInflater.inflate(
                        R.layout.list_item_widget_voice_button,
                        parent,
                        false
                    )
                )
                else -> DraggedWidgetHolder(
                    layoutInflater.inflate(
                        R.layout.list_item_widget_drag,
                        parent,
                        false
                    )
                )
            }

        override fun onBindViewHolder(holder: WidgetHolder, position: Int) {
            holder.bind(getItem(position))
        }

        override fun onBindViewHolder(
            holder: WidgetHolder,
            position: Int,
            payloads: MutableList<Any>
        ) {
            if (payloads.isNotEmpty() && payloads[0] == true) {
                holder.bindState(getItem(position))
            } else {
                super.onBindViewHolder(holder, position, payloads)
            }
        }

        override fun getItemViewType(position: Int): Int =
            if (controllerViewModel.isDragged) {
                DRAGGED_TYPE
            } else {
                getItem(position).type.ordinal
            }

        override fun onCurrentListChanged(
            previousList: MutableList<Widget>,
            currentList: MutableList<Widget>
        ) {
            super.onCurrentListChanged(previousList, currentList)
            diffCallback.isForceUpdate = false
        }

        fun submitSortedList(list: List<Widget>?) {
            submitList(list?.sortedBy { it.order })
        }

        fun forceUpdateSortedList() {
            diffCallback.isForceUpdate = true
            submitSortedList(currentList)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Widget>() {
        var isForceUpdate: Boolean = false

        override fun areItemsTheSame(
            oldItem: Widget,
            newItem: Widget
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Widget,
            newItem: Widget
        ): Boolean = oldItem == newItem && oldItem.desiredState == newItem.state && !isForceUpdate

        override fun getChangePayload(oldItem: Widget, newItem: Widget): Any? =
            if (oldItem.desiredState != newItem.state) true else null
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
            val fromWidget =
                widgetsAdapter.currentList[viewHolder.adapterPosition]
            val toWidget = widgetsAdapter.currentList[target.adapterPosition]
            val fromOrder = fromWidget.order
            val toOrder = toWidget.order
            controllerViewModel.tempList = widgetsAdapter.currentList.map { widget ->
                widget.apply {
                    if (widget == fromWidget) {
                        order = toOrder
                    } else if (widget == toWidget) {
                        order = fromOrder
                    }
                }
            }
            updateWidgetsRecyclerView(controllerViewModel.tempList)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }

        override fun isLongPressDragEnabled(): Boolean = controllerViewModel.isDragged
    }

    companion object {
        private const val SPAN_COUNT = 2

        private const val DRAGGED_TYPE = -1
    }
}
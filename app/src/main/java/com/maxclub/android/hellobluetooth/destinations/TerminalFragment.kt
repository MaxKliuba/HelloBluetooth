package com.maxclub.android.hellobluetooth.destinations

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.*
import com.google.android.material.textfield.TextInputLayout
import com.maxclub.android.hellobluetooth.R
import com.maxclub.android.hellobluetooth.bluetooth.IBluetoothDataCallbacks
import com.maxclub.android.hellobluetooth.model.Command
import com.maxclub.android.hellobluetooth.viewmodel.TerminalViewModel
import java.text.SimpleDateFormat
import java.util.*

class TerminalFragment : Fragment() {
    private var callbacks: IBluetoothDataCallbacks? = null

    private val terminalViewModel by lazy {
        ViewModelProvider(this)[TerminalViewModel::class.java]
    }

    private lateinit var commandsRecyclerView: RecyclerView
    private lateinit var commandsAdapter: CommandsAdapter
    private lateinit var commandTextField: TextInputLayout

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
        val view = inflater.inflate(R.layout.fragment_terminal, container, false)

        commandsRecyclerView = view.findViewById<RecyclerView>(R.id.commands_recycler_view).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
            adapter = CommandsAdapter().apply {
                commandsAdapter = this
            }
        }

        commandTextField = view.findViewById<TextInputLayout>(R.id.command_input_field).apply {
            setEndIconOnClickListener {
                editText?.let {
                    val text = it.text.toString().trim()
                    if (text.isNotEmpty()) {
                        callbacks?.onSend(text)
                        it.text = null
                    }
                }
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        callbacks?.onCommandListener()?.observe(viewLifecycleOwner) {
            commandsAdapter.submitList(
                terminalViewModel.getCommands().sortedByDescending { it.time }
            )
            activity?.invalidateOptionsMenu()
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_tetminal, menu)
        menu.findItem(R.id.clear).isVisible = terminalViewModel.getCommands().isNotEmpty()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.clear -> {
                terminalViewModel.clearCommands()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    private abstract inner class CommandHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        protected lateinit var command: Command

        protected val commandTextView: TextView = itemView.findViewById(R.id.command_text_view)
        protected val timeTextView: TextView = itemView.findViewById(R.id.time_text_view)

        init {
            itemView.setOnLongClickListener {
                commandTextField.editText?.append(command.text)
                true
            }
        }

        open fun bind(command: Command) {
            this.command = command
            commandTextView.text = command.text
            timeTextView.text = command.time.run {
                val pattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "hh:mm a"
                val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
                simpleDateFormat.format(this)
            }
        }
    }

    private inner class InputCommandHolder(itemView: View) : CommandHolder(itemView)

    private inner class OutputCommandHolder(itemView: View) : CommandHolder(itemView) {
        private val errorImageView: ImageView = itemView.findViewById(R.id.error_image_view)

        override fun bind(command: Command) {
            super.bind(command)
            errorImageView.visibility = if (command.isSuccess) View.GONE else View.VISIBLE
        }
    }

    private inner class CommandsAdapter : ListAdapter<Command, CommandHolder>(DiffCallback()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommandHolder =
            when (viewType) {
                Command.INPUT_COMMAND -> InputCommandHolder(
                    layoutInflater.inflate(
                        R.layout.list_item_input_command,
                        parent,
                        false
                    )
                )
                else -> OutputCommandHolder(
                    layoutInflater.inflate(
                        R.layout.list_item_output_command,
                        parent,
                        false
                    )
                )
            }

        override fun onBindViewHolder(holder: CommandHolder, position: Int) {
            holder.bind(getItem(position))
        }

        override fun getItemViewType(position: Int): Int = getItem(position).type

        override fun onCurrentListChanged(
            previousList: MutableList<Command>,
            currentList: MutableList<Command>
        ) {
            super.onCurrentListChanged(previousList, currentList)
            commandsRecyclerView.smoothScrollToPosition(0)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Command>() {
        override fun areItemsTheSame(
            oldItem: Command,
            newItem: Command
        ): Boolean = oldItem == newItem

        override fun areContentsTheSame(
            oldItem: Command,
            newItem: Command
        ): Boolean = oldItem == newItem
    }
}
package com.maxclub.android.hellobluetooth.destinations

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.google.android.material.textfield.TextInputLayout
import com.maxclub.android.hellobluetooth.R
import com.maxclub.android.hellobluetooth.data.Command
import java.text.SimpleDateFormat
import java.util.*

class TerminalFragment : Fragment() {
    interface Callbacks {
        fun onSend(data: String)

        fun getCommands(): LiveData<List<Command>>
    }

    private var callbacks: Callbacks? = null

    private lateinit var commandsRecyclerView: RecyclerView
    private lateinit var commandsAdapter: CommandsAdapter
    private lateinit var commandTextField: TextInputLayout

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_terminal, container, false)

        commandsRecyclerView = view.findViewById<RecyclerView>(R.id.commandsRecyclerView).apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
            adapter = CommandsAdapter().apply {
                commandsAdapter = this
            }
        }

        commandTextField = view.findViewById<TextInputLayout>(R.id.commandInputField).apply {
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
        callbacks?.getCommands()?.observe(viewLifecycleOwner) { commands ->
            commandsRecyclerView.smoothScrollToPosition(0)
            commandsAdapter.submitList(commands)
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private abstract inner class CommandHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        protected lateinit var command: Command

        private val commandTextView: TextView = itemView.findViewById(R.id.commandTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)

        init {
            itemView.setOnLongClickListener {
                commandTextField.editText?.append(command.text)
                true
            }
        }

        fun bind(command: Command) {
            this.command = command
            commandTextView.text = command.text
            timeTextView.text = command.time.run {
                val pattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "hh:mm a"
                val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
                simpleDateFormat.format(this)
            }
        }
    }

    private inner class InputCommandHolder(itemView: View) : CommandHolder(itemView) {
        constructor(parent: ViewGroup) : this(
            layoutInflater.inflate(
                R.layout.list_item_input_command,
                parent,
                false
            )
        )
    }

    private inner class OutputCommandHolder(itemView: View) : CommandHolder(itemView) {
        constructor(parent: ViewGroup) : this(
            layoutInflater.inflate(
                R.layout.list_item_output_command,
                parent,
                false
            )
        )
    }

    private inner class CommandsAdapter : RecyclerView.Adapter<CommandHolder>() {
        private val commands: SortedList<Command> = SortedList(
            Command::class.java,
            object : SortedList.Callback<Command>() {
                override fun compare(item1: Command, item2: Command): Int =
                    item2.time.compareTo(item1.time)

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

                override fun areContentsTheSame(oldItem: Command, newItem: Command): Boolean =
                    oldItem == newItem

                override fun areItemsTheSame(item1: Command, item2: Command): Boolean =
                    item1 == item2
            })

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommandHolder =
            when (viewType) {
                Command.INPUT_COMMAND -> InputCommandHolder(parent)
                else -> OutputCommandHolder(parent)
            }

        override fun onBindViewHolder(holder: CommandHolder, position: Int) {
            holder.bind(commands[position])
        }

        override fun getItemCount(): Int = commands.size()

        override fun getItemViewType(position: Int): Int = commands[position].type

        fun submitList(items: List<Command>) {
            commands.replaceAll(items)
        }
    }
}
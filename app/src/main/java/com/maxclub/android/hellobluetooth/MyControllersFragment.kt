package com.maxclub.android.hellobluetooth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.maxclub.android.hellobluetooth.model.Controller

class MyControllersFragment : Fragment() {
    private lateinit var controllersRecyclerView: RecyclerView
    private lateinit var controllersAdapter: ControllersAdapter
    private lateinit var addControllerFloatingActionButton: FloatingActionButton

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_controllers, container, false)
        navController = findNavController()

        controllersRecyclerView =
            view.findViewById<RecyclerView?>(R.id.controllersRecyclerView).apply {
                layoutManager = LinearLayoutManager(context)
                adapter = ControllersAdapter().apply {
                    controllersAdapter = this
                }
            }

        addControllerFloatingActionButton =
            view.findViewById<FloatingActionButton?>(R.id.addControllerFloatingActionButton).apply {
                setOnClickListener {
                    val controllers: List<Controller> = listOf(
                        Controller(0, "Controller 0", 0),
                        Controller(1, "Controller 1", 1),
                        Controller(2, "Controller 2", 2),
                        Controller(3, "Controller 3", 3),
                    )
                    controllersAdapter.submitList(controllers)
                }
            }

        return view
    }

    private inner class ControllerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var controller: Controller

        private val controllerNameTextView: TextView =
            itemView.findViewById(R.id.controllerNameTextView)
        private val widgetsAmountTextView: TextView =
            itemView.findViewById(R.id.widgetsAmountTextView)

        init {
            itemView.setOnClickListener {
                val direction =
                    MyControllersFragmentDirections.actionMyControllersFragmentToControllerFragment(
                        controller.id
                    )
                navController.navigate(direction)
            }
        }

        fun bind(controller: Controller) {
            this.controller = controller
            controllerNameTextView.text = controller.name
            widgetsAmountTextView.text = resources.getQuantityString(R.plurals.widget_plural, 0, 0)
        }
    }

    private inner class ControllersAdapter : RecyclerView.Adapter<ControllerHolder>() {
        private val controllers: SortedList<Controller> = SortedList(
            Controller::class.java,
            object : SortedList.Callback<Controller>() {
                override fun compare(item1: Controller, item2: Controller): Int =
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

                override fun areContentsTheSame(oldItem: Controller, newItem: Controller): Boolean =
                    oldItem == newItem

                override fun areItemsTheSame(item1: Controller, item2: Controller): Boolean =
                    item1.id == item2.id
            })

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ControllerHolder =
            ControllerHolder(layoutInflater.inflate(R.layout.list_item_controller, parent, false))

        override fun onBindViewHolder(holder: ControllerHolder, position: Int) {
            holder.bind(controllers[position])
        }

        override fun getItemCount(): Int = controllers.size()

        fun submitList(items: List<Controller>) {
            controllers.replaceAll(items)
        }
    }
}
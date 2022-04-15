package com.maxclub.android.hellobluetooth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController

class MyControllersFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_controllers, container, false)

//        val navController = findNavController()
//        view.setOnClickListener {
//            val action =
//                MyControllersFragment.actionConnectionFragmentToMyControllersFragment()
//            navController.navigate(action)
//        }

        return view
    }
}
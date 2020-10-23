package com.zhenl.launcher.preference

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.android.launcher3.R

/**
 * Created by lin on 2020/10/23.
 */
abstract class RecyclerViewFragment : Fragment() {

    open val layoutId = R.layout.preference_insettable_recyclerview

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutInflater.from(container!!.context).inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onRecyclerViewCreated(view.findViewById(R.id.list))
    }

    abstract fun onRecyclerViewCreated(recyclerView: RecyclerView)
}
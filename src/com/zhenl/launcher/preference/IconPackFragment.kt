package com.zhenl.launcher.preference

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhenl.launcher.LauncherPreferences

/**
 * Created by lin on 2020/10/23.
 */
class IconPackFragment : RecyclerViewFragment() {

    private val adapter by lazy { IconPackAdapter(requireContext()) }

    override fun onRecyclerViewCreated(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        (recyclerView.itemAnimator as? DefaultItemAnimator)?.supportsChangeAnimations = false
        adapter.itemTouchHelper = ItemTouchHelper(adapter.TouchHelperCallback()).apply {
            attachToRecyclerView(recyclerView)
        }
    }

    override fun onPause() {
        super.onPause()

        LauncherPreferences.getInstance(requireContext()).iconPacks.setAll(adapter.saveSpecs())
    }
}
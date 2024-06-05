package com.example.taichungtourism

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.taichungtourism.databinding.FragmentCollectionBinding

/**
 * A fragment representing a list of Items.
 */
class CollectionFragment : Fragment() {
    private lateinit var binding: FragmentCollectionBinding

    private val collectionList = ArrayList<Attraction>()
    private val listAdapter: CollectionAdapter by lazy { CollectionAdapter(collectionList) }

    private lateinit var fileHelper: FileHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = android.transition.TransitionInflater.from(context).inflateTransition(android.R.transition.fade)
        fileHelper = FileHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCollectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRvList()
    }

    private fun setRvList() = binding.apply {
        rvList.adapter = listAdapter
        collectionList.addAll(fileHelper.readAll())
        listAdapter.notifyDataSetChanged()
    }

}
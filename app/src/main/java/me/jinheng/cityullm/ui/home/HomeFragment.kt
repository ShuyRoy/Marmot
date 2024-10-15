package me.jinheng.cityullm.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.jinheng.cityullm.ChatRecordsAdapter
import me.jinheng.cityullm.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        // Set up the RecyclerView for chat records
        val chatRecordsRecyclerView: RecyclerView = binding.chatRecordsRecyclerView
        chatRecordsRecyclerView.layoutManager = LinearLayoutManager(context)
        val chatRecordsAdapter = ChatRecordsAdapter()
        chatRecordsRecyclerView.adapter = chatRecordsAdapter

        homeViewModel.chatRecords.observe(viewLifecycleOwner) { records ->
            chatRecordsAdapter.submitList(records)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
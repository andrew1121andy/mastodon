package io.keiji.sample.mastodonclient

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.keiji.sample.mastodonclient.databinding.FragmentTootListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TootListFragment : Fragment(R.layout.fragment_toot_list) {

    private var binding: FragmentTootListBinding? = null

    private lateinit var adapter: TootListAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private val viewModel: TootListViewModel by ViewModels {
        TootViewModelFactory(
            API_BASE_URL,
            lifecycleScope,
            requireContext()
        )
    }

    private val loadNextScrollListener = object : RecyclerView.OnScrollListener()
    {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx,dy)

                val isLoadingSnapshot = viewModel.isLoading.value ?: return
                 if (isLoadingSnapshot || !viewModel.hasNext){
                return
            }

            val visibleItemCount = recyclerView.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.
            findFirstVisibleItemPosition()

            if ((totalItemCount - visibleItemCount) <= firstVisibleItemPosition) {
                viewModel.loadNext()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tootListSnapshot = viewModel.tootList.value ?: ArrayList<Toot>().also {
            viewModel.tootList.value = it
        }


        adapter = TootListAdapter(layoutInflater, tootListSnapshot)
        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        val bindingData: FragmentTootListBinding? = DataBindingUtil.bind(view)
        binding = bindingData ?: return

        bindingData.recyclerView.also {
            it.layoutManager = layoutManager
            it.adapter = adapter
            it.addOnScrollListener(loadNextScrollListener)
        }
        bindingData.swipeRefreshLayout.setOnRefreshListener {
            viewModel.clear()
            viewModel.loadNext()
        }

        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            binding?.swipeRefreshLayout?.isRefreshing = it
        })
        viewModel.tootList.observe(viewLifecycleOwner, Observer {
            adapter.notifyDataSetChanged()
        })

        viewModel.loadNext()
    }
    private fun loadNext() {
        lifecycleScope.launch {

            isLoading.postValue(true)

            val tootListSnapshot = tootList.value ?: ArrayList()

            val tootListResponse = tootRepository.fetchPublicTimeline(
                maxId = tootListSnapshot.lastOrNull()?.id,
                onlyMedia = true
            )
            Log.d(TAG, "fetchPublicTimeline")



            tootListSnapshot.addAll(tootListResponse.filter { !it.sensitive })
            Log.d(TAG, "addAll")

            tootList.postValue(tootListSnapshot)


            hasNext.set(tootListResponse.isNotEmpty())
            isLoading.postValue(false)
            Log.d(TAG, "dismissProgress")
        }
    }


}
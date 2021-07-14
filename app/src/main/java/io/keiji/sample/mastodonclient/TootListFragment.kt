package io.keiji.sample.mastodonclient

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.keiji.sample.mastodonclient.databinding.FragmentTootListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class TootListFragment : Fragment(R.layout.fragment_toot_list) {

    companion object {
        val TAG = TootListFragment::class.java.simpleName

        private const val API_BASE_URL = "https://androidbook2020.keiji.io"
    }

    private var binding: FragmentTootListBinding? = null

    private val tootRepository = TootRepository(API_BASE_URL)




    private lateinit var adapter: TootListAdapter
    private lateinit var layoutManager: LinearLayoutManager


    private var isLoading = MutableLiveData<Boolean>()
    private var hasNext = AtomicBoolean().apply { set(true) }

    private var loadNextScrollListener = object : RecyclerView.
    OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx,dy)
                val isLoadingSnapshot = isLoading.value ?: return
                if (isLoadingSnapshot || !hasNext.get()) {
                return
            }

            val visibleItemCount = recyclerView.childCount
            val totalItemCount = layoutManager.itemCount
            val firstVisibleItemPosition = layoutManager.
            findFirstVisibleItemPosition()

            if ((totalItemCount - visibleItemCount) <=
            firstVisibleItemPosition) {
                loadNext()
            }
        }
    }

    private val tootList = MutableLiveData<ArrayList<Toot>>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        val tootListSnapshot = tootList.value ?: ArrayList<Toot>().also {
            tootList.value = it
        }

        adapter = TootListAdapter(layoutInflater,tootListSnapshot)
        layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false)
        val bindingData: FragmentTootListBinding? = DataBindingUtil.bind(view)

        binding = bindingData ?: return

        bindingData.recyclerView.also {
            it.layoutManager = layoutManager
            it.adapter = adapter
            it.addOnScrollListener(loadNextScrollListener)
        }
        bindingData.swipeRefreshLayout.setOnRefreshListener {
            tootListSnapshot.clear()
            loadNext()
        }

        isLoading.observe(viewLifecycleOwner, Observer {
            binding?.swipeRefreshLayout?.isRefreshing = it
        })
        tootList.observe(viewLifecycleOwner, Observer {
            adapter.notifyDataSetChanged()
        })

        loadNext()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        binding?.unbind()
    }



    private fun loadNext() {
        lifecycleScope.launch {

            isLoading.postValue(true)

            val tootListSnapshot = tootList.value ?: ArrayList()

            val tootListResponse = tootRepository.fetchPublicTimeline(
                maxid = tootListSnapshot.lastOrNull()?.id,
                onlyMedia = true
            )
            Log.d(TAG, "fetchPublicTimeline")

            Thread.sleep(10 * 10000)

            tootListSnapshot.addAll(tootListResponse.filter { !it.sensitive })
            Log.d(TAG, "addAll")

            tootList.postValue(tootListSnapshot)


            hasNext.set(tootListResponse.isNotEmpty())
            isLoading.postValue(false)
            Log.d(TAG, "dismissProgress")
        }
    }


}
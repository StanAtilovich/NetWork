package ru.netology.network.ui.activity.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.network.R
import ru.netology.network.databinding.FragmentNewJobBinding
import ru.netology.network.databinding.JobsFeedBinding
import ru.netology.network.ui.activity.adapter.JobAdapter
import ru.netology.network.ui.activity.adapter.OnInteractionJobListener
import ru.netology.network.ui.activity.dto.Job
import ru.netology.network.ui.activity.util.LongArg
import ru.netology.network.ui.activity.viewmodel.AuthViewModel
import ru.netology.network.ui.activity.viewmodel.PostViewModel

class FeedJobFragment : Fragment() {
    companion object {
        var Bundle.user_Id: Long by LongArg
    }

    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = JobsFeedBinding.inflate(inflater, container, false)
        val userId = (arguments?.user_Id ?: 0).toLong()
        val currentUser = viewModel.getCurrentUser()
        viewModel.loadJobs(userId, currentUser)
        if (currentUser == userId)
            binding.fab.visibility =View.VISIBLE
        else
            binding.fab.visibility = View.GONE

        val adapter = JobAdapter(object : OnInteractionJobListener{
            override fun onEdit(job: Job) {
                viewModel.editJob(job)
                findNavController().navigate(R.id.action_feedJobsFragment_to_newJobFragment)
            }

            override fun onRemove(job: Job) {
                viewModel.removeJobById(job.id)
            }
        })
        binding.list.adapter = adapter

        viewModel.dataState.observe(viewLifecycleOwner){
            state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.refreshing
            if (state.error){
                Snackbar.make(binding.root, R.string.error_load, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry){viewModel.loadEvents()}
                    .show()
            }
        }
        viewModel.dataJobs.observe(viewLifecycleOwner){
            state ->
            adapter.submitList(state.jobs.filter {
                it.userId == userId
            })
            binding.emptyText.isVisible = state.empty
        }
        binding.swiperefresh.setOnRefreshListener {
            viewModel.refreshJobs(userId,currentUser)
        }
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedJobsFragment_to_newJobFragment)
        }
        return binding.root
    }
}
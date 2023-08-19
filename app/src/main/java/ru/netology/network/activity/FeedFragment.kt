package ru.netology.network

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.network.ImagePreviewFragment.Companion.textArg
import ru.netology.network.MapsPreviewFragment.Companion.doubleArg1
import ru.netology.network.MapsPreviewFragment.Companion.doubleArg2
import ru.netology.network.WallFragment.Companion.userAvatar
import ru.netology.network.WallFragment.Companion.userId
import ru.netology.network.WallFragment.Companion.userName
import ru.netology.network.WallFragment.Companion.userPosition
import ru.netology.network.databinding.FragmentPostFeedBinding
import ru.netology.network.dto.Post
import ru.netology.network.viewmodel.PostViewModel


@AndroidEntryPoint
class FeedFragment : Fragment() {
    private val viewModel: PostViewModel by viewModels(
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostFeedBinding.inflate(inflater, container, false)
        val adapter = PostAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.editPosts(post)
                findNavController().navigate(R.id.action_postFeedFragment_to_newPostFragment)
            }

            override fun onLike(post: Post) {
                viewModel.likePostById(post.id)
            }

            override fun onRemove(post: Post) {
                viewModel.removePostById(post.id)
            }

            override fun onPreviewMap(post: Post) {
                if (post.coords != null && post.coords.lat != null && post.coords.long != null) {
                    findNavController().navigate(R.id.action_postFeedFragment_to_mapsPreviewFragment,
                        Bundle().apply {
                            doubleArg1 = post.coords.lat.toDouble()
                            doubleArg2 = post.coords.lat.toDouble()
                        })
                }
            }

            override fun onGo2Wall(
                userId: Long,
                userName: String,
                userPosition: String?,
                userAvatar: String?
            ) {
                findNavController().navigate(R.id.action_postFeedFragment_to_wallFragment,
                    Bundle().apply
                    {
                        this.userId = userId
                        this.userName = userName
                        this.userPosition = userPosition
                        this.userAvatar = userAvatar
                    })
            }

            override fun onPreviewImage(post: Post) {
                findNavController().navigate(R.id.action_postFeedFragment_to_imagePreviewFragment,
                    Bundle().apply
                    { textArg = post.attachment?.url })
            }
        })
        binding.list.adapter = adapter

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swipeRefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_load, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) {
                        viewModel.loadPosts()
                    }
                    .show()
            }
        }
        viewModel.dataPosts.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.emptyText.isVisible = state.empty
        }
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshPosts()
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_postFeedFragment_to_newPostFragment)
        }
        return binding.root
    }
}
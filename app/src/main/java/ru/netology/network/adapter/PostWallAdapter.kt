package ru.netology.network.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import android.widget.MediaController
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.network.R
import ru.netology.network.databinding.CardWallPostBinding
import ru.netology.network.dto.Post
import ru.netology.network.enumeration.AttachmentType
import ru.netology.network.view.load


interface OnInteractionWallListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onPreviewImage(post: Post) {}
    fun onPreviewMap(post: Post) {}
}

class PostWallAdapter(
    private val onInteractionWallListener: OnInteractionWallListener
) : ListAdapter<Post, PostWallViewHolder>(PostWallDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostWallViewHolder {
        val binding =
            CardWallPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostWallViewHolder(binding, onInteractionWallListener)
    }

    override fun onBindViewHolder(holder: PostWallViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostWallDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}


class PostWallViewHolder(
    private val binding: CardWallPostBinding,
    private val onInteractionWallListener: OnInteractionWallListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            content.text = post.content

            if (post.link != null) content.text = "${content.text} \n${post.link}"

            buttonLike.isChecked = post.likedByMe

            buttonMap.isVisible = post.coords != null

            if (post.mentionIds?.isEmpty() == true) {
                mentions.visibility = View.GONE
                mentionsInfo.visibility = View.GONE
            } else {
                mentions.visibility = View.VISIBLE
                mentionsInfo.visibility = View.VISIBLE
                mentions.text = post.mentionList?.joinToString(", ", "", "", 10, "...", null)
            }

            when (post.attachment?.type) {
                AttachmentType.IMAGE -> {
                    AttachmentFrame.visibility = View.VISIBLE
                    AttachmentImage.visibility = View.VISIBLE
                    AttachmentVideo.visibility = View.GONE
                    AttachmentImage.load(post.attachment.url)
                }

                AttachmentType.VIDEO -> {
                    AttachmentFrame.visibility = View.VISIBLE
                    AttachmentImage.visibility = View.GONE
                    AttachmentVideo.apply {
                        visibility = View.VISIBLE
                        setMediaController(MediaController(binding.root.context))
                        setVideoURI(Uri.parse(post.attachment.url))
                        setOnPreparedListener {
                            animate().alpha(1F)
                            seekTo(0)
                            setZOrderOnTop(false)
                        }
                        setOnCompletionListener {
                            stopPlayback()
                        }
                    }

                }

                AttachmentType.AUDIO -> {
                    AttachmentFrame.visibility = View.VISIBLE
                    AttachmentImage.visibility = View.GONE
                    AttachmentVideo.apply {
                        visibility = View.VISIBLE
                        setMediaController(MediaController(binding.root.context))
                        setVideoURI(Uri.parse(post.attachment.url))
                        setBackgroundResource(R.drawable.audio)
                        setOnPreparedListener {
                            setZOrderOnTop(true)
                        }
                        setOnCompletionListener {
                            stopPlayback()
                        }
                    }
                }

                null -> {
                    AttachmentFrame.visibility = View.GONE
                }
            }

            menu.visibility = if (post.ownedByMe) View.VISIBLE else View.INVISIBLE

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    menu.setGroupVisible(R.id.owned, post.ownedByMe)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionWallListener.onRemove(post)
                                true
                            }

                            R.id.edit_content -> {
                                onInteractionWallListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            buttonLike.setOnClickListener {
                onInteractionWallListener.onLike(post)
            }
            buttonMap.setOnClickListener {
                onInteractionWallListener.onPreviewMap(post)
            }
        }
    }
}

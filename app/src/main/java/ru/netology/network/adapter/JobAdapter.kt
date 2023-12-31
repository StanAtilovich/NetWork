package ru.netology.network.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.network.R
import ru.netology.network.databinding.CardJobBinding
import ru.netology.network.dto.Job
import ru.netology.network.util.convertString2DateTime2String


interface OnInteractionJobListener {
    fun onEdit(job: Job) {}
    fun onRemove(job: Job) {}
}

class JobAdapter(
    private val onInteractionJobListener: OnInteractionJobListener
) : ListAdapter<Job, JobViewHolder>(JobDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = CardJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onInteractionJobListener)
    }



    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = getItem(position)
        holder.bind(job)
    }
}


class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }
}

class JobViewHolder(
    private val binding: CardJobBinding,
    private val onInteractionJobListener: OnInteractionJobListener
) : RecyclerView.ViewHolder(binding.root) {


    fun bind(job: Job) {
        binding.apply {
            startFinish.text = convertString2DateTime2String(job.start) + " - " +
                    if (job.finish == null) "..."
                    else convertString2DateTime2String(job.finish)
            name.text = job.name
            position.text = job.position
            if (job.link == null)
                link.visibility = View.GONE
            else {
                link.text = job.link
                link.visibility = View.VISIBLE
            }
            menuJob.visibility = if (job.ownedByMe) View.VISIBLE else View.INVISIBLE
            menuJob.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionJobListener.onRemove(job)
                                true
                            }

                            R.id.edit_content -> {
                                onInteractionJobListener.onEdit(job)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

        }
    }
}
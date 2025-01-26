package com.example.cricketcounter.ui.fragments.playersFragment.viewModel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cricketcounter.data.models.Player
import com.example.cricketcounter.databinding.ItemPlayersBinding

class PlayersAdapter(private val listener: PlayerClickListener) :
    ListAdapter<Player, PlayersAdapter.PlayerViewHolder>(PlayerDiffCallback()) {

    interface PlayerClickListener {
        fun onEditClick(player: Player)
        fun onDeleteClick(player: Player)
    }

    class PlayerDiffCallback : DiffUtil.ItemCallback<Player>() {
        override fun areItemsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val binding = ItemPlayersBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlayerViewHolder(private val binding: ItemPlayersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                ivEdit.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onEditClick(getItem(position))
                    }
                }

                ivDelete.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(getItem(position))
                    }
                }
            }
        }

        fun bind(player: Player) {
            binding.tvPlayerName.text = player.name
        }
    }
}
package com.example.cricketcounter.ui.fragments.teamsFragment.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.cricketcounter.databinding.ItemTeamBinding
import com.example.cricketcounter.data.models.Team
import androidx.recyclerview.widget.DiffUtil

class TeamsAdapter(private val listener: TeamClickListener) :
    ListAdapter<Team, TeamsAdapter.TeamViewHolder>(TeamDiffCallback()) {

    interface TeamClickListener {
        fun onEditClick(team: Team)
        fun onDeleteClick(team: Team)
        fun onItemClick(team: Team)
    }

    class TeamDiffCallback : DiffUtil.ItemCallback<Team>() {
        override fun areItemsTheSame(oldItem: Team, newItem: Team): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Team, newItem: Team): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val binding = ItemTeamBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TeamViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TeamViewHolder(private val binding: ItemTeamBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(getItem(position))
                    }
                }

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

        @SuppressLint("SetTextI18n")
        fun bind(team: Team) {
            binding.apply {
                tvTeamName.text = team.name
                tvMatches.text = "Matches: ${team.matches}"
                tvWon.text = "Won: ${team.won}"
                tvLost.text = "Lost: ${team.lost}"
            }
        }
    }
}
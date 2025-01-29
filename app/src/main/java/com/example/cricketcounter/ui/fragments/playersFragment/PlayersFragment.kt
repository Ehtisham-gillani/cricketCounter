package com.example.cricketcounter.ui.fragments.playersFragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cricketcounter.R
import com.example.cricketcounter.data.models.Player
import com.example.cricketcounter.databinding.AddPlayerDialogBinding
import com.example.cricketcounter.databinding.FragmentPlayersBinding
import com.example.cricketcounter.ui.fragments.playersFragment.adapter.PlayersAdapter
import com.example.cricketcounter.ui.fragments.playersFragment.viewModel.PlayersViewModel
import com.example.cricketcounter.ui.fragments.playersFragment.viewModel.PlayersViewModelFactory

class PlayersFragment : Fragment(), PlayersAdapter.PlayerClickListener {
    private lateinit var binding: FragmentPlayersBinding
    private lateinit var playersAdapter: PlayersAdapter
    private var currentTeamId: Int? = null
    private val viewModel: PlayersViewModel by viewModels {
        PlayersViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentTeamId = arguments?.getInt("teamId")
        currentTeamId?.let { teamId ->
            viewModel.setTeamId(teamId)
        }
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        playersAdapter = PlayersAdapter(this)
        binding.rvPlayers.apply {
            adapter = playersAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners() {
        binding.fabAddPlayer.setOnClickListener {
            showCreatePlayerDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.players.observe(viewLifecycleOwner) { players ->
            playersAdapter.submitList(players)
        }
    }

    private fun showCreatePlayerDialog() {
        val dialogBinding = AddPlayerDialogBinding.inflate(LayoutInflater.from(requireContext()))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.apply {
            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            okButton.setOnClickListener {
                val playerName = playerNameInput.text?.toString()?.trim() ?: ""

                if (playerName.isEmpty()) {
                    playerNameInputLayout.error = "Please enter player name"
                    return@setOnClickListener
                }

                currentTeamId?.let { teamId ->
                    viewModel.addPlayer(playerName, teamId)
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showEditPlayerDialog(player: Player) {
        val dialogBinding = AddPlayerDialogBinding.inflate(LayoutInflater.from(requireContext()))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.apply {
            playerNameInput.setText(player.name)
            titleText.text = "Edit Player"

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            okButton.setOnClickListener {
                val newPlayerName = playerNameInput.text?.toString()?.trim() ?: ""

                if (newPlayerName.isEmpty()) {
                    playerNameInputLayout.error = "Please enter player name"
                    return@setOnClickListener
                }

                viewModel.updatePlayer(player, newPlayerName)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    override fun onPlayerClick(player: Player) {
        findNavController().navigate(
            R.id.action_playersFragment_to_playerProfileFragment,
            bundleOf("playerId" to player.id)
        )
    }

    override fun onEditClick(player: Player) {
        showEditPlayerDialog(player)
    }

    override fun onDeleteClick(player: Player) {
        viewModel.deletePlayer(player)
    }
}
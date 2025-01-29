package com.example.cricketcounter.ui.fragments.teamsFragment

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cricketcounter.R
import com.example.cricketcounter.databinding.AddTeamDialogBinding
import com.example.cricketcounter.databinding.FragmentTeamsBinding
import com.example.cricketcounter.data.models.Team
import com.example.cricketcounter.ui.fragments.teamsFragment.adapter.TeamsAdapter
import com.example.cricketcounter.ui.fragments.teamsFragment.viewModel.TeamsViewModel
import com.example.cricketcounter.ui.fragments.teamsFragment.viewModel.TeamsViewModelFactory
import kotlinx.coroutines.launch

class TeamsFragment : Fragment(), TeamsAdapter.TeamClickListener {
    private lateinit var binding: FragmentTeamsBinding
    private lateinit var teamsAdapter: TeamsAdapter
    private val viewModel: TeamsViewModel by viewModels {
        TeamsViewModelFactory(requireActivity().application)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTeamsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setUpRecyclerView() {
        teamsAdapter = TeamsAdapter(this)
        binding.rvTeams.apply {
            adapter = teamsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            rvTeams.setOnClickListener {
                findNavController().navigate(R.id.action_teamsFragment_to_playersFragment)
            }

            fabAddTeam.setOnClickListener {
                showCreateTeamDialog()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.teams.collect { teams ->
                    teamsAdapter.submitList(teams)
                }
            }
        }
    }

    private fun showCreateTeamDialog() {
        val dialogBinding = AddTeamDialogBinding.inflate(LayoutInflater.from(requireContext()))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.apply {
            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            okButton.setOnClickListener {
                val teamName = teamNameInput.text?.toString()?.trim() ?: ""

                if (teamName.isEmpty()) {
                    teamNameInputLayout.error = "Please enter team name"
                    return@setOnClickListener
                }

                viewModel.addTeam(teamName)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    override fun onDeleteClick(team: Team) {
        viewModel.deleteTeam(team)
    }

    @SuppressLint("SetTextI18n")
    private fun showEditTeamDialog(team: Team) {
        val dialogBinding = AddTeamDialogBinding.inflate(LayoutInflater.from(requireContext()))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.apply {
            teamNameInput.setText(team.name)
            titleText.text = "Edit Team"

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            okButton.setOnClickListener {
                val newTeamName = teamNameInput.text?.toString()?.trim() ?: ""

                if (newTeamName.isEmpty()) {
                    teamNameInputLayout.error = "Please enter team name"
                    return@setOnClickListener
                }

                viewModel.updateTeam(team, newTeamName)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    override fun onEditClick(team: Team) {
        showEditTeamDialog(team)
    }

    override fun onItemClick(team: Team) {
        findNavController().navigate(
            R.id.action_teamsFragment_to_playersFragment,
            bundleOf("teamId" to team.id)
        )
    }
}
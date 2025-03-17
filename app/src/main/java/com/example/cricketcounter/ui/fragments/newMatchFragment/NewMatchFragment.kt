package com.example.cricketcounter.ui.fragments.newMatchFragment

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cricketcounter.R
import com.example.cricketcounter.databinding.FirstIningsLayoutBinding
import com.example.cricketcounter.databinding.FragmentNewMatchBinding
import com.example.cricketcounter.ui.fragments.newMatchFragment.viewModel.NewMatchViewModel
import com.example.cricketcounter.ui.fragments.newMatchFragment.viewModel.NewMatchViewModelFactory
import kotlinx.coroutines.launch

class NewMatchFragment : Fragment() {
    private lateinit var binding: FragmentNewMatchBinding
    private var matchSetup: NewMatchViewModel.MatchSetup? = null
    private val viewModel: NewMatchViewModel by viewModels {
        NewMatchViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewMatchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.statusBarColor)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        binding.apply {
            buttonStartMatch.setOnClickListener {
                if (validateInputs()) {
                    saveTeamsAndShowDialog()
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        binding.apply {
            var isValid = true

            val playerCount = editPlayerCount.text?.toString()?.trim()
            when {
                playerCount.isNullOrBlank() -> {
                    editPlayerCount.error = "Please enter number of players"
                    isValid = false
                }

                playerCount.toIntOrNull() !in 2..99 -> {
                    editPlayerCount.error = "Players must be between 2 and 99"
                    isValid = false
                }
            }

            val team1Name = editTeam1.text?.toString()?.trim()
            val team2Name = editTeam2.text?.toString()?.trim()

            when {
                team1Name.isNullOrBlank() -> {
                    editTeam1.error = "Please enter team 1 name"
                    isValid = false
                }

                team2Name.isNullOrBlank() -> {
                    editTeam2.error = "Please enter team 2 name"
                    isValid = false
                }

                team1Name == team2Name -> {
                    editTeam1.error = "Team names cannot be same"
                    editTeam2.error = "Team names cannot be same"
                    isValid = false
                }
            }

            val overs = editOvers.text?.toString()?.trim()
            when {
                overs.isNullOrBlank() -> {
                    editOvers.error = "Please enter number of overs"
                    isValid = false
                }

                overs.toIntOrNull() !in 1..99 -> {
                    editOvers.error = "Overs must be between 1 and 99"
                    isValid = false
                }
            }

            return isValid
        }
    }

    private fun saveTeamsAndShowDialog() {
        val team1Name = binding.editTeam1.text?.toString()?.trim() ?: ""
        val team2Name = binding.editTeam2.text?.toString()?.trim() ?: ""
        val tossWonByTeam1 = binding.radioTeam1.isChecked
        val choseToBat = binding.radioBat.isChecked

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val overs = binding.editOvers.text.toString().toInt()
                val teamIds = viewModel.addTeamsForMatch(team1Name, team2Name, overs)
                matchSetup = NewMatchViewModel.MatchSetup(
                    team1Id = teamIds.first,
                    team2Id = teamIds.second,
                    team1Name = team1Name,
                    team2Name = team2Name,
                    tossWonByTeam1 = tossWonByTeam1,
                    choseToBat = choseToBat,
                    overs = overs
                )
                showStartMatchDialog()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error saving teams: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d("NewMatchFragment", "Error saving teams: ${e.message}")
            }
        }
    }

    companion object {
        fun showStartMatchDialog(
            context: Context,
            lifecycleScope: LifecycleCoroutineScope,
            viewModel: NewMatchViewModel,
            matchId: Int,
            isSecondInnings: Boolean = false,
            onSubmit: (striker: String, nonStriker: String, bowler: String) -> Unit
        ) {
            val dialog = Dialog(context, android.R.style.Theme_Material_Light_Dialog)
            val dialogBinding = FirstIningsLayoutBinding.inflate(LayoutInflater.from(context))
            dialog.setContentView(dialogBinding.root)

            dialog.window?.apply {
                val width = (context.resources.displayMetrics.widthPixels * 0.9).toInt()
                setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
                setGravity(Gravity.CENTER)
                addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                setDimAmount(0.5f)
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }

            dialogBinding.apply {
                // Change title based on innings
                dialogTitle.text = if (isSecondInnings) "Second Innings" else "First Innings"

                buttonSubmit.setOnClickListener {
                    val striker = editStriker.text?.toString()?.trim()
                    val nonStriker = editNonStriker.text?.toString()?.trim()
                    val bowler = editBowler.text?.toString()?.trim()

                    when {
                        striker.isNullOrBlank() -> editStriker.error = "Please enter striker name"
                        nonStriker.isNullOrBlank() -> editNonStriker.error = "Please enter non-striker name"
                        bowler.isNullOrBlank() -> editBowler.error = "Please enter bowler name"
                        striker == nonStriker -> {
                            editNonStriker.error = "Striker and non-striker cannot be same"
                            editStriker.error = "Striker and non-striker cannot be same"
                        }
                        else -> {
                            lifecycleScope.launch {
                                try {
                                    onSubmit(striker, nonStriker, bowler)
                                    dialog.dismiss()
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Error: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }
            }

            dialog.show()
        }
    }

    private fun showStartMatchDialog() {
        val matchSetup = matchSetup ?: return
        showStartMatchDialog(
            context = requireContext(),
            lifecycleScope = viewLifecycleOwner.lifecycleScope,
            viewModel = viewModel,
            matchId = -1, // Not needed for first innings here
            isSecondInnings = false
        ) { striker, nonStriker, bowler ->
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.addOrUpdatePlayers(striker, nonStriker, bowler, matchSetup)
                startMatch()
            }
        }
    }

    private fun startMatch() {
        viewModel.matchDetails?.let { details ->
            val directions = NewMatchFragmentDirections.actionNewMatchFragmentToMatchScoringFragment(
                matchId = details.matchId,
                battingTeam = details.battingTeam,
                bowlingTeam = details.bowlingTeam,
                striker = details.striker,
                nonStriker = details.nonStriker,
                bowler = details.bowler,
                overs = details.overs
            )
            findNavController().navigate(directions)
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }
}
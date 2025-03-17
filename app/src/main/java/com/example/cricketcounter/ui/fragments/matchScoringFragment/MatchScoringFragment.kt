package com.example.cricketcounter.ui.fragments.matchScoringFragment

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.cricketcounter.R
import com.example.cricketcounter.data.extensions.ExtraType
import com.example.cricketcounter.data.extensions.WicketType
import com.example.cricketcounter.data.models.Match
import com.example.cricketcounter.databinding.FragmentMatchScoringBinding
import com.example.cricketcounter.ui.fragments.matchScoringFragment.viewModel.MatchScoringViewModel
import com.example.cricketcounter.ui.fragments.matchScoringFragment.viewModel.MatchScoringViewModelFactory
import kotlinx.coroutines.launch

class MatchScoringFragment : Fragment() {

    private lateinit var binding: FragmentMatchScoringBinding
    private val args: MatchScoringFragmentArgs by navArgs()
    private val viewModel: MatchScoringViewModel by viewModels {
        MatchScoringViewModelFactory(requireActivity().application, args.matchId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMatchScoringBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.statusBarColor)

        val args = MatchScoringFragmentArgs.fromBundle(requireArguments())
        setupMatchDetails(args)
        setupClickListeners()
        observeMatchData()
        observeInningsTransition()
    }

    private fun observeInningsTransition() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.showTargetDialog.collect { shouldShow ->
                if (shouldShow) {
                    showInningsCompleteDialog()
                }
            }
        }
    }

    private fun showInningsCompleteDialog() {
        viewModel.currentMatch.value?.let { match ->
            AlertDialog.Builder(requireContext())
                .setTitle("Innings Complete")
                .setMessage("First innings score: ${match.totalScore}/${match.wickets}\n" +
                        "Target: ${match.totalScore + 1} runs")
                .setPositiveButton("Start Next Innings") { _, _ ->
                    viewModel.startNextInnings()
                }
                .setCancelable(false)
                .create()
                .show()
        }
    }

    private fun setupMatchDetails(args: MatchScoringFragmentArgs) {
        binding.apply {
            // Update batting team name and overs
            teamName.text = "${args.battingTeam}, Inning1, (${args.overs} overs)"

            // Update batsmen names
            (batsmanTable.getChildAt(1) as TableRow).apply {
                (getChildAt(0) as TextView).text = "${args.striker}*"
            }
            (batsmanTable.getChildAt(2) as TableRow).apply {
                (getChildAt(0) as TextView).text = args.nonStriker
            }

            // Update bowler name
            (bowlerTable.getChildAt(1) as TableRow).apply {
                (getChildAt(0) as TextView).text = args.bowler
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            // Regular runs
            btn0.setOnClickListener { viewModel.addRuns(0) }
            btn1.setOnClickListener { viewModel.addRuns(1) }
            btn2.setOnClickListener { viewModel.addRuns(2) }
            btn3.setOnClickListener { viewModel.addRuns(3) }
            btn4.setOnClickListener { viewModel.addRuns(4) }
            btn6.setOnClickListener { viewModel.addRuns(6) }

            // Manual swap
            btnSwap.setOnClickListener {
                lifecycleScope.launch { viewModel.swapBatsmen() }
            }

            // Wicket
            wicketCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) showWicketDialog()
            }

            // Wide
            wideCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) showExtraRunsDialog(ExtraType.WIDE)
            }

            // No ball
            nbCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) showExtraRunsDialog(ExtraType.NO_BALL)
            }

            // Byes
            byesCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) showExtraRunsDialog(ExtraType.BYE)
            }

            // Leg byes
            legByCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) showExtraRunsDialog(ExtraType.LEG_BYE)
            }
        }
    }

    private fun showExtraRunsDialog(type: ExtraType) {
        val options = arrayOf("0", "1", "2", "3", "4", "6")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Runs")
            .setItems(options) { _, which ->
                viewModel.addExtra(type, options[which].toInt())
                resetDeliveryOptions()
            }
            .setNegativeButton("Cancel") { _, _ ->
                resetDeliveryOptions()
            }
            .create()
            .show()
    }

    private fun observeMatchData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentMatch.collect { match ->
                match?.let { updateUI(it) }
            }
        }
    }

    private fun updateUI(match: Match) {
        binding.apply {
            // Update main score and overs
            score.text = "${match.totalScore}/${match.wickets}     (${formatOvers(match.currentOver)}/${args.overs})"

            // Update team name and innings
            teamName.text = "${args.battingTeam}, Inning${match.inning}, (${formatOvers(match.currentOver)}/${args.overs})"

            // Update run rates
            val currentRunRate = if (match.currentOver > 0) {
                String.format("%.2f", match.totalScore / match.currentOver)
            } else "0.00"

            crrText.text = "CRR: $currentRunRate"

            // Update batsmen stats
            (batsmanTable.getChildAt(1) as TableRow).apply {
                val strikerRow = this
                strikerRow.apply {
                    (getChildAt(0) as TextView).text = "${match.striker}*"  // Name
                    (getChildAt(1) as TextView).text = match.strikerRuns.toString()  // Runs
                    (getChildAt(2) as TextView).text = match.strikerBalls.toString()  // Balls
                    (getChildAt(3) as TextView).text = match.strikerFours.toString()  // 4s
                    (getChildAt(4) as TextView).text = match.strikerSixes.toString()  // 6s
                    // Strike rate
                    val strikerSR = if (match.strikerBalls > 0) {
                        String.format("%.2f", (match.strikerRuns * 100.0) / match.strikerBalls)
                    } else "0.00"
                    (getChildAt(5) as TextView).text = strikerSR
                }
            }

            (batsmanTable.getChildAt(2) as TableRow).apply {
                val nonStrikerRow = this
                nonStrikerRow.apply {
                    (getChildAt(0) as TextView).text = match.nonStriker  // Name
                    (getChildAt(1) as TextView).text = match.nonStrikerRuns.toString()  // Runs
                    (getChildAt(2) as TextView).text = match.nonStrikerBalls.toString()  // Balls
                    (getChildAt(3) as TextView).text = match.nonStrikerFours.toString()  // 4s
                    (getChildAt(4) as TextView).text = match.nonStrikerSixes.toString()  // 6s
                    // Strike rate
                    val nonStrikerSR = if (match.nonStrikerBalls > 0) {
                        String.format(
                            "%.2f",
                            (match.nonStrikerRuns * 100.0) / match.nonStrikerBalls
                        )
                    } else "0.00"
                    (getChildAt(5) as TextView).text = nonStrikerSR
                }
            }

            // Update bowler stats
            (bowlerTable.getChildAt(1) as TableRow).apply {
                val bowlerRow = this
                bowlerRow.apply {
                    (getChildAt(0) as TextView).text = match.currentBowler  // Name
                    (getChildAt(1) as TextView).text = formatOvers(match.bowlerOvers)  // Overs
                    (getChildAt(2) as TextView).text = match.bowlerMaidens.toString()  // Maidens
                    (getChildAt(3) as TextView).text = match.bowlerRuns.toString()  // Runs
                    (getChildAt(4) as TextView).text = match.bowlerWickets.toString()  // Wickets
                    // Economy rate
                    val economyRate = if (match.bowlerOvers > 0) {
                        String.format("%.2f", match.bowlerRuns / match.bowlerOvers)
                    } else "0.00"
                    (getChildAt(5) as TextView).text = economyRate
                }
            }

            // Update this over balls
            thisOverLabel.text = "This over: ${match.currentOverBalls.joinToString(" ")}"

            if (match.inning == 2 && match.targetRuns != null) {
                val remainingRuns = match.targetRuns - match.totalScore
                val remainingOvers = match.totalOvers - match.currentOver
                val requiredRunRate = if (remainingOvers > 0) {
                    remainingRuns.toDouble() / remainingOvers
                } else 0.0

                rrrText.text = "RRR: ${String.format("%.2f", requiredRunRate)}"
                targetText.text = "Target: ${match.targetRuns}"
            } else {
                rrrText.text = "RRR: NA"
                targetText.text = "Target: NA"
            }

            // Disable all inputs if innings is ended
            if (match.currentInningsEnded) {
                disableAllInputs()
            }

            // Reset delivery options after each ball
            resetDeliveryOptions()
        }
    }

    private fun formatOvers(overs: Double): String {
        val fullOvers = overs.toInt()
        val balls = ((overs - fullOvers) * 10).toInt()
        return "$fullOvers.$balls"
    }

    private fun resetDeliveryOptions() {
        binding.apply {
            wicketCheckbox.isChecked = false
            wideCheckbox.isChecked = false
            nbCheckbox.isChecked = false
            byesCheckbox.isChecked = false
            legByCheckbox.isChecked = false
        }
    }

    private fun disableAllInputs() {
        binding.apply {
            btn0.isEnabled = false
            btn1.isEnabled = false
            btn2.isEnabled = false
            btn3.isEnabled = false
            btn4.isEnabled = false
            btn6.isEnabled = false
            btnSwap.isEnabled = false
            wicketCheckbox.isEnabled = false
            wideCheckbox.isEnabled = false
            nbCheckbox.isEnabled = false
            byesCheckbox.isEnabled = false
            legByCheckbox.isEnabled = false
        }
    }

    private fun showWicketDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Select Wicket Type")
            .setItems(WicketType.values().map { it.name }.toTypedArray()) { _, which ->
                val wicketType = WicketType.values()[which]
                viewModel.addWicket(wicketType)
                binding.wicketCheckbox.isChecked = false
            }
            .setNegativeButton("Cancel") { _, _ ->
                binding.wicketCheckbox.isChecked = false
            }
            .create()
        dialog.show()
    }
}

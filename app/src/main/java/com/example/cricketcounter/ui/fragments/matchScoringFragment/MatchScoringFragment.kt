package com.example.cricketcounter.ui.fragments.matchScoringFragment

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMatchScoringBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClicks()
        observeState()
    }

    private fun setupClicks() {
        binding.apply {
            btn0.setOnClickListener { viewModel.addRuns(0) }
            btn1.setOnClickListener { viewModel.addRuns(1) }
            btn2.setOnClickListener { viewModel.addRuns(2) }
            btn3.setOnClickListener { viewModel.addRuns(3) }
            btn4.setOnClickListener { viewModel.addRuns(4) }
            btn5.setOnClickListener { viewModel.addRuns(5) }
            btn6.setOnClickListener { viewModel.addRuns(6) }
            btnWicket.setOnClickListener { showWicketDialog() }
            btnWide.setOnClickListener { showExtraDialog(ExtraType.WIDE) }
            btnNb.setOnClickListener { showExtraDialog(ExtraType.NO_BALL) }
            btnBye.setOnClickListener { showExtraDialog(ExtraType.BYE) }
            btnLegBye.setOnClickListener { showExtraDialog(ExtraType.LEG_BYE) }
            btnSwap.setOnClickListener { viewModel.swapBatsmenManual() }
            btnUndo.setOnClickListener { viewModel.undoLastBall() }
            btnScorecard.setOnClickListener {
                val action = MatchScoringFragmentDirections
                    .actionMatchScoringFragmentToScorecardFragment(args.matchId)
                findNavController().navigate(action)
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.currentMatch.collect { match -> match?.let { updateUI(it) } }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.showNewBatsmanDialog.collect { if (it) showNewBatsmanDialog() }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.showNewNonStrikerDialog.collect { if (it) showNewNonStrikerDialog() }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.showNewBowlerDialog.collect { if (it) showNewBowlerDialog() }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.showTargetDialog.collect { if (it) showInningsEndDialog() }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.matchResult.collect { it?.let { msg -> showMatchResultDialog(msg) } }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.toastMessage.collect { it?.let { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                viewModel.clearToast()
            }}
        }
    }

    private fun updateUI(match: Match) {
        binding.apply {
            val battingName = if (match.inning == 1) args.battingTeam else args.bowlingTeam
            teamName.text = "$battingName  •  Inn ${match.inning}  •  ${match.completedOvers}/${match.totalOvers} ov"
            score.text = "${match.totalScore}/${match.wickets}"
            oversText.text = "${match.completedOvers}.${match.ballsInCurrentOver} ov"

            val actualOv = viewModel.actualOversDecimal(match.completedOvers, match.ballsInCurrentOver)
            val crr = if (actualOv > 0) match.totalScore / actualOv else 0.0
            crrText.text = "CRR: ${"%.2f".format(crr)}"

            if (match.inning == 2 && match.targetRuns != null) {
                val rem = match.targetRuns - match.totalScore
                val remOv = match.totalOvers - actualOv
                val rrr = if (remOv > 0) rem / remOv else 0.0
                rrrText.text = "RRR: ${"%.2f".format(rrr)}"
                targetText.text = "Need $rem off ${match.totalOvers - match.completedOvers} ov"
            } else {
                rrrText.text = "RRR: —"
                targetText.text = "Target: —"
            }

            if (match.nextBallIsFreeHit) {
                freeHitBadge.visibility = View.VISIBLE
            } else {
                freeHitBadge.visibility = View.GONE
            }

            // Batsmen table
            (batsmanTable.getChildAt(1) as TableRow).apply {
                (getChildAt(0) as TextView).text = "${match.striker}*"
                (getChildAt(1) as TextView).text = match.strikerRuns.toString()
                (getChildAt(2) as TextView).text = match.strikerBalls.toString()
                (getChildAt(3) as TextView).text = match.strikerFours.toString()
                (getChildAt(4) as TextView).text = match.strikerSixes.toString()
                val sr = if (match.strikerBalls > 0) "${"%.1f".format(match.strikerRuns * 100.0 / match.strikerBalls)}" else "—"
                (getChildAt(5) as TextView).text = sr
            }
            (batsmanTable.getChildAt(2) as TableRow).apply {
                (getChildAt(0) as TextView).text = match.nonStriker
                (getChildAt(1) as TextView).text = match.nonStrikerRuns.toString()
                (getChildAt(2) as TextView).text = match.nonStrikerBalls.toString()
                (getChildAt(3) as TextView).text = match.nonStrikerFours.toString()
                (getChildAt(4) as TextView).text = match.nonStrikerSixes.toString()
                val sr = if (match.nonStrikerBalls > 0) "${"%.1f".format(match.nonStrikerRuns * 100.0 / match.nonStrikerBalls)}" else "—"
                (getChildAt(5) as TextView).text = sr
            }

            // Bowler table
            (bowlerTable.getChildAt(1) as TableRow).apply {
                (getChildAt(0) as TextView).text = match.currentBowler
                (getChildAt(1) as TextView).text = viewModel.formatOvers(match.bowlerCompletedOvers, match.bowlerBallsInOver)
                (getChildAt(2) as TextView).text = match.bowlerMaidens.toString()
                (getChildAt(3) as TextView).text = match.bowlerRuns.toString()
                (getChildAt(4) as TextView).text = match.bowlerWickets.toString()
                val bowlerAv = viewModel.actualOversDecimal(match.bowlerCompletedOvers, match.bowlerBallsInOver)
                val eco = if (bowlerAv > 0) match.bowlerRuns / bowlerAv else 0.0
                (getChildAt(5) as TextView).text = "${"%.2f".format(eco)}"
            }

            updateBallChips(match.currentOverBalls)
            val enabled = !match.currentInningsEnded
            listOf(btn0,btn1,btn2,btn3,btn4,btn5,btn6,btnWicket,btnWide,btnNb,btnBye,btnLegBye,btnSwap,btnUndo).forEach { it.isEnabled = enabled }
        }
    }

    private fun updateBallChips(balls: List<String>) {
        binding.ballChipsContainer.removeAllViews()
        val dp = resources.displayMetrics.density
        if (balls.isEmpty()) {
            binding.ballChipsContainer.addView(TextView(requireContext()).apply {
                text = "No balls this over"; textSize = 12f; setTextColor(Color.parseColor("#9E9E9E"))
            })
            return
        }
        balls.forEach { ball ->
            val chip = TextView(requireContext()).apply {
                text = ball; textSize = 10f; setTypeface(null, Typeface.BOLD)
                setTextColor(Color.WHITE); gravity = Gravity.CENTER
                val size = (38 * dp).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size).apply { setMargins((4*dp).toInt(),0,(4*dp).toInt(),0) }
                background = chipBg(ball)
            }
            binding.ballChipsContainer.addView(chip)
        }
    }

    private fun chipBg(ball: String): android.graphics.drawable.GradientDrawable {
        val c = when {
            ball.contains("W") && !ball.contains("wd") -> Color.parseColor("#E65100")
            ball == "·" || ball == "0" -> Color.parseColor("#546E7A")
            ball.contains("wd") -> Color.parseColor("#F57F17")
            ball.contains("nb") -> Color.parseColor("#AD1457")
            ball.contains("lb") -> Color.parseColor("#4527A0")
            ball.endsWith("b") -> Color.parseColor("#6A1B9A")
            ball == "4" -> Color.parseColor("#1565C0")
            ball == "6" -> Color.parseColor("#B71C1C")
            ball.replace("W","").toIntOrNull() != null -> Color.parseColor("#2E7D32")
            else -> Color.parseColor("#546E7A")
        }
        return android.graphics.drawable.GradientDrawable().apply { shape = android.graphics.drawable.GradientDrawable.OVAL; setColor(c) }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    private fun showNewBatsmanDialog() {
        val input = EditText(requireContext()).apply { hint = "New batsman name"; inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS }
        AlertDialog.Builder(requireContext()).setTitle("🏏 New Batsman").setView(input)
            .setPositiveButton("Confirm") { _, _ -> viewModel.updateNewBatsman(input.text.toString().trim().ifEmpty { "Batsman" }) }
            .setCancelable(false).show()
    }

    private fun showNewNonStrikerDialog() {
        val input = EditText(requireContext()).apply { hint = "Non-striker name"; inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS }
        AlertDialog.Builder(requireContext()).setTitle("🏏 Opening Partner").setView(input)
            .setPositiveButton("Confirm") { _, _ -> viewModel.updateNewNonStriker(input.text.toString().trim().ifEmpty { "Batsman" }) }
            .setCancelable(false).show()
    }

    private fun showNewBowlerDialog() {
        val match = viewModel.currentMatch.value
        val prevBowler = match?.previousBowler ?: ""
        val maxOv = match?.let { (it.totalOvers + 4) / 5 } ?: 0
        val input = EditText(requireContext()).apply {
            hint = "Bowler name"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        }
        val msg = buildString {
            if (prevBowler.isNotEmpty()) append("⚠️ $prevBowler cannot bowl again.\nMax $maxOv overs/bowler.\n\n")
            append("Enter next bowler:")
        }
        AlertDialog.Builder(requireContext()).setTitle("🎳 New Bowler").setMessage(msg).setView(input)
            .setPositiveButton("Confirm") { _, _ -> viewModel.updateNewBowler(input.text.toString().trim().ifEmpty { "Bowler" }) }
            .setCancelable(false).show()
    }

    private fun showWicketDialog() {
        val withRunsOptions = arrayOf("0 runs","1 run","2 runs","3 runs")
        AlertDialog.Builder(requireContext()).setTitle("⚡ Runs before wicket?")
            .setItems(withRunsOptions) { _, runsIdx ->
                val types = WicketType.values().map { it.name.replace("_"," ") }.toTypedArray()
                AlertDialog.Builder(requireContext()).setTitle("🔴 Dismissal Type")
                    .setItems(types) { _, which -> viewModel.addWicket(WicketType.values()[which], runsIdx) }
                    .setNegativeButton("Cancel", null).show()
            }.setNegativeButton("Cancel", null).show()
    }

    private fun showExtraDialog(type: ExtraType) {
        val label = type.name.replace("_"," ")
        val options = arrayOf("0","1","2","3","4","5","6")
        AlertDialog.Builder(requireContext()).setTitle("$label — Runs scored?")
            .setItems(options) { _, i -> viewModel.addExtra(type, i) }
            .setNegativeButton("Cancel", null).show()
    }

    private fun showInningsEndDialog() {
        viewModel.currentMatch.value?.let { match ->
            AlertDialog.Builder(requireContext()).setTitle("🏏 Innings Complete!")
                .setMessage("Score: ${match.totalScore}/${match.wickets} in ${match.completedOvers}.${match.ballsInCurrentOver} overs\n\nTarget: ${match.totalScore + 1}")
                .setPositiveButton("Start 2nd Innings") { _, _ -> viewModel.startNextInnings() }
                .setNeutralButton("Scorecard") { _, _ ->
                    findNavController().navigate(MatchScoringFragmentDirections.actionMatchScoringFragmentToScorecardFragment(args.matchId))
                }
                .setCancelable(false).show()
        }
    }

    private fun showMatchResultDialog(result: String) {
        viewModel.clearResult()
        AlertDialog.Builder(requireContext()).setTitle("🏆 Match Over!")
            .setMessage(result)
            .setPositiveButton("Scorecard") { _, _ ->
                findNavController().navigate(MatchScoringFragmentDirections.actionMatchScoringFragmentToScorecardFragment(args.matchId))
            }
            .setNegativeButton("Close", null).setCancelable(false).show()
    }
}

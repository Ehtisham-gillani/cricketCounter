package com.example.cricketcounter.ui.fragments.scorecardFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.cricketcounter.R
import com.example.cricketcounter.databinding.FragmentScorecardBinding
import kotlinx.coroutines.launch

class ScorecardFragment : Fragment() {

    private lateinit var binding: FragmentScorecardBinding
    private val args: ScorecardFragmentArgs by navArgs()
    private val viewModel: ScorecardViewModel by viewModels {
        ScorecardViewModelFactory(requireActivity().application, args.matchId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentScorecardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.scorecardState.collect { state ->
                state?.let { renderScorecard(it) }
            }
        }
    }

    private fun renderScorecard(state: ScorecardViewModel.ScorecardState) {
        binding.apply {
            // Match header
            tvMatchTitle.text = "${state.battingTeamName} vs ${state.bowlingTeamName}"
            tvTotalOvers.text = "${state.totalOvers} Overs Match"

            // Innings 1
            tvInn1Title.text = "${state.inn1BattingTeam} — ${state.inn1Score}/${state.inn1Wickets} (${state.inn1Overs})"
            populateBatting(inn1BattingContainer, state.inn1Batting)
            populateExtras(tvInn1Extras, state.inn1Extras)
            populateBowling(inn1BowlingContainer, state.inn1Bowling)

            // Innings 2
            if (state.matchStarted2ndInnings) {
                inn2Section.visibility = View.VISIBLE
                tvInn2Title.text = "${state.inn2BattingTeam} — ${state.inn2Score}/${state.inn2Wickets} (${state.inn2Overs})"
                populateBatting(inn2BattingContainer, state.inn2Batting)
                populateExtras(tvInn2Extras, state.inn2Extras)
                populateBowling(inn2BowlingContainer, state.inn2Bowling)
            } else {
                inn2Section.visibility = View.GONE
            }

            // Result
            if (state.result.isNotEmpty()) {
                tvResult.visibility = View.VISIBLE
                tvResult.text = state.result
            } else {
                tvResult.visibility = View.GONE
            }
        }
    }

    private fun populateBatting(container: LinearLayout, entries: List<ScorecardViewModel.BatRow>) {
        container.removeAllViews()
        // Header
        addBatRow(container, "Batsman", "Dismissal", "R", "B", "4s", "6s", "SR", isHeader = true)
        entries.forEach { row ->
            val dismissal = when {
                row.isNotOut -> "not out"
                row.dismissalType.isEmpty() -> "batting"
                else -> row.dismissalType.replace("_"," ").lowercase() + (if (row.dismissedBy.isNotEmpty()) " b ${row.dismissedBy}" else "")
            }
            val sr = if (row.balls > 0) "${"%.1f".format(row.runs * 100.0 / row.balls)}" else "—"
            addBatRow(container, row.name, dismissal, row.runs.toString(), row.balls.toString(), row.fours.toString(), row.sixes.toString(), sr)
        }
    }

    private fun addBatRow(container: LinearLayout, name: String, dismissal: String, r: String, b: String, fours: String, sixes: String, sr: String, isHeader: Boolean = false) {
        val row = TableRow(requireContext()).apply { layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT) }
        fun tv(text: String, weight: Float, alignEnd: Boolean = false) = TextView(requireContext()).apply {
            this.text = text
            textSize = if (isHeader) 11f else 13f
            setTextColor(ContextCompat.getColor(requireContext(), if (isHeader) R.color.text_secondary else R.color.text_primary))
            if (isHeader) setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 6, if (alignEnd) 0 else 8, 6)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
            if (alignEnd) gravity = android.view.Gravity.END
        }
        container.addView(LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            addView(tv(name, 1.8f))
            addView(tv(dismissal, 2.2f))
            addView(tv(r, 0.5f, true))
            addView(tv(b, 0.5f, true))
            addView(tv(fours, 0.5f, true))
            addView(tv(sixes, 0.5f, true))
            addView(tv(sr, 0.8f, true))
        })
    }

    private fun populateExtras(tv: TextView, extras: String) { tv.text = extras }

    private fun populateBowling(container: LinearLayout, entries: List<ScorecardViewModel.BowlRow>) {
        container.removeAllViews()
        addBowlRow(container, "Bowler", "O", "M", "R", "W", "Eco", isHeader = true)
        entries.forEach { row ->
            val eco = if (row.completedOvers > 0 || row.balls > 0) {
                val av = row.completedOvers + row.balls / 6.0
                if (av > 0) "${"%.2f".format(row.runs / av)}" else "—"
            } else "—"
            addBowlRow(container, row.name, "${row.completedOvers}.${row.balls}", row.maidens.toString(), row.runs.toString(), row.wickets.toString(), eco)
        }
    }

    private fun addBowlRow(container: LinearLayout, name: String, o: String, m: String, r: String, w: String, eco: String, isHeader: Boolean = false) {
        container.addView(LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            fun tv(text: String, weight: Float, alignEnd: Boolean = false) = TextView(requireContext()).apply {
                this.text = text
                textSize = if (isHeader) 11f else 13f
                setTextColor(ContextCompat.getColor(requireContext(), if (isHeader) R.color.text_secondary else R.color.text_primary))
                if (isHeader) setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 6, if (alignEnd) 0 else 8, 6)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
                if (alignEnd) gravity = android.view.Gravity.END
            }
            addView(tv(name, 2.2f))
            addView(tv(o, 0.8f, true))
            addView(tv(m, 0.6f, true))
            addView(tv(r, 0.6f, true))
            addView(tv(w, 0.6f, true))
            addView(tv(eco, 0.9f, true))
        })
    }
}

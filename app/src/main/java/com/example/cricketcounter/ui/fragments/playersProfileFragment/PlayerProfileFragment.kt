package com.example.cricketcounter.ui.fragments.playersProfileFragment

import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.fragment.app.viewModels
import com.example.cricketcounter.R
import com.example.cricketcounter.data.models.Player
import com.example.cricketcounter.databinding.FragmentPlayerProfileBinding
import com.example.cricketcounter.databinding.StatItemBinding
import com.example.cricketcounter.ui.fragments.playersProfileFragment.viewModel.PlayerProfileViewModel
import com.example.cricketcounter.ui.fragments.playersProfileFragment.viewModel.PlayerProfileViewModelFactory
import com.google.android.material.tabs.TabLayout

class PlayerProfileFragment : Fragment() {
    private lateinit var binding: FragmentPlayerProfileBinding
    private var currentPlayer: Player? = null
    private val viewModel: PlayerProfileViewModel by viewModels {
        PlayerProfileViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayerProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getInt("playerId")?.let { playerId ->
            viewModel.loadPlayer(playerId)
        }

        setupTabs()
        observeViewModel()
    }

    private fun setupTabs() {
        binding.apply {
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    currentPlayer?.let { player ->
                        when (tab?.position) {
                            0 -> showBattingStats(player)
                            1 -> showBowlingStats(player)
                            2 -> showFieldingStats(player)
                        }
                    }
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun observeViewModel() {
        viewModel.player.observe(viewLifecycleOwner) { player ->
            currentPlayer = player
            updateUI(player)
        }
    }

    private fun updateUI(player: Player) {
        binding.apply {
            toolbarTitle.text = player.name
            when (tabLayout.selectedTabPosition) {
                0 -> showBattingStats(player)
                1 -> showBowlingStats(player)
                2 -> showFieldingStats(player)
            }
        }
    }

    private fun showBattingStats(player: Player) {
        binding.statsContainer.removeAllViews()

        // Batting stats in fixed order
        val battingStats = listOf(
            "Matches" to player.matches.toString(),
            "Innings" to player.innings.toString(),
            "Runs" to player.runs.toString(),
            "Not Outs" to player.notOuts.toString(),
            "Best Score" to player.bestScore.toString(),
            "Strike Rate" to "%.2f".format(player.strikeRate),
            "Average" to "%.2f".format(player.average),
            "Fours" to player.fours.toString(),
            "Sixes" to player.sixes.toString(),
            "Thirties" to player.thirties.toString(),
            "Fifties" to player.fifties.toString(),
            "Hundreds" to player.hundreds.toString(),
            "Ducks" to player.ducks.toString()
        )

        battingStats.forEach { (label, value) ->
            addStatItem(label, value)
        }
    }

    private fun showBowlingStats(player: Player) {
        binding.statsContainer.removeAllViews()

        // Bowling stats in fixed order
        val bowlingStats = listOf(
            "Matches" to player.matches.toString(),
            "Innings" to player.bowlingInnings.toString(),
            "Seq. Innings" to "0",
            "Overs" to player.overs.toString(),
            "Wickets" to player.wickets.toString(),
            "Runs" to player.bowlingRuns.toString(),
            "B. Bowling" to "-",
            "Eco. Rate" to "%.2f".format(player.economyRate),
            "Maidens" to player.maidens.toString(),
            "Average" to "%.2f".format(if (player.wickets > 0) player.bowlingRuns.toDouble() / player.wickets else 0.0),
            "Wides" to player.wides.toString(),
            "No Balls" to player.noBalls.toString(),
            "Dots Balls" to player.dotBalls.toString(),
            "4 Wickets" to player.fourWickets.toString(),
            "5 Wickets" to player.fiveWickets.toString()
        )

        bowlingStats.forEach { (label, value) ->
            addStatItem(label, value)
        }
    }

    private fun showFieldingStats(player: Player) {
        binding.statsContainer.removeAllViews()

        // Fielding stats in fixed order
        val fieldingStats = listOf(
            "Matches" to player.matches.toString(),
            "Catches" to player.catches.toString(),
            "Stumpings" to player.stumpings.toString(),
            "Run Outs" to player.runOuts.toString()
        )

        fieldingStats.forEach { (label, value) ->
            addStatItem(label, value)
        }
    }

    private fun addStatItem(label: String, value: String) {
        val statView = StatItemBinding.inflate(layoutInflater)
        statView.apply {
            tvLabel.text = label
            tvValue.text = value

            root.layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                setGravity(Gravity.FILL_HORIZONTAL)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)

                setMargins(8, 8, 8, 8)
            }
        }
        binding.statsContainer.addView(statView.root)
    }
}
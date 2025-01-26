package com.example.cricketcounter.ui.fragments.newMatchFragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.cricketcounter.R
import com.example.cricketcounter.databinding.FirstIningsLayoutBinding
import com.example.cricketcounter.databinding.FragmentNewMatchBinding

class NewMatchFragment : Fragment() {
    private lateinit var binding: FragmentNewMatchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewMatchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.statusBarColor)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        binding.apply {
            editPlayerCount.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    scrollToView(editPlayerCount)
                }
            }
            editTeam1.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    scrollToView(editTeam1)
                }
            }
            editTeam2.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    scrollToView(editTeam2)
                }
            }
            editOvers.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    scrollToView(editOvers)
                }
            }
            buttonStartMatch.setOnClickListener {
                showStartMatchDialog()
            }
        }
    }

    private fun scrollToView(view: View) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val scrollView = binding.root
                val offset = resources.displayMetrics.heightPixels / 3

                val coordinates = IntArray(2)
                view.getLocationInWindow(coordinates)

                scrollView.smoothScrollTo(0, coordinates[1] - offset)
            }
        })
    }

    private fun showStartMatchDialog() {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Material_Light_Dialog)

        val dialogBinding = FirstIningsLayoutBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.apply {
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.CENTER)

            // Enable standard dialog features
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.5f)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        dialogBinding.apply {
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
                        startMatch()
                        dialog.dismiss()
                    }
                }
            }
        }

        dialog.show()
    }

    private fun startMatch() {
        findNavController().navigate(R.id.action_newMatchFragment_to_matchScoringFragment)
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
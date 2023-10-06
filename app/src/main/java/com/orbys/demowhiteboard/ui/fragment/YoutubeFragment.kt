package com.orbys.demowhiteboard.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.orbys.demowhiteboard.R
import com.orbys.demowhiteboard.databinding.FragmentYoutubeBinding

class YoutubeFragment : Fragment() {

    private lateinit var binding: FragmentYoutubeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentYoutubeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        initListenners()
    }

    private fun initListenners() {
        binding.etSearchYoutube.setOnEditorActionListener { v, actionId, event ->
            // Handle the IME options
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    // Do something when the user presses "Done"
                    val wordSearch = binding.etSearchYoutube.text.toString()
                    if (wordSearch.isNotBlank()) {

                    } else {
                        Toast.makeText(
                            context,
                            "Debes introducir un valor para buscar",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    true
                }

                else -> false
            }
        }
    }
}
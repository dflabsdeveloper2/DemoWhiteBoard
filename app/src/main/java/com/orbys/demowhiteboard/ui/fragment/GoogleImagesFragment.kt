package com.orbys.demowhiteboard.ui.fragment

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.orbys.demowhiteboard.R
import com.orbys.demowhiteboard.core.GlobalConfig
import com.orbys.demowhiteboard.core.Util
import com.orbys.demowhiteboard.core.hideKeyboard
import com.orbys.demowhiteboard.data.api.RetrofitClient
import com.orbys.demowhiteboard.data.api.model.ImageModel
import com.orbys.demowhiteboard.data.api.model.image.ListImageModelApi
import com.orbys.demowhiteboard.databinding.FragmentGoogleImagesBinding
import com.orbys.demowhiteboard.ui.adapter.AdapterGoogle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoogleImagesFragment : Fragment() {

    private lateinit var binding: FragmentGoogleImagesBinding
    private lateinit var adapterGoogle: AdapterGoogle

    private lateinit var listImages: MutableList<ImageModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentGoogleImagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
    }

    private fun initUI() {
        iniValues()
        initListenners()
        initReciclerView()
    }

    private fun iniValues() {
        listImages = mutableListOf()
    }

    private fun initReciclerView() {
        adapterGoogle = AdapterGoogle(listOf()) {
            Toast.makeText(context, it.title,Toast.LENGTH_SHORT).show()

            lifecycleScope.launch(Dispatchers.IO){
                val loader = ImageLoader(requireContext())
                val request = ImageRequest.Builder(requireContext())
                    .data(it.url)
                    .allowHardware(false) // Disable hardware bitmaps.
                    .build()

                val result = (loader.execute(request) as SuccessResult).drawable
                val bitmap = (result as BitmapDrawable).bitmap

                Log.d("BITMAP","bitmap: $bitmap")
            }
        }

        binding.rvGoogle.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = adapterGoogle
        }
    }

    private fun initListenners() {
        binding.etSearchImages.setOnEditorActionListener { v, actionId, event ->
            // Handle the IME options
            when (actionId) {
                EditorInfo.IME_ACTION_GO -> {
                    Log.d("GOOGLE","CLICK")
                    v.hideKeyboard()
                    listImages.clear()
                    binding.pbLoading.isVisible = true
                    // Do something when the user presses "Done"
                    val wordSearch = binding.etSearchImages.text.toString()
                    val licenseCheck = binding.swLicense.isChecked
                    val safeSearchCheck = binding.swLicense.isChecked
                    if (wordSearch.isNotBlank()) {
                        lifecycleScope.launch {

                            val apikey = context?.resources?.getString(R.string.api_key).orEmpty()
                            val motor = context?.resources?.getString(R.string.api_cx).orEmpty()

                           val listImagesApi = withContext(Dispatchers.IO) {
                               try {
                                   when {
                                       licenseCheck && safeSearchCheck -> {
                                           RetrofitClient.serviceImage.getListImages(
                                               key = apikey,
                                               motor = motor,
                                               searchWord = wordSearch,
                                               licencia = Util.rights,
                                               safesearch = Util.safe,
                                               type = Util.searchType,
                                               num = Util.num
                                           )
                                       }

                                       licenseCheck && !safeSearchCheck -> {
                                           RetrofitClient.serviceImage.getListImagesWithoutSafeSearch(
                                               key = apikey,
                                               motor = motor,
                                               searchWord = wordSearch,
                                               licencia = Util.rights,
                                               type = Util.searchType,
                                               num = Util.num
                                           )
                                       }

                                       !licenseCheck && safeSearchCheck -> {
                                           RetrofitClient.serviceImage.getListImagesWithoutLicense(
                                               key = apikey,
                                               motor = motor,
                                               searchWord = wordSearch,
                                               safesearch = Util.safe,
                                               type = Util.searchType,
                                               num = Util.num
                                           )
                                       }

                                       else -> {
                                           RetrofitClient.serviceImage.getListImagesWithoutSafeSearchAndLicense(
                                               key = apikey,
                                               motor = motor,
                                               searchWord = wordSearch,
                                               type = Util.searchType,
                                               num = Util.num
                                           )
                                       }
                                   }
                               } catch (e: Exception) {
                                   withContext(Dispatchers.Main) {
                                       Toast.makeText(context, "ERROR $e", Toast.LENGTH_SHORT)
                                           .show()
                                       binding.pbLoading.isVisible = false
                                   }
                                   null
                               }
                           }

                            listImagesApi?.let {
                                if (it.items.isNotEmpty()) {
                                    listImages?.addAll(it.items.map { item ->
                                        ImageModel(
                                            item.link,
                                            item.title
                                        )
                                    })
                                }
                            }

                            if(listImages.isNotEmpty()){
                                Log.d("GOOGLE","prueba $listImages")
                                adapterGoogle.updateList(listImages.toList())
                            }
                            binding.pbLoading.isVisible = false
                        }
                    } else {
                        binding.pbLoading.isVisible = false
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
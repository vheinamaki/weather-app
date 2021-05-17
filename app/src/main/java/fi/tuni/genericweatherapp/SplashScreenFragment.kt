package fi.tuni.genericweatherapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import fi.tuni.genericweatherapp.databinding.FragmentSplashScreenBinding

class SplashScreenFragment : Fragment(R.layout.fragment_weather) {
    private val model: WeatherViewModel by activityViewModels()

    lateinit var binding: FragmentSplashScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSplashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.retryButton.setOnClickListener(this::onRetryButtonClicked)

        setFragmentResultListener("showButtons") { _, bundle ->
            binding.retryButton.isVisible = bundle.getBoolean("retryButton", false)
        }
    }

    fun onRetryButtonClicked(view: View) {
        model.refreshForecast()
    }
}

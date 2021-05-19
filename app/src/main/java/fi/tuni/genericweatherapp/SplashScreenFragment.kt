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

/**
 * The fragment view shown initially when the app is launched.
 *
 * Shows the app's title, and a 'retry' button if a connection error is encountered.
 */
class SplashScreenFragment : Fragment(R.layout.fragment_weather) {

    /**
     * Get reference to the activity's ViewModel to observe its data.
     */
    private val model: WeatherViewModel by activityViewModels()

    /**
     * ViewBinding for the fragment.
     */
    private lateinit var binding: FragmentSplashScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Setup view binding
        binding = FragmentSplashScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.retryButton.setOnClickListener(this::onRetryButtonClicked)

        // Listen for fragment results sent by the host activity
        setFragmentResultListener("showButtons") { _, bundle ->
            // Make the retry button visible
            binding.retryButton.isVisible = bundle.getBoolean("retryButton", false)
        }
    }

    /**
     * Attempts to retry the request by refreshing the forecast.
     */
    private fun onRetryButtonClicked(view: View) {
        model.refreshForecast()
    }
}

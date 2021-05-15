package fi.tuni.genericweatherapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Manages the state of LocationsActivity
 */
@HiltViewModel
class LocationListViewModel @Inject constructor(private val locationRepo: LocationRepository) :
    ViewModel() {}

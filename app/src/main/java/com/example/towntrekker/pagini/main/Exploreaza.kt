package com.example.towntrekker.pagini.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.towntrekker.ActivityMain
import com.example.towntrekker.R
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener


@SuppressLint("MissingPermission")
class Exploreaza : Fragment(), OnMapReadyCallback, OnMapsSdkInitializedCallback {
    private lateinit var mapView: MapView
    private lateinit var harta: GoogleMap
    private lateinit var autocompletare: AutocompleteSupportFragment

    private var ultimaLocatie: LatLng = LatLng(0.0,0.0)
    private var marker: Marker? = null

    private lateinit var mainActivityContext: ActivityMain


    private val reqAccesLocatie = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { p ->
        if (p[Manifest.permission.ACCESS_FINE_LOCATION] == true || p[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            harta.isMyLocationEnabled = true
            harta.uiSettings.isMyLocationButtonEnabled = true

            harta.setOnMyLocationButtonClickListener {
                verificaLocatieActivata()
                false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapsInitializer.initialize(requireContext(), MapsInitializer.Renderer.LATEST, this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.pagina_exploreaza, container, false)

        mainActivityContext = context as ActivityMain

        autocompletare = childFragmentManager.findFragmentById(R.id.cautare_locatie) as AutocompleteSupportFragment
        autocompletare.setCountries("RO")
        autocompletare.setHint(resources.getString(R.string.cauta_locatie))
        autocompletare.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG))

        mapView = rootView.findViewById(R.id.exploreaza_harta)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        return rootView
    }

    override fun onMapReady(googleMap: GoogleMap) {
        harta = googleMap

        marker = harta.addMarker(MarkerOptions().position(LatLng(0.0, 0.0)))
        marker?.isVisible = false

        harta.uiSettings.isZoomControlsEnabled = true
        harta.uiSettings.isCompassEnabled = true
        harta.uiSettings.isMyLocationButtonEnabled = true

        val coordUnibuc = LatLng(44.4355, 26.0995)
        harta.moveCamera(CameraUpdateFactory.newLatLngZoom(coordUnibuc, 17f))

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            harta.isMyLocationEnabled = true

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

            fusedLocationClient.lastLocation.addOnSuccessListener { locatie ->
                if (locatie != null) {
                    ultimaLocatie = LatLng(locatie.latitude, locatie.longitude)
                }
            }

            harta.setOnMyLocationButtonClickListener {
                verificaLocatieActivata()
                true
            }

        }
        else {
            reqAccesLocatie.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
        }

        autocompletare.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i(mainActivityContext.getTag(), "Place: " + place.id + "," + place.name + ", " + place.address+ ", " + place.latLng)

                if (place.latLng != null) {
                    marker?.position = LatLng(place.latLng!!.latitude, place.latLng!!.longitude)
                    marker?.title = place.name
                    marker?.snippet = place.address

                    marker?.isVisible = true

                    val updateCamera = CameraUpdateFactory.newLatLngZoom(
                        LatLng(place.latLng!!.latitude, place.latLng!!.longitude), 19f
                    )

                    harta.animateCamera(updateCamera)
                }
            }

            override fun onError(status: Status) {
                Log.i(mainActivityContext.getTag(), "A apărut o eroare: $status")
            }
        })

        val autocompletareText = autocompletare.view?.findViewById<AppCompatEditText>(com.google.android.libraries.places.R.id.places_autocomplete_search_input)

        autocompletareText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    marker?.isVisible = false
                }
            }
        })

        harta.setOnMapClickListener { latLng ->
            marker?.hideInfoWindow()

            marker?.position = latLng

            marker?.isVisible = true
            marker?.showInfoWindow()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> Log.d("MapsDemo", "The latest version of the renderer is used.")
            MapsInitializer.Renderer.LEGACY -> Log.d("MapsDemo", "The legacy version of the renderer is used.")
        }
    }

    private fun verificaLocatieActivata() {
        val managerLocatie =
            mainActivityContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locatieActivata = managerLocatie.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!locatieActivata) {
            Toast.makeText(mainActivityContext, "Locația nu este activată.", Toast.LENGTH_LONG)
                .show()
        } else {
            val updateCamera = CameraUpdateFactory.newLatLngZoom(
                LatLng(ultimaLocatie.latitude, ultimaLocatie.longitude), 17f
            )

            harta.animateCamera(updateCamera)
        }
    }
}
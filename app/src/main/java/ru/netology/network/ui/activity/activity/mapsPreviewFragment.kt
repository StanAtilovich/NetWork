package ru.netology.network.ui.activity.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.Runtime
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.network.R
import ru.netology.network.databinding.FragmentMapPreviewBinding
import ru.netology.network.ui.activity.util.DoubleArg
@AndroidEntryPoint
class mapsPreviewFragment: Fragment() {

    companion object{
        var Bundle.doubleArg1: Double by DoubleArg
        var Bundle.doubleArg2: Double by DoubleArg
    }
    private var mapView: MapView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMapPreviewBinding.inflate(layoutInflater)

        mapView = binding.mapview

        val markerLatitude = (arguments?.doubleArg1 ?: 0.0).toDouble()
        val markerLongitude =(arguments?.doubleArg2 ?: 0.0).toDouble()

        mapView?.getMap()?.move(
            CameraPosition(Point(markerLatitude, markerLongitude), 15.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 2F),
            null
        )

        val marker = mapView?.map?.mapObjects?.addPlacemark(
            Point(markerLatitude, markerLongitude),
            ImageProvider.fromResource(Runtime.getApplicationContext(), R.drawable.star)
        )
        marker?.opacity = 0.5f

        mapView?.map?.mapObjects?.addPlacemark(
            Point(markerLatitude - 0.0005, markerLongitude),
        )
        binding.fabBack.setOnClickListener {
            findNavController().navigateUp()
        }
        return binding.root
    }

    override fun onStop() {
        mapView?.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView?.onStart()
    }
}
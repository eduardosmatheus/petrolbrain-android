package br.com.smartgas.adapters

import android.app.AlertDialog
import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.os.PersistableBundle
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback

class MapsActivity: FragmentActivity(), OnMapReadyCallback,
  GoogleApiClient.ConnectionCallbacks,
  GoogleApiClient.OnConnectionFailedListener {
  override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
    super.onCreate(savedInstanceState, persistentState)
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val isGpsActive = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    if (!isGpsActive) {
      val dialog = AlertDialog.Builder(this)
      dialog.setMessage("Para utilizar este app, é necessário estar com a localização ativa.")
      dialog.setPositiveButton("Ok") { dialogInterface, _ ->
        dialogInterface.dismiss()
        finish()
      }
      dialog.create().show()
    }
  }
  override fun onMapReady(map: GoogleMap?) {
    TODO("Not yet implemented")
  }

  override fun onConnected(bundle: Bundle?) {
    TODO("Not yet implemented")
  }

  override fun onConnectionSuspended(p0: Int) {
    TODO("Not yet implemented")
  }

  override fun onConnectionFailed(result: ConnectionResult) {
    TODO("Not yet implemented")
  }

}
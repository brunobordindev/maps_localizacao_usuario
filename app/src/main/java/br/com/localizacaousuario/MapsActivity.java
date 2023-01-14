package br.com.localizacaousuario;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import br.com.localizacaousuario.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private String[] permissoes = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //validar permissoes
        Permissoes.validarPermissoes(permissoes, this, 1);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //objeto responsavel por gerenciar a localização do usuário
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                Double latitude = location.getLatitude();
                Double longitude = location.getLongitude();

                mMap.clear();
                
                // Add a marker in Sydney and move the camera
//                LatLng localUsuario = new LatLng(latitude, longitude);
//                mMap.addMarker(new MarkerOptions().position(localUsuario).title("Meu local!"));
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localUsuario, 15));

                /*
                GEOCODING - processo de transfomar um endereço ou descrição de um local em latitude/longitude.
                REREVERSE GEOCODING - processo de transformar latitude/longitude em um endereço.
                 */
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                try {

                    String stringEndereco = "Rua Cambará, 55 - Juvevê, Curitiba-PR";

//                    List<Address> listaEndereco = geocoder.getFromLocation(latitude, longitude, 1);
                    List<Address> listaEndereco = geocoder.getFromLocationName(stringEndereco, 1);

                    if (listaEndereco != null && listaEndereco.size() > 0){
                        Address endereco = listaEndereco.get(0);

                        /*
                        endereco selecionado
                        Address[
                        addressLines=[0:"R. Cambará, 55 - Juvevê, Curitiba - PR, 80030-380, Brasil"],
                        feature=55,
                        admin=Paraná,
                        sub-admin=Curitiba,
                        locality=null,
                        thoroughfare=Rua Cambará,
                        postalCode=80030-380,
                        countryCode=BR,
                        countryName=Brasil,
                        hasLatitude=true,
                        latitude=-25.417974299999997,
                        hasLongitude=true,
                        longitude=-49.2560752,
                        phone=null,
                        url=null,
                        extras=null]
                         */

                        Double lat = endereco.getLatitude();
                        Double lon = endereco.getLongitude();

                        LatLng localUsuario = new LatLng(lat, lon);
                        mMap.addMarker(new MarkerOptions().position(localUsuario).title("Meu local!"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localUsuario, 15));

                        Log.d("local", "endereco selecionado " + endereco.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };

        //precisa aqui tambem, alem de ser so na permissao
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0,
                    20,
                    locationListener
            );
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {

                alertaValidacaoPermissao();

            } else if (permissaoResultado == PackageManager.PERMISSION_GRANTED) {

                //RECUPERAR LOCALIZACAO DO USUARIO
                // 1 .provedor da localização
                //2 . tempo mínimo entre atualizações de localização (milisegundos)
                //3. distancia mínima entre atualizacoes de localizacao (metros)
                //4. Location listener (para recebermos a localizacao)
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0,
                            20,
                            locationListener
                    );
                }

            }
        }
    }

    private void alertaValidacaoPermissao() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para usar o App é necessários aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
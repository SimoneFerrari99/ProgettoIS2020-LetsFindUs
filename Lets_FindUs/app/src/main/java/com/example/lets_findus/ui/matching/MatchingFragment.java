package com.example.lets_findus.ui.matching;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.lets_findus.R;
import com.example.lets_findus.ui.MissingPermissionDialog;
import com.example.lets_findus.ui.favourites.FavAdapter;
import com.example.lets_findus.utilities.AppDatabase;
import com.example.lets_findus.utilities.MeetingDao;
import com.example.lets_findus.utilities.MeetingPerson;
import com.example.lets_findus.utilities.PersonDao;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MatchingFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener {

    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap map;
    private View root;
    private MaterialButton show_match;

    private BottomSheetBehavior sheetBehavior;
    private static RecyclerView.Adapter adapter; //l'adapter serve per popolare ogni riga
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    static View.OnClickListener myOnClickListener;

    private static AppDatabase db;
    private static MeetingDao md;
    private static PersonDao pd;

    private static List<MeetingPerson> meetings;
    private List<Marker> visibleMarkers;

    private ActivityResultLauncher<String> requestPermissionLauncher;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        root = inflater.inflate(R.layout.fragment_matching, container, false);

        show_match = root.findViewById(R.id.matching_button);
        show_match.setVisibility(View.GONE);
        show_match.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(map.getCameraPosition().target)
                        .zoom(map.getCameraPosition().zoom)
                        .bearing(0)                // Sets the orientation of the camera to east
                        .build();                   // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        setupMarkers(map);
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                animateHide(v);
            }
        });

        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onActivityResult(Boolean isGranted) {
                        if (isGranted) {
                            mapFragment.getMapAsync(MatchingFragment.this);
                        } else {
                            DialogFragment newFragment = new MissingPermissionDialog();
                            newFragment.show(getParentFragmentManager(), "missing_permission");
                        }
                    }
                });

        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        }
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance(new GoogleMapOptions().minZoomPreference(0f));
            mapFragment.getMapAsync(this);
        }

        // R.id.map is a FrameLayout, not a Fragment
        getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();

        if(db == null){
            db = Room.databaseBuilder(getContext(), AppDatabase.class, "meeting_db").build();
            md = db.meetingDao();
            pd = db.personDao();
        }

        sheetBehavior = BottomSheetBehavior.from(root.findViewById(R.id.bs_card_view));
        sheetBehavior.setGestureInsetBottomIgnored(true);

        myOnClickListener = new MatchingFragment.MyOnClickListener(root.getContext());

        recyclerView = root.findViewById(R.id.bottom_sheet_rec_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(root.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        meetings = new ArrayList<>();
        visibleMarkers = new ArrayList<>();

        adapter = new FavAdapter(meetings);
        recyclerView.setAdapter(adapter);

        return root;
    }

    @SuppressLint("MissingPermission")
    private void setupMap(final GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, null).addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 17f));
                    }
                    setupMarkers(googleMap);
                }
            });
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        map = googleMap;
        setupMap(map);
        map.setOnCameraMoveStartedListener(this);
    }

    @Override
    public void onCameraMoveStarted(int i) {
        if(i == REASON_GESTURE && show_match.getVisibility() == View.GONE)
            animateShow(show_match);
    }

    private static class MyOnClickListener implements View.OnClickListener {

        private final Context context;

        private MyOnClickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            removeItem(v);
        }

        private void removeItem(View v) {
            int selectedItemPosition = recyclerView.getChildAdapterPosition(v);
            Toast.makeText(v.getContext(), "Ciao "+meetings.get(selectedItemPosition).person.nickname, Toast.LENGTH_SHORT).show();
        }
    }

    private ListenableFuture<List<MeetingPerson>> loadVisibleMeeting(GoogleMap map){
        return md.getMeetingsBetweenVisibleRegion(map.getProjection().getVisibleRegion());
    }

    private List<Marker> setVisibleMeetingsMarker(List<MeetingPerson> meetings, GoogleMap map){
        List<Marker> markers = new ArrayList<>();
        for(MeetingPerson mp : meetings){
            Log.d("setMarker", String.valueOf(mp.meeting.id));
            markers.add(map.addMarker(new MarkerOptions()
                    .position(new LatLng(mp.meeting.latitude, mp.meeting.longitude))
                    .title(mp.person.nickname)));
        }
        return markers;
    }

    private void removeVisibleMarker(List<Marker> markers){
        for(Marker m : markers){
            m.remove();
        }
    }

    private void setupMarkers(final GoogleMap map){
        final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
        Futures.addCallback(loadVisibleMeeting(map), new FutureCallback<List<MeetingPerson>>() {
            @Override
            public void onSuccess(@NullableDecl final List<MeetingPerson> result) {
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MatchingFragment.meetings.clear();
                        MatchingFragment.meetings.addAll(result);
                        adapter.notifyDataSetChanged();
                        removeVisibleMarker(visibleMarkers);
                        visibleMarkers = setVisibleMeetingsMarker(MatchingFragment.meetings, map);
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {

            }
        }, Executors.newSingleThreadExecutor());

    }

    private void animateShow(View v){
        v.setAlpha(0f);
        v.setVisibility(View.VISIBLE);

        v.animate()
                .alpha(1f)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime))
                .setListener(null);
    }

    private void animateHide(final View v){
        v.animate()
                .alpha(0f)
                .setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        v.setVisibility(View.GONE);
                    }
                });
    }
}
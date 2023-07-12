package com.example.happynewplaces.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.happynewplaces.R
import com.example.happynewplaces.database.DataBaseHandler
import com.example.happynewplaces.databinding.ActivityAddHappyPlaceBinding
import com.example.happynewplaces.models.HappyPlaceModel
import com.example.happynewplaces.utils.GetAddressFromLatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private var binding: ActivityAddHappyPlaceBinding? = null
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0

    private var mHappyPlaceDetails: HappyPlaceModel? = null

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolAddPlace?.setNavigationOnClickListener {
            onBackPressed()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if(!Places.isInitialized()){
            Places.initialize(
                this@AddHappyPlaceActivity,
                resources.getString(R.string.google_maps_api)
            )
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetails = intent.getParcelableExtra<HappyPlaceModel>(
                MainActivity.EXTRA_PLACE_DETAILS
            ) as HappyPlaceModel
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            updateDateInView()
        }
        updateDateInView()

        if(mHappyPlaceDetails != null){
            supportActionBar?.title = "Edit Happy Place"

            binding?.txtTitle?.setText(mHappyPlaceDetails!!.title)
            binding?.txtDescription?.setText(mHappyPlaceDetails!!.description)
            binding?.txtDate?.setText(mHappyPlaceDetails!!.date)
            binding?.txtLocation?.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitude
            mLongitude = mHappyPlaceDetails!!.longitude

            saveImageToInternalStorage = Uri.parse(
                mHappyPlaceDetails!!.image
            )

            binding?.imgPlaceImage?.setImageURI(saveImageToInternalStorage)
            binding?.btnSave?.text = "UPDATE"
        }

        binding?.txtDate?.setOnClickListener(this)
        binding?.txtAddImage?.setOnClickListener(this)
        binding?.btnSave?.setOnClickListener(this)
        binding?.txtLocation?.setOnClickListener(this)
        binding?.txtSelectCurrentLocation?.setOnClickListener(this)

    }

    private fun isLocationEnabled(): Boolean{
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(){
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1000
        mLocationRequest.numUpdates = 1

        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallBack,
            Looper.myLooper()
        )
    }

    private val mLocationCallBack = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult!!.lastLocation
            mLatitude = mLastLocation!!.latitude
            Log.i("Current Latitude", "$mLatitude")
            mLongitude = mLastLocation!!.longitude
            Log.i("Current Longitude", "$mLongitude")

            val addressTask = GetAddressFromLatLng(this@AddHappyPlaceActivity, mLatitude, mLongitude)
            addressTask.setAddressListener(object: GetAddressFromLatLng.AddressListener{
                override fun onAddressFound(address: String?){

                }

                override fun onError(){
                    Log.e("Get Address:: ", "Something went wrong")
                }
            })
            addressTask.getAddress()
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.txtDate ->{
                DatePickerDialog(
                    this@AddHappyPlaceActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            R.id.txtAddImage ->{
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf("Select photo from Gallery",
                    "Capture photo from camera")
                pictureDialog.setItems(pictureDialogItems){ _, which ->

                    when(which){
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }

            R.id.txtLocation -> {
                if(!isLocationEnabled()){
                    Toast.makeText(
                        this,
                        "Your location provider is turned off. Please turn it on.",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }else {
                    Dexter.withActivity(this).withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ).withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report!!.areAllPermissionsGranted()) {
                                try {
                                    val fields = listOf(
                                        Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                                        Place.Field.ADDRESS
                                    )

                                    val intent = Autocomplete.IntentBuilder(
                                        AutocompleteActivityMode.FULLSCREEN, fields
                                    ).build(this@AddHappyPlaceActivity)

                                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            showRationalDialogForPermissions()
                        }
                    }).onSameThread().check()
                }

            }
            R.id.txtSelectCurrentLocation -> {
                if(!isLocationEnabled()){
                    Toast.makeText(
                        this,
                        "Your location provider is turned off. Please turn it on.",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }else{
                    Dexter.withActivity(this).withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ).withListener(object: MultiplePermissionsListener{

                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if(report!!.areAllPermissionsGranted()){
                                requestNewLocationData()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            showRationalDialogForPermissions()
                        }
                    }).onSameThread().check()
                }
            }

            R.id.btnSave ->{
                when{
                    binding?.txtTitle?.text.isNullOrEmpty() ->{
                        Toast.makeText(this@AddHappyPlaceActivity,
                            "Please enter a title",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    binding?.txtDescription?.text.isNullOrEmpty() ->{
                        Toast.makeText(this@AddHappyPlaceActivity,
                            "Please enter a description",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    binding?.txtDate?.text.isNullOrEmpty() ->{
                        Toast.makeText(this@AddHappyPlaceActivity,
                            "Please enter a date",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    binding?.txtLocation?.text.isNullOrEmpty() ->{
                        Toast.makeText(this@AddHappyPlaceActivity,
                            "Please enter a location",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    saveImageToInternalStorage == null ->{
                        Toast.makeText(this@AddHappyPlaceActivity,
                            "Please select an image",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else ->{
                        val happyPlaceModel = HappyPlaceModel(
                            if (mHappyPlaceDetails == null) 0
                            else mHappyPlaceDetails!!.id,
                            binding?.txtTitle?.text.toString(),
                            saveImageToInternalStorage.toString(),
                            binding?.txtDescription?.text.toString(),
                            binding?.txtDate?.text.toString(),
                            binding?.txtLocation?.text.toString(),
                            mLatitude,
                            mLongitude
                        )

                    val dbHandler = DataBaseHandler(this)

                        if(mHappyPlaceDetails == null){
                            val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)

                            if(addHappyPlace > 0){
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }else{
                            val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModel)

                            if(updateHappyPlace > 0){
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK){
            if(requestCode == GALLERY){
                if(data != null){
                    val contentURI = data.data
                    try{
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)

                        Log.e("Saved Image: ", "Path :: $saveImageToInternalStorage")

                        binding?.imgPlaceImage?.setImageBitmap(selectedImageBitmap)
                    }catch (e: IOException){
                        e.printStackTrace()
                        Toast.makeText(
                            this@AddHappyPlaceActivity,
                            "Failed to load the Image from Gallery ",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }else if(requestCode == CAMERA){
                val thumbNail: Bitmap = data!!.extras!!.get("data") as Bitmap

                saveImageToInternalStorage = saveImageToInternalStorage(thumbNail)

                Log.e("Saved Image: ", "Path :: $saveImageToInternalStorage")

                binding?.imgPlaceImage?.setImageBitmap(thumbNail)

            }else if(requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){
                val place: Place = Autocomplete.getPlaceFromIntent(data!!)

                binding?.txtLocation?.setText(place.address)
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude
            }

        }
    }

    private fun takePhotoFromCamera(){
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){
                    val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(galleryIntent, CAMERA)
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>,
                token: PermissionToken) {
                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }

    private fun choosePhotoFromGallery(){
        Dexter.withActivity(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if(report!!.areAllPermissionsGranted()){
                    val galleryIntent = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }
            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>,
                token: PermissionToken) {

                showRationalDialogForPermissions()
            }
        }).onSameThread().check()
    }
    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this).setMessage("It looks you have turned off permission " +
                "required for this feature. It can be enabled under " +
                "the Applications Settings")
            .setPositiveButton("GO TO SETTINGS")
            { _, _ ->
                try{
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)

                }catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun updateDateInView(){
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())

        binding?.txtDate?.setText(sdf.format(cal.time).toString())
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)

        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

            stream.flush()
            stream.close()

        }catch (e: IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }
    companion object{
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }
}
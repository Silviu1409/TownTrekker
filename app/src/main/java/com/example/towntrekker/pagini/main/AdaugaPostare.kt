package com.example.towntrekker.pagini.main

import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.towntrekker.ActivityMain
import com.example.towntrekker.R
import com.example.towntrekker.pagini.main.adaugare_media_recyclerview.AdaugaImaginiAdapter
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.R as PlacesR


class AdaugaPostare: DialogFragment() {
    private var numeLocatie = ""
    private var adresaLocatie = ""

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdaugaImaginiAdapter

    private lateinit var mainActivityContext: ActivityMain
    private lateinit var autocompleteFragment: AutocompleteSupportFragment

    private lateinit var descriereRef: AppCompatEditText
    private lateinit var refButonAdaugare: Button
    private lateinit var refPreviewMedia: RelativeLayout

    private val limitaSizeFisier = 10 * 1024 * 1024L    // limită de 10MB pentru un fișier

    private var descFisiere = mutableListOf<Uri>()


    @Suppress("BlockingMethodInNonBlockingContext")
    private val getDocumentContent =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if (uris.isNotEmpty()) {
                if (uris.size + descFisiere.size > 5){
                    Log.e(mainActivityContext.getTag(), "Poți selecta maxim 5 fișiere!")
                    Toast.makeText(context, "Poți selecta maxim 5 fișiere!", Toast.LENGTH_SHORT).show()
                }
                if (descFisiere.size < 5){
                    @Suppress("CanBeVal") var descFisiereNoi = mutableListOf<Uri>()

                    for (uri in uris) {
                        if (descFisiere.size < 5){
                            val size = requireActivity().contentResolver.openInputStream(uri)?.available()?.toLong() ?: 0L

                            if (size > limitaSizeFisier) {
                                Log.e(mainActivityContext.getTag(), "Fișier prea mare (> 10MB)")
                                Toast.makeText(context, "Unul dintre fișiere depășește limita!", Toast.LENGTH_SHORT).show()

                                return@registerForActivityResult
                            } else {
                                if (uri !in descFisiere) {
                                    descFisiereNoi.add(uri)
                                    descFisiere.add(uri)
                                }
                            }
                        }
                    }

                    if (descFisiereNoi.isNotEmpty()) {
                        adaugaImagini(descFisiereNoi)
                    }
                }
            }
        }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val view = layoutInflater.inflate(R.layout.adaugare_postare, null, false)

        recyclerView = view.findViewById(R.id.recyclerview_media)
        adapter = AdaugaImaginiAdapter(context, this, arrayListOf())
        recyclerView.adapter = adapter

        mainActivityContext = activity as ActivityMain

        descriereRef = view.findViewById(R.id.adaugare_descriere)
        refButonAdaugare = view.findViewById(R.id.adaugare_media)
        refPreviewMedia = view.findViewById(R.id.previzualizare_media)

        autocompleteFragment = childFragmentManager.findFragmentById(R.id.fragment_locatie_autocompletare) as AutocompleteSupportFragment
        autocompleteFragment.setCountries("RO")
        autocompleteFragment.setHint(resources.getString(R.string.adaugare_postare_locatie_hint))
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS))

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i(mainActivityContext.getTag(), "Place: " + place.name + ", " + place.address)

                numeLocatie = place.name?.toString() ?: ""
                adresaLocatie = place.address?.toString() ?: ""
            }

            override fun onError(status: Status) {
                Log.i(mainActivityContext.getTag(), "A apărut o eroare: $status")
            }
        })

        descriereRef.addTextChangedListener(object : TextWatcher {
            var numarLinii = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            @Suppress("KotlinConstantConditions", "UNUSED_VALUE")
            override fun afterTextChanged(s: Editable?) {
                val bioNou = s.toString()
                var ignoraSchimbari = false

                numarLinii = descriereRef.lineCount

                if (numarLinii > 10 && !ignoraSchimbari) {
                    val bioVechi = bioNou.substring(0, (bioNou.length) - 1)

                    ignoraSchimbari = true
                    descriereRef.removeTextChangedListener(this)

                    descriereRef.setText(bioVechi)
                    descriereRef.setSelection(bioVechi.length)

                    descriereRef.addTextChangedListener(this)
                    ignoraSchimbari = false
                }
            }
        })

        refButonAdaugare.setOnClickListener {
            getDocumentContent.launch(arrayOf("image/*", "video/*"))
        }

        view.findViewById<ImageButton>(R.id.adaugare_media_plus).setOnClickListener{
            getDocumentContent.launch(arrayOf("image/*", "video/*"))
        }

        builder.setPositiveButton("Postează", null)
        builder.setNegativeButton("Anulează", null)

        builder.setView(view)

        return builder.create()
    }

    override fun onResume() {
        super.onResume()

        val d = dialog as AlertDialog

        if (dialog != null){
            val negativeButton = d.getButton(Dialog.BUTTON_NEGATIVE)

            negativeButton.setOnClickListener {
                d.dismiss()
            }

            val positiveButton = d.getButton(Dialog.BUTTON_POSITIVE)

            positiveButton.setOnClickListener {
                Log.d(mainActivityContext.getTag(), numeLocatie + adresaLocatie)

                if (numeLocatie != "" || adresaLocatie != "") {
                    if (descriereRef.text.toString() != "" || descFisiere.size > 0) {
                        val datePostare = hashMapOf(
                            "user" to mainActivityContext.getUser()!!.uid,
                            "numeLocatie" to numeLocatie,
                            "adresaLocatie" to adresaLocatie,
                            "descriere" to descriereRef.text.toString(),
                            "media" to (descFisiere.size != 0),
                            "aprecieri" to 0
                        )

                        val refDocNou = mainActivityContext.getDB().collection("postari").document()

                        refDocNou.set(datePostare)
                            .addOnSuccessListener {
                                Log.d(mainActivityContext.getTag(), "Datalii despre postare adăugate.")

                                var incarcareFisiere = true
                                val refPostare = mainActivityContext.getStorage().child(refDocNou.id)

                                for ((idx, uri) in descFisiere.withIndex()){
                                    val extensie = MimeTypeMap.getSingleton().getExtensionFromMimeType(
                                        mainActivityContext.contentResolver.getType(uri)).toString().lowercase()

                                    val refMedia = refPostare.child("fisier${idx+1}.$extensie")
                                    val uploadTask = refMedia.putFile(uri)

                                    uploadTask
                                        .addOnSuccessListener {
                                            Log.d(mainActivityContext.getTag(), "Fișier incărcat!")
                                        }
                                        .addOnFailureListener { e ->
                                            incarcareFisiere = false
                                            Log.e(mainActivityContext.getErrTag(), "Eroare încărcare fișier: ${e.message}")
                                        }
                                }

                                if (!incarcareFisiere){
                                    Log.w(mainActivityContext.getTag(), "O parte din fișiere s-au incărcat!")
                                    Toast.makeText(activity, "O parte din fișiere s-au incărcat!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Log.i(mainActivityContext.getTag(), "Postare făcută cu succes!")
                                    Toast.makeText(activity, "Postare făcută cu succes!", Toast.LENGTH_SHORT).show()
                                }

                                d.dismiss()
                            }
                            .addOnFailureListener { e ->
                                Log.e(mainActivityContext.getErrTag(), "Eroare la crearea postării: ${e.message}")
                                Toast.makeText(activity, "Eroare la crearea postării.\nMai încearcă!", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }

            val searchView = autocompleteFragment.view?.findViewById<EditText>(PlacesR.id.places_autocomplete_search_input)

            searchView?.addTextChangedListener(object: TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    if(s.toString().isEmpty()){
                        numeLocatie = ""
                        adresaLocatie = ""

                        Log.d(mainActivityContext.getTag(), "Text căutare locație curățat!")
                    }
                }
            })

            searchView?.setTextColor(searchView.hintTextColors)
        }
    }

    private fun adaugaImagini(descFisiere: MutableList<Uri>){
        for (item in descFisiere){
            if  (adapter.itemCount == 0){
                refButonAdaugare.visibility = View.GONE
                refPreviewMedia.visibility = View.VISIBLE
            }

            adapter.adaugaItem(item)
        }
    }

    fun ascundePreview(){
        refButonAdaugare.visibility = View.VISIBLE
        refPreviewMedia.visibility = View.GONE
    }

    fun stergereDescFisier(descFisier: Uri){
        descFisiere.remove(descFisier)
    }
}
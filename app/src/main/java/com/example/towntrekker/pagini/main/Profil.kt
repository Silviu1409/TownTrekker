package com.example.towntrekker.pagini.main

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.towntrekker.ActivityMain
import com.example.towntrekker.ActivityAuth
import com.example.towntrekker.databinding.PaginaProfilBinding
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream
import java.io.File


class Profil : Fragment() {
    private var _binding: PaginaProfilBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainActivityContext: ActivityMain

    private lateinit var preiaProzaProfil: ActivityResultLauncher<String>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PaginaProfilBinding.inflate(inflater, container, false)

        mainActivityContext = activity as ActivityMain

        preiaProzaProfil = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { schimbaPozaProfil(it) }
        }

        val tempLocalFile = File(requireContext().cacheDir, "icon.jpg")

        val imageRef = mainActivityContext.getFolderRef().child("icon.jpg")

        imageRef.metadata
            .addOnSuccessListener {  metadata ->

                if(tempLocalFile.exists() && tempLocalFile.length() == metadata.sizeBytes){
                    Log.d(mainActivityContext.getTag(), "Poza de profil este deja în cache!")

                    Glide.with(this)
                        .asBitmap()
                        .load(tempLocalFile)
                        .circleCrop()
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(binding.ProfilIcon)
                } else {
                    Log.w(mainActivityContext.getTag(), "Poza de profil nu este în cache")

                    Glide.with(this)
                        .asBitmap()
                        .load(imageRef)
                        .circleCrop()
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(binding.ProfilIcon)

                    imageRef.getFile(tempLocalFile)
                        .addOnSuccessListener {
                            Log.d(mainActivityContext.getTag(), "Poza de profil salvată în cache!!")
                        }
                        .addOnFailureListener { e ->
                            Log.e(mainActivityContext.getErrTag(), "Salvarea pozei de profil în cache a eșuat: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                if (e.message == "Object does not exist at location."){
                    Log.e(mainActivityContext.getErrTag(), "User-ul nu are un icon setat: ${e.message}")
                }
                else {
                    Log.e(mainActivityContext.getErrTag(), "Preluarea datelor despre icon a eșuat: ${e.message}")
                }
            }

        binding.ProfilAlias.setText(mainActivityContext.getUser()!!.alias)
        binding.ProfilEmail.text = binding.ProfilEmail.text.toString().plus(" ").plus(mainActivityContext.getUser()!!.email)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.ProfilIcon.setOnClickListener {
            preiaProzaProfil.launch("image/*")
        }

        binding.ProfilAlias.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val newUserAlias = binding.ProfilAlias.text.toString()
                mainActivityContext.getUser()!!.alias = newUserAlias

                mainActivityContext.getDB().collection("useri")
                    .document(mainActivityContext.getUser()!!.uid)
                    .update("alias", newUserAlias)
                    .addOnSuccessListener {
                        Log.d(mainActivityContext.getTag(), "Am actualizat alias-ul!")
                        Toast.makeText(context, "Alias actualizat!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {  e ->
                        Log.e(mainActivityContext.getErrTag(), "Could not update user alias: ${e.message}")
                        Toast.makeText(context, "Cannot update alias!", Toast.LENGTH_SHORT).show()
                    }

                mainActivityContext.ascundereTastatura()

                true
            }
            else {
                false
            }
        }

        binding.ProfilLogout.setOnClickListener {
            mainActivityContext.stergeSharedprefUser()
            FirebaseAuth.getInstance().signOut()

            Log.d(mainActivityContext.getTag(), "Delogare făcută cu succes.")
            Toast.makeText(activity, "Delogare făcută cu succes!", Toast.LENGTH_SHORT).show()


            val intent = Intent(activity, ActivityAuth::class.java)
            intent.putExtra("logout", true)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        binding.ProfilAlias.setText(mainActivityContext.getUser()!!.alias)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun schimbaPozaProfil(uri: Uri) {
        val storageRef = mainActivityContext.getFolderRef().child("icon.jpg")

        requireActivity().contentResolver.openInputStream(uri)?.use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 75, 75, false)

            val baos = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imgData = baos.toByteArray()

            storageRef.putBytes(imgData)
                .addOnSuccessListener {
                    Glide.with(this)
                        .load(storageRef)
                        .transform(CircleCrop())
                        .into(binding.ProfilIcon)
                }
                .addOnFailureListener { e ->
                    Log.e(mainActivityContext.getErrTag(), "Eroare la încărcarea imaginii: ${e.message}")
                }
        }
    }
}
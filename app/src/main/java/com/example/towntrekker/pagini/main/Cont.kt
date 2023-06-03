package com.example.towntrekker.pagini.main

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.towntrekker.ActivityAuth
import com.example.towntrekker.ActivityMain
import com.example.towntrekker.R
import com.example.towntrekker.databinding.PaginaContBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import java.io.ByteArrayOutputStream


class Cont : Fragment() {
    private var _binding: PaginaContBinding? = null
    private val binding get() = _binding!!

    private lateinit var mainActivityContext: ActivityMain

    private lateinit var preiaProzaCont: ActivityResultLauncher<String>

    private var parolaAfisata: Boolean = false

    private lateinit var layoutParamsCont: FrameLayout.LayoutParams


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = PaginaContBinding.inflate(inflater, container, false)

        mainActivityContext = activity as ActivityMain

        preiaProzaCont = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { schimbaPozaCont(it) }
        }

        if (mainActivityContext.getUserIconFile().exists()) {
            Glide.with(this)
                .asBitmap()
                .load(mainActivityContext.getUserIconFile())
                .circleCrop()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.ContIcon)
        }

        binding.ContAlias.setText(mainActivityContext.getUser()!!.alias)
        binding.ContEmailUser.text = mainActivityContext.getUser()!!.email

        binding.vizualizareUrmareste.text = binding.vizualizareUrmareste.text.toString().plus(numarTransform(mainActivityContext.getUser()!!.urmareste.size))
        binding.vizualizareUrmaritori.text = binding.vizualizareUrmaritori.text.toString().plus(numarTransform(mainActivityContext.getUser()!!.urmaritori.size))

        binding.ContUserBio.setText(mainActivityContext.getUser()!!.bio)
        binding.ContUserBio.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.ContUserBio.setRawInputType(InputType.TYPE_CLASS_TEXT)

        if (mainActivityContext.getUser()!!.parola == ""){
            binding.ContParola.visibility = View.GONE
        }
        else {
            binding.ContParolaEdit.setText(mainActivityContext.getUser()!!.parola)
        }

        layoutParamsCont = binding.Cont.layoutParams as FrameLayout.LayoutParams

        view?.rootView?.setOnApplyWindowInsetsListener { _, insets ->
            val tastaturaDeschisa = insets.isVisible(WindowInsets.Type.ime())

            if (!tastaturaDeschisa) {
                binding.Cont.layoutParams = layoutParamsCont
            }

            insets
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ContIcon.setOnClickListener {
            preiaProzaCont.launch("image/*")
        }

        binding.ContAlias.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val newUserAlias = binding.ContAlias.text.toString()

                if (newUserAlias != mainActivityContext.getUser()!!.alias) {
                    mainActivityContext.getUser()!!.alias = newUserAlias
                    mainActivityContext.getSharedPrefsUser().edit().putString("alias", newUserAlias).apply()

                    mainActivityContext.getDB().collection("useri")
                        .document(mainActivityContext.getUser()!!.uid)
                        .update("alias", newUserAlias)
                        .addOnSuccessListener {
                            Log.d(mainActivityContext.getTag(), "Am actualizat alias-ul!")
                            Toast.makeText(context, "Alias actualizat!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e(mainActivityContext.getErrTag(), "Nu s-a putut actualiza alias-ul: ${e.message}")
                            Toast.makeText(context, "Nu am putut actualiza alias-ul!", Toast.LENGTH_SHORT).show()
                        }
                }

                mainActivityContext.ascundereTastatura()

                binding.ContAlias.clearFocus()

                true
            }
            else {
                false
            }
        }

        binding.ContAlias.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val layoutNouParamsCont = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                )

                binding.Cont.layoutParams = layoutNouParamsCont
            } else {
                binding.Cont.layoutParams = layoutParamsCont
            }
        }

        if (mainActivityContext.getUser()!!.parola != "") {
            binding.ContParolaEdit.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val newParola = binding.ContParolaEdit.text.toString()

                    if (newParola != mainActivityContext.getUser()!!.parola){
                        mainActivityContext.getUser()!!.parola = newParola
                        mainActivityContext.getSharedPrefsUser().edit().putString("parola", newParola).apply()

                        mainActivityContext.getDB().collection("useri")
                            .document(mainActivityContext.getUser()!!.uid)
                            .update("parola", newParola)
                            .addOnSuccessListener {
                                mainActivityContext.getAuth().currentUser!!.updatePassword(newParola)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.d(mainActivityContext.getTag(), "Am actualizat parola!")

                                            Toast.makeText(context, "Parolă actualizată!", Toast.LENGTH_SHORT).show()
                                        }
                                        else {
                                            try {
                                                throw task.exception!!
                                            }
                                            catch (e: FirebaseAuthWeakPasswordException) {
                                                Log.e(mainActivityContext.getErrTag(), "Parola este slabă: ${e.message}")
                                                Log.d("parola", e.reason.toString())
                                                Toast.makeText(activity, "Parola este slabă.\n Încearcă o altă parolă!", Toast.LENGTH_SHORT).show()
                                            }
                                            catch (e: FirebaseAuthRecentLoginRequiredException) {
                                                Log.e(mainActivityContext.getErrTag(), "Utilizatorul a trebuit să se re-autentifice: ${e.message}")

                                                val cred = EmailAuthProvider
                                                    .getCredential(mainActivityContext.getUser()!!.email, mainActivityContext.getUser()!!.parola)

                                                mainActivityContext.getAuth().currentUser!!.reauthenticate(cred)
                                                    .addOnSuccessListener {
                                                        Log.d(mainActivityContext.getTag(), "Utilizatorul s-a re-autentificat cu succes!")

                                                        mainActivityContext.getAuth().currentUser!!.updatePassword(newParola)
                                                            .addOnSuccessListener {
                                                                Log.d(mainActivityContext.getTag(), "Parolă actualizată!")

                                                                Toast.makeText(context, "Parolă actualizată!", Toast.LENGTH_SHORT).show()
                                                            }
                                                            .addOnFailureListener { e ->
                                                                Log.e(mainActivityContext.getErrTag(), "Nu s-a putut actualiza parola: ${e.message}")
                                                                Toast.makeText(context, "Nu am putut actualiza parola!", Toast.LENGTH_SHORT).show()
                                                            }
                                                    }
                                                    .addOnFailureListener { e2 ->
                                                        Log.e(mainActivityContext.getErrTag(), "Eroare la re-autentificare: ${e2.message}")
                                                        Toast.makeText(activity, "Schimbare parolă eșuată. Mai încearcă", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                            catch (e: Exception) {
                                                Log.e(mainActivityContext.getErrTag(), "Eroare la actualizarea parolei: ${e.message}")
                                                Toast.makeText(activity, "Schimbare parolă eșuată. Mai încearcă", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }

                            }
                            .addOnFailureListener { e ->
                                Log.e(mainActivityContext.getErrTag(), "Nu s-a putut actualiza parola: ${e.message}")
                                Toast.makeText(context, "Nu am putut actualiza parola!", Toast.LENGTH_SHORT).show()
                            }
                    }

                    mainActivityContext.ascundereTastatura()

                    binding.ContParolaEdit.clearFocus()

                    true
                } else {
                    false
                }
            }

            binding.ContParolaEdit.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    val layoutNouParamsCont = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                    )

                    binding.Cont.layoutParams = layoutNouParamsCont
                } else {
                    binding.Cont.layoutParams = layoutParamsCont
                }
            }

            binding.ContParolaToggle.setOnClickListener {
                if (!parolaAfisata) {
                    binding.ContParolaEdit.transformationMethod = null
                    parolaAfisata = true
                    binding.ContParolaToggle.setImageResource(R.drawable.icon_vis_off)
                } else {
                    binding.ContParolaEdit.transformationMethod = PasswordTransformationMethod()
                    parolaAfisata = false
                    binding.ContParolaToggle.setImageResource(R.drawable.icon_vis_on)
                }
            }

            binding.ContUserBio.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val newBio = binding.ContUserBio.text.toString()

                    if (newBio != mainActivityContext.getUser()!!.bio) {
                        mainActivityContext.getUser()!!.bio = newBio
                        mainActivityContext.getSharedPrefsUser().edit().putString("bio", newBio).apply()

                        mainActivityContext.getDB().collection("useri")
                            .document(mainActivityContext.getUser()!!.uid)
                            .update("bio", newBio)
                            .addOnSuccessListener {
                                Log.d(mainActivityContext.getTag(), "Am actualizat bio-ul!")
                                Toast.makeText(context, "Bio actualizat!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e(mainActivityContext.getErrTag(), "Nu s-a putut actualiza bio-ul: ${e.message}")
                                Toast.makeText(context, "Nu am putut actualiza bio-ul!", Toast.LENGTH_SHORT).show()
                            }
                    }

                    mainActivityContext.ascundereTastatura()

                    binding.ContUserBio.clearFocus()

                    true
                }
                else {
                    false
                }
            }
            
            binding.ContUserBio.addTextChangedListener(object : TextWatcher {
                var numarLinii = 0

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                @Suppress("KotlinConstantConditions", "UNUSED_VALUE")
                override fun afterTextChanged(s: Editable?) {
                    val bioNou = s.toString()
                    var ignoraSchimbari = false

                    numarLinii = binding.ContUserBio.lineCount

                    if (numarLinii > 7 && !ignoraSchimbari) {
                        val bioVechi = bioNou.substring(0, (bioNou.length) - 1)

                        ignoraSchimbari = true
                        binding.ContUserBio.removeTextChangedListener(this)

                        binding.ContUserBio.setText(bioVechi)
                        binding.ContUserBio.setSelection(bioVechi.length)

                        binding.ContUserBio.addTextChangedListener(this)
                        ignoraSchimbari = false
                    }
                }
            })

            binding.ContUserBio.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    val layoutNouParamsCont = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                    )

                    binding.Cont.layoutParams = layoutNouParamsCont
                } else {
                    binding.Cont.layoutParams = layoutParamsCont
                }
            }
        }

        binding.ContLogout.setOnClickListener {
            mainActivityContext.stergeSharedPrefsUser()
            mainActivityContext.deleteUserIconFile()
            FirebaseAuth.getInstance().signOut()

            Log.d(mainActivityContext.getTag(), "Delogare făcută cu succes.")
            Toast.makeText(activity, "Delogare făcută cu succes!", Toast.LENGTH_SHORT).show()

            val intent = Intent(activity, ActivityAuth::class.java)
            intent.putExtra("logout", true)
            startActivity(intent)
        }

        val postariLiveData = mainActivityContext.postari
        postariLiveData.observe(viewLifecycleOwner) { listaPostari ->
            val postariUser = listaPostari.filter { it.user == mainActivityContext.getUser()!!.uid }

            binding.vizualizarePostari.text = mainActivityContext.getString(R.string.vizualizare_postari_cont).plus(numarTransform(postariUser.size))

            if (postariUser.isNotEmpty()){
                binding.vizualizarePostariCard.setOnClickListener {
                    val docVizUser = mainActivityContext.getSharedPreferences("vizUser", Context.MODE_PRIVATE)

                    docVizUser.edit().putString("refUser", mainActivityContext.getUser()!!.uid).apply()

                    val dialog = VizualizarePostariUser()
                    dialog.show(mainActivityContext.supportFragmentManager, "Vizualizează postări user")
                }
            }
        }

        if (mainActivityContext.getUser()!!.urmareste.isNotEmpty()) {
            binding.vizualizareUrmaresteCard.setOnClickListener {
                val docVizUser = mainActivityContext.getSharedPreferences("vizUser", Context.MODE_PRIVATE)

                docVizUser.edit().putString("refUser", mainActivityContext.getUser()!!.uid).apply()

                val dialog = VizualizareUrmareste()
                dialog.show(mainActivityContext.supportFragmentManager, "Vizualizează urmărește")
            }
        }

        if (mainActivityContext.getUser()!!.urmaritori.isNotEmpty()) {
            binding.vizualizareUrmaritoriCard.setOnClickListener {
                mainActivityContext.getDB().collection("useri").document(mainActivityContext.getUser()!!.uid).get()
                    .addOnSuccessListener { doc ->
                        @Suppress("UNCHECKED_CAST")
                        mainActivityContext.getUser()!!.urmaritori = doc.get("urmaritori") as? List<String> ?: listOf()

                        val docVizUser = mainActivityContext.getSharedPreferences("vizUser", Context.MODE_PRIVATE)

                        docVizUser.edit().putString("refUser", mainActivityContext.getUser()!!.uid).apply()

                        val dialog = VizualizareUrmaritori()
                        dialog.show(mainActivityContext.supportFragmentManager, "Vizualizează urmăritori")
                    }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.ContAlias.setText(mainActivityContext.getUser()!!.alias)
        binding.ContUserBio.setText(mainActivityContext.getUser()!!.bio)

        binding.Cont.layoutParams = layoutParamsCont

        if (mainActivityContext.getUser()!!.parola == ""){
            binding.ContParola.visibility = View.GONE
            binding.ContParolaEditIcon.visibility = View.GONE
        }
        else {
            binding.ContParolaEdit.setText(mainActivityContext.getUser()!!.parola)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun schimbaPozaCont(uri: Uri) {
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
                        .into(binding.ContIcon)
                }
                .addOnFailureListener { e ->
                    Log.e(mainActivityContext.getErrTag(), "Eroare la încărcarea imaginii: ${e.message}")
                }
        }
    }

    private fun numarTransform(numar: Int): String{
        return when {
            numar < 1000 -> numar.toString()
            numar < 1000000 -> String.format("%.1fK", numar / 1000)
            else -> String.format("%.1fM", numar / 1000000)
        }
    }
}
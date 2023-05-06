package com.example.towntrekker.pagini.main

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.towntrekker.ActivityMain
import com.example.towntrekker.R


class VizualizareDetaliiUser: DialogFragment() {
    private lateinit var mainActivityContext: ActivityMain
    private lateinit var refUser: String

    private lateinit var vizualizarePostariCard: CardView
    private lateinit var vizualizareUrmaresteCard: CardView
    private lateinit var vizualizareUrmaritoriCard: CardView


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val view = layoutInflater.inflate(R.layout.vizualizare_detalii_user, null, false)

        mainActivityContext = requireActivity() as ActivityMain
        val sharedPreferences = requireContext().getSharedPreferences("vizUser", Context.MODE_PRIVATE)
        refUser = sharedPreferences?.getString("refUser", "") ?: ""

        val profilIcon: AppCompatImageButton = view.findViewById(R.id.Profil_Icon)
        val profilAlias: AppCompatTextView = view.findViewById(R.id.Profil_Alias)
        val vizualizareUrmareste: AppCompatTextView = view.findViewById(R.id.vizualizare_urmareste)
        val vizualizareUrmaritori: AppCompatTextView = view.findViewById(R.id.vizualizare_urmaritori)
        val profilBio: AppCompatTextView = view.findViewById(R.id.Profil_Bio)

        vizualizarePostariCard = view.findViewById(R.id.vizualizare_postari_card)
        vizualizareUrmaresteCard = view.findViewById(R.id.vizualizare_urmareste_card)
        vizualizareUrmaritoriCard = view.findViewById(R.id.vizualizare_urmaritori_card)

        mainActivityContext.getDB().collection("useri")
            .document(refUser)
            .get()
            .addOnSuccessListener {document ->
                @Suppress("UNCHECKED_CAST")
                if (document != null && document.exists()) {

                    val aliasUser = document.getString("alias") ?: ""
                    val urmaritoriUser = document.get("urmaritori") as? List<String> ?: listOf()
                    val urmaresteUser = document.get("urmareste") as? List<String> ?: listOf()
                    val bioUser = document.getString("bio") ?: ""

                    val userIconRef = mainActivityContext.getStorage().child("useri").child(refUser).child("icon.jpg")

                    userIconRef.metadata.addOnSuccessListener {
                        Glide.with(mainActivityContext)
                            .load(userIconRef)
                            .override(35, 35)
                            .centerCrop()
                            .into(profilIcon)
                    }

                    profilAlias.text = aliasUser
                    vizualizareUrmareste.text = vizualizareUrmareste.text.toString().plus(numarTransform(urmaresteUser.size))
                    vizualizareUrmaritori.text = vizualizareUrmaritori.text.toString().plus(numarTransform(urmaritoriUser.size))
                    profilBio.text = bioUser
                }
            }

        builder.setView(view)

        return builder.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.vizualizare_detalii_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postariLiveData = mainActivityContext.postari
        postariLiveData.observe(viewLifecycleOwner) { listaPostari ->
            val postariUser = listaPostari.filter { it.user == refUser }

            if (postariUser.isNotEmpty()){
                vizualizarePostariCard.post {
                    vizualizarePostariCard.visibility = View.VISIBLE

                    vizualizarePostariCard.setOnClickListener {
                        val dialog = VizualizarePostariUser()
                        dialog.show(mainActivityContext.supportFragmentManager, "Vizualizează postări user")
                    }
                }
            }
        }

        if (mainActivityContext.getUser()!!.urmareste.isNotEmpty()) {
            vizualizareUrmaresteCard.post {
                vizualizareUrmaresteCard.visibility = View.VISIBLE

                vizualizareUrmaresteCard.setOnClickListener {
                    val dialog = VizualizareUrmareste()
                    dialog.show(mainActivityContext.supportFragmentManager, "Vizualizează urmărește")
                }
            }
        }
        if (mainActivityContext.getUser()!!.urmaritori.isNotEmpty()) {
            vizualizareUrmaritoriCard.post {
                vizualizareUrmaritoriCard.visibility = View.VISIBLE

                vizualizareUrmaritoriCard.setOnClickListener {
                    mainActivityContext.getDB().collection("useri").document(mainActivityContext.getUser()!!.uid).get()
                        .addOnSuccessListener { doc ->
                            @Suppress("UNCHECKED_CAST")
                            mainActivityContext.getUser()!!.urmaritori = doc.get("urmaritori") as? List<String> ?: listOf()

                            val dialog = VizualizareUrmaritori()
                            dialog.show(mainActivityContext.supportFragmentManager, "Vizualizează urmăritori")
                        }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.65).toInt()
        dialog!!.window!!.setLayout(width, height)
    }

    private fun numarTransform(numar: Int): String{
        return when {
            numar < 1000 -> numar.toString()
            numar < 1000000 -> String.format("%.1fK", numar / 1000)
            else -> String.format("%.1fM", numar / 1000000)
        }
    }
}
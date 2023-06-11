package com.example.towntrekker.pagini.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.towntrekker.ActivityMain
import com.example.towntrekker.databinding.PaginaPostariBinding
import com.example.towntrekker.datatypes.Postare
import com.example.towntrekker.pagini.main.postari_feed_recyclerview.PostariFeedAdapter
import kotlin.random.Random


class Postari : Fragment() {
    private var _binding: PaginaPostariBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostariFeedAdapter

    private lateinit var listaPostariFiltrate: MutableList<Postare>
    private var listaPostariRamase: MutableList<Postare> = mutableListOf()

    private var procentCategoriiInteresActualizat: MutableMap<String, Double> = mutableMapOf()
    private var utilizatoriSferaInteres:  MutableList<String> = mutableListOf()

    private val incrementPostari = 10
    private var procentCategoriiInteres = 0.0
    private var procentPostariSferaInteres = 0.0
    private var procentAleator = 0.0

    private lateinit var mainActivityContext: ActivityMain

    private val tagTestPostari = "testPostari"


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = PaginaPostariBinding.inflate(inflater, container, false)

        mainActivityContext = context as ActivityMain

        listaPostariFiltrate = mutableListOf()
        procentCategoriiInteres = 0.5
        procentPostariSferaInteres = 0.3
        procentAleator = 0.2

        for ((categorie, probabilitate) in mainActivityContext.procentPostariInteresUser) {
            procentCategoriiInteresActualizat[categorie] = probabilitate
        }

        Log.d(tagTestPostari, procentCategoriiInteresActualizat.toString())

        val utilizatoriSferaInteredLiveData = mainActivityContext.utilizatoriSferaInteres
        utilizatoriSferaInteredLiveData.observe(viewLifecycleOwner) { date ->
            utilizatoriSferaInteres = date.toMutableList()
        }

        Log.d(tagTestPostari, utilizatoriSferaInteres.toString())
        verificaUtilizatoriSferaInteres()

        if (procentCategoriiInteresActualizat.isEmpty()) {
            procentCategoriiInteres = 0.0
            procentPostariSferaInteres *= 2
            procentAleator *= 2
        }

        recyclerView = binding.postariFeed
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = PostariFeedAdapter(context, arrayListOf())
        recyclerView.adapter = adapter

        val postariLiveData = mainActivityContext.postari
        postariLiveData.observe(viewLifecycleOwner) { date ->
            val postariFiltrate = date.filter { it.user != mainActivityContext.getUser()!!.uid }
            listaPostariRamase = postariFiltrate.toMutableList()
            var listaPostariVeche: List<Postare> = listOf()

            procentCategoriiInteresActualizat = actualizeazaProcenteCategoriiInteres(procentCategoriiInteresActualizat)

            Log.d("testNrPostari", postariFiltrate.size.toString() + ", " + date.size.toString())

            while (listaPostariFiltrate.size != postariFiltrate.size) {
                Log.d(tagTestPostari, "$procentCategoriiInteres, $procentPostariSferaInteres, $procentAleator" )

                if (procentCategoriiInteres != 0.0)
                    preiaPostariCategoriiInteres()

                if (procentPostariSferaInteres != 0.0 && listaPostariFiltrate.size != postariFiltrate.size) {
                    preiaPostariSferaInteres()
                }

                if (listaPostariFiltrate.size != postariFiltrate.size) {
                    preiaAltePostariAleatoare()
                }

                val listaPostariNoi = listaPostariFiltrate.subtract(listaPostariVeche.toSet()).toList()
                Log.d(tagTestPostari, listaPostariNoi.map { it.categorieLocatie }.toString())

                adapter.adaugaPostari(listaPostariNoi)
                recyclerView.adapter = adapter

                listaPostariVeche = listaPostariFiltrate.toSet().toList()

                Log.d(tagTestPostari, listaPostariFiltrate.size.toString())
            }

            Log.d(tagTestPostari, "Refresh postari")

        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // funcția ce actualizează catgeoriile de interes, prin reajustarea ponderilor acestora
    private fun actualizeazaProcenteCategoriiInteres(categoriiProb: MutableMap<String, Double>): MutableMap<String, Double> {
        val iterator = categoriiProb.keys.iterator()

        while (iterator.hasNext()) {
            val categorie = iterator.next()

            Log.d("testPostariCategorii", listaPostariRamase.size.toString())

            if (listaPostariRamase.count { it.categorieLocatie == categorie } == 0) {
                iterator.remove()

                val sumaProbabilitati = categoriiProb.values.sum()

                categoriiProb.forEach { (cat, prob) ->
                    categoriiProb[cat] = prob / sumaProbabilitati
                }
            }
        }

        return categoriiProb
    }

    // funcție ce verifică dacă există postări ale utilizatorilor din sfera de interes
    private fun verificaUtilizatoriSferaInteres() {
        val iterator = utilizatoriSferaInteres.iterator()

        while (iterator.hasNext()) {
            val user = iterator.next()

            val postariRamaseUser = listaPostariRamase.filter { it.user == user }

            if (postariRamaseUser.isEmpty()){
                iterator.remove()
            }
        }
    }

    // funcție ce selectează postări pe baza categoriilor de interes
    private fun preiaPostariCategoriiInteres() {
        repeat((procentCategoriiInteres * incrementPostari).toInt()) {
            if (procentCategoriiInteresActualizat.isEmpty()){
                procentCategoriiInteres = 0.0

                if (procentPostariSferaInteres == 0.0) {
                    procentAleator = 1.0
                }
                else {
                    procentPostariSferaInteres *= 2
                    procentAleator *= 2
                }

                return@repeat
            }

            preiaPostariAleator(procentCategoriiInteresActualizat)

            procentCategoriiInteresActualizat = actualizeazaProcenteCategoriiInteres(procentCategoriiInteresActualizat)
        }
    }

    // funcție ce selectează postări pe baza persoanelor din sfera de interes a utilizatorului
    private fun preiaPostariSferaInteres() {
        repeat((procentPostariSferaInteres * incrementPostari).toInt()) {
            if (utilizatoriSferaInteres.isEmpty()){
                procentPostariSferaInteres = 0.0

                if (procentCategoriiInteres == 0.0) {
                    procentAleator = 1.0
                }
                else {
                    procentCategoriiInteres = 0.7
                    procentAleator = 0.3
                }

                return@repeat
            }

            selecteazaPostareSferaInteres()

            verificaUtilizatoriSferaInteres()
        }
    }

    // funcție care preia o postare în mod aleatoriu, folosind categoriile dintr-un dicționar de probabilități
    private fun preiaPostariAleator(categoriiProb: MutableMap<String, Double>) {
        val categorieAleasa = preiaCategorieAleator(categoriiProb)
        val recomandariCategorieAleasa = listaPostariRamase.filter { it.categorieLocatie == categorieAleasa }
        val recomandareAleatoare = recomandariCategorieAleasa[Random.nextInt(recomandariCategorieAleasa.size)]

        listaPostariFiltrate.add(recomandareAleatoare)
        listaPostariRamase.remove(recomandareAleatoare)
    }

    // funcție care preia o categorie de locație în mod aleatoriu, folosind categoriile dintr-un dicționar de probabilități
    private fun preiaCategorieAleator(categoriiProb: MutableMap<String, Double>): String{
        val valoareAleatoare = Random.nextDouble()

        var probCumulativa = 0.0
        for ((categorie, prob) in categoriiProb) {
            probCumulativa += prob
            if (valoareAleatoare <= probCumulativa) {
                return categorie
            }
        }

        return categoriiProb.keys.last()
    }

    // funcție care alege o postare pe baza utilizatorilor din sfera de interes
    private fun selecteazaPostareSferaInteres() {
        val postariSferaInteres = listaPostariRamase.filter { it.user in utilizatoriSferaInteres }

        if (postariSferaInteres.isNotEmpty()) {
            val indexAleator = Random.nextInt(postariSferaInteres.size)
            val postareAleatoare = postariSferaInteres[indexAleator]

            listaPostariFiltrate.add(postareAleatoare)
            listaPostariRamase.remove(postareAleatoare)

            if (postareAleatoare.categorieLocatie in procentCategoriiInteresActualizat) {
                procentCategoriiInteresActualizat = actualizeazaProcenteCategoriiInteres(procentCategoriiInteresActualizat)
            }
        }
    }

    // funcție care preia postări în mod aleator
    private fun preiaAltePostariAleatoare() {
        repeat((procentAleator * incrementPostari).toInt()) {
            if (listaPostariRamase.isEmpty())
                return@repeat

            val indexPostareAleatoare = Random.nextInt(listaPostariRamase.size)
            val postareAleatoare = listaPostariRamase[indexPostareAleatoare]

            listaPostariFiltrate.add(postareAleatoare)
            listaPostariRamase.removeAt(indexPostareAleatoare)

            if (postareAleatoare.categorieLocatie in procentCategoriiInteresActualizat) {
                procentCategoriiInteresActualizat = actualizeazaProcenteCategoriiInteres(procentCategoriiInteresActualizat)
            }
            if (postareAleatoare.user in utilizatoriSferaInteres) {
                verificaUtilizatoriSferaInteres()
            }
        }
    }
}
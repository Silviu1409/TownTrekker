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
import com.example.towntrekker.databinding.PaginaDescoperaBinding
import com.example.towntrekker.datatypes.Recomandare
import com.example.towntrekker.pagini.main.descopera_recomandari_recyclerview.DescoperaRecomandariAdapter
import kotlin.random.Random


class Descopera : Fragment() {
    private var _binding: PaginaDescoperaBinding? = null

    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DescoperaRecomandariAdapter

    private lateinit var listaRecomandariFiltrate: MutableList<Recomandare>
    private var listaRecomandariRamase: MutableList<Recomandare> = mutableListOf()

    private var procentPostariInteresActualizat: HashMap<String, Double> = hashMapOf()
    private var categoriiPostariUrmaresteActualizat: HashMap<String, Double> = hashMapOf()

    private val incrementRecomandari = 10
    private var procentPostariInteres = 0.0
    private var procentUtilizatoriUrmariti = 0.0
    private var procentAleator = 0.0

    private lateinit var mainActivityContext: ActivityMain

    private val tagTestRecomandari = "testRecomandari"


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = PaginaDescoperaBinding.inflate(inflater, container, false)

        mainActivityContext = context as ActivityMain

        listaRecomandariFiltrate = mutableListOf()
        procentPostariInteres = 0.5
        procentUtilizatoriUrmariti = 0.3
        procentAleator = 0.2

        for ((categorie, probabilitate) in mainActivityContext.procentPostariInteresUser) {
            procentPostariInteresActualizat[categorie] = probabilitate
        }
        for ((categorie, probabilitate) in mainActivityContext.categoriiPostariUrmareste) {
            categoriiPostariUrmaresteActualizat[categorie] = probabilitate
        }

        Log.d(tagTestRecomandari, procentPostariInteresActualizat.toString())

        if (procentPostariInteresActualizat.isEmpty()) {
            procentPostariInteres = 0.0
            procentUtilizatoriUrmariti *= 2
            procentAleator *= 2
        }

        if (categoriiPostariUrmaresteActualizat.isEmpty()){
            procentUtilizatoriUrmariti = 0.0

            if (procentPostariInteres == 0.0) {
                procentAleator = 1.0
            }
            else {
                procentPostariInteres = 0.7
                procentAleator = 0.3
            }
        }

        recyclerView = binding.descoperaRecomandari
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = DescoperaRecomandariAdapter(context, arrayListOf())
        recyclerView.adapter = adapter

        val recomandariLiveData = mainActivityContext.recomandari
        recomandariLiveData.observe(viewLifecycleOwner) { date ->
            listaRecomandariRamase = date.toMutableList()
            var listaRecomandariVeche: List<Recomandare> = listOf()

            while (listaRecomandariFiltrate.size != date.size) {

                if (procentPostariInteres != 0.0)
                    preiaRecomandariPostariInteres()

                if (procentUtilizatoriUrmariti != 0.0 && listaRecomandariFiltrate.size != date.size)
                    preiaRecomandariUtilizatoriUrmariti()

                if (listaRecomandariFiltrate.size != date.size)
                    preiaAlteRecomandariAleatoare()

                val listaRecomandariNoi = listaRecomandariFiltrate.subtract(listaRecomandariVeche.toSet()).toList()
                Log.d(tagTestRecomandari, listaRecomandariNoi.map { it.categorie }.toString())

                adapter.adaugaRecomandari(listaRecomandariNoi)
                recyclerView.adapter = adapter

                listaRecomandariVeche = listaRecomandariFiltrate.toSet().toList()

                Log.d(tagTestRecomandari, listaRecomandariFiltrate.size.toString())
            }

            Log.d(tagTestRecomandari, "Refresh recomandari")
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // funcție care preia recomandări în funcție de postările de interes
    private fun preiaRecomandariPostariInteres() {
        repeat((procentPostariInteres * incrementRecomandari).toInt()) {
            if (procentPostariInteresActualizat.isEmpty()){
                procentPostariInteres = 0.0

                if (procentUtilizatoriUrmariti == 0.0) {
                    procentAleator = 1.0
                }
                else {
                    procentUtilizatoriUrmariti *= 2
                    procentAleator *= 2
                }

                return@repeat
            }

            preiaRecomandariAleator(procentPostariInteresActualizat)

            procentPostariInteresActualizat = actualizeazaProcenteCategorii(procentPostariInteresActualizat)
        }
    }

    // funcție ce preia recomandări pe baza utilizatorilor urmăriți pe platformă
    private fun preiaRecomandariUtilizatoriUrmariti() {
        repeat((procentUtilizatoriUrmariti * incrementRecomandari).toInt()) {
            if (categoriiPostariUrmaresteActualizat.isEmpty()){
                procentUtilizatoriUrmariti = 0.0

                if (procentPostariInteres == 0.0) {
                    procentAleator = 1.0
                }
                else {
                    procentPostariInteres = 0.7
                    procentAleator = 0.3
                }

                return@repeat
            }

            preiaRecomandariAleator(categoriiPostariUrmaresteActualizat)

            categoriiPostariUrmaresteActualizat = actualizeazaProcenteCategorii(categoriiPostariUrmaresteActualizat)
        }
    }

    // funcție care preia recomandări în mod aleator
    private fun preiaAlteRecomandariAleatoare() {
        repeat((procentAleator * incrementRecomandari).toInt()) {
            if (listaRecomandariRamase.isEmpty())
                return@repeat

            val indexRecomandareAleatoare = Random.nextInt(listaRecomandariRamase.size)
            val recomandareAleatoare = listaRecomandariRamase[indexRecomandareAleatoare]

            listaRecomandariFiltrate.add(recomandareAleatoare)
            listaRecomandariRamase.removeAt(indexRecomandareAleatoare)

            if (recomandareAleatoare.categorie in procentPostariInteresActualizat) {
                procentPostariInteresActualizat = actualizeazaProcenteCategorii(procentPostariInteresActualizat)
            }
            if (recomandareAleatoare.categorie in categoriiPostariUrmaresteActualizat) {
                categoriiPostariUrmaresteActualizat = actualizeazaProcenteCategorii(categoriiPostariUrmaresteActualizat)
            }
        }
    }

    // funcția ce actualizează catgeoriile de interes, prin reajustarea ponderilor acestora
    private fun actualizeazaProcenteCategorii(categoriiProb: HashMap<String, Double>): HashMap<String, Double> {
        val iterator = categoriiProb.keys.iterator()

        while (iterator.hasNext()) {
            val categorie = iterator.next()

            if (listaRecomandariRamase.count { it.categorie == categorie } == 0) {
               iterator.remove()

                val sumaProbabilitati = categoriiProb.values.sum()

                categoriiProb.forEach { (cat, prob) ->
                    categoriiProb[cat] = prob / sumaProbabilitati
                }
            }
        }

        return categoriiProb
    }

    // funcție care preia o recomandare în mod aleatoriu, folosind categoriile dintr-un dicționar de probabilități
    private fun preiaRecomandariAleator(categoriiProb: HashMap<String, Double>) {
        val categorieAleasa = preiaCategorieAleator(categoriiProb)
        val recomandariCategorieAleasa = listaRecomandariRamase.filter { it.categorie == categorieAleasa }
        if (recomandariCategorieAleasa.isNotEmpty()) {
            val recomandareAleatoare = recomandariCategorieAleasa[Random.nextInt(recomandariCategorieAleasa.size)]

            listaRecomandariFiltrate.add(recomandareAleatoare)
            listaRecomandariRamase.remove(recomandareAleatoare)
        }
    }

    // funcție care preia o categorie de locație în mod aleatoriu, folosind categoriile dintr-un dicționar de probabilități
    private fun preiaCategorieAleator(categoriiProb: HashMap<String, Double>): String{
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
}
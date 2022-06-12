package com.example.glucohealth.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.glucohealth.R
import com.example.glucohealth.database.entity.FavEntity
import com.example.glucohealth.databinding.ActivityProductDetailBinding
import com.example.glucohealth.helper.ViewModelFactory
import com.example.glucohealth.viewmodels.FavViewModel
import com.example.glucohealth.viewmodels.SugarViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var viewModel : SugarViewModel
    private lateinit var favViewModel: FavViewModel
    var sugarConsum = 0

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        val productName = intent.getStringExtra(EXTRA_PRODUCTNAME) ?: ""
        val kalori = intent.getIntExtra(EXTRA_CALORIES, 0)
        val protein = intent.getIntExtra(EXTRA_PROTEIN, 0)
        val lemak = intent.getIntExtra(EXTRA_FAT, 0)
        val takaran = intent.getIntExtra(EXTRA_SERVINGSIZE, 0)
        val garam = intent.getIntExtra(EXTRA_SODIUM, 0)
        val gula = intent.getIntExtra(EXTRA_SUGAR, 0)
        val karbohidrat = intent.getIntExtra(EXTRA_CARBO, 0)
        val imgUrl = intent.getStringExtra(EXTRA_IMGURL) ?: ""
        val productid = intent.getStringExtra(EXTRA_PRODUCTID) ?: ""

        val rumus = (gula / 5.0)
        binding.rbSendok.rating = rumus.toFloat()

        supportActionBar?.title = productName

        viewModel = obtainSugarViewModel(this)
        favViewModel = obtainFavViewModel(this)

        Glide.with(this).load(imgUrl).into(binding.imgProduct)
        binding.tvTakaran.text = getString(R.string.takaran).format(takaran)
        binding.tvBanyakgula.text = getString(R.string.komposisi).format(gula)
        binding.tvBanyakgaram.text = getString(R.string.komposisi).format(garam)
        binding.tvBanyakkalori.text = getString(R.string.komposisi).format(kalori)
        binding.tvBanyakkarbohidrat.text = getString(R.string.komposisi).format(karbohidrat)
        binding.tvBanyaklemak.text = getString(R.string.komposisi).format(lemak)
        binding.tvBanyakprotein.text = getString(R.string.komposisi).format(protein)
        binding.lblTambahan.text = getString(R.string.tambah).format(gula)
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val formatedTime = dateFormat.format(currentDate)

        viewModel.getAllProduct(formatedTime).observe(this){product->
            product.forEach {
                sugarConsum += it.sugar
            }
            setpgProgress(gula + sugarConsum)
        }

        val productFav = FavEntity(productid, productName, imgUrl)

        var isChecked = false
        CoroutineScope(Dispatchers.IO).launch {
            val count = favViewModel.isFavorite(productid)
            withContext(Dispatchers.Main){
                if(count>0){
                    isChecked = true
                    binding.toggleFavorite.isChecked = isChecked
                }else{
                    isChecked = false
                    binding.toggleFavorite.isChecked = isChecked
                }
            }
        }

        binding.toggleFavorite.setOnClickListener {
            isChecked = !isChecked
            if(isChecked){
                favViewModel.insert(productFav)
            }else{
                favViewModel.delete(productid)
            }
            binding.toggleFavorite.isChecked = isChecked
        }

        setContentView(binding.root)
    }

    private fun setpgProgress(sugar: Int){
        binding.pgTvProgress.text = getString(R.string.lblprogressharian).format(sugar)
        val konsumsi = ((sugar / 50.0) * 100.0).toInt()
        binding.pgGulaharian.progress = konsumsi
    }

    companion object{
        const val EXTRA_PRODUCTID = "extra_productid"
        const val EXTRA_PRODUCTNAME = "extra_productname"
        const val EXTRA_CALORIES = "extra_calories"
        const val EXTRA_PROTEIN = "extra_protein"
        const val EXTRA_FAT = "extra_fat"
        const val EXTRA_SERVINGSIZE = "extra_servingsize"
        const val EXTRA_SODIUM = "extra_sodium"
        const val EXTRA_SUGAR = "extra_sugar"
        const val EXTRA_CARBO = "extra_carbo"
        const val EXTRA_IMGURL = "extra_imgurl"
    }

    private fun obtainSugarViewModel(activity: AppCompatActivity): SugarViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory)[SugarViewModel::class.java]
    }

    private fun obtainFavViewModel(activity: AppCompatActivity): FavViewModel {
        val factory = ViewModelFactory.getInstance(activity.application)
        return ViewModelProvider(activity, factory)[FavViewModel::class.java]
    }
}
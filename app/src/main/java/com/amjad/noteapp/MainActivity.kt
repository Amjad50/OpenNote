package com.amjad.noteapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.amjad.noteapp.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<MainActivityBinding>(this, R.layout.main_activity)

        val navController = findNavController(R.id.nav_host)
        setupActionBarWithNavController(navController)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            // overrides the actionbar title on these fragments/destinations
            if (destination.id == R.id.note_edit_fragment) {
                supportActionBar?.title = null
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host).navigateUp() ||
                super.onSupportNavigateUp()
    }
}

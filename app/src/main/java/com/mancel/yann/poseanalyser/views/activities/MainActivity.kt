package com.mancel.yann.poseanalyser.views.activities

import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.mancel.yann.poseanalyser.R

/**
 * Created by Yann MANCEL on 04/09/2020.
 * Name of the project: PoseAnalyser
 * Name of the package: com.mancel.yann.poseanalyser.views.activities
 *
 * A [BaseActivity] subclass.
 */
class MainActivity : BaseActivity() {

    // FIELDS --------------------------------------------------------------------------------------

    private lateinit var _navController: NavController
    private lateinit var _appBarConfiguration: AppBarConfiguration

    // METHODS -------------------------------------------------------------------------------------

    // -- BaseActivity --

    override fun getActivityLayout(): Int = R.layout.activity_main

    override fun doOnCreate() = this.configureActionBarForNavigation()

    // -- Activity --

    override fun onSupportNavigateUp(): Boolean {
        return this._navController.navigateUp(this._appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    // -- Action bar --

    /**
     * Configures the Action bar for the navigation
     */
    private fun configureActionBarForNavigation() {
        // NavController
        this._navController = this.findNavController(R.id.activity_main_NavHostFragment)

        // AppBarConfiguration
        this._appBarConfiguration = AppBarConfiguration(this._navController.graph)

        // Action bar
        this.setupActionBarWithNavController(this._navController, this._appBarConfiguration)
    }
}
package com.lemzeeyyy.sayfe.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.lemzeeyyy.sayfe.R
import com.lemzeeyyy.sayfe.adapters.OnboardingViewPagerAdapter
import com.lemzeeyyy.sayfe.database.SharedPrefs
import com.lemzeeyyy.sayfe.databinding.FragmentOnBoardingBinding

class OnBoardingFragment : Fragment(), MultiplePermissionsListener {
   private lateinit var binding : FragmentOnBoardingBinding
    lateinit var pagerAdapter: OnboardingViewPagerAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentOnBoardingBinding.inflate(inflater, container, false)

        SharedPrefs.init(requireContext())
        if (SharedPrefs.getBoolean("SUB_LAUNCH",false)) {
            findNavController().navigate(OnBoardingFragmentDirections.actionOnBoardingFragmentToNavHome())

        }
        Log.d(" CREATEVIEW CALLED", "onCreateView: CALLED")
        Log.d("CREATEVIEW CALLED", "onCreateView: ${SharedPrefs.getBoolean("SUB_LAUNCH",false)} ")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SharedPrefs.init(requireContext())
        pagerAdapter = OnboardingViewPagerAdapter(requireContext())
        binding.viewpager.adapter = pagerAdapter
        binding.dotsIndicator.attachTo(binding.viewpager)
        binding.viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {


                if(position == 0){
                    onboardFirstPageBtnDesign()
                    binding.contBtn.setOnClickListener {
                        binding.viewpager.setCurrentItem(binding.viewpager.getCurrentItem() + 1)
                    }
                    binding.skipBtn.setOnClickListener {
                        binding.viewpager.setCurrentItem(binding.viewpager.getAdapter()!!.getCount())
                    }
                }

                if (position == 1){
                    onboardFirstPageBtnDesign()
                    binding.contBtn.setOnClickListener {
                        getLocationPermission()
                        if (binding.viewpager.currentItem < binding.viewpager.adapter!!.count)
                            binding.viewpager.setCurrentItem(binding.viewpager.currentItem + 1)
                    }
                    binding.skipBtn.setOnClickListener {
                        getLocationPermission()
                        binding.viewpager.setCurrentItem(binding.viewpager.adapter!!.count)
                    }
                }

                if (position == 2){
                    onboardSecondPageBtnDesign()
                    binding.contBtn.setOnClickListener {
                        getAccessibilityPermission()
                        if (binding.viewpager.currentItem < binding.viewpager.adapter!!.count)
                            binding.viewpager.setCurrentItem(binding.viewpager.currentItem + 1)
                    }
                    binding.skipBtn.setOnClickListener {
                        //getAccessibilityPermission()
                        binding.viewpager.setCurrentItem(binding.viewpager.adapter!!.count)
                    }
                }

                if (position==3){
                    onboardThirdPageBtnDesign()
                    binding.skipBtn.setOnClickListener {
                        findNavController().navigate(R.id.signInFragment)
                    }
                }

            }
            override fun onPageSelected(position: Int) {
                // TODO("Not yet implemented")
            }

            override fun onPageScrollStateChanged(state: Int) {
                // TODO("Not yet implemented")
            }
        })

    }

    private fun getAccessibilityPermission(): Boolean {
        var accessEnabled = 0
        try {
            accessEnabled =
                Settings.Secure.getInt(requireActivity().contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }
        return if (accessEnabled == 0) {
            /** if not construct intent to request permission  */
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            /** request permission via start activity for result  */
            startActivity(intent)
            false
        } else {
            true
        }
    }


    private fun onboardThirdPageBtnDesign() {
        binding.locationRationalImg.visibility = View.INVISIBLE
        binding.dotFrame.visibility = View.VISIBLE
        binding.bottomLayout.visibility = View.VISIBLE
        binding.viewpager.visibility = View.VISIBLE
        binding.contBtn.visibility = View.INVISIBLE
        binding.skipBtn.setBackgroundResource(R.drawable.button_blue_bg)
        binding.skipBtn.setText(getString(R.string.getStarted))
        binding.skipBtn.setTextColor(Color.WHITE)
    }

    private fun onboardSecondPageBtnDesign() {
        binding.locationRationalImg.visibility = View.INVISIBLE
        binding.dotFrame.visibility = View.VISIBLE
        binding.bottomLayout.visibility = View.VISIBLE
        binding.viewpager.visibility = View.VISIBLE
        binding.contBtn.visibility = View.VISIBLE
        binding.skipBtn.visibility = View.VISIBLE
        binding.contBtn.setBackgroundResource(R.drawable.button_blue_bg)
        binding.contBtn.setText(getString(R.string.open_settings))
        binding.contBtn.setTextColor(Color.WHITE)
        binding.skipBtn.setTextColor(resources.getColor(R.color.dark_blue_color))
        binding.skipBtn.setText(getString(R.string.skip))
        binding.skipBtn.setBackgroundResource(R.drawable.button_transparent_bg)
        binding.skipBtn.setTextColor(Color.WHITE)
    }

    private fun onboardFirstPageBtnDesign() {
        binding.locationRationalImg.visibility = View.INVISIBLE
        binding.dotFrame.visibility = View.VISIBLE
        binding.bottomLayout.visibility = View.VISIBLE
        binding.viewpager.visibility = View.VISIBLE
        binding.contBtn.visibility = View.VISIBLE
        binding.skipBtn.visibility = View.VISIBLE
        binding.contBtn.setBackgroundResource(R.drawable.button_blue_bg)
        binding.contBtn.setText(getString(R.string.continueForm))
        binding.contBtn.setTextColor(Color.WHITE)
        binding.skipBtn.setTextColor(resources.getColor(R.color.dark_blue_color))
        binding.skipBtn.setText(getString(R.string.skip))
        binding.skipBtn.setBackgroundResource(R.drawable.button_transparent_bg)
        binding.skipBtn.setTextColor(Color.WHITE)
    }

    private fun rationalView(){
        binding.locationRationalImg.visibility = View.VISIBLE
        binding.dotFrame.visibility = View.INVISIBLE
        binding.bottomLayout.visibility = View.INVISIBLE
        binding.viewpager.visibility = View.INVISIBLE
    }

    private fun getLocationPermission() {
        Dexter.withContext(requireContext())
            .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.SEND_SMS)
            .withListener(this@OnBoardingFragment)
            .check()
    }


    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
        report?.let {
            if(report.areAllPermissionsGranted()){
                Toast.makeText(requireContext(),"All permissions granted",Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPermissionRationaleShouldBeShown(
        p0: MutableList<PermissionRequest>?,
        p1: PermissionToken?
    ) {
       p1?.continuePermissionRequest()
    }

    override fun onStop() {
        super.onStop()
        Log.d("STOP CALLED", "onStop: STOP")
        SharedPrefs.putBoolean("SUB_LAUNCH",true)
        Log.d("STOP CALLED", "onStop: ${SharedPrefs.getBoolean("SUB_LAUNCH",true)}")
    }


}

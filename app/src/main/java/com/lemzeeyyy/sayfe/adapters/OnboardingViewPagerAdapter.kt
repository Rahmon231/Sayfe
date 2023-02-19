package com.lemzeeyyy.sayfe.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.lemzeeyyy.sayfe.R

class OnboardingViewPagerAdapter(val context: Context) : PagerAdapter() {
    var layoutInflater : LayoutInflater? = null

    private val imgArray = arrayOf(
        R.drawable.welcome_img,
        R.drawable.location_permission_image,
        R.drawable.terms,
        R.drawable.progress_img

    )

    private val titleArray = arrayOf(
        R.string.welcome_txt,
        R.string.location_permission_txt,
        R.string.accessibility_permission_text,
        R.string.getStarted

    )

    override fun getCount(): Int {
        return titleArray.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object` as RelativeLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater!!.inflate(R.layout.onboarding_page_layout,container,false)
        val img = view.findViewById<ImageView>(R.id.onboarding_image)

        val titleTxt = view.findViewById<TextView>(R.id.title_text)
        img.setImageResource(imgArray[position])
        titleTxt.setText(titleArray[position])
        container.addView(view)
        return view

    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as RelativeLayout)
    }

}
package com.byagowi.persiancalendar.ui.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.databinding.FragmentSettingsBinding
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.InterfaceCalendarFragment
import com.byagowi.persiancalendar.ui.preferences.locationathan.LocationAthanFragment
import com.byagowi.persiancalendar.ui.preferences.widgetnotification.WidgetNotificationFragment
import com.byagowi.persiancalendar.utils.setupUpNavigation
import com.google.android.material.tabs.TabLayoutMediator

/**
 * @author MEHDI DIMYADI
 * MEHDIMYADI
 */
class PreferencesFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = FragmentSettingsBinding.inflate(inflater, container, false).also { binding ->
        binding.appBar.toolbar.let {
            it.setTitle(R.string.settings)
            it.setupUpNavigation()
        }

        val tabs = listOf(
            R.string.pref_header_interface_calendar to InterfaceCalendarFragment::class.java,
            R.string.pref_header_widget_location to WidgetNotificationFragment::class.java,
            R.string.pref_header_location_athan to LocationAthanFragment::class.java
        )
        binding.viewPager.adapter = object : FragmentStateAdapter(this@PreferencesFragment) {
            override fun getItemCount() = tabs.size
            override fun createFragment(position: Int) = tabs[position].second.newInstance()
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, i ->
            tab.setText(tabs[i].first)
        }.attach()
        val args: PreferencesFragmentArgs by navArgs()
        binding.viewPager.currentItem = args.tab
    }.root
}

val INTERFACE_CALENDAR_TAB = 0
val WIDGET_NOTIFICATION_TAB = 1
val LOCATION_ATHAN_TAB = 2

package com.github.stargrey.secret.ui.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class CollectionFragmentAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    companion object{
        const val HOME_PAGE = 0
        const val NOTES_PAGE = 1
        const val CARD_PAGE = 2
        const val SETTINGS_PAGE = 3
        private val fragmentList = Array<Fragment?>(4){null}
        fun getFragment(position: Int): Fragment? {
            return fragmentList[position]
        }

    }
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {

        return when(position){
            HOME_PAGE -> HomeFragment().also { fragmentList[position] = it }
            NOTES_PAGE -> NotesFragment().also { fragmentList[position] = it }
            CARD_PAGE -> CardsFragment().also { fragmentList[position] = it }
            SETTINGS_PAGE -> SettingFragment().also { fragmentList[position] = it }
            else -> HomeFragment()
        }
    }
}
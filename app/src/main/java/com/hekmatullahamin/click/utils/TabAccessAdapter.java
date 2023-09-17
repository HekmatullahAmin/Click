package com.hekmatullahamin.click.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.hekmatullahamin.click.fragments.ChatFragment;
import com.hekmatullahamin.click.fragments.GroupFragment;
import com.hekmatullahamin.click.fragments.RequestFragment;

public class TabAccessAdapter extends FragmentPagerAdapter {

    private Fragment fragment;
    private int numberOfTabs;
    public TabAccessAdapter(@NonNull FragmentManager fm, int numberOfTabs) {
        super(fm);
        this.numberOfTabs = numberOfTabs;
    }

//    public TabAccessAdapter(@NonNull FragmentManager fm, int behavior) {
//        super(fm, behavior);
//    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                fragment = new ChatFragment();
                break;
            case 1:
                fragment = new GroupFragment();
                break;
            case 2:
                fragment = new RequestFragment();
                break;
            default:
                fragment = null;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Chats";
            case 1:
                return "Groups";
            case 2:
                return "Requests";
            default:
                return null;
        }
    }
}

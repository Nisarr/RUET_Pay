package com.ruet.pay;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.ruet.pay.registration_fragments.FacultyMember;
import com.ruet.pay.registration_fragments.Others;
import com.ruet.pay.registration_fragments.Student;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0 :
                return new Student();
            case 1:
                return new FacultyMember();
            case 2:
                return new Others();
            default:
                return new Student();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

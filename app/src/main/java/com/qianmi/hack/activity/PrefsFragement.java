package com.qianmi.hack.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.qianmi.hack.R;

/**
 * Created by wv on 2015/8/20.
 */
public class PrefsFragement extends Fragment implements View.OnClickListener {

    private Button logout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, null);
        logout = (Button) view.findViewById(R.id.btn_logout);
        logout.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_logout:
                //SPUtils.clear(getActivity());
                //Intent intent = new Intent(PrefsFragement.this.getActivity(), LoginActivity.class);
                //this.startActivity(intent);
                ((TabHostActivity) this.getActivity()).exitApplication(this.getActivity());
                break;
        }
    }
}

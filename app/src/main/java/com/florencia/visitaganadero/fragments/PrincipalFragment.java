package com.florencia.visitaganadero.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.florencia.visitaganadero.R;

public class PrincipalFragment extends Fragment {
    private View view;
    private Toolbar toolbar;

    public PrincipalFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_principal, container, false);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        return view;
    }

    @Override
    public void onResume() {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        String titulo="Inicio" ;
        toolbar.setTitle(titulo);
        toolbar.setTitleTextColor(Color.WHITE);
        super.onResume();
    }

}
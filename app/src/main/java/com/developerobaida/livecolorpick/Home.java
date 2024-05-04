package com.developerobaida.livecolorpick;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.developerobaida.livecolorpick.databinding.ActivityHomeBinding;

public class Home extends AppCompatActivity {
    ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.button.setOnClickListener(v ->{});

        binding.button2.setOnClickListener(v -> startActivity(new Intent(this,MainActivity.class)));
        binding.button3.setOnClickListener(v -> startActivity(new Intent(this, MainActivity2.class)));

    }
}
package com.developerobaida.livecolorpick;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.SeekBar;

import com.developerobaida.livecolorpick.databinding.ActivityMain2Binding;

public class MainActivity2 extends AppCompatActivity {
    ActivityMain2Binding binding;
    int red,green,blue,alpha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.search.setOnClickListener(v -> {
            String hex = binding.input.getText().toString();
            hex = hex.replace("#", "");
            int[] rgb = hexToRgb(hex);

            red = rgb[0];
            green = rgb[1];
            blue = rgb[2];
            alpha = rgb[3];
            updateView(alpha,red,green,blue);
            binding.rCount.setText(String.valueOf(red));
            binding.gCount.setText(String.valueOf(green));
            binding.bCount.setText(String.valueOf(blue));
            binding.aCount.setText(String.valueOf(alpha));

            binding.seekR.setProgress(red);
            binding.seekG.setProgress(green);
            binding.seekB.setProgress(blue);
            binding.seekA.setProgress(alpha);

        });

        binding.seekA.setMax(255);
        binding.seekA.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.aCount.setText(""+progress);
                alpha = progress;
                updateView(progress,red,green,blue);

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.seekR.setMax(255);
        binding.seekR.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.rCount.setText(""+progress);
                red = progress;
                updateView(alpha,progress,green,blue);

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.seekG.setMax(255);
        binding.seekG.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.gCount.setText(""+progress);
                green = progress;
                updateView(alpha,red,progress,blue);

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.seekB.setMax(255);
        binding.seekB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.bCount.setText(""+progress);
                blue = progress;
                updateView(alpha,red,green,progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

    }
    void updateView(int alpha,int r,int g,int b){
        binding.colorView.setBackgroundColor(Color.argb(alpha,r,g,b));
        int color = ((ColorDrawable) binding.colorView.getBackground()).getColor();
        String hex = String.format("#%08X", color);
        binding.input.setText("Hex = "+hex+" \nARGB = ("+alpha+","+red+","+green+","+blue+")");
    }
    public static int[] hexToRgb(String hexCode) {

        int[] argb = new int[4];

        argb[0] = Integer.parseInt(hexCode.substring(0, 2), 16);
        argb[1] = Integer.parseInt(hexCode.substring(2, 4), 16);
        argb[2] = Integer.parseInt(hexCode.substring(4, 6), 16);
        argb[3] = Integer.parseInt(hexCode.substring(6, 8), 16);

        return argb;
    }
}
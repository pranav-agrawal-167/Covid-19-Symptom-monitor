package com.example.covid19app;

public class SensorHandler {

    public int calculate_filter_arrays(float[] sensor_values) {

        float[] lpf_array = get_lpf_array(sensor_values);
        double[] hpf_array = get_hpf_array(sensor_values, lpf_array);

        int peak_count = peak_detection(hpf_array);
        return peak_count;
    }

    private int peak_detection(double[] hpf_array) {
        double sum = 0.0;
        double max_value = -1000.0;
        double min_value = 1000.0;

        for(int i = 0; i < hpf_array.length; i++) {
            hpf_array[i] = -1*hpf_array[i];
            sum += hpf_array[i];
            if(hpf_array[i] > max_value){
                max_value = hpf_array[i];
            }
            if(hpf_array[i] < min_value){
                min_value = hpf_array[i];
            }
        }
        double average = sum/hpf_array.length;
        double DynamicRangeUp = max_value - average;
        double DynamicRangeDown = average - min_value;

        double thresholdUp = 0.000002*DynamicRangeUp;
        double thresholdR = 0.005*DynamicRangeUp;
        double thresholdDown = 0.000002*DynamicRangeDown;

        int up = 1;
        double previouspeak = hpf_array[1];
        int k = 0;
        int possiblepeak = 0;

        int Rpeak = 0;
        int PeakType = 0;
        int i = 1;
        int[] peak_index = new int[hpf_array.length];

        double maximum = -1000.0;
        double minimum = 1000.0;

        while(i<hpf_array.length){
            if(hpf_array[i] > maximum){
                maximum = hpf_array[i];
            }
            if(hpf_array[i] < minimum){
                minimum = hpf_array[i];
            }

            if(up == 1){
                if(hpf_array[i] < maximum){
                    if(possiblepeak == 0){
                        possiblepeak = i;
                    }
                    if(hpf_array[i] < (maximum - thresholdUp)){
                        k = k+1;
                        peak_index[k] = possiblepeak - 1;
                        minimum = hpf_array[i];
                        up = 0;
                        possiblepeak = 0;
                        if(PeakType == 0){
                            if(hpf_array[peak_index[k]] > average + thresholdR){
                                Rpeak = Rpeak + 1;
                                previouspeak = hpf_array[peak_index[k]];
                            }
                        }else{
                            if((Math.abs((hpf_array[peak_index[k]] - previouspeak) / previouspeak) > 1.5) && (hpf_array[peak_index[k]] > average+thresholdR)){
                                Rpeak = Rpeak + 1;
                                previouspeak = hpf_array[peak_index[k]];
                            }
                        }
                    }
                }
            }else{
                if(hpf_array[i] > minimum){
                    if(possiblepeak == 0){
                        possiblepeak = 1;
                    }
                    if(hpf_array[i] > (minimum + thresholdDown)){
                        k = k + 1;
                        peak_index[k] = possiblepeak - 1;
                        maximum = hpf_array[i];
                        up = 1;
                        possiblepeak = 0;
                    }
                }
            }
            i++;
        }
        return Rpeak;
    }

    private double[] get_hpf_array(float[] sensor_values, float[] lpf_array) {
        int index, index2;
        double[] hpf_array = new double[sensor_values.length];
        for(int i = 46; i<sensor_values.length; i++){
            index = i-12;
            index2 = i-45;
            lpf_array[index] = (float) (0.5*(2*lpf_array[index-1] - lpf_array[index-2] + sensor_values[i] - 2*sensor_values[i-6] + sensor_values[i-12]));
            if(index2 < 2){
                hpf_array[index2] = (0.03125)*(32*lpf_array[index-16] + lpf_array[index] - lpf_array[index-32]);
            }else{
                hpf_array[index2] = (0.03125)*(32*lpf_array[index-16] - (hpf_array[index2-1] + lpf_array[index] - lpf_array[index-32]));
            }
        }
        return hpf_array;
    }


    private float[] get_lpf_array(float[] sensor_values) {
        int index;
        float[] lpf_array = new float[sensor_values.length];
        for(int i = 12; i <= 45; i++){
            index = i-12;
            if(index<2){
                lpf_array[index] = (float) (0.5*(sensor_values[i] - 2*sensor_values[i-6] + sensor_values[i-12]));
            }else{
                lpf_array[index] = (float) (0.5*(2*lpf_array[index-1] - lpf_array[index-2] + sensor_values[i] - 2*sensor_values[i-6] + sensor_values[i-12]));
            }
        }

        return lpf_array;
    }
}

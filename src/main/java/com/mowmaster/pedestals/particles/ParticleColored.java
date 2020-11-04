package com.mowmaster.pedestals.particles;

public class ParticleColored {
    private final float r;
    private final float g;
    private final float b;
    private final int color;
    private final float alpha;
    public ParticleColored(int r, int g, int b) {
        this.r = r / 255F;
        this.g = g / 255F;
        this.b = b / 255F;
        this.color = (r << 16) | (g << 8) | b;
        alpha = 1.0f;
    }

    public ParticleColored(int r, int g, int b, float a){
        this.r = r / 255F;
        this.g = g / 255F;
        this.b = b / 255F;
        this.color = (r << 16) | (g << 8) | b;
        this.alpha = a;
    }

    public float getRed() {
        return r;
    }

    public float getGreen() {
        return g;
    }

    public float getBlue() {
        return b;
    }

    public float getAlpha() {
        return alpha;
    }

    public int getColor() {
        return color;
    }

    public String serialize(){
        return "" + this.r + "," + this.g +","+this.b + "," + this.alpha;
    }

    public static ParticleColored deserialize(String string){
        String[] arr = string.split(",");
        return new ParticleColored(Integer.parseInt(arr[0].trim()), Integer.parseInt(arr[1].trim()), Integer.parseInt(arr[2].trim()), Float.parseFloat(arr[3].trim()));
    }
}

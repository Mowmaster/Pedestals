package com.mowmaster.pedestals.crafting;

public class CalculateColor
{

    public static int getColorFromRGB(double red, double green, double blue)
    {
        int rgbInt = 0;
        int r = 0;
        int g = 0;
        int b = 0;

        r= rounder((red*65536));
        g= rounder(green*256);
        b= rounder(blue);

        rgbInt = r+g+b;

        return rgbInt;
    }

    public static int rounder(double numIn)
    {
        ////System.out.println("NumIn: "+numIn);
        int numOut = 0;

        double numProc = Math.floor((numIn - Math.floor(numIn))*10);
        ////System.out.println(numProc);
        if(numProc>=5)
        {
            numOut = (int)Math.floor((double)numIn) + 1;
        }
        else {
            numOut = (int)Math.floor((double)numIn);
        }

        return numOut;
    }

    public static int[] getRGBColorFromInt(int getINTColor)
    {
        int[] intRGB = new int[]{0,0,0};

        intRGB[0]=Math.floorDiv(((getINTColor/65536)%256),1);
        intRGB[1]=Math.floorDiv(((getINTColor/256)%256),1);
        intRGB[2]=Math.floorDiv((getINTColor%256),1);

        return intRGB;
    }

    public static double[] getRGBColorFromIntCount(int getINTColor, int count)
    {
        int INTColor = getINTColor;
        int counted = count;
        double[] intRGB = new double[]{0.0,0.0,0.0};
        double r=getRGBColorFromInt(INTColor)[0];
        double g=getRGBColorFromInt(INTColor)[1];
        double b=getRGBColorFromInt(INTColor)[2];

        double red = r/3;
        double green = g/3;
        double blue = b/3;

        double value1 = (counted%3);
        if(value1 == 0.0d)
        {
            value1 = 3;
        }

        intRGB[0]= (value1*red);
        intRGB[1]= (value1*green);
        intRGB[2]= (value1*blue);

        return intRGB;
    }
}

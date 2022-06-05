package com.mowmaster.pedestals.PedestalUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class NameComponentUtils
{
    public static Component createComponentName(Component componentItemStack, String name)
    {
        Component component = new Component() {
            @Override
            public Style getStyle() {
                return componentItemStack.getStyle();
            }

            @Override
            public String getContents() {
                return name + componentItemStack.getString();
            }

            @Override
            public List<Component> getSiblings() {
                return componentItemStack.getSiblings();
            }

            @Override
            public MutableComponent plainCopy() {
                return componentItemStack.plainCopy();
            }

            @Override
            public MutableComponent copy() {
                return componentItemStack.copy();
            }

            @Override
            public FormattedCharSequence getVisualOrderText() {
                return componentItemStack.getVisualOrderText();
            }
        };

        return component;
    }
}

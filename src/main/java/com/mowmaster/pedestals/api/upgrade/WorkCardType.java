package com.mowmaster.pedestals.api.upgrade;

// The three kinds of work card (1.20's numbers 1, 2, 3), and what an upgrade accepts.
public enum WorkCardType
{
    // Two corners of a box
    AREA,
    // A list of single positions
    LOCATIONS,
    // A list of pedestals (crafter, modification upgrade)
    PEDESTALS,
    // An upgrade that works with either an area or locations (1.20's type 0)
    AREA_OR_LOCATIONS;

    // Can an upgrade wanting this type use a card of cardType?
    public boolean accepts(WorkCardType cardType)
    {
        return this == cardType || (this == AREA_OR_LOCATIONS && (cardType == AREA || cardType == LOCATIONS));
    }
}

package com.mowmaster.pedestals.particles;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class ParticleEngine {
    List<ParticleBase> effects;
    // Allows effects to add other effects without concurrent modification
    List<ParticleBase> scheduled;
    public void tick(){
        if(!scheduled.isEmpty()){
            if(effects == null)
                effects = new ArrayList<>();
            effects.addAll(scheduled);
            //System.out.println("adding effect");
            scheduled = new ArrayList<>();
        }

        if(effects.isEmpty()) {
            return;
        }

        ListIterator<ParticleBase> effectListIterator = effects.listIterator();
        List<ParticleBase> stale = new ArrayList<>();

        for(ParticleBase effect1 : effects){
            if(effect1.isDone){
                stale.add(effect1);
                continue;
            }
            effect1.tick();
        }
        for(ParticleBase effect : stale){
            effects.remove(effect);
        }


    }

    public void addEffect(ParticleBase effect){
        effects.add(effect);
    }

    // Schedule for next tick.
    public void scheduleEffect(ParticleBase effect){
        scheduled.add(effect);
    }

    public static ParticleEngine getInstance(){
        if(particleEngine == null)
            particleEngine = new ParticleEngine();
        return particleEngine;
    }

    private static ParticleEngine particleEngine;
    private ParticleEngine(){
        effects = new ArrayList<>();
        scheduled = new ArrayList<>();
    }
}

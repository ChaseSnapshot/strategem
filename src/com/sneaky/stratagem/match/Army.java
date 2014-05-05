package com.sneaky.stratagem.match;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sneaky.stratagem.units.Unit;
import com.sneaky.stratagem.units.commanders.Commander;


public class Army implements Serializable {
    /** Generated serialization UUID. */
    private static final long serialVersionUID = 741582415902316595L;

    /** Commander of the army. */
    private Commander commander;
    
    private Map<Unit, Integer> minions = new HashMap<Unit, Integer>();
    
    public final void addMinion(final Unit minion, final int numEnlisted) {
        checkArgument(minion != null, "Minion must not be null!");
        checkArgument(!minions.containsKey(minion), "Minion definition has already been added to the army!");
        checkArgument(numEnlisted > 0, "The number of enlisted units must be greater than 0!");
        
        minions.put(minion, numEnlisted);
    }
    
    public final Commander getCommander() { return commander; }
    
    public final Map<Unit, Integer> getMinions() { return minions; }
    
    public final List<Entry<Unit, Integer>> getSortedMinions() {
        List<Entry<Unit, Integer>> entries = new ArrayList<Entry<Unit, Integer>>(minions.entrySet());
        
        Collections.sort(entries, new MinionComparator());
        
        return entries;
    }
    
    public final void setCommander(final Commander commander) {
        checkArgument(commander != null, "Commander must not be null!");
        this.commander = commander;
    }

    public class MinionComparator implements Comparator<Entry<Unit, Integer>> {
        @Override
        public int compare(Entry<Unit, Integer> a, Entry<Unit, Integer> b) {
            return (a.getKey().getName().compareTo(b.getKey().getName()));
        }
    }
}

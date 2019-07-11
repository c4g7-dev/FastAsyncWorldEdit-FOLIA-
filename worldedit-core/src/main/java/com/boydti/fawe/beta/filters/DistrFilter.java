package com.boydti.fawe.beta.filters;

import com.boydti.fawe.beta.FilterBlock;
import com.boydti.fawe.config.BBC;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.mask.ABlockMask;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DistrFilter extends ForkedFilter<DistrFilter> {
    private final int[] counter = new int[BlockTypes.states.length];

    public DistrFilter() {
        super(null);
    }

    private DistrFilter(DistrFilter root) {
        super(root);
    }

    @Override
    public DistrFilter init() {
        return new DistrFilter(this);
    }

    @Override
    public void join(DistrFilter filter) {
        for (int i = 0; i < filter.counter.length; i++) {
            this.counter[i] += filter.counter[i];
        }
    }

    /*
    Implementation
     */

    @Override
    public final void applyBlock(final FilterBlock block) {
        counter[block.getOrdinal()]++;
    }

    public int getTotal(ABlockMask mask) {
        int total = 0;
        for (int i = 0; i < counter.length; i++) {
            int value = counter[i];
            if (value != 0 && mask.test(BlockTypes.states[i])) {
                total += value;
            }
        }
        return total;
    }

    public int getTotal() {
        int total = 0;
        for (int value : counter) total += value;
        return total;
    }

    public List<Countable<BlockState>> getDistribution() {
        final List<Countable<BlockState>> distribution = new ArrayList<>();
        for (int i = 0; i < counter.length; i++) {
            final int count = counter[i];
            if (count != 0) {
                distribution.add(new Countable<>(BlockTypes.states[i], count));
            }
        }
        Collections.sort(distribution);
        return distribution;
    }

    public List<Countable<BlockType>> getTypeDistribution() {
        final List<Countable<BlockType>> distribution = new ArrayList<>();
        int[] typeCounter = new int[BlockTypes.values.length];
        for (int i = 0; i < counter.length; i++) {
            final int count = counter[i];
            if (count != 0) {
                BlockState state = BlockTypes.states[i];
                typeCounter[state.getBlockType().getInternalId()] += count;
            }
        }
        for (int i = 0; i < typeCounter.length; i++) {
            final int count = typeCounter[i];
            if (count != 0) {
                distribution.add(new Countable<>(BlockTypes.values[i], count));
            }
        }
        Collections.sort(distribution);
        return distribution;
    }

    public void print(final Actor actor, final long size) {
        for (final Countable c : getDistribution()) {
            final String name = c.getID().toString();
            final String str = String.format("%-7s (%.3f%%) %s",
                    String.valueOf(c.getAmount()),
                    c.getAmount() / (double) size * 100,
                    name);
            actor.print(BBC.getPrefix() + str);
        }
    }
}

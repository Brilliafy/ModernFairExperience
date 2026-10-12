package com.brilliafy.fairexperience.mixin;

import com.brilliafy.fairexperience.FairExperienceConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Fair anvil costs and order-independent enchantment combining.
 *
 * <p>Vanilla computes anvil enchantment costs from the "prior work penalty" plus a flat
 * per-level fee, which makes the price depend on how you got there. Fair Experience replaces
 * the enchantment part with the difference of a potential function
 * {@code Phi(I) = sum(multiplier * level) + count * (count - 1) / 2}, so applying books in any
 * order costs exactly the same and combining books to a higher level never costs more than a
 * raw book of that level.</p>
 */
@Mixin(AnvilMenu.class)
public abstract class MixinAnvilMenu {

    @Shadow
    protected Container inputSlots;

    @Shadow
    protected ResultContainer resultSlots;

    @Shadow
    protected Player player;

    @Shadow
    public int repairItemCountCost;

    @Shadow
    private String itemName;

    @Shadow
    @Final
    private DataSlot cost;

    @Shadow
    public abstract void broadcastChanges();

    //? if <1.20.5 {
    /*@Unique
    private static int fairExperience$getRarityMultiplier(Enchantment enchantment, boolean isBook) {
        if (enchantment == null || enchantment.getRarity() == null) {
            return 1;
        }
        int multiplier;
        switch (enchantment.getRarity()) {
            case UNCOMMON -> multiplier = 2;
            case RARE -> multiplier = 4;
            case VERY_RARE -> multiplier = 8;
            default -> multiplier = 1;
        }
        if (isBook) {
            multiplier = Math.max(1, multiplier / 2);
        }
        return multiplier;
    }

    @Unique
    private static int fairExperience$calculateEnchantmentPotential(Map<Enchantment, Integer> enchants, boolean isBook) {
        if (enchants == null || enchants.isEmpty()) {
            return 0;
        }
        int baseSum = 0;
        int count = 0;
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            Enchantment ench = entry.getKey();
            Integer lvlObj = entry.getValue();
            if (ench != null && lvlObj != null && lvlObj > 0) {
                baseSum += fairExperience$getRarityMultiplier(ench, isBook) * lvlObj;
                count++;
            }
        }
        return baseSum + count * (count - 1) / 2;
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void fairExperience$createResult(CallbackInfo ci) {
        if (!FairExperienceConfig.enableFairAnvilCombining) {
            return;
        }

        ci.cancel();

        ItemStack itemstack = this.inputSlots.getItem(0);
        this.cost.set(1);
        int i = 0;
        int j = 0; // Prior work penalty is zeroed out in fair mode
        int k = 0;

        if (itemstack.isEmpty()) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.cost.set(0);
            return;
        }

        ItemStack itemstack1 = itemstack.copy();
        ItemStack itemstack2 = this.inputSlots.getItem(1);
        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(itemstack1);
        this.repairItemCountCost = 0;
        boolean flag = false;

        //? if forge {
        if (!net.minecraftforge.common.ForgeHooks.onAnvilChange((AnvilMenu) (Object) this, itemstack, itemstack2, this.resultSlots, this.itemName, j, this.player)) {
            return;
        }
        //?}

        if (!itemstack2.isEmpty()) {
            flag = itemstack2.is(Items.ENCHANTED_BOOK) && !net.minecraft.world.item.EnchantedBookItem.getEnchantments(itemstack2).isEmpty();

            if (itemstack1.isDamageableItem() && itemstack1.getItem().isValidRepairItem(itemstack, itemstack2)) {
                int l2 = Math.min(itemstack1.getDamageValue(), itemstack1.getMaxDamage() / 4);
                if (l2 <= 0) {
                    this.resultSlots.setItem(0, ItemStack.EMPTY);
                    this.cost.set(0);
                    return;
                }

                int i3;
                for (i3 = 0; l2 > 0 && i3 < itemstack2.getCount(); ++i3) {
                    int j3 = itemstack1.getDamageValue() - l2;
                    itemstack1.setDamageValue(j3);
                    ++i;
                    l2 = Math.min(itemstack1.getDamageValue(), itemstack1.getMaxDamage() / 4);
                }
                this.repairItemCountCost = i3;

                // Existing enchantments make repairs fairly more expensive: +1 level each.
                i += map.size();
            } else {
                if (!flag && (!itemstack1.is(itemstack2.getItem()) || !itemstack1.isDamageableItem())) {
                    this.resultSlots.setItem(0, ItemStack.EMPTY);
                    this.cost.set(0);
                    return;
                }

                if (itemstack1.isDamageableItem() && !flag) {
                    int l = itemstack.getMaxDamage() - itemstack.getDamageValue();
                    int i1 = itemstack2.getMaxDamage() - itemstack2.getDamageValue();
                    int j1 = i1 + itemstack1.getMaxDamage() * 12 / 100;
                    int k1 = l + j1;
                    int l1 = itemstack1.getMaxDamage() - k1;
                    if (l1 < 0) {
                        l1 = 0;
                    }
                    if (l1 < itemstack1.getDamageValue()) {
                        itemstack1.setDamageValue(l1);
                        i += 2;
                        i += map.size();
                    }
                }

                Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(itemstack2);
                boolean isBook1 = itemstack1.is(Items.ENCHANTED_BOOK);
                boolean isBook2 = flag;

                if (isBook1 && isBook2) {
                    Set<Enchantment> allEnchants = new LinkedHashSet<>(map.keySet());
                    allEnchants.addAll(map1.keySet());

                    boolean anyIncompatible = false;
                    boolean anyCompatible = false;
                    int upgradeCost = 0;
                    Map<Enchantment, Integer> resultMap = new LinkedHashMap<>();

                    for (Enchantment ench : allEnchants) {
                        if (ench == null) {
                            continue;
                        }

                        int lvl1 = map.getOrDefault(ench, 0);
                        int lvl2 = map1.getOrDefault(ench, 0);
                        int resLvl = (lvl1 == lvl2 && lvl1 > 0)
                            ? Math.min(lvl1 + 1, ench.getMaxLevel())
                            : Math.max(lvl1, lvl2);

                        boolean canApply = true;
                        for (Enchantment other : resultMap.keySet()) {
                            if (other != ench && !ench.isCompatibleWith(other)) {
                                canApply = false;
                                ++i;
                                break;
                            }
                        }

                        if (!canApply) {
                            anyIncompatible = true;
                        } else {
                            anyCompatible = true;
                            resultMap.put(ench, resLvl);

                            if (lvl1 == lvl2 && lvl1 > 0) {
                                upgradeCost += fairExperience$getRarityMultiplier(ench, true) * (resLvl - lvl1);
                            }
                        }
                    }

                    if (anyIncompatible && !anyCompatible) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.cost.set(0);
                        return;
                    }

                    if (upgradeCost > 0) {
                        i += upgradeCost;
                    } else if (anyCompatible) {
                        i += 1;
                    }

                    map = resultMap;

                    if (itemstack.getCount() > 1) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.cost.set(0);
                        return;
                    }
                } else {
                    boolean flag2 = false;
                    boolean flag3 = false;
                    int oldPotential = fairExperience$calculateEnchantmentPotential(map, false);
                    Map<Enchantment, Integer> targetMap = new LinkedHashMap<>(map);

                    for (Enchantment enchantment1 : map1.keySet()) {
                        if (enchantment1 == null) {
                            continue;
                        }

                        int i2 = targetMap.getOrDefault(enchantment1, 0);
                        int j2 = map1.get(enchantment1);
                        j2 = (i2 == j2) ? j2 + 1 : Math.max(j2, i2);

                        boolean flag1 = enchantment1.canEnchant(itemstack);
                        if (this.player.getAbilities().instabuild || itemstack.is(Items.ENCHANTED_BOOK)) {
                            flag1 = true;
                        }

                        for (Enchantment enchantment : targetMap.keySet()) {
                            if (enchantment != enchantment1 && !enchantment1.isCompatibleWith(enchantment)) {
                                flag1 = false;
                                ++i;
                            }
                        }

                        if (!flag1) {
                            flag3 = true;
                        } else {
                            flag2 = true;
                            if (j2 > enchantment1.getMaxLevel()) {
                                j2 = enchantment1.getMaxLevel();
                            }
                            targetMap.put(enchantment1, j2);

                            if (itemstack.getCount() > 1) {
                                this.resultSlots.setItem(0, ItemStack.EMPTY);
                                this.cost.set(0);
                                return;
                            }
                        }
                    }

                    if (flag3 && !flag2) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.cost.set(0);
                        return;
                    }

                    if (flag2) {
                        map = targetMap;
                        i += Math.max(0, fairExperience$calculateEnchantmentPotential(map, false) - oldPotential);
                    }
                }
            }
        }

        if (this.itemName != null && !this.itemName.isBlank()) {
            if (!this.itemName.equals(itemstack.getHoverName().getString())) {
                k = 1;
                i += k;
                itemstack1.setHoverName(Component.literal(this.itemName));
            }
        } else if (itemstack.hasCustomHoverName()) {
            k = 1;
            i += k;
            itemstack1.resetHoverName();
        }

        // `isBookEnchantable` is a Forge patch; vanilla dropped the guard in this era.
        //? if forge {
        if (flag && !itemstack1.isBookEnchantable(itemstack2)) {
            itemstack1 = ItemStack.EMPTY;
        }
        //?}

        this.cost.set(i);
        if (i <= 0) {
            itemstack1 = ItemStack.EMPTY;
        }

        if (k == i && k > 0 && this.cost.get() >= 40) {
            this.cost.set(39);
        }

        if (!FairExperienceConfig.removeTooExpensiveLimit && this.cost.get() >= 40 && !this.player.getAbilities().instabuild) {
            itemstack1 = ItemStack.EMPTY;
        }

        if (!itemstack1.isEmpty()) {
            itemstack1.setRepairCost(0);
            if (itemstack1.hasTag()) {
                itemstack1.getTag().remove("RepairCost");
                if (itemstack1.is(Items.ENCHANTED_BOOK)) {
                    itemstack1.getTag().remove("StoredEnchantments");
                }
            }
            EnchantmentHelper.setEnchantments(map, itemstack1);
        }

        this.resultSlots.setItem(0, itemstack1);
        this.broadcastChanges();
    }

    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40, ordinal = 2))
    private int fairExperience$modifyMaxCostVanilla(int constant) {
        return FairExperienceConfig.removeTooExpensiveLimit ? Integer.MAX_VALUE : constant;
    }
    *///?} else {
    @Unique
    private static Map<net.minecraft.core.Holder<Enchantment>, Integer> fairExperience$toMap(net.minecraft.world.item.enchantment.ItemEnchantments enchantments) {
        Map<net.minecraft.core.Holder<Enchantment>, Integer> result = new LinkedHashMap<>();
        //? if <1.21.2 {
        for (Map.Entry<net.minecraft.core.Holder<Enchantment>, Integer> entry : enchantments.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        //?} else {
        /*for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<net.minecraft.core.Holder<Enchantment>> entry : enchantments.entrySet()) {
            result.put(entry.getKey(), entry.getIntValue());
        }
        *///?}
        return result;
    }

    @Unique
    private static int fairExperience$getAnvilCostMultiplier(net.minecraft.core.Holder<Enchantment> holder, boolean isBook) {
        int multiplier = Math.max(1, holder.value().getAnvilCost());
        return isBook ? Math.max(1, multiplier / 2) : multiplier;
    }

    @Unique
    private static int fairExperience$calculateEnchantmentPotential(Map<net.minecraft.core.Holder<Enchantment>, Integer> enchants, boolean isBook) {
        if (enchants == null || enchants.isEmpty()) {
            return 0;
        }
        int baseSum = 0;
        int count = 0;
        for (Map.Entry<net.minecraft.core.Holder<Enchantment>, Integer> entry : enchants.entrySet()) {
            Integer lvlObj = entry.getValue();
            if (lvlObj != null && lvlObj > 0) {
                baseSum += fairExperience$getAnvilCostMultiplier(entry.getKey(), isBook) * lvlObj;
                count++;
            }
        }
        return baseSum + count * (count - 1) / 2;
    }

    //? if >=1.21.2 && neoforge {
    @Inject(method = "createResultInternal", at = @At("HEAD"), cancellable = true)
    //?} else {
    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    //?}
    private void fairExperience$createResult(CallbackInfo ci) {
        if (!FairExperienceConfig.enableFairAnvilCombining) {
            return;
        }

        ci.cancel();

        ItemStack itemstack = this.inputSlots.getItem(0);
        this.cost.set(1);
        int i = 0;
        long j = 0L; // Prior work penalty is zeroed out in fair mode
        int k = 0;

        //? if >=1.20.5 && <1.21.2 && neoforge {
        if (!itemstack.isEmpty()) {
            if (!net.neoforged.neoforge.common.CommonHooks.onAnvilChange((AnvilMenu) (Object) this, itemstack, this.inputSlots.getItem(1), this.resultSlots, this.itemName, j, this.player)) {
                return;
            }
        }
        //?}

        if (itemstack.isEmpty() || !EnchantmentHelper.canStoreEnchantments(itemstack)) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
            this.cost.set(0);
            return;
        }

        ItemStack itemstack1 = itemstack.copy();
        ItemStack itemstack2 = this.inputSlots.getItem(1);
        net.minecraft.world.item.enchantment.ItemEnchantments.Mutable mutable =
            new net.minecraft.world.item.enchantment.ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(itemstack1));
        this.repairItemCountCost = 0;
        boolean flag = false;

        if (!itemstack2.isEmpty()) {
            flag = itemstack2.has(net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS);

            //? if <1.21.2 {
            boolean repairsWithInput = itemstack1.isDamageableItem() && itemstack1.getItem().isValidRepairItem(itemstack, itemstack2);
            //?} else {
            /*boolean repairsWithInput = itemstack1.isDamageableItem() && itemstack.isValidRepairItem(itemstack2);
            *///?}

            if (repairsWithInput) {
                int l2 = Math.min(itemstack1.getDamageValue(), itemstack1.getMaxDamage() / 4);
                if (l2 <= 0) {
                    this.resultSlots.setItem(0, ItemStack.EMPTY);
                    this.cost.set(0);
                    return;
                }

                int i3;
                for (i3 = 0; l2 > 0 && i3 < itemstack2.getCount(); ++i3) {
                    int j3 = itemstack1.getDamageValue() - l2;
                    itemstack1.setDamageValue(j3);
                    ++i;
                    l2 = Math.min(itemstack1.getDamageValue(), itemstack1.getMaxDamage() / 4);
                }
                this.repairItemCountCost = i3;

                i += mutable.keySet().size();
            } else {
                if (!flag && (!itemstack1.is(itemstack2.getItem()) || !itemstack1.isDamageableItem())) {
                    this.resultSlots.setItem(0, ItemStack.EMPTY);
                    this.cost.set(0);
                    return;
                }

                if (itemstack1.isDamageableItem() && !flag) {
                    int l = itemstack.getMaxDamage() - itemstack.getDamageValue();
                    int i1 = itemstack2.getMaxDamage() - itemstack2.getDamageValue();
                    int j1 = i1 + itemstack1.getMaxDamage() * 12 / 100;
                    int k1 = l + j1;
                    int l1 = itemstack1.getMaxDamage() - k1;
                    if (l1 < 0) {
                        l1 = 0;
                    }
                    if (l1 < itemstack1.getDamageValue()) {
                        itemstack1.setDamageValue(l1);
                        i += 2;
                        i += mutable.keySet().size();
                    }
                }

                net.minecraft.world.item.enchantment.ItemEnchantments enchantments =
                    EnchantmentHelper.getEnchantmentsForCrafting(itemstack2);
                boolean isBook1 = itemstack1.is(Items.ENCHANTED_BOOK);
                boolean isBook2 = flag;

                if (isBook1 && isBook2) {
                    Map<net.minecraft.core.Holder<Enchantment>, Integer> map = fairExperience$toMap(EnchantmentHelper.getEnchantmentsForCrafting(itemstack1));
                    Map<net.minecraft.core.Holder<Enchantment>, Integer> map1 = fairExperience$toMap(enchantments);

                    Set<net.minecraft.core.Holder<Enchantment>> allEnchants = new LinkedHashSet<>(map.keySet());
                    allEnchants.addAll(map1.keySet());

                    boolean anyIncompatible = false;
                    boolean anyCompatible = false;
                    int upgradeCost = 0;
                    Map<net.minecraft.core.Holder<Enchantment>, Integer> resultMap = new LinkedHashMap<>();

                    for (net.minecraft.core.Holder<Enchantment> holder : allEnchants) {
                        int lvl1 = map.getOrDefault(holder, 0);
                        int lvl2 = map1.getOrDefault(holder, 0);
                        int resLvl = (lvl1 == lvl2 && lvl1 > 0)
                            ? Math.min(lvl1 + 1, holder.value().getMaxLevel())
                            : Math.max(lvl1, lvl2);

                        boolean canApply = true;
                        for (net.minecraft.core.Holder<Enchantment> other : resultMap.keySet()) {
                            if (!other.equals(holder) && !Enchantment.areCompatible(holder, other)) {
                                canApply = false;
                                ++i;
                                break;
                            }
                        }

                        if (!canApply) {
                            anyIncompatible = true;
                        } else {
                            anyCompatible = true;
                            resultMap.put(holder, resLvl);

                            if (lvl1 == lvl2 && lvl1 > 0) {
                                upgradeCost += fairExperience$getAnvilCostMultiplier(holder, true) * (resLvl - lvl1);
                            }
                        }
                    }

                    if (anyIncompatible && !anyCompatible) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.cost.set(0);
                        return;
                    }

                    if (upgradeCost > 0) {
                        i += upgradeCost;
                    } else if (anyCompatible) {
                        i += 1;
                    }

                    for (net.minecraft.core.Holder<Enchantment> holder : allEnchants) {
                        mutable.set(holder, resultMap.getOrDefault(holder, 0));
                    }

                    if (itemstack.getCount() > 1) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.cost.set(0);
                        return;
                    }
                } else {
                    boolean flag2 = false;
                    boolean flag3 = false;
                    int oldPotential = fairExperience$calculateEnchantmentPotential(fairExperience$toMap(mutable.toImmutable()), false);

                    // Work on a copy so the cost calculation stays independent from vanilla's mutation order.
                    Map<net.minecraft.core.Holder<Enchantment>, Integer> targetMap =
                        fairExperience$toMap(EnchantmentHelper.getEnchantmentsForCrafting(itemstack1));

                    for (Map.Entry<net.minecraft.core.Holder<Enchantment>, Integer> entry : fairExperience$toMap(enchantments).entrySet()) {
                        net.minecraft.core.Holder<Enchantment> holder = entry.getKey();
                        int i2 = targetMap.getOrDefault(holder, 0);
                        int j2 = entry.getValue();
                        j2 = (i2 == j2) ? j2 + 1 : Math.max(j2, i2);

                        boolean flag1 = fairExperience$supports(itemstack, holder);
                        if (this.player.getAbilities().instabuild) {
                            flag1 = true;
                        }

                        for (net.minecraft.core.Holder<Enchantment> other : targetMap.keySet()) {
                            if (!other.equals(holder) && !Enchantment.areCompatible(holder, other)) {
                                flag1 = false;
                                ++i;
                            }
                        }

                        if (!flag1) {
                            flag3 = true;
                        } else {
                            flag2 = true;
                            if (j2 > holder.value().getMaxLevel()) {
                                j2 = holder.value().getMaxLevel();
                            }
                            targetMap.put(holder, j2);

                            if (itemstack.getCount() > 1) {
                                this.resultSlots.setItem(0, ItemStack.EMPTY);
                                this.cost.set(0);
                                return;
                            }
                        }
                    }

                    if (flag3 && !flag2) {
                        this.resultSlots.setItem(0, ItemStack.EMPTY);
                        this.cost.set(0);
                        return;
                    }

                    if (flag2) {
                        i += Math.max(0, fairExperience$calculateEnchantmentPotential(targetMap, false) - oldPotential);
                        for (Map.Entry<net.minecraft.core.Holder<Enchantment>, Integer> entry : targetMap.entrySet()) {
                            mutable.set(entry.getKey(), entry.getValue());
                        }
                    }
                }
            }
        }

        if (this.itemName != null && !this.itemName.isBlank()) {
            if (!this.itemName.equals(itemstack.getHoverName().getString())) {
                k = 1;
                i += k;
                itemstack1.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal(this.itemName));
            }
        } else if (itemstack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
            k = 1;
            i += k;
            itemstack1.remove(net.minecraft.core.component.DataComponents.CUSTOM_NAME);
        }

        // Vanilla dropped the "book enchantable" guard after 1.20.1; NeoForge kept it until 1.21.1.
        //? if neoforge && <1.21.2 {
        if (flag && !itemstack1.isBookEnchantable(itemstack2)) {
            itemstack1 = ItemStack.EMPTY;
        }
        //?}

        this.cost.set(i);
        if (i <= 0) {
            itemstack1 = ItemStack.EMPTY;
        }

        if (k == i && k > 0 && this.cost.get() >= 40) {
            this.cost.set(39);
        }

        if (!FairExperienceConfig.removeTooExpensiveLimit && this.cost.get() >= 40 && !fairExperience$hasInfiniteMaterials()) {
            itemstack1 = ItemStack.EMPTY;
        }

        if (!itemstack1.isEmpty()) {
            itemstack1.set(net.minecraft.core.component.DataComponents.REPAIR_COST, 0);
            EnchantmentHelper.setEnchantments(itemstack1, mutable.toImmutable());
        }

        this.resultSlots.setItem(0, itemstack1);
        this.broadcastChanges();
    }

    @Unique
    private boolean fairExperience$hasInfiniteMaterials() {
        //? if <1.21.2 {
        return this.player.getAbilities().instabuild;
        //?} else {
        /*return this.player.hasInfiniteMaterials();
        *///?}
    }

    @Unique
    private static boolean fairExperience$supports(ItemStack stack, net.minecraft.core.Holder<Enchantment> holder) {
        //? if neoforge {
        return stack.supportsEnchantment(holder);
        //?} else {
        /*return holder.value().canEnchant(stack);
        *///?}
    }

    //? if >=1.21.2 && neoforge {
    @ModifyConstant(method = "createResultInternal", constant = @Constant(intValue = 40, ordinal = 2))
    //?} else {
    @ModifyConstant(method = "createResult", constant = @Constant(intValue = 40, ordinal = 2))
    //?}
    private int fairExperience$modifyMaxCostVanilla(int constant) {
        return FairExperienceConfig.removeTooExpensiveLimit ? Integer.MAX_VALUE : constant;
    }
    //?}

    /**
     * Zeroes the prior-work penalty on the vanilla path too, so gear repaired with fair
     * anvils enabled never accumulates an ever-growing repair cost.
     */
    @WrapOperation(
        method = "createResult",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AnvilMenu;calculateIncreasedRepairCost(I)I")
    )
    private int fairExperience$noPriorWorkPenalty(int repairCost, Operation<Integer> original) {
        if (FairExperienceConfig.enableFairAnvils) {
            return 0;
        }
        return original.call(repairCost);
    }
}

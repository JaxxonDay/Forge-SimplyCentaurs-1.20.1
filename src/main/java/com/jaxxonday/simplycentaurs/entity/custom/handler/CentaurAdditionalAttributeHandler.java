package com.jaxxonday.simplycentaurs.entity.custom.handler;

import com.jaxxonday.simplycentaurs.entity.custom.CentaurEntity;
import com.jaxxonday.simplycentaurs.util.ModMethods;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.UUID;

public class CentaurAdditionalAttributeHandler {
    private final CentaurEntity centaurEntity;
    private static final UUID ATTACK_DAMAGE_MODIFIER_UUID = UUID.randomUUID();

    public CentaurAdditionalAttributeHandler(CentaurEntity centaurEntity) {
        this.centaurEntity = centaurEntity;
    }


    public void updateAttackAttribute() {
        CentaurEntity entity = this.centaurEntity;
        ItemStack heldItem = entity.getInventory().getItem(CentaurEntity.InventorySlot.HAND.ordinal()); // Check the designated slot for the item
        if(!ModMethods.canCauseDamage(entity.getInventory())) {
            removeAttackAttribute();
            return;
        }
        double additionalDamage = getAdditionalDamage(heldItem);
        if (additionalDamage != 0.0d) {
            AttributeModifier modifier = new AttributeModifier(ATTACK_DAMAGE_MODIFIER_UUID, "Weapon modifier", additionalDamage, AttributeModifier.Operation.ADDITION);
            if (entity.getAttribute(Attributes.ATTACK_DAMAGE).getModifier(ATTACK_DAMAGE_MODIFIER_UUID) == null) {
                entity.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(modifier);
                System.out.println("Added Attack Damage modifier of amount " + additionalDamage);
            }
        } else {
            removeAttackAttribute();
        }
    }

    private void removeAttackAttribute() {
        CentaurEntity entity = this.centaurEntity;
        if (entity.getAttribute(Attributes.ATTACK_DAMAGE).getModifier(ATTACK_DAMAGE_MODIFIER_UUID) != null) {
            entity.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATTACK_DAMAGE_MODIFIER_UUID);
            System.out.println("Removed Attack Damage modifier");
        }
    }

    private double getAdditionalDamage(ItemStack itemStack) {
        if (itemStack != null && !itemStack.isEmpty()) { // Check if itemStack is not null and not empty
            Collection<AttributeModifier> modifiers = itemStack.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE);
            if (modifiers != null && !modifiers.isEmpty()) { // Ensure modifiers are not null or empty
                return modifiers.stream()
                        .filter(modifier -> modifier.getOperation() == AttributeModifier.Operation.ADDITION)
                        .mapToDouble(AttributeModifier::getAmount)
                        .sum();  // Sum all the addition modifiers
            }
        }
        return 0;  // Return 0 if no valid modifiers are found or itemStack is null/empty
    }
}

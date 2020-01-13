package com.github.lotear.timekill;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;

public class EquipmentManager
{
    private final ArrayList<TimeEquipment> list = new ArrayList<>();

    public EquipmentManager()
    {
        //~4분 : 다이아 칼, 4분~2분 : 철 칼, 2분~30초 : 금 칼, 30초~0초 : 막대기, ~2분 : 인챈트 활 & 금갑옷, 2분~0초 : 활 & 가족갑옷
        list.add(new TimeEquipment(20 * 60 * 20,
                create(Material.DIAMOND_SWORD, "고대의 배틀엑스", null),
                create(Material.BOW, "강력한 총", Enchantment.ARROW_INFINITE),
                null, create(Material.IRON_CHESTPLATE, "화려한 갑옷", null), null, null
        ));
        list.add(new TimeEquipment(4 * 60 * 20,
                create(Material.IRON_SWORD, "푸른 검기의 칼", null),
                create(Material.BOW, "강력한 총", Enchantment.ARROW_INFINITE),
                null, create(Material.IRON_CHESTPLATE, "화려한 갑옷", null), null, null
        ));
        list.add(new TimeEquipment(2 * 60 * 20,
                create(Material.GOLD_SWORD, "평범한 칼", null),
                create(Material.BOW, "평범한 총", null),
                null, create(Material.LEATHER_CHESTPLATE, "평범한 갑옷", null), null, null
        ));
        list.add(new TimeEquipment(30 * 20,
                create(Material.STICK, "젓가락", null),
                create(Material.BOW, " 평범한 총", null),
                null, create(Material.LEATHER_CHESTPLATE, "평범한 갑옷", null), null, null
        ));

        list.sort(Comparator.comparingInt(TimeEquipment::getTick));
        list.trimToSize();


    }

    public TimeEquipment getEquipmentByTick(int tick)
    {
        for (TimeEquipment timeEquipment : list)
        {
            if (tick < timeEquipment.getTick())
            {
                return timeEquipment;
            }
        }

        return list.get(list.size() - 1);
    }

    private ItemStack create(Material type, String name, Enchantment enchantment)
    {
        ItemStack itemStack = new ItemStack(type);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(name);
        if (enchantment != null)
        {
            itemMeta.addEnchant(enchantment, 1, true);
        }
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }
}
